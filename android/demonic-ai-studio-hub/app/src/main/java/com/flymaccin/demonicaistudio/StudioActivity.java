package com.flymaccin.demonicaistudio;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
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

public final class StudioActivity extends Activity {
    private static final int BG = Color.rgb(8, 9, 14);
    private static final int PANEL = Color.rgb(19, 22, 31);
    private static final int PANEL_2 = Color.rgb(30, 33, 44);
    private static final int PURPLE = Color.rgb(164, 92, 255);
    private static final int RED = Color.rgb(255, 61, 88);
    private static final int CYAN = Color.rgb(55, 226, 255);
    private static final int GOLD = Color.rgb(255, 206, 84);
    private static final int GREEN = Color.rgb(77, 230, 151);
    private static final int WHITE = Color.rgb(239, 241, 248);
    private static final int MUTED = Color.rgb(157, 163, 180);
    private static final int MIC_PERMISSION = 2201;
    private static final String PREFS = "demonic_studio_v110";
    private static final String PROJECT_KEY = "autosave_project";
    private static final String[] TRACK_NAMES = {"PIANO", "GUITAR", "DRUMS", "VOICE"};
    private static final int[] TRACK_COLORS = {CYAN, GOLD, RED, PURPLE};

    private final AudioEngine audio = new AudioEngine();
    private final WavRecorder wavRecorder = new WavRecorder();
    private final Handler transportHandler = new Handler(Looper.getMainLooper());
    private final Button[][] timelineCells = new Button[StudioProject.TRACK_COUNT][StudioProject.STEPS];
    private final Button[][] drumButtons = new Button[4][StudioProject.STEPS];
    private final String[] drumNames = {"808", "SNARE", "HAT", "PERC"};
    private final int[] drumColors = {RED, PURPLE, CYAN, GOLD};

    private StudioProject project;
    private FrameLayout content;
    private TextView status;
    private TextView position;
    private boolean playing;
    private int transportStep;
    private int lastPlayedStep;
    private int selectedStep;
    private int selectedTrack;
    private int octave = 4;
    private boolean sustain;
    private boolean pianoArmed = true;
    private boolean guitarArmed = true;
    private boolean dropD;
    private int selectedChord;
    private String clipboard = "";
    private int clipboardTrack = -1;
    private MediaPlayer voicePlayer;
    private Uri lastExportUri;
    private String lastExportName = "No export yet";
    private boolean pendingRecord;

    private final String[] chordNames = {"C", "G", "D", "Am", "Em", "F", "E", "A"};
    private final int[][] chordMidi = {
            {48, 52, 55, 60, 64}, {43, 47, 50, 55, 59, 67}, {50, 54, 57, 62, 66},
            {45, 52, 57, 60, 64}, {40, 47, 52, 55, 59, 64}, {41, 48, 53, 57, 60, 65},
            {40, 47, 52, 56, 59, 64}, {45, 52, 57, 61, 64, 69}
    };

