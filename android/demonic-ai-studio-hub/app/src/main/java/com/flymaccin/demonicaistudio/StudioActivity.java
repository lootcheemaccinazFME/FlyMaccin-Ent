package com.flymaccin.demonicaistudio;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Native, offline-first studio shell. UI state is kept in the project model and saved atomically. */
public final class StudioActivity extends Activity {
    private static final int BG = Color.rgb(9, 11, 17);
    private static final int PANEL = Color.rgb(22, 26, 36);
    private static final int PANEL_LIGHT = Color.rgb(36, 41, 54);
    private static final int WHITE = Color.rgb(241, 243, 248);
    private static final int MUTED = Color.rgb(153, 162, 181);
    private static final int CYAN = Color.rgb(58, 218, 244);
    private static final int GOLD = Color.rgb(247, 190, 78);
    private static final int RED = Color.rgb(255, 88, 105);
    private static final int PURPLE = Color.rgb(175, 115, 255);
    private static final int GREEN = Color.rgb(102, 224, 162);
    private static final int[] TRACK_COLORS = {CYAN, GOLD, RED, PURPLE};
    private static final String[] TRACK_NAMES = {"PIANO", "GUITAR", "DRUMS", "VOICE"};
    private static final String[] DRUM_NAMES = {"KICK", "SNARE", "HAT", "PERC"};
    private static final int MIC_REQUEST = 31;
    private static final int STORAGE_REQUEST = 32;
    private static final int[][] CHORDS = {
            {48, 52, 55, 60, 64}, {43, 47, 50, 55, 59, 67}, {50, 54, 57, 62, 66},
            {45, 52, 57, 60, 64}, {40, 47, 52, 55, 59, 64}, {41, 48, 53, 57, 60, 65},
            {40, 47, 52, 56, 59, 64}, {45, 52, 57, 61, 64, 69}
    };
    private static final String[] CHORD_NAMES = {"C", "G", "D", "Am", "Em", "F", "E", "A"};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AudioEngine audio = new AudioEngine();
    private final WavRecorder recorder = new WavRecorder();
    private final ExecutorService fileWorker = Executors.newSingleThreadExecutor();
    private final ArrayList<Uri> lastExportUris = new ArrayList<>();
    private final ArrayList<Button> navButtons = new ArrayList<>();
    private final ArrayList<String> navNames = new ArrayList<>();
    private ProjectStore store;
    private StudioProject project;
    private FrameLayout pageHost;
    private TextView positionLabel;
    private TextView tempoLabel;
    private TextView statusLabel;
    private boolean playing;
    private boolean recording;
    private boolean exporting;
    private boolean pianoArmed = true;
    private boolean guitarArmed = true;
    private boolean dropD;
    private boolean sustain;
    private int octave = 4;
    private int selectedStep;
    private int selectedTrack;
    private int transportStep;
    private int chordIndex;
    private int lastPlayedStep;
    private int recordingStep;
    private String screen = "HOME";
    private String status = "READY · OFFLINE";
    private String lastExportName = "No WAV exported yet";
    private Uri masterUri;
    private MediaPlayer voicePlayer;
    private StudioProject clipboard;
    private int clipboardTrack = -1;
    private boolean pendingRecord;
    private boolean pendingExport;
    private boolean pendingStems;

    private final Runnable transportTick = new Runnable() {
        @Override public void run() {
            if (!playing) return;
            lastPlayedStep = transportStep;
            triggerStep(transportStep);
            positionLabel.setText(String.format(Locale.US, "%02d / %02d · %d BPM",
                    transportStep + 1, StudioProject.STEPS, project.bpm));
            if ("ARRANGE".equals(screen)) showArrangement();
            int current = transportStep++;
            if (transportStep >= StudioProject.STEPS) {
                if (project.loop) transportStep = 0;
                else {
                    playing = false;
                    transportStep = current;
                    status("PLAYBACK COMPLETE");
                    return;
                }
            }
            long beat = 60000L / project.bpm / 4L;
            long swing = beat * project.swing / 100L;
            handler.postDelayed(this, current % 2 == 0 ? beat - swing : beat + swing);
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemBars();
        store = new ProjectStore(this);
        project = store.load();
        setContentView(buildShell());
        showHome();
    }

    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(5894 | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private View buildShell() {
        navButtons.clear();
        navNames.clear();
        LinearLayout root = column();
        root.setBackgroundColor(BG);
        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(10), 0, dp(10), 0);
        header.addView(text("DEMONIC  /  STUDIO", 17, WHITE, true));
        header.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        positionLabel = text("01 / 16 · " + project.bpm + " BPM", 12, CYAN, true);
        header.addView(positionLabel);
        statusLabel = text(status, 10, GREEN, true);
        header.addView(statusLabel);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(42)));

