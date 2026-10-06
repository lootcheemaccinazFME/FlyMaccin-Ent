package com.flymaccin.demonicaistudio;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(8, 9, 14);
    private static final int PANEL = Color.rgb(19, 22, 31);
    private static final int PANEL_2 = Color.rgb(30, 33, 44);
    private static final int PURPLE = Color.rgb(164, 92, 255);
    private static final int RED = Color.rgb(255, 61, 88);
    private static final int CYAN = Color.rgb(55, 226, 255);
    private static final int GOLD = Color.rgb(255, 206, 84);
    private static final int WHITE = Color.rgb(239, 241, 248);
    private static final int MUTED = Color.rgb(157, 163, 180);
    private static final int PICK_AUDIO = 1401;
    private static final int MIC_PERMISSION = 1402;

    private final AudioEngine audio = new AudioEngine();
    private final Handler beatHandler = new Handler(Looper.getMainLooper());
    private final boolean[][] drumSteps = new boolean[4][16];
    private final Button[][] drumButtons = new Button[4][16];
    private final String[] drumNames = {"808", "SNARE", "HAT", "PERC"};
    private final int[] laneColors = {RED, PURPLE, CYAN, GOLD};

    private FrameLayout content;
    private TextView status;
    private int bpm = 96;
    private int swing = 0;
    private int octave = 4;
    private int beatStep = 0;
    private boolean beatPlaying;
    private boolean sustain;
    private Uri importedSample;
    private String importedSampleName = "No sample loaded";
    private MediaRecorder recorder;
    private boolean recording;
    private File recordingFile;

    private final Runnable beatRunner = new Runnable() {
        @Override
        public void run() {
            if (!beatPlaying) return;
            int previous = beatStep;
            beatStep = (beatStep + 1) % 16;
            for (int lane = 0; lane < 4; lane++) {
                if (drumSteps[lane][beatStep]) audio.playDrum(lane);
                if (drumButtons[lane][previous] != null) styleStep(drumButtons[lane][previous], lane, previous);
                if (drumButtons[lane][beatStep] != null) styleStep(drumButtons[lane][beatStep], lane, beatStep);
            }
            long base = Math.max(60, 60000L / bpm / 4L);
            long delay = beatStep % 2 == 1 ? base + (base * swing / 100) : base - (base * swing / 100);
            beatHandler.postDelayed(this, delay);
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemBars();
        setContentView(buildShell());
        restorePattern();
        showHome();
    }

    private void hideSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
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
        LinearLayout root = column();
        root.setBackgroundColor(BG);

        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(10), dp(18), dp(10));
        TextView brand = text("DEMONIC AI", 23, WHITE, true);
        header.addView(brand);
        TextView build = text("  STUDIO HUB / NATIVE BUILD 001", 13, PURPLE, true);
        header.addView(build);
        header.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        status = text("ENGINE READY", 12, CYAN, true);
        header.addView(status);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(54)));

        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout nav = row();
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(6), dp(8), dp(8));
        nav.addView(navButton("⌂ HOME", v -> showHome()));
        nav.addView(navButton("♫ PIANO", v -> showPiano()));
        nav.addView(navButton("▦ DRUMS", v -> showDrums()));
        nav.addView(navButton("◉ RECORD", v -> showRecorder()));
        nav.addView(navButton("▤ PROJECTS", v -> showProjects()));
        nav.addView(navButton("◆ LIBRARY", v -> showLibrary()));
        root.addView(nav, new LinearLayout.LayoutParams(-1, dp(62)));
        return root;
    }

    private void showHome() {
        stopBeat();
        LinearLayout page = column();
        page.setPadding(dp(22), dp(12), dp(22), dp(12));
        page.addView(text("PROFESSIONAL MOBILE MUSIC PRODUCTION", 14, MUTED, true));
        page.addView(text("One native hub for instruments, beats, recording, patterns, and local files.", 17, WHITE, false));
        LinearLayout cards = row();
        cards.setGravity(Gravity.CENTER);
        cards.setPadding(0, dp(20), 0, 0);
        cards.addView(featureCard("PIANO STUDIO", "Playable synth · octave · sustain", CYAN, v -> showPiano()));
        cards.addView(featureCard("CHORD / GUITAR", "C · G · D · Am · Em", GOLD, v -> showChords()));
        cards.addView(featureCard("BEAT MAKER", "808 kit · 16-step sequencer", RED, v -> showDrums()));
        cards.addView(featureCard("VOICE RECORD", "Local takes · no upload", PURPLE, v -> showRecorder()));
        setPage(page);
        status.setText("LOCAL SESSION · ENGINE READY");
    }

    private void showPiano() {
        stopBeat();
        LinearLayout page = column();
        page.setPadding(dp(18), dp(10), dp(18), dp(10));
        LinearLayout controls = row();
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(text("PIANO STANDARD", 18, WHITE, true));
        controls.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        controls.addView(smallButton("− OCT", v -> { octave = Math.max(2, octave - 1); showPiano(); }));
        controls.addView(text("  " + octave + "  ", 19, CYAN, true));
        controls.addView(smallButton("OCT +", v -> { octave = Math.min(6, octave + 1); showPiano(); }));
        Button sustainButton = smallButton(sustain ? "SUSTAIN ON" : "SUSTAIN OFF", v -> {
            sustain = !sustain;
            showPiano();
        });
        controls.addView(sustainButton);
        page.addView(controls, new LinearLayout.LayoutParams(-1, dp(50)));

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        LinearLayout keys = row();
        String[] names = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E"};
        int[] semitones = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
        for (int i = 0; i < names.length; i++) {
            boolean black = names[i].contains("#");
            final int semitone = semitones[i];
            Button key = new Button(this);
            key.setText(names[i] + "\n" + (octave + (semitone / 12)));
            key.setTextColor(black ? WHITE : Color.rgb(15, 16, 22));
            key.setTextSize(15);
            key.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            key.setBackgroundColor(black ? PANEL : WHITE);
            key.setAllCaps(false);
            key.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    double midi = 12 * (octave + 1) + semitone;
                    double frequency = 440.0 * Math.pow(2.0, (midi - 69.0) / 12.0);
                    audio.playPiano(frequency, sustain ? 1500 : 620);
                    status.setText("PLAYING " + names[semitone % 12] + (octave + semitone / 12));
                    return true;
                }
                return false;
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(72), black ? dp(175) : dp(235));
            params.setMargins(dp(2), dp(2), dp(2), dp(2));
            keys.addView(key, params);
        }
        scroll.addView(keys);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        page.addView(text("Tap keys to perform. All sound is synthesized on-device.", 13, MUTED, false));
        setPage(page);
    }

    private void showChords() {
        stopBeat();
        LinearLayout page = column();
        page.setGravity(Gravity.CENTER);
        page.setPadding(dp(22), dp(12), dp(22), dp(12));
        page.addView(text("CHORD / GUITAR STUDIO", 24, GOLD, true));
        page.addView(text("Merged from Guitar Simulator. Tap a pad to strum a synthesized chord.", 15, MUTED, false));
        LinearLayout pads = row();
        pads.setGravity(Gravity.CENTER);
        String[] names = {"C", "G", "D", "Am", "Em"};
        double[][] notes = {
                {130.81, 164.81, 196.00}, {98.00, 123.47, 146.83, 196.00},
                {146.83, 185.00, 220.00}, {110.00, 130.81, 164.81, 220.00},
                {82.41, 123.47, 164.81, 196.00}
        };
        for (int i = 0; i < names.length; i++) {
            final int index = i;
            Button pad = new Button(this);
            pad.setText(names[i]);
            pad.setTextSize(26);
            pad.setTextColor(BG);
            pad.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            pad.setBackgroundColor(GOLD);
            pad.setOnClickListener(v -> {
                audio.playGuitarChord(notes[index]);
                status.setText("STRUM " + names[index]);
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(145), dp(145));
            params.setMargins(dp(8), dp(24), dp(8), dp(8));
            pads.addView(pad, params);
        }
        page.addView(pads);
        setPage(page);
    }

    private void showDrums() {
        LinearLayout page = column();
        page.setPadding(dp(14), dp(8), dp(14), dp(8));
        LinearLayout controls = row();
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(text("808 BEAT MAKER · 16 STEPS", 18, WHITE, true));
        controls.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        controls.addView(smallButton("BPM −", v -> { bpm = Math.max(50, bpm - 2); showDrums(); }));
        controls.addView(text("  " + bpm + " BPM  ", 16, CYAN, true));
        controls.addView(smallButton("BPM +", v -> { bpm = Math.min(190, bpm + 2); showDrums(); }));
        controls.addView(smallButton("SWING " + swing + "%", v -> { swing = (swing + 5) % 35; showDrums(); }));
        page.addView(controls);

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(17);
        grid.setRowCount(5);
        grid.addView(text("KIT", 12, MUTED, true), cellParams(100, 40));
        for (int step = 0; step < 16; step++) grid.addView(text(String.valueOf(step + 1), 11, MUTED, true), cellParams(48, 40));
        for (int lane = 0; lane < 4; lane++) {
            final int laneIndex = lane;
            Button audition = smallButton(drumNames[lane], v -> audio.playDrum(laneIndex));
            grid.addView(audition, cellParams(100, 48));
            for (int step = 0; step < 16; step++) {
                final int stepIndex = step;
                Button cell = new Button(this);
                cell.setText("");
                cell.setMinWidth(0);
                cell.setMinimumWidth(0);
                cell.setPadding(0, 0, 0, 0);
                drumButtons[lane][step] = cell;
                styleStep(cell, lane, step);
                cell.setOnClickListener(v -> {
                    drumSteps[laneIndex][stepIndex] = !drumSteps[laneIndex][stepIndex];
                    styleStep((Button) v, laneIndex, stepIndex);
                    if (drumSteps[laneIndex][stepIndex]) audio.playDrum(laneIndex);
                });
                grid.addView(cell, cellParams(48, 48));
            }
        }
        scroll.addView(grid);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout actions = row();
        actions.setGravity(Gravity.CENTER);
        actions.addView(actionButton(beatPlaying ? "■ STOP" : "▶ PLAY LOOP", beatPlaying ? RED : CYAN,
                v -> { if (beatPlaying) stopBeat(); else startBeat(); showDrums(); }));
        actions.addView(actionButton("SAVE PATTERN", PURPLE, v -> savePattern()));
        actions.addView(actionButton("CLEAR GRID", PANEL_2, v -> { clearPattern(); showDrums(); }));
        page.addView(actions);
        setPage(page);
        status.setText(String.format(Locale.US, "STEP %02d / 16 · %d BPM", beatStep + 1, bpm));
    }

    private void startBeat() {
        beatPlaying = true;
        beatStep = 15;
        beatHandler.removeCallbacks(beatRunner);
        beatHandler.post(beatRunner);
    }

    private void stopBeat() {
        beatPlaying = false;
        beatHandler.removeCallbacks(beatRunner);
    }

    private void showRecorder() {
        stopBeat();
        LinearLayout page = column();
        page.setGravity(Gravity.CENTER);
        page.addView(text("VOICE RECORDER", 25, PURPLE, true));
        page.addView(text("Record a local vocal or idea. Audio stays in this app's private Music folder.", 15, MUTED, false));
        Button record = actionButton(recording ? "■ STOP & SAVE" : "● START RECORDING", recording ? CYAN : RED,
                v -> toggleRecording());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(260), dp(70));
        params.setMargins(0, dp(30), 0, 0);
        page.addView(record, params);
        if (recordingFile != null) page.addView(text("LAST TAKE: " + recordingFile.getName(), 13, WHITE, true));
        setPage(page);
    }

    private void toggleRecording() {
        if (recording) {
            stopRecording();
            showRecorder();
            return;
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION);
            return;
        }
        startRecording();
        showRecorder();
    }

    private void startRecording() {
        try {
            File directory = new File(getExternalFilesDir(null), "Music");
            if (!directory.exists() && !directory.mkdirs()) throw new IllegalStateException("Cannot create Music folder");
            recordingFile = new File(directory, "Demonic_Take_" + System.currentTimeMillis() + ".m4a");
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(128000);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(recordingFile.getAbsolutePath());
            recorder.prepare();
            recorder.start();
            recording = true;
            status.setText("RECORDING · LOCAL TAKE");
        } catch (Exception error) {
            recording = false;
            status.setText("RECORD FAILED · " + error.getClass().getSimpleName());
            releaseRecorder();
        }
    }

    private void stopRecording() {
        try {
            if (recorder != null) recorder.stop();
            status.setText("TAKE SAVED LOCALLY");
        } catch (RuntimeException error) {
            if (recordingFile != null) recordingFile.delete();
            status.setText("TAKE DISCARDED");
        } finally {
            recording = false;
            releaseRecorder();
        }
    }

    private void releaseRecorder() {
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

    private void showProjects() {
        stopBeat();
        LinearLayout page = column();
        page.setPadding(dp(24), dp(16), dp(24), dp(16));
        page.addView(text("PROJECTS / LOCAL SESSION", 24, WHITE, true));
        SharedPreferences prefs = getSharedPreferences("demonic", MODE_PRIVATE);
        boolean saved = prefs.contains("pattern");
        View patternCard = featureCard(saved ? "PATTERN 1 · SAVED" : "PATTERN 1 · EMPTY",
                saved ? bpm + " BPM · 16-step drum grid" : "Create a beat, then save the pattern.",
                saved ? PURPLE : PANEL_2, v -> showDrums());
        LinearLayout.LayoutParams projectParams = new LinearLayout.LayoutParams(-1, dp(92));
        projectParams.setMargins(0, dp(10), 0, 0);
        patternCard.setLayoutParams(projectParams);
        page.addView(patternCard);
        if (recordingFile != null) {
            View takeCard = featureCard("LATEST VOCAL TAKE", recordingFile.getName(), RED, v -> showRecorder());
            takeCard.setLayoutParams(projectParams);
            page.addView(takeCard);
        }
        setPage(page);
    }

    private void showLibrary() {
        stopBeat();
        LinearLayout page = column();
        page.setGravity(Gravity.CENTER);
        page.addView(text("SAMPLE VAULT", 25, CYAN, true));
        page.addView(text("Use Android's file picker to bring an audio file into the current local session.", 15, MUTED, false));
        page.addView(actionButton("IMPORT SAMPLE", CYAN, v -> chooseAudio()));
        page.addView(text(importedSampleName, 15, WHITE, true));
        Button play = actionButton("PLAY SAMPLE", PURPLE, v -> playImportedSample());
        play.setEnabled(importedSample != null);
        page.addView(play);
        setPage(page);
    }

    private void chooseAudio() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_AUDIO);
    }

    private void playImportedSample() {
        if (importedSample == null) return;
        try {
            MediaPlayer player = MediaPlayer.create(this, importedSample);
            if (player == null) throw new IllegalStateException("Unsupported audio");
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
            status.setText("PLAYING SAMPLE · " + importedSampleName);
        } catch (Exception error) {
            status.setText("SAMPLE PLAYBACK FAILED");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            importedSample = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(importedSample, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
            }
            importedSampleName = displayName(importedSample);
            status.setText("SAMPLE READY · " + importedSampleName);
            showLibrary();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MIC_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
            showRecorder();
        } else if (requestCode == MIC_PERMISSION) {
            status.setText("MICROPHONE PERMISSION REQUIRED");
        }
    }

    private String displayName(Uri uri) {
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) return cursor.getString(index);
            }
        }
        return uri.getLastPathSegment() == null ? "Imported sample" : uri.getLastPathSegment();
    }

    private void savePattern() {
        StringBuilder encoded = new StringBuilder();
        for (int lane = 0; lane < 4; lane++) {
            for (int step = 0; step < 16; step++) encoded.append(drumSteps[lane][step] ? '1' : '0');
        }
        getSharedPreferences("demonic", MODE_PRIVATE).edit()
                .putString("pattern", encoded.toString())
                .putInt("bpm", bpm)
                .putInt("swing", swing)
                .apply();
        status.setText("PATTERN 1 SAVED LOCALLY");
    }

    private void restorePattern() {
        SharedPreferences prefs = getSharedPreferences("demonic", MODE_PRIVATE);
        String encoded = prefs.getString("pattern", "");
        bpm = prefs.getInt("bpm", 96);
        swing = prefs.getInt("swing", 0);
        if (encoded.length() == 64) {
            for (int lane = 0; lane < 4; lane++) {
                for (int step = 0; step < 16; step++) drumSteps[lane][step] = encoded.charAt(lane * 16 + step) == '1';
            }
        }
    }

    private void clearPattern() {
        for (int lane = 0; lane < 4; lane++) for (int step = 0; step < 16; step++) drumSteps[lane][step] = false;
        status.setText("GRID CLEARED");
    }

    private void styleStep(Button button, int lane, int step) {
        boolean active = drumSteps[lane][step];
        boolean current = beatPlaying && beatStep == step;
        button.setBackgroundColor(current ? WHITE : active ? laneColors[lane] : PANEL_2);
    }

    private void setPage(View page) {
        content.removeAllViews();
        content.addView(page, new FrameLayout.LayoutParams(-1, -1));
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
        view.setPadding(dp(8), dp(5), dp(8), dp(5));
        return view;
    }

    private Button navButton(String label, View.OnClickListener listener) {
        Button button = smallButton(label, listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1);
        params.setMargins(dp(3), 0, dp(3), 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button smallButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(12);
        button.setTextColor(WHITE);
        button.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        button.setBackgroundColor(PANEL_2);
        button.setOnClickListener(listener);
        button.setPadding(dp(8), 0, dp(8), 0);
        return button;
    }

    private Button actionButton(String label, int color, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setTextColor(color == PANEL_2 ? WHITE : BG);
        button.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        button.setBackgroundColor(color);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(56));
        params.setMargins(dp(8), dp(10), dp(8), dp(10));
        button.setLayoutParams(params);
        return button;
    }

    private View featureCard(String title, String description, int accent, View.OnClickListener listener) {
        LinearLayout card = column();
        card.setBackgroundColor(PANEL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.addView(text(title, 17, accent, true));
        card.addView(text(description, 13, MUTED, false));
        card.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(150), 1);
        params.setMargins(dp(7), dp(8), dp(7), dp(8));
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBeat();
        if (recording) stopRecording();
    }
}