    private final Runnable transportRunner = new Runnable() {
        @Override public void run() {
            if (!playing) return;
            lastPlayedStep = transportStep;
            selectedStep = transportStep;
            triggerStep(transportStep);
            position.setText(String.format(Locale.US, "%02d/16 · %d BPM", transportStep + 1, project.bpm));
            refreshTimeline();
            int completed = transportStep;
            transportStep++;
            if (transportStep >= StudioProject.STEPS) {
                if (project.loop) transportStep = 0;
                else {
                    playing = false;
                    transportStep = completed;
                    status.setText("PLAYBACK COMPLETE");
                    return;
                }
            }
            long base = Math.max(55, 60000L / project.bpm / 4L);
            long swing = base * project.swing / 100L;
            transportHandler.postDelayed(this, completed % 2 == 0 ? base - swing : base + swing);
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemBars();
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        project = StudioProject.fromJson(prefs.getString(PROJECT_KEY, ""));
        setContentView(buildShell());
        showHome();
    }

    private void hideSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else getWindow().getDecorView().setSystemUiVisibility(5894 | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private View buildShell() {
        LinearLayout root = column();
        root.setBackgroundColor(BG);
        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(12), dp(5), dp(12), dp(5));
        header.addView(text("DEMONIC AI STUDIO", 20, WHITE, true));
        position = text("01/16 · " + project.bpm + " BPM", 12, CYAN, true);
        header.addView(position);
        header.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        header.addView(smallButton("▶ PLAY", v -> startTransport()));
        header.addView(smallButton("Ⅱ PAUSE", v -> pauseTransport()));
        header.addView(smallButton("■ STOP", v -> stopTransport()));
        status = text("AUTOSAVE RESTORED", 11, GREEN, true);
        header.addView(status);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(56)));

        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        HorizontalScrollView navScroll = new HorizontalScrollView(this);
        navScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout nav = row();
        nav.setPadding(dp(5), dp(5), dp(5), dp(7));
        nav.addView(navButton("HOME", v -> showHome()));
        nav.addView(navButton("PIANO", v -> showPiano()));
        nav.addView(navButton("GUITAR", v -> showGuitar()));
        nav.addView(navButton("DRUMS", v -> showDrums()));
        nav.addView(navButton("TIMELINE", v -> showTimeline()));
        nav.addView(navButton("MIXER + FX", v -> showMixer()));
        nav.addView(navButton("RECORD", v -> showRecorder()));
        nav.addView(navButton("EXPORT", v -> showExport()));
        nav.addView(navButton("PROJECT", v -> showProject()));
        navScroll.addView(nav);
        root.addView(navScroll, new LinearLayout.LayoutParams(-1, dp(64)));
        return root;
    }

    private void showHome() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = column();
        page.setPadding(dp(18), dp(10), dp(18), dp(12));
        page.addView(text("OFFLINE MULTITRACK PRODUCTION", 13, MUTED, true));
        page.addView(text("One session. Four connected tracks. Live instruments, recording, mixing and WAV delivery.", 17, WHITE, false));
        LinearLayout rowOne = row();
        rowOne.addView(featureCard("REAL PIANO", "Two-octave multitouch keyboard", CYAN, v -> showPiano()));
        rowOne.addView(featureCard("6-STRING GUITAR", "Fretboard · chords · strumming", GOLD, v -> showGuitar()));
        rowOne.addView(featureCard("DRUM MACHINE", "808 kit · 16-step pattern", RED, v -> showDrums()));
        page.addView(rowOne);
        LinearLayout rowTwo = row();
        rowTwo.addView(featureCard("TIMELINE", "Edit and arrange all four tracks", GREEN, v -> showTimeline()));
        rowTwo.addView(featureCard("MIXER + FX", "Volume · pan · solo · effects", PURPLE, v -> showMixer()));
        rowTwo.addView(featureCard("WAV EXPORT", "Master mix and four stems", WHITE, v -> showExport()));
        page.addView(rowTwo);
        scroll.addView(page);
        setPage(scroll);
    }

    private void showPiano() {
        LinearLayout page = column();
        page.setPadding(dp(12), dp(7), dp(12), dp(8));
        LinearLayout controls = row();
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(text("PIANO TRACK", 19, CYAN, true));
        controls.addView(smallButton(pianoArmed ? "● ARMED" : "○ ARM", v -> { pianoArmed = !pianoArmed; showPiano(); }));
        controls.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        controls.addView(smallButton("OCT −", v -> { octave = Math.max(2, octave - 1); showPiano(); }));
        controls.addView(text("OCT " + octave, 14, WHITE, true));
        controls.addView(smallButton("OCT +", v -> { octave = Math.min(6, octave + 1); showPiano(); }));
        controls.addView(smallButton(sustain ? "SUSTAIN ON" : "SUSTAIN OFF", v -> { sustain = !sustain; showPiano(); }));
        controls.addView(smallButton("CLEAR STEP " + (selectedStep + 1), v -> {
            project.clearClip(StudioProject.TRACK_PIANO, selectedStep); autosave("PIANO CLIP CLEARED");
        }));
        page.addView(controls, new LinearLayout.LayoutParams(-1, dp(50)));
        PianoKeyboardView keyboard = new PianoKeyboardView(this);
        keyboard.setOctave(octave);
        keyboard.setNoteListener((frequency, name) -> {
            playPiano(frequency);
            if (pianoArmed) {
                int step = recordStep();
                project.addFrequency(StudioProject.TRACK_PIANO, step, frequency);
                selectedStep = step;
                autosave("PIANO REC · " + name + " → STEP " + (step + 1));
            } else status.setText("PIANO · " + name);
        });
        page.addView(keyboard, new LinearLayout.LayoutParams(-1, 0, 1));
        page.addView(text("Multi-touch enabled. ARMED notes are written into the selected transport step.", 12, MUTED, false));
        setPage(page);
    }

    private void showGuitar() {
        LinearLayout page = column();
        page.setPadding(dp(12), dp(6), dp(12), dp(8));
        LinearLayout controls = row();
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(text("GUITAR TRACK", 18, GOLD, true));
        controls.addView(smallButton(guitarArmed ? "● ARMED" : "○ ARM", v -> { guitarArmed = !guitarArmed; showGuitar(); }));
        controls.addView(smallButton(dropD ? "TUNING: DROP D" : "TUNING: STANDARD", v -> { dropD = !dropD; showGuitar(); }));
        controls.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        controls.addView(smallButton("↓ STRUM", v -> strumChord(true)));
        controls.addView(smallButton("↑ STRUM", v -> strumChord(false)));
        controls.addView(smallButton("CLEAR STEP", v -> {
            project.clearClip(StudioProject.TRACK_GUITAR, selectedStep); autosave("GUITAR CLIP CLEARED");
        }));
        page.addView(controls, new LinearLayout.LayoutParams(-1, dp(46)));

        HorizontalScrollView chordScroll = new HorizontalScrollView(this);
        LinearLayout chords = row();
        for (int index = 0; index < chordNames.length; index++) {
            final int chord = index;
            Button button = actionButton(chordNames[index], index == selectedChord ? GOLD : PANEL_2, v -> {
                selectedChord = chord;
                strumChord(true);
                showGuitar();
            });
            chords.addView(button);
        }
        chordScroll.addView(chords);
        page.addView(chordScroll, new LinearLayout.LayoutParams(-1, dp(64)));

        GuitarFretboardView fretboard = new GuitarFretboardView(this);
        fretboard.setDropD(dropD);
        fretboard.setNoteListener((frequency, label) -> {
            playGuitarNote(frequency);
            if (guitarArmed) {
                int step = recordStep();
                project.addFrequency(StudioProject.TRACK_GUITAR, step, frequency);
                selectedStep = step;
                autosave("GUITAR REC · " + label + " → STEP " + (step + 1));
            } else status.setText("GUITAR · " + label);
        });
        page.addView(fretboard, new LinearLayout.LayoutParams(-1, 0, 1));
        page.addView(text("Tap any string/fret, select a chord, then strum up or down. ARMED performances enter the timeline.", 12, MUTED, false));
        setPage(page);
    }

    private void showDrums() {
        LinearLayout page = column();
        page.setPadding(dp(9), dp(5), dp(9), dp(6));
        LinearLayout controls = row();
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(text("DRUM TRACK · 16 STEPS", 18, RED, true));
        controls.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        controls.addView(smallButton("BPM −", v -> { project.bpm = Math.max(50, project.bpm - 2); autosave("TEMPO " + project.bpm); showDrums(); }));
        controls.addView(text(project.bpm + " BPM", 14, CYAN, true));
        controls.addView(smallButton("BPM +", v -> { project.bpm = Math.min(190, project.bpm + 2); autosave("TEMPO " + project.bpm); showDrums(); }));
        controls.addView(smallButton("SWING " + project.swing + "%", v -> { project.swing = (project.swing + 5) % 40; autosave("SWING " + project.swing + "%"); showDrums(); }));
        controls.addView(smallButton("CLEAR DRUMS", v -> {
            for (boolean[] lane : project.drums) java.util.Arrays.fill(lane, false);
            autosave("DRUM PATTERN CLEARED"); showDrums();
        }));
        page.addView(controls, new LinearLayout.LayoutParams(-1, dp(48)));

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(17);
        grid.setRowCount(5);
        grid.addView(text("KIT", 11, MUTED, true), cellParams(88, 35));
        for (int step = 0; step < StudioProject.STEPS; step++)
            grid.addView(text(String.valueOf(step + 1), 10, MUTED, true), cellParams(46, 35));
        for (int lane = 0; lane < 4; lane++) {
            final int laneIndex = lane;
            grid.addView(smallButton(drumNames[lane], v -> playDrum(laneIndex)), cellParams(88, 48));
            for (int step = 0; step < StudioProject.STEPS; step++) {
                final int stepIndex = step;
                Button cell = new Button(this);
                cell.setText("");
                cell.setPadding(0, 0, 0, 0);
                drumButtons[lane][step] = cell;
                styleDrum(cell, lane, step);
                cell.setOnClickListener(v -> {
                    project.drums[laneIndex][stepIndex] = !project.drums[laneIndex][stepIndex];
                    selectedTrack = StudioProject.TRACK_DRUMS;
                    selectedStep = stepIndex;
                    styleDrum((Button) v, laneIndex, stepIndex);
                    if (project.drums[laneIndex][stepIndex]) playDrum(laneIndex);
                    autosave("DRUM STEP " + (stepIndex + 1) + " UPDATED");
                });
                grid.addView(cell, cellParams(46, 48));
            }
        }
        scroll.addView(grid);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setPage(page);
    }

    private void showTimeline() {
        LinearLayout page = column();
        page.setPadding(dp(8), dp(5), dp(8), dp(6));
        LinearLayout title = row();
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.addView(text("MULTITRACK TIMELINE", 18, GREEN, true));
        title.addView(text(" Selected: " + TRACK_NAMES[selectedTrack] + " / Step " + (selectedStep + 1), 12, WHITE, true));
        title.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        title.addView(smallButton(project.loop ? "LOOP ON" : "LOOP OFF", v -> { project.loop = !project.loop; autosave("LOOP " + (project.loop ? "ON" : "OFF")); showTimeline(); }));
        title.addView(smallButton("COPY", v -> copyClip()));
        title.addView(smallButton("PASTE", v -> pasteClip()));
        title.addView(smallButton("← MOVE", v -> moveSelected(-1)));
        title.addView(smallButton("MOVE →", v -> moveSelected(1)));
        title.addView(smallButton("DELETE", v -> { project.clearClip(selectedTrack, selectedStep); autosave("CLIP DELETED"); showTimeline(); }));
        page.addView(title, new LinearLayout.LayoutParams(-1, dp(48)));

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(17);
        grid.setRowCount(5);
        grid.addView(text("TRACK", 10, MUTED, true), cellParams(104, 34));
        for (int step = 0; step < StudioProject.STEPS; step++)
            grid.addView(text(String.valueOf(step + 1), 10, MUTED, true), cellParams(54, 34));
        for (int track = 0; track < StudioProject.TRACK_COUNT; track++) {
            final int trackIndex = track;
            Button label = smallButton(TRACK_NAMES[track], v -> { selectedTrack = trackIndex; showTimeline(); });
            label.setTextColor(TRACK_COLORS[track]);
            grid.addView(label, cellParams(104, 58));
            for (int step = 0; step < StudioProject.STEPS; step++) {
                final int stepIndex = step;
                Button cell = new Button(this);
                cell.setText(project.hasClip(track, step) ? "●" : "");
                cell.setTextSize(15);
                cell.setTextColor(BG);
                cell.setPadding(0, 0, 0, 0);
                timelineCells[track][step] = cell;
                cell.setOnClickListener(v -> {
                    selectedTrack = trackIndex;
                    selectedStep = stepIndex;
                    status.setText(TRACK_NAMES[trackIndex] + " · STEP " + (stepIndex + 1));
                    showTimeline();
                });
                grid.addView(cell, cellParams(54, 58));
            }
        }
        refreshTimeline();
        scroll.addView(grid);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        page.addView(text("Tap a clip cell to select it. COPY/PASTE, MOVE and DELETE edit the arrangement. Live armed notes land on the current step.", 11, MUTED, false));
        setPage(page);
    }

    private void showMixer() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = column();
        page.setPadding(dp(12), dp(7), dp(12), dp(10));
        page.addView(text("MIXER + EFFECTS", 20, PURPLE, true));
        for (int track = 0; track < StudioProject.TRACK_COUNT; track++) page.addView(mixerStrip(track));
        scroll.addView(page);
        setPage(scroll);
    }

    private View mixerStrip(int track) {
        LinearLayout strip = row();
        strip.setGravity(Gravity.CENTER_VERTICAL);
        strip.setPadding(dp(8), dp(5), dp(8), dp(5));
        strip.setBackgroundColor(PANEL);
        LinearLayout.LayoutParams stripParams = new LinearLayout.LayoutParams(-1, dp(82));
        stripParams.setMargins(0, dp(4), 0, dp(4));
        strip.setLayoutParams(stripParams);
        TextView name = text(TRACK_NAMES[track], 16, TRACK_COLORS[track], true);
        strip.addView(name, new LinearLayout.LayoutParams(dp(105), -1));
        strip.addView(text("VOL", 11, MUTED, true));
        SeekBar volume = new SeekBar(this);
        volume.setMax(100);
        volume.setProgress(Math.round(project.volume[track] * 100));
        volume.setOnSeekBarChangeListener(seekListener(value -> { project.volume[track] = value / 100f; autosave("MIXER UPDATED"); }));
        strip.addView(volume, new LinearLayout.LayoutParams(dp(190), -1));
        strip.addView(text("PAN", 11, MUTED, true));
        SeekBar pan = new SeekBar(this);
        pan.setMax(200);
        pan.setProgress(Math.round(project.pan[track] * 100 + 100));
        pan.setOnSeekBarChangeListener(seekListener(value -> { project.pan[track] = (value - 100) / 100f; autosave("PAN UPDATED"); }));
        strip.addView(pan, new LinearLayout.LayoutParams(dp(170), -1));
        strip.addView(toggleButton(project.muted[track] ? "MUTED" : "MUTE", project.muted[track], v -> { project.muted[track] = !project.muted[track]; autosave("MUTE UPDATED"); showMixer(); }));
        strip.addView(toggleButton(project.solo[track] ? "SOLO ON" : "SOLO", project.solo[track], v -> { project.solo[track] = !project.solo[track]; autosave("SOLO UPDATED"); showMixer(); }));
        strip.addView(toggleButton(project.reverb[track] ? "REVERB ON" : "REVERB", project.reverb[track], v -> { project.reverb[track] = !project.reverb[track]; autosave("REVERB UPDATED"); showMixer(); }));
        strip.addView(toggleButton(project.delay[track] ? "DELAY ON" : "DELAY", project.delay[track], v -> { project.delay[track] = !project.delay[track]; autosave("DELAY UPDATED"); showMixer(); }));
        return strip;
    }

    private void showRecorder() {
        LinearLayout page = column();
        page.setGravity(Gravity.CENTER);
        page.addView(text("VOICE TRACK RECORDER", 24, PURPLE, true));
        page.addView(text("44.1 kHz / 16-bit WAV · recorded locally · automatically attached to the timeline", 14, MUTED, false));
        page.addView(text("CLIP START: STEP " + (selectedStep + 1), 16, WHITE, true));
        page.addView(actionButton(wavRecorder.isRunning() ? "■ STOP & KEEP TAKE" : "● RECORD WAV",
                wavRecorder.isRunning() ? GREEN : RED, v -> toggleRecording()));
        if (!project.voicePath.isEmpty()) {
            File take = new File(project.voicePath);
            page.addView(text("CURRENT TAKE: " + take.getName(), 13, WHITE, true));
            page.addView(actionButton("▶ PLAY TAKE", CYAN, v -> playVoice()));
            page.addView(actionButton("REMOVE TAKE", PANEL_2, v -> { project.voicePath = ""; autosave("VOICE TAKE REMOVED"); showRecorder(); }));
        }
        setPage(page);
    }

    private void showExport() {
        LinearLayout page = column();
        page.setGravity(Gravity.CENTER);
        page.addView(text("MASTER + STEM DELIVERY", 24, WHITE, true));
        page.addView(text("Offline 44.1 kHz / 16-bit stereo WAV. Exports appear in Music/DemonicAIStudio.", 14, MUTED, false));
        page.addView(actionButton("EXPORT MASTER WAV", CYAN, v -> exportProject(false)));
        page.addView(actionButton("EXPORT MASTER + 4 STEMS", PURPLE, v -> exportProject(true)));
        page.addView(text(lastExportName, 13, GREEN, true));
        Button share = actionButton("SHARE LAST MASTER", GOLD, v -> shareLastExport());
        share.setEnabled(lastExportUri != null);
        page.addView(share);
        setPage(page);
    }

    private void showProject() {
        LinearLayout page = column();
        page.setPadding(dp(24), dp(18), dp(24), dp(18));
        page.addView(text("PROJECT + AUTOSAVE", 23, GREEN, true));
        EditText name = new EditText(this);
        name.setText(project.name);
        name.setTextColor(WHITE);
        name.setHintTextColor(MUTED);
        name.setSingleLine(true);
        name.setBackgroundColor(PANEL_2);
        page.addView(name, new LinearLayout.LayoutParams(-1, dp(58)));
        page.addView(actionButton("SAVE PROJECT NAME", GREEN, v -> {
            String value = name.getText().toString().trim();
            if (!value.isEmpty()) project.name = value;
            autosave("PROJECT SAVED");
            showProject();
        }));
        page.addView(text("Every note, drum step, voice take, mixer move and effect setting is automatically restored after closing the app.", 14, MUTED, false));
        page.addView(text(sessionSummary(), 15, WHITE, true));
        page.addView(actionButton("NEW EMPTY PROJECT", RED, v -> confirmNewProject()));
        setPage(page);
    }

    private void startTransport() {
        if (playing) return;
        playing = true;
        transportStep = selectedStep;
        transportHandler.removeCallbacks(transportRunner);
        transportHandler.post(transportRunner);
        status.setText("TRANSPORT PLAYING");
    }

    private void pauseTransport() {
        playing = false;
        transportHandler.removeCallbacks(transportRunner);
        selectedStep = lastPlayedStep;
        stopVoicePlayer();
        status.setText("TRANSPORT PAUSED");
        refreshTimeline();
    }

    private void stopTransport() {
        playing = false;
        transportHandler.removeCallbacks(transportRunner);
        transportStep = 0;
        lastPlayedStep = 0;
        selectedStep = 0;
        stopVoicePlayer();
        position.setText("01/16 · " + project.bpm + " BPM");
        status.setText("TRANSPORT STOPPED");
        refreshTimeline();
    }

    private void triggerStep(int step) {
        if (project.trackAudible(StudioProject.TRACK_PIANO))
            for (double frequency : project.frequenciesAt(StudioProject.TRACK_PIANO, step)) if (frequency > 0) playPiano(frequency);
        if (project.trackAudible(StudioProject.TRACK_GUITAR)) {
            double[] guitar = project.frequenciesAt(StudioProject.TRACK_GUITAR, step);
            if (guitar.length > 0) audio.playGuitarChord(guitar, true, project.volume[1], project.pan[1], project.reverb[1], project.delay[1]);
        }
        if (project.trackAudible(StudioProject.TRACK_DRUMS))
            for (int lane = 0; lane < 4; lane++) if (project.drums[lane][step]) playDrum(lane);
        if (project.trackAudible(StudioProject.TRACK_VOICE) && !project.voicePath.isEmpty() && project.voiceStartStep == step)
            playVoice();
    }

    private void playPiano(double frequency) {
        if (!project.trackAudible(0)) return;
        audio.playPiano(frequency, sustain ? 1450 : 760, project.volume[0], project.pan[0], project.reverb[0], project.delay[0]);
    }

    private void playGuitarNote(double frequency) {
        if (!project.trackAudible(1)) return;
        audio.playGuitarNote(frequency, project.volume[1], project.pan[1], project.reverb[1], project.delay[1]);
    }

    private void playDrum(int lane) {
        if (!project.trackAudible(2)) return;
        audio.playDrum(lane, project.volume[2], project.pan[2], project.reverb[2], project.delay[2]);
    }

    private void strumChord(boolean down) {
        double[] frequencies = midiFrequencies(chordMidi[selectedChord]);
        if (project.trackAudible(1)) audio.playGuitarChord(frequencies, down, project.volume[1], project.pan[1], project.reverb[1], project.delay[1]);
        if (guitarArmed) {
            int step = recordStep();
            project.setFrequencies(StudioProject.TRACK_GUITAR, step, frequencies);
            selectedStep = step;
            autosave("GUITAR REC · " + chordNames[selectedChord] + " → STEP " + (step + 1));
        }
    }

    private int recordStep() { return playing ? lastPlayedStep : selectedStep; }

    private void toggleRecording() {
        if (wavRecorder.isRunning()) {
            File take = wavRecorder.stop();
            if (take != null) {
                project.voicePath = take.getAbsolutePath();
                project.voiceStartStep = selectedStep;
                autosave("VOICE TAKE SAVED TO STEP " + (selectedStep + 1));
            } else status.setText("RECORDING FAILED · " + wavRecorder.getLastError());
            showRecorder();
            return;
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            pendingRecord = true;
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION);
            return;
        }
        beginRecording();
    }

    private void beginRecording() {
        try {
            File directory = new File(getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC), "DemonicTakes");
            if (!directory.exists() && !directory.mkdirs()) throw new IllegalStateException("Cannot create take folder");
            File output = new File(directory, "Take_" + System.currentTimeMillis() + ".wav");
            if (wavRecorder.start(output)) {
                status.setText("RECORDING WAV · STEP " + (selectedStep + 1));
                showRecorder();
            } else status.setText("MIC FAILED · " + wavRecorder.getLastError());
        } catch (Exception error) {
            status.setText("RECORD FAILED · " + error.getClass().getSimpleName());
        }
    }

    private void playVoice() {
        if (project.voicePath.isEmpty()) return;
        stopVoicePlayer();
        try {
            voicePlayer = new MediaPlayer();
            voicePlayer.setDataSource(project.voicePath);
            float left = project.volume[3] * (project.pan[3] <= 0 ? 1f : 1f - project.pan[3]);
            float right = project.volume[3] * (project.pan[3] >= 0 ? 1f : 1f + project.pan[3]);
            voicePlayer.setVolume(left, right);
            voicePlayer.setOnCompletionListener(player -> { player.release(); if (voicePlayer == player) voicePlayer = null; });
            voicePlayer.prepare();
            voicePlayer.start();
            status.setText("PLAYING VOICE TAKE");
        } catch (Exception error) {
            stopVoicePlayer();
            status.setText("VOICE PLAYBACK FAILED");
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
        stopTransport();
        status.setText(stems ? "RENDERING MASTER + STEMS…" : "RENDERING MASTER…");
        new Thread(() -> {
            try {
                StudioRenderer.Result result = new StudioRenderer().render(this, project, stems);
                Uri masterUri = StudioRenderer.publish(this, result.master);
                for (File stem : result.stems) StudioRenderer.publish(this, stem);
                runOnUiThread(() -> {
                    lastExportUri = masterUri;
                    lastExportName = result.master.getName() + (stems ? " + 4 stems" : "");
                    status.setText("EXPORT COMPLETE · MUSIC/DEMONICAISTUDIO");
                    showExport();
                });
            } catch (Exception error) {
                runOnUiThread(() -> status.setText("EXPORT FAILED · " + error.getClass().getSimpleName()));
            }
        }, "demonic-export").start();
    }

    private void shareLastExport() {
        if (lastExportUri == null) return;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/wav");
        share.putExtra(Intent.EXTRA_STREAM, lastExportUri);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share master WAV"));
    }

    private void copyClip() {
        clipboardTrack = selectedTrack;
        if (selectedTrack == 0) clipboard = project.piano[selectedStep];
        else if (selectedTrack == 1) clipboard = project.guitar[selectedStep];
        else if (selectedTrack == 2) {
            StringBuilder value = new StringBuilder();
            for (boolean[] lane : project.drums) value.append(lane[selectedStep] ? '1' : '0');
            clipboard = value.toString();
        } else clipboard = project.voicePath;
        status.setText(clipboard.isEmpty() ? "EMPTY CLIP COPIED" : "CLIP COPIED");
    }

    private void pasteClip() {
        if (clipboardTrack != selectedTrack || clipboard.isEmpty()) {
            status.setText("COPY A CLIP FROM THIS TRACK FIRST");
            return;
        }
        if (selectedTrack == 0) project.piano[selectedStep] = clipboard;
        else if (selectedTrack == 1) project.guitar[selectedStep] = clipboard;
        else if (selectedTrack == 2 && clipboard.length() == 4)
            for (int lane = 0; lane < 4; lane++) project.drums[lane][selectedStep] = clipboard.charAt(lane) == '1';
        else if (selectedTrack == 3) { project.voicePath = clipboard; project.voiceStartStep = selectedStep; }
        autosave("CLIP PASTED");
        showTimeline();
    }

    private void moveSelected(int direction) {
        int destination = (selectedStep + direction + StudioProject.STEPS) % StudioProject.STEPS;
        project.moveClip(selectedTrack, selectedStep, destination);
        selectedStep = destination;
        autosave("CLIP MOVED");
        showTimeline();
    }

    private void refreshTimeline() {
        for (int track = 0; track < StudioProject.TRACK_COUNT; track++) {
            for (int step = 0; step < StudioProject.STEPS; step++) {
                Button cell = timelineCells[track][step];
                if (cell == null) continue;
                boolean clip = project.hasClip(track, step);
                boolean current = playing && lastPlayedStep == step;
                boolean selected = selectedTrack == track && selectedStep == step;
                cell.setText(clip ? "●" : "");
                cell.setBackgroundColor(current ? WHITE : selected ? GREEN : clip ? TRACK_COLORS[track] : PANEL_2);
            }
        }
        for (int lane = 0; lane < 4; lane++) for (int step = 0; step < StudioProject.STEPS; step++)
            if (drumButtons[lane][step] != null) styleDrum(drumButtons[lane][step], lane, step);
    }

    private void styleDrum(Button button, int lane, int step) {
        boolean current = playing && lastPlayedStep == step;
        button.setBackgroundColor(current ? WHITE : project.drums[lane][step] ? drumColors[lane] : PANEL_2);
    }

    private void autosave(String message) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PROJECT_KEY, project.toJson()).apply();
        status.setText(message + " · AUTOSAVED");
        position.setText(String.format(Locale.US, "%02d/16 · %d BPM", selectedStep + 1, project.bpm));
    }

    private void confirmNewProject() {
        new AlertDialog.Builder(this)
                .setTitle("Start a new project?")
                .setMessage("This clears the current timeline and mixer autosave. Export anything you want to keep first.")
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("NEW PROJECT", (dialog, which) -> {
                    stopTransport();
                    project = new StudioProject();
                    selectedStep = 0;
                    selectedTrack = 0;
                    autosave("NEW PROJECT READY");
                    showHome();
                }).show();
    }

    private String sessionSummary() {
        int piano = 0, guitar = 0, drums = 0;
        for (int step = 0; step < StudioProject.STEPS; step++) {
            if (project.hasClip(0, step)) piano++;
            if (project.hasClip(1, step)) guitar++;
            if (project.hasClip(2, step)) drums++;
        }
        return "Piano clips: " + piano + "  ·  Guitar clips: " + guitar + "  ·  Drum steps: " + drums
                + "  ·  Voice: " + (project.voicePath.isEmpty() ? "none" : "attached") + "  ·  Tempo: " + project.bpm;
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == MIC_PERMISSION && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED && pendingRecord) {
            pendingRecord = false;
            beginRecording();
        } else if (requestCode == MIC_PERMISSION) {
            pendingRecord = false;
            status.setText("MICROPHONE PERMISSION REQUIRED");
        }
    }

    private SeekBar.OnSeekBarChangeListener seekListener(IntValue action) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { if (fromUser) action.set(progress); }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        };
    }

    private interface IntValue { void set(int value); }

    private static double[] midiFrequencies(int[] midi) {
        double[] frequencies = new double[midi.length];
        for (int index = 0; index < midi.length; index++) frequencies[index] = 440.0 * Math.pow(2.0, (midi[index] - 69.0) / 12.0);
        return frequencies;
    }

    private void setPage(View page) {
        content.removeAllViews();
        content.addView(page, new FrameLayout.LayoutParams(-1, -1));
    }

    private LinearLayout column() { LinearLayout value = new LinearLayout(this); value.setOrientation(LinearLayout.VERTICAL); return value; }
    private LinearLayout row() { LinearLayout value = new LinearLayout(this); value.setOrientation(LinearLayout.HORIZONTAL); return value; }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setTypeface(Typeface.MONOSPACE, bold ? Typeface.BOLD : Typeface.NORMAL);
        view.setPadding(dp(7), dp(4), dp(7), dp(4));
        return view;
    }

    private Button navButton(String label, View.OnClickListener listener) {
        Button button = smallButton(label, listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(118), -1);
        params.setMargins(dp(3), 0, dp(3), 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button smallButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(11);
        button.setTextColor(WHITE);
        button.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        button.setBackgroundColor(PANEL_2);
        button.setOnClickListener(listener);
        button.setPadding(dp(7), 0, dp(7), 0);
        return button;
    }

    private Button toggleButton(String label, boolean active, View.OnClickListener listener) {
        Button button = smallButton(label, listener);
        button.setTextColor(active ? BG : WHITE);
        button.setBackgroundColor(active ? GREEN : PANEL_2);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(105), dp(52));
        params.setMargins(dp(3), 0, dp(3), 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button actionButton(String label, int color, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTextColor(color == PANEL_2 ? WHITE : BG);
        button.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        button.setBackgroundColor(color);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(54));
        params.setMargins(dp(7), dp(7), dp(7), dp(7));
        button.setLayoutParams(params);
        return button;
    }

    private View featureCard(String title, String description, int accent, View.OnClickListener listener) {
        LinearLayout card = column();
        card.setBackgroundColor(PANEL);
        card.setPadding(dp(14), dp(10), dp(14), dp(10));
        card.addView(text(title, 16, accent, true));
        card.addView(text(description, 12, MUTED, false));
        card.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(116), 1);
        params.setMargins(dp(5), dp(5), dp(5), dp(5));
        card.setLayoutParams(params);
        return card;
    }

    private GridLayout.LayoutParams cellParams(int width, int height) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = dp(width);
        params.height = dp(height);
        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        return params;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onPause() {
        super.onPause();
        pauseTransport();
        if (wavRecorder.isRunning()) {
            File take = wavRecorder.stop();
            if (take != null) {
                project.voicePath = take.getAbsolutePath();
                project.voiceStartStep = selectedStep;
                autosave("VOICE TAKE SAVED");
            }
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        transportHandler.removeCallbacksAndMessages(null);
        stopVoicePlayer();
    }
}