        LinearLayout transport = row();
        transport.setGravity(Gravity.CENTER_VERTICAL);
        transport.setPadding(dp(6), 0, dp(6), 0);
        transport.addView(control("▶ PLAY", CYAN, v -> startTransport()));
        transport.addView(control("Ⅱ PAUSE", PANEL_LIGHT, v -> pauseTransport()));
        transport.addView(control("■ STOP", PANEL_LIGHT, v -> stopTransport()));
        transport.addView(text("TEMPO", 10, MUTED, true));
        transport.addView(control("−", PANEL_LIGHT, v -> setTempo(project.bpm - 2)));
        tempoLabel = text(project.bpm + " BPM", 12, WHITE, true);
        transport.addView(tempoLabel);
        transport.addView(control("+", PANEL_LIGHT, v -> setTempo(project.bpm + 2)));
        transport.addView(control(project.loop ? "LOOP ON" : "LOOP OFF", project.loop ? GREEN : PANEL_LIGHT,
                v -> { project.loop = !project.loop; save("LOOP " + (project.loop ? "ON" : "OFF")); rebuildShellPage(); }));
        root.addView(transport, new LinearLayout.LayoutParams(-1, dp(43)));

        pageHost = new FrameLayout(this);
        root.addView(pageHost, new LinearLayout.LayoutParams(-1, 0, 1));
        HorizontalScrollView navScroll = new HorizontalScrollView(this);
        navScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout nav = row();
        nav.setPadding(dp(4), dp(3), dp(4), dp(3));
        addNav(nav, "HOME", this::showHome);
        addNav(nav, "PIANO", this::showPiano);
        addNav(nav, "GUITAR", this::showGuitar);
        addNav(nav, "DRUMS", this::showDrums);
        addNav(nav, "ARRANGE", this::showArrangement);
        addNav(nav, "MIXER", this::showMixer);
        addNav(nav, "VOICE", this::showRecorder);
        addNav(nav, "EXPORT", this::showExport);
        addNav(nav, "PROJECT", this::showProject);
        navScroll.addView(nav);
        root.addView(navScroll, new LinearLayout.LayoutParams(-1, dp(53)));
        updateHeader();
        return root;
    }

    private void addNav(LinearLayout nav, String label, Runnable action) {
        Button button = button(label, screen.equals(label) ? CYAN : PANEL_LIGHT, view -> action.run());
        navButtons.add(button);
        navNames.add(label);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(99), -1);
        params.setMargins(dp(2), 0, dp(2), 0);
        nav.addView(button, params);
    }

    private void showHome() {
        screen = "HOME";
        LinearLayout page = column();
        page.setPadding(dp(12), dp(7), dp(12), dp(8));
        page.addView(text(project.name, 23, WHITE, true));
        page.addView(text("LOCAL SESSION · 4 TRACKS · 16 STEPS · NO ACCOUNT OR NETWORK REQUIRED",
                11, MUTED, true));
        LinearLayout first = row();
        first.addView(card("PIANO", "Two-octave keyboard", CYAN, this::showPiano));
        first.addView(card("GUITAR", "Fretboard + chord strums", GOLD, this::showGuitar));
        first.addView(card("DRUM MACHINE", "Four voices · 16-step grid", RED, this::showDrums));
        page.addView(first, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout second = row();
        second.addView(card("ARRANGEMENT", "Sequence and edit clips", GREEN, this::showArrangement));
        second.addView(card("MIXER", "Level · pan · mute · FX", PURPLE, this::showMixer));
        second.addView(card("VOICE + WAV", "Record locally · export mix", WHITE, this::showRecorder));
        page.addView(second, new LinearLayout.LayoutParams(-1, 0, 1));
        page.addView(text(sessionSummary(), 12, MUTED, false));
        setPage(page);
    }

    private void showPiano() {
        screen = "PIANO";
        LinearLayout page = column();
        page.setPadding(dp(8), dp(5), dp(8), dp(5));
        LinearLayout controls = row();
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(text("PIANO", 17, CYAN, true));
        controls.addView(toggle(pianoArmed ? "ARMED" : "ARM", pianoArmed, v -> {
            pianoArmed = !pianoArmed; showPiano();
        }));
        controls.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        controls.addView(button("OCT −", PANEL_LIGHT, v -> { octave = Math.max(2, octave - 1); showPiano(); }));
        controls.addView(text("OCT " + octave, 12, WHITE, true));
        controls.addView(button("OCT +", PANEL_LIGHT, v -> { octave = Math.min(6, octave + 1); showPiano(); }));
        controls.addView(toggle(sustain ? "SUSTAIN" : "SHORT", sustain, v -> { sustain = !sustain; showPiano(); }));
        controls.addView(button("CLEAR STEP", PANEL_LIGHT, v -> {
            project.clearClip(StudioProject.TRACK_PIANO, selectedStep); save("PIANO STEP CLEARED"); showPiano();
        }));
        page.addView(controls, new LinearLayout.LayoutParams(-1, dp(43)));
        PianoKeyboardView keyboard = new PianoKeyboardView(this);
        keyboard.setOctave(octave);
        keyboard.setNoteListener((midi, label) -> {
            playPiano(midi);
            if (pianoArmed) {
                int step = recordStep();
                project.addMidi(StudioProject.TRACK_PIANO, step, midi);
                selectedStep = step;
                save("PIANO · " + label + " → STEP " + (step + 1));
            } else status("PIANO · " + label);
        });
        page.addView(keyboard, new LinearLayout.LayoutParams(-1, 0, 1));
        page.addView(text("Touch keys to audition. ARM writes played notes into the selected transport step.",
                11, MUTED, false), new LinearLayout.LayoutParams(-1, dp(28)));
        setPage(page);
    }

    private void showGuitar() {
        screen = "GUITAR";
        LinearLayout page = column();
        page.setPadding(dp(8), dp(4), dp(8), dp(4));
        LinearLayout controls = row();
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(text("GUITAR", 17, GOLD, true));
        controls.addView(toggle(guitarArmed ? "ARMED" : "ARM", guitarArmed, v -> {
            guitarArmed = !guitarArmed; showGuitar();
        }));
        controls.addView(toggle(dropD ? "DROP D" : "STANDARD", dropD, v -> {
            dropD = !dropD; showGuitar();
        }));
        controls.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        controls.addView(button("↓ STRUM", PANEL_LIGHT, v -> strumChord(true)));
        controls.addView(button("↑ STRUM", PANEL_LIGHT, v -> strumChord(false)));
        controls.addView(button("CLEAR STEP", PANEL_LIGHT, v -> {
            project.clearClip(StudioProject.TRACK_GUITAR, selectedStep); save("GUITAR STEP CLEARED"); showGuitar();
        }));
        page.addView(controls, new LinearLayout.LayoutParams(-1, dp(42)));
        HorizontalScrollView chordsScroll = new HorizontalScrollView(this);
        chordsScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout chords = row();
        for (int i = 0; i < CHORD_NAMES.length; i++) {
            final int index = i;
            chords.addView(button(CHORD_NAMES[i], i == chordIndex ? GOLD : PANEL_LIGHT, v -> {
                chordIndex = index; strumChord(true); showGuitar();
            }), new LinearLayout.LayoutParams(dp(61), dp(46)));
        }
        chordsScroll.addView(chords);
        page.addView(chordsScroll, new LinearLayout.LayoutParams(-1, dp(48)));
        GuitarFretboardView fretboard = new GuitarFretboardView(this);
        fretboard.setDropD(dropD);
        fretboard.setNoteListener((midi, label) -> {
            playGuitar(midi);
            if (guitarArmed) {
                int step = recordStep();
                project.addMidi(StudioProject.TRACK_GUITAR, step, midi);
                selectedStep = step;
                save("GUITAR · " + label + " → STEP " + (step + 1));
            } else status("GUITAR · " + label);
        });
        page.addView(fretboard, new LinearLayout.LayoutParams(-1, 0, 1));
        page.addView(text("Tap a string/fret, or choose a chord and strum. ARM captures notes to the arrangement.",
                11, MUTED, false), new LinearLayout.LayoutParams(-1, dp(27)));
        setPage(page);
    }

    private void showDrums() {
        screen = "DRUMS";
        LinearLayout page = column();
        page.setPadding(dp(8), dp(5), dp(8), dp(5));
        LinearLayout controls = row();
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(text("DRUM MACHINE", 17, RED, true));
        controls.addView(text("SWING " + project.swing + "%", 12, WHITE, true));
        controls.addView(button("SWING −", PANEL_LIGHT, v -> {
            project.swing = Math.max(0, project.swing - 5); save("SWING UPDATED"); showDrums();
        }));
        controls.addView(button("SWING +", PANEL_LIGHT, v -> {
            project.swing = Math.min(35, project.swing + 5); save("SWING UPDATED"); showDrums();
        }));
        controls.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        controls.addView(button("CLEAR", PANEL_LIGHT, v -> {
            for (boolean[] lane : project.drums) java.util.Arrays.fill(lane, false);
            save("DRUM PATTERN CLEARED"); showDrums();
        }));
        page.addView(controls, new LinearLayout.LayoutParams(-1, dp(44)));

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(true);
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(StudioProject.STEPS + 1);
        grid.setRowCount(5);
        grid.addView(text("KIT", 11, MUTED, true), gridCell(88, 39));
        for (int step = 0; step < StudioProject.STEPS; step++)
            grid.addView(text(String.format(Locale.US, "%02d", step + 1), 10, MUTED, true), gridCell(49, 39));
        for (int lane = 0; lane < 4; lane++) {
            final int laneIndex = lane;
            grid.addView(button(DRUM_NAMES[lane], TRACK_COLORS[lane], v -> playDrum(laneIndex)),
                    gridCell(88, 51));
            for (int step = 0; step < StudioProject.STEPS; step++) {
                final int stepIndex = step;
                Button cell = button(project.drums[lane][step] ? "●" : "",
                        project.drums[lane][step] ? TRACK_COLORS[lane] : PANEL_LIGHT, v -> {
                            project.drums[laneIndex][stepIndex] = !project.drums[laneIndex][stepIndex];
                            selectedTrack = StudioProject.TRACK_DRUMS;
                            selectedStep = stepIndex;
                            if (project.drums[laneIndex][stepIndex]) playDrum(laneIndex);
                            save("DRUM STEP " + (stepIndex + 1) + " UPDATED");
                            showDrums();
                        });
                grid.addView(cell, gridCell(49, 51));
            }
        }
        horizontal.addView(grid);
        page.addView(horizontal, new LinearLayout.LayoutParams(-1, 0, 1));
        page.addView(text("Tap pads to preview; tap step cells to program the loop.", 11, MUTED, false));
        setPage(page);
    }

    private void showArrangement() {
        screen = "ARRANGE";
        LinearLayout page = column();
        page.setPadding(dp(7), dp(4), dp(7), dp(5));
        LinearLayout controls = row();
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(text("ARRANGEMENT", 16, GREEN, true));
        controls.addView(text(TRACK_NAMES[selectedTrack] + " · STEP " + (selectedStep + 1), 11, WHITE, true));
        controls.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        controls.addView(button("COPY", PANEL_LIGHT, v -> copyClip()));
        controls.addView(button("PASTE", PANEL_LIGHT, v -> pasteClip()));
        controls.addView(button("← MOVE", PANEL_LIGHT, v -> moveClip(-1)));
        controls.addView(button("MOVE →", PANEL_LIGHT, v -> moveClip(1)));
        controls.addView(button("DELETE", PANEL_LIGHT, v -> {
            project.clearClip(selectedTrack, selectedStep); save("CLIP DELETED"); showArrangement();
        }));
        page.addView(controls, new LinearLayout.LayoutParams(-1, dp(43)));

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(true);
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(StudioProject.STEPS + 1);
        grid.setRowCount(StudioProject.TRACK_COUNT + 1);
        grid.addView(text("TRACK", 10, MUTED, true), gridCell(100, 38));
        for (int step = 0; step < StudioProject.STEPS; step++)
            grid.addView(text(String.valueOf(step + 1), 10, MUTED, true), gridCell(51, 38));
        for (int track = 0; track < StudioProject.TRACK_COUNT; track++) {
            final int trackIndex = track;
            grid.addView(button(TRACK_NAMES[track], TRACK_COLORS[track], v -> {
                selectedTrack = trackIndex; showArrangement();
            }), gridCell(100, 57));
            for (int step = 0; step < StudioProject.STEPS; step++) {
                final int stepIndex = step;
                boolean active = project.hasClip(track, step);
                boolean selected = selectedTrack == track && selectedStep == step;
                int color = (playing && lastPlayedStep == step) ? WHITE
                        : selected ? GREEN : active ? TRACK_COLORS[track] : PANEL_LIGHT;
                grid.addView(button(active ? "●" : "", color, v -> {
                    selectedTrack = trackIndex;
                    selectedStep = stepIndex;
                    showArrangement();
                }), gridCell(51, 57));
            }
        }
        horizontal.addView(grid);
        page.addView(horizontal, new LinearLayout.LayoutParams(-1, 0, 1));
        page.addView(text("Selected cells can be copied, pasted, moved, or deleted. Live keyboard takes are placed at the playhead.",
                11, MUTED, false));
        setPage(page);
    }

    private void showMixer() {
        screen = "MIXER";
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = column();
        page.setPadding(dp(8), dp(5), dp(8), dp(6));
        page.addView(text("MIXER · PER-TRACK LEVEL, PAN AND INSERT FX", 16, PURPLE, true));
        for (int track = 0; track < StudioProject.TRACK_COUNT; track++) page.addView(mixerStrip(track));
        page.addView(text("Mute and solo affect both transport playback and the exported master.", 11, MUTED, false));
        scroll.addView(page);
        setPage(scroll);
    }

    private View mixerStrip(int track) {
        LinearLayout strip = row();
        strip.setGravity(Gravity.CENTER_VERTICAL);
        strip.setPadding(dp(5), dp(3), dp(5), dp(3));
        strip.setBackgroundColor(PANEL);
        LinearLayout.LayoutParams stripParams = new LinearLayout.LayoutParams(-1, dp(78));
        stripParams.setMargins(0, dp(3), 0, dp(3));
        strip.setLayoutParams(stripParams);
        strip.addView(text(TRACK_NAMES[track], 13, TRACK_COLORS[track], true),
                new LinearLayout.LayoutParams(dp(79), -1));
        strip.addView(text("VOL", 9, MUTED, true));
        SeekBar volume = new SeekBar(this);
        volume.setMax(100);
        volume.setProgress(Math.round(project.volume[track] * 100));
        volume.setOnSeekBarChangeListener(seek(value -> {
            project.volume[track] = value / 100f;
            save("MIXER UPDATED");
        }));
        strip.addView(volume, new LinearLayout.LayoutParams(dp(122), dp(49)));
        strip.addView(text("PAN", 9, MUTED, true));
        SeekBar pan = new SeekBar(this);
        pan.setMax(200);
        pan.setProgress(Math.round(project.pan[track] * 100 + 100));
        pan.setOnSeekBarChangeListener(seek(value -> {
            project.pan[track] = (value - 100) / 100f;
            save("PAN UPDATED");
        }));
        strip.addView(pan, new LinearLayout.LayoutParams(dp(116), dp(49)));
        strip.addView(toggle("M", project.muted[track], v -> {
            project.muted[track] = !project.muted[track]; save("MUTE UPDATED"); showMixer();
        }));
        strip.addView(toggle("S", project.solo[track], v -> {
            project.solo[track] = !project.solo[track]; save("SOLO UPDATED"); showMixer();
        }));
        strip.addView(toggle("RV", project.reverb[track], v -> {
            project.reverb[track] = !project.reverb[track]; save("REVERB UPDATED"); showMixer();
        }));
        strip.addView(toggle("DLY", project.delay[track], v -> {
            project.delay[track] = !project.delay[track]; save("DELAY UPDATED"); showMixer();
        }));
        return strip;
    }

    private void showRecorder() {
        screen = "VOICE";
        LinearLayout page = column();
        page.setGravity(Gravity.CENTER);
        page.setPadding(dp(12), dp(8), dp(12), dp(8));
        page.addView(text("VOICE TRACK", 23, PURPLE, true));
        page.addView(text("Record microphone audio to a local mono 44.1 kHz / 16-bit WAV take.",
                13, MUTED, false));
        page.addView(text("TAKE STARTS AT STEP " + ((recording ? recordingStep : selectedStep) + 1),
                14, WHITE, true));
        page.addView(action(recording ? "■ STOP + ATTACH TAKE" : "● RECORD TAKE",
                recording ? GREEN : RED, v -> toggleRecording()));
        if (!project.voicePath.isEmpty()) {
            page.addView(text(new File(project.voicePath).getName(), 12, WHITE, true));
            LinearLayout actions = row();
            actions.addView(action("▶ PLAY", CYAN, v -> playVoice(project.voicePath)));
            actions.addView(action("REMOVE TAKE", PANEL_LIGHT, v -> {
                stopVoicePlayer();
                project.voicePath = "";
                save("VOICE TAKE REMOVED");
                showRecorder();
            }));
            page.addView(actions);
        }
        page.addView(text("The take is automatically placed on the selected timeline step and included in WAV renders.",
                11, MUTED, false));
        setPage(page);
    }

    private void showExport() {
        screen = "EXPORT";
        LinearLayout page = column();
        page.setGravity(Gravity.CENTER);
        page.setPadding(dp(10), dp(5), dp(10), dp(5));
        page.addView(text("OFFLINE WAV DELIVERY", 22, WHITE, true));
        page.addView(text("44.1 kHz · 16-bit stereo · exports are written to Music/DemonicAIStudio.",
                13, MUTED, false));
        Button master = action(exporting ? "RENDERING…" : "EXPORT MASTER WAV", CYAN,
                v -> exportProject(false));
        master.setEnabled(!exporting);
        page.addView(master);
        Button stems = action("EXPORT MASTER + 4 TRACK STEMS", PURPLE, v -> exportProject(true));
        stems.setEnabled(!exporting);
        page.addView(stems);
        page.addView(text(lastExportName, 12, GREEN, true));
        if (masterUri != null) page.addView(action("SHARE MASTER WAV", GOLD, v -> share(false)));
        if (lastExportUris.size() > 1) page.addView(action("SHARE MASTER + STEMS", GOLD, v -> share(true)));
        page.addView(text("Rendering uses the same local synth, track levels, pan and effects as playback.",
                11, MUTED, false));
        setPage(page);
    }

    private void showProject() {
        screen = "PROJECT";
        LinearLayout page = column();
        page.setPadding(dp(16), dp(10), dp(16), dp(10));
        page.addView(text("PROJECT FILE", 21, GREEN, true));
        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setText(project.name);
        name.setTextColor(WHITE);
        name.setHintTextColor(MUTED);
        name.setBackgroundColor(PANEL_LIGHT);
        page.addView(name, new LinearLayout.LayoutParams(-1, dp(54)));
        page.addView(action("SAVE SESSION", GREEN, v -> {
            String value = name.getText().toString().trim();
            if (!value.isEmpty()) project.name = value;
            save("PROJECT SAVED LOCALLY");
            showProject();
        }));
        page.addView(text("A private app-storage project file is atomically autosaved whenever edits are made. Your microphone take remains on this device.",
                13, MUTED, false));
        page.addView(text(sessionSummary(), 13, WHITE, true));
        page.addView(action("NEW EMPTY SESSION", RED, v -> confirmNewProject()));
        setPage(page);
    }

    private void rebuildShellPage() {
        setContentView(buildShell());
        switch (screen) {
            case "PIANO": showPiano(); break;
            case "GUITAR": showGuitar(); break;
            case "DRUMS": showDrums(); break;
            case "ARRANGE": showArrangement(); break;
            case "MIXER": showMixer(); break;
            case "VOICE": showRecorder(); break;
            case "EXPORT": showExport(); break;
            case "PROJECT": showProject(); break;
            default: showHome();
        }
    }

    private void startTransport() {
        if (playing) return;
        playing = true;
        transportStep = selectedStep;
        handler.removeCallbacks(transportTick);
        transportTick.run();
        status("TRANSPORT PLAYING");
    }

    private void pauseTransport() {
        playing = false;
        handler.removeCallbacks(transportTick);
        selectedStep = lastPlayedStep;
        stopVoicePlayer();
        status("TRANSPORT PAUSED");
        if ("ARRANGE".equals(screen)) showArrangement();
    }

    private void stopTransport() {
        playing = false;
        handler.removeCallbacks(transportTick);
        transportStep = 0;
        lastPlayedStep = 0;
        selectedStep = 0;
        stopVoicePlayer();
        if (positionLabel != null) positionLabel.setText(String.format(Locale.US, "01 / 16 · %d BPM", project.bpm));
        status("TRANSPORT STOPPED");
        if ("ARRANGE".equals(screen)) showArrangement();
    }

    private void triggerStep(int step) {
        if (project.trackAudible(StudioProject.TRACK_PIANO)) {
            for (int midi : project.midiNotesAt(StudioProject.TRACK_PIANO, step))
                audio.playPiano(midi, 850, project.volume[0], project.pan[0],
                        project.reverb[0], project.delay[0]);
        }
        if (project.trackAudible(StudioProject.TRACK_GUITAR)) {
            int[] notes = project.midiNotesAt(StudioProject.TRACK_GUITAR, step);
            if (notes.length > 0) audio.playGuitarChord(notes, true, project.volume[1],
                    project.pan[1], project.reverb[1], project.delay[1]);
        }
        if (project.trackAudible(StudioProject.TRACK_DRUMS)) {
            for (int lane = 0; lane < 4; lane++)
                if (project.drums[lane][step]) playDrum(lane);
        }
        if (project.trackAudible(StudioProject.TRACK_VOICE) && !project.voicePath.isEmpty()
                && project.voiceStartStep == step) playVoice(project.voicePath);
    }

    private void playPiano(int midi) {
        if (project.trackAudible(StudioProject.TRACK_PIANO))
            audio.playPiano(midi, sustain ? 1500 : 800, project.volume[0], project.pan[0],
                    project.reverb[0], project.delay[0]);
    }

    private void playGuitar(int midi) {
        if (project.trackAudible(StudioProject.TRACK_GUITAR))
            audio.playGuitar(midi, project.volume[1], project.pan[1],
                    project.reverb[1], project.delay[1]);
    }

    private void playDrum(int lane) {
        if (project.trackAudible(StudioProject.TRACK_DRUMS))
            audio.playDrum(lane, project.volume[2], project.pan[2],
                    project.reverb[2], project.delay[2]);
    }

    private void strumChord(boolean downStroke) {
        int[] notes = CHORDS[chordIndex].clone();
        if (project.trackAudible(StudioProject.TRACK_GUITAR))
            audio.playGuitarChord(notes, downStroke, project.volume[1], project.pan[1],
                    project.reverb[1], project.delay[1]);
        if (guitarArmed) {
            int step = recordStep();
            project.setMidiNotes(StudioProject.TRACK_GUITAR, step, notes);
            selectedStep = step;
            save("GUITAR " + CHORD_NAMES[chordIndex] + " → STEP " + (step + 1));
        }
    }

    private int recordStep() {
        return playing ? lastPlayedStep : selectedStep;
    }

    private void toggleRecording() {
        if (recording) {
            recording = false;
            File take = recorder.stop();
            if (take != null) {
                project.voicePath = take.getAbsolutePath();
                project.voiceStartStep = recordingStep;
                save("VOICE TAKE ATTACHED");
            } else {
                status("RECORDING FAILED · " + recorder.getLastError());
            }
            showRecorder();
            return;
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            pendingRecord = true;
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQUEST);
            return;
        }
        beginRecording();
    }

    private void beginRecording() {
        recordingStep = recordStep();
        File directory = new File(getFilesDir(), "recordings");
        if (!directory.isDirectory() && !directory.mkdirs()) {
            status("CANNOT CREATE LOCAL RECORDING FOLDER");
            return;
        }
        File output = new File(directory, "Take_" + System.currentTimeMillis() + ".wav");
        if (recorder.start(output)) {
            recording = true;
            status("RECORDING · STEP " + (selectedStep + 1));
            showRecorder();
        } else {
            status("MICROPHONE FAILED · " + recorder.getLastError());
        }
    }

    private void playVoice(String path) {
        stopVoicePlayer();
        try {
            MediaPlayer player = new MediaPlayer();
            voicePlayer = player;
            player.setDataSource(path);
            float left = project.volume[3] * (project.pan[3] > 0 ? 1f - project.pan[3] : 1f);
            float right = project.volume[3] * (project.pan[3] < 0 ? 1f + project.pan[3] : 1f);
            player.setVolume(left, right);
            player.setOnCompletionListener(done -> {
                done.release();
                if (voicePlayer == done) voicePlayer = null;
            });
            player.setOnPreparedListener(MediaPlayer::start);
            player.prepareAsync();
            status("PLAYING VOICE TAKE");
        } catch (Exception error) {
            stopVoicePlayer();
            status("VOICE PLAYBACK FAILED");
        }
    }

    private void stopVoicePlayer() {
        if (voicePlayer != null) {
            try { voicePlayer.stop(); } catch (Exception ignored) { }
            voicePlayer.release();
            voicePlayer = null;
        }
    }

    private void exportProject(boolean stems) {
        if (exporting) return;
        if (Build.VERSION.SDK_INT <= 28
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pendingExport = true;
            pendingStems = stems;
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST);
            return;
        }
        renderExport(stems);
    }

    private void renderExport(boolean stems) {
        stopTransport();
        exporting = true;
        status(stems ? "RENDERING MASTER + STEMS…" : "RENDERING MASTER…");
        if ("EXPORT".equals(screen)) showExport();
        StudioProject snapshot = StudioProject.fromJson(project.toJson());
        fileWorker.execute(() -> {
            try {
                StudioRenderer.Result result = new StudioRenderer().render(this, snapshot, stems);
                Uri master = StudioRenderer.publish(this, result.master);
                ArrayList<Uri> uris = new ArrayList<>();
                uris.add(master);
                for (File stem : result.stems) uris.add(StudioRenderer.publish(this, stem));
                runOnUiThread(() -> {
                    exporting = false;
                    masterUri = master;
                    lastExportUris.clear();
                    lastExportUris.addAll(uris);
                    lastExportName = result.master.getName() + (stems ? " + 4 stems" : "");
                    status("WAV EXPORTED · MUSIC/DEMONICAISTUDIO");
                    if ("EXPORT".equals(screen)) showExport();
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    exporting = false;
                    status("EXPORT FAILED · " + error.getMessage());
                    if ("EXPORT".equals(screen)) showExport();
                });
            }
        });
    }

    private void share(boolean all) {
        if (all && !lastExportUris.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("audio/wav");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(lastExportUris));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share WAV exports"));
        } else if (masterUri != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("audio/wav");
            intent.putExtra(Intent.EXTRA_STREAM, masterUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share master WAV"));
        }
    }

    private void copyClip() {
        clipboard = StudioProject.fromJson(project.toJson());
        clipboardTrack = selectedTrack;
        status(project.hasClip(selectedTrack, selectedStep) ? "CLIP COPIED" : "EMPTY STEP COPIED");
    }

    private void pasteClip() {
        if (clipboard == null || clipboardTrack != selectedTrack) {
            status("COPY A CLIP FROM THIS TRACK FIRST");
            return;
        }
        project.copyClipTo(clipboard, selectedTrack, selectedStep, selectedStep);
        save("CLIP PASTED");
        showArrangement();
    }

    private void moveClip(int direction) {
        int destination = StudioProject.normalizeStep(selectedStep + direction);
        project.moveClip(selectedTrack, selectedStep, destination);
        selectedStep = destination;
        save("CLIP MOVED");
        showArrangement();
    }

    private void setTempo(int bpm) {
        project.bpm = Math.max(50, Math.min(190, bpm));
        save("TEMPO " + project.bpm + " BPM");
        updateHeader();
        if (tempoLabel != null) tempoLabel.setText(project.bpm + " BPM");
    }

    private void confirmNewProject() {
        new AlertDialog.Builder(this)
                .setTitle("Start a new session?")
                .setMessage("The current autosaved arrangement and mixer settings will be replaced.")
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("NEW SESSION", (dialog, which) -> {
                    stopTransport();
                    project = new StudioProject();
                    selectedStep = 0;
                    selectedTrack = 0;
                    save("NEW SESSION READY");
                    showHome();
                }).show();
    }

    private void save(String message) {
        try {
            store.save(project);
            status(message + " · SAVED");
        } catch (Exception error) {
            status("SAVE FAILED · " + error.getMessage());
        }
        updateHeader();
    }

    private String sessionSummary() {
        int piano = 0;
        int guitar = 0;
        int drumSteps = 0;
        for (int step = 0; step < StudioProject.STEPS; step++) {
            if (project.hasClip(StudioProject.TRACK_PIANO, step)) piano++;
            if (project.hasClip(StudioProject.TRACK_GUITAR, step)) guitar++;
            if (project.hasClip(StudioProject.TRACK_DRUMS, step)) drumSteps++;
        }
        return "Piano " + piano + " clips · Guitar " + guitar + " clips · Drums " + drumSteps
                + " active steps · Voice " + (project.voicePath.isEmpty() ? "empty" : "attached")
                + " · " + project.bpm + " BPM";
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        boolean granted = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED;
        if (requestCode == MIC_REQUEST) {
            boolean shouldRecord = pendingRecord;
            pendingRecord = false;
            if (granted && shouldRecord) beginRecording();
            else status("MICROPHONE PERMISSION REQUIRED");
        } else if (requestCode == STORAGE_REQUEST) {
            boolean shouldExport = pendingExport;
            boolean includeStems = pendingStems;
            pendingExport = false;
            if (granted && shouldExport) renderExport(includeStems);
            else status("STORAGE PERMISSION REQUIRED FOR EXPORT");
        }
    }

    private void setPage(View page) {
        pageHost.removeAllViews();
        pageHost.addView(page, new FrameLayout.LayoutParams(-1, -1));
        updateNavigation();
        updateHeader();
    }

    private void updateNavigation() {
        for (int index = 0; index < navButtons.size(); index++) {
            boolean active = screen.equals(navNames.get(index));
            navButtons.get(index).setBackgroundColor(active ? CYAN : PANEL_LIGHT);
            navButtons.get(index).setTextColor(active ? BG : WHITE);
        }
    }

    private void status(String value) {
        status = value;
        if (statusLabel != null) statusLabel.setText(value);
    }

    private void updateHeader() {
        if (positionLabel != null) positionLabel.setText(String.format(Locale.US,
                "%02d / 16 · %d BPM", selectedStep + 1, project.bpm));
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout row() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setTypeface(Typeface.MONOSPACE, bold ? Typeface.BOLD : Typeface.NORMAL);
        view.setPadding(dp(6), dp(3), dp(6), dp(3));
        return view;
    }

    private Button button(String label, int color, View.OnClickListener action) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(11);
        button.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        button.setTextColor(color == CYAN || color == GOLD || color == RED || color == PURPLE
                || color == GREEN || color == WHITE ? BG : WHITE);
        button.setBackgroundColor(color);
        button.setPadding(dp(5), 0, dp(5), 0);
        button.setOnClickListener(action);
        return button;
    }

    private Button control(String label, int color, View.OnClickListener action) {
        Button button = button(label, color, action);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(37));
        params.setMargins(dp(2), 0, dp(2), 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button toggle(String label, boolean active, View.OnClickListener action) {
        Button button = button(label, active ? GREEN : PANEL_LIGHT, action);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(38));
        params.setMargins(dp(2), 0, dp(2), 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button action(String label, int color, View.OnClickListener action) {
        Button button = button(label, color, action);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(50));
        params.setMargins(dp(5), dp(5), dp(5), dp(5));
        button.setLayoutParams(params);
        return button;
    }

    private View card(String title, String detail, int color, Runnable action) {
        LinearLayout card = column();
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundColor(PANEL);
        card.setPadding(dp(10), dp(6), dp(10), dp(6));
        card.addView(text(title, 15, color, true));
        card.addView(text(detail, 11, MUTED, false));
        card.setOnClickListener(view -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1);
        params.setMargins(dp(3), dp(3), dp(3), dp(3));
        card.setLayoutParams(params);
        return card;
    }

    private GridLayout.LayoutParams gridCell(int width, int height) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = dp(width);
        params.height = dp(height);
        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        return params;
    }

    private SeekBar.OnSeekBarChangeListener seek(IntChange change) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                if (fromUser) change.accept(value);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        };
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private interface IntChange { void accept(int value); }

    @Override protected void onPause() {
        super.onPause();
        pauseTransport();
        audio.release();
        if (recording) {
            recording = false;
            File take = recorder.stop();
            if (take != null) {
                project.voicePath = take.getAbsolutePath();
                project.voiceStartStep = recordingStep;
                save("VOICE TAKE SAVED");
            }
        }
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        stopVoicePlayer();
        audio.release();
        fileWorker.shutdown();
        super.onDestroy();
    }
}
