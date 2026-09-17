package com.flymaccin.demonicaistudio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SongAiActivity extends Activity {
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
    private static final String PREFS = "demonic_song_ai_state_v1";
    private static final String STUDIO_PREFS = "demonic_studio_v110";
    private static final String STUDIO_PROJECT_KEY = "autosave_project";
    private static final String[] STEMS = {
            "BEAT", "SP3 MAIN LEAD", "SP3 DOUBLE", "SP3 LOW UNDERLAYER",
            "AD-LIBS", "HOOK RESPONSE", "ECHO THROW", "FX"
    };

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile HttpURLConnection activeConnection;

    private EditText title;
    private EditText brief;
    private EditText beatCode;
    private EditText bpm;
    private EditText delivery;
    private EditText register;
    private EditText energy;
    private EditText projection;
    private EditText chestWeight;
    private EditText brightness;
    private EditText adlibDensity;
    private EditText fxCode;
    private EditText voiceProfile;
    private EditText masterLyrics;
    private EditText performanceMap;
    private EditText renderPackage;
    private TextView status;
    private TextView configStatus;
    private MediaPlayer previewPlayer;
    private File lastVoiceRender;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemBars();
        setContentView(buildUi());
        restoreState();
        refreshConfigStatus();
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

    private View buildUi() {
        LinearLayout root = column();
        root.setBackgroundColor(BG);

        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(12), dp(6), dp(12), dp(6));
        header.addView(text("DEMONIC SONG AI", 20, WHITE, true));
        header.addView(text("  MASTER → SP3 → VOICE → DAW", 12, PURPLE, true));
        header.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
        configStatus = text("CONFIG", 11, MUTED, true);
        header.addView(configStatus);
        header.addView(smallButton("CONFIG", v -> showConfigDialog()));
        header.addView(smallButton("DAW", v -> sendToDaw()));
        header.addView(smallButton("CLOSE", v -> finish()));
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(54)));

        HorizontalScrollView outer = new HorizontalScrollView(this);
        LinearLayout workspace = row();
        workspace.setPadding(dp(8), dp(6), dp(8), dp(8));

        ScrollView leftScroll = new ScrollView(this);
        LinearLayout left = column();
        left.setPadding(dp(8), dp(4), dp(12), dp(12));
        left.addView(text("SONG COMMAND", 16, CYAN, true));
        title = field(left, "Project title", "Untitled SP3 Song", false);
        brief = field(left, "Song brief", "Funny grimy Richmond hyphy song about fake people talking too much.", true);
        beatCode = field(left, "Beat code", "FME_SP3_B10225_104BPM_HYPHYFUNK_808BOUNCE", false);
        bpm = field(left, "BPM", "104", false);
        delivery = field(left, "SP3 delivery", "SP3_BAY_PLAYFUL_MOB_V2", false);
        register = field(left, "Register lift", "18", false);
        energy = field(left, "Energy %", "50", false);
        projection = field(left, "Projection %", "40", false);
        chestWeight = field(left, "Chest weight", "-20", false);
        brightness = field(left, "Brightness", "18", false);
        adlibDensity = field(left, "Ad-lib density %", "38", false);
        fxCode = field(left, "FX code", "ECHO_1/4_DELAY_25%FEEDBACK_20%WET", false);
        voiceProfile = field(left, "Authorized voice profile", "SP3", false);

        left.addView(actionButton("GENERATE MASTER LYRICS", PURPLE, v -> generateLyrics()));
        left.addView(actionButton("BUILD PERFORMANCE MAP", CYAN, v -> buildPerformanceMap()));
        left.addView(actionButton("BUILD RENDER PACKAGE", GOLD, v -> buildRenderPackage()));
        left.addView(actionButton("RENDER MY VOICE", GREEN, v -> renderVoice()));
        left.addView(actionButton("STOP ACTIVE REQUEST", RED, v -> cancelActive()));
        left.addView(text("Creator rule: the master lyric is authoritative. Provider compatibility never rewrites it automatically.", 11, MUTED, false));
        leftScroll.addView(left);
        workspace.addView(leftScroll, new LinearLayout.LayoutParams(dp(390), -1));

        ScrollView rightScroll = new ScrollView(this);
        LinearLayout right = column();
        right.setPadding(dp(10), dp(4), dp(12), dp(12));
        right.addView(text("MASTER LYRICS", 16, GOLD, true));
        masterLyrics = largeField("Write or generate the master lyrics here.", 350);
        right.addView(masterLyrics);
        LinearLayout masterActions = row();
        masterActions.addView(smallButton("SAVE MASTER", v -> saveState("MASTER SAVED")));
        masterActions.addView(smallButton("COPY", v -> copy(masterLyrics.getText().toString())));
        masterActions.addView(smallButton("CLEAR", v -> { masterLyrics.setText(""); saveState("MASTER CLEARED"); }));
        right.addView(masterActions);

        right.addView(text("SP3 PERFORMANCE MAP", 16, PURPLE, true));
        performanceMap = largeField("Build the performance map after the lyrics are locked.", 250);
        right.addView(performanceMap);

        right.addView(text("RENDER / DAW PACKAGE", 16, CYAN, true));
        renderPackage = largeField("Render package appears here.", 220);
        right.addView(renderPackage);

        LinearLayout renderActions = row();
        renderActions.addView(actionButton("PREVIEW VOCAL", PURPLE, v -> previewLastRender()));
        renderActions.addView(actionButton("SEND VOCAL TO DAW", GREEN, v -> sendToDaw()));
        renderActions.addView(actionButton("SAVE PACKAGE", PANEL_2, v -> savePackageFile()));
        right.addView(renderActions);

        right.addView(text("STEM PLAN", 15, WHITE, true));
        for (int i = 0; i < STEMS.length; i++) {
            right.addView(text(String.format(Locale.US, "%02d  %s", i + 1, STEMS[i]), 12, i == 1 ? PURPLE : MUTED, i == 1));
        }
        rightScroll.addView(right);
        workspace.addView(rightScroll, new LinearLayout.LayoutParams(dp(820), -1));

        outer.addView(workspace);
        root.addView(outer, new LinearLayout.LayoutParams(-1, 0, 1));

        status = text("READY", 11, GREEN, true);
        status.setPadding(dp(12), dp(5), dp(12), dp(5));
        root.addView(status, new LinearLayout.LayoutParams(-1, dp(38)));
        return root;
    }

    private void generateLyrics() {
        DemonicSecureConfig.Config config = DemonicSecureConfig.load(this);
        if (config.lyricEndpoint.isEmpty()) {
            status.setText("CONFIGURE YOUR LYRIC AI ENDPOINT FIRST");
            return;
        }
        saveState("MASTER PRESERVED");
        JSONObject body = new JSONObject();
        try {
            body.put("task", "generate_lyrics");
            body.put("schema", "fme.demonic.lyrics.v1");
            body.put("title", title.getText().toString());
            body.put("brief", brief.getText().toString());
            body.put("beatCode", beatCode.getText().toString());
            body.put("bpm", intValue(bpm, 104));
            body.put("voiceProfile", voiceProfile.getText().toString());
            body.put("delivery", delivery.getText().toString());
            body.put("registerLift", intValue(register, 18));
            body.put("energy", intValue(energy, 50));
            body.put("projection", intValue(projection, 40));
            body.put("chestWeight", intValue(chestWeight, -20));
            body.put("brightness", intValue(brightness, 18));
            body.put("adlibDensity", intValue(adlibDensity, 38));
            body.put("fx", fxCode.getText().toString());
            body.put("creatorPolicy", new JSONObject()
                    .put("masterIsAuthoritative", true)
                    .put("autoSanitize", false)
                    .put("replaceProfanity", false)
                    .put("normalizeSlang", false)
                    .put("directCloneLivingArtist", false));
            body.put("structure", new JSONArray()
                    .put("Intro").put("Verse 1 long").put("Hook").put("Pre-Chorus").put("Chorus")
                    .put("Verse 2 long").put("Pre-Chorus").put("Chorus").put("Skit")
                    .put("Verse 3 long").put("Spoken Bridge").put("Outro").put("Outro Echo"));
        } catch (Exception e) {
            status.setText("LYRIC JOB ERROR: " + e.getMessage());
            return;
        }
        status.setText("GENERATING MASTER LYRICS…");
        postJson(config.lyricEndpoint, config.lyricToken, body, result -> {
            String lyrics = result.optString("lyrics", result.optString("text", ""));
            if (lyrics.isEmpty()) {
                status.setText("LYRIC ENDPOINT RETURNED NO LYRICS");
                return;
            }
            masterLyrics.setText(lyrics);
            saveState("MASTER LYRICS GENERATED + SAVED");
        });
    }

    private void buildPerformanceMap() {
        String lyrics = masterLyrics.getText().toString().trim();
        if (lyrics.isEmpty()) {
            status.setText("MASTER LYRICS REQUIRED");
            return;
        }
        try {
            JSONObject map = new JSONObject();
            map.put("version", 1);
            map.put("voice", voiceProfile.getText().toString().trim());
            map.put("beatCode", beatCode.getText().toString().trim());
            map.put("bpm", intValue(bpm, 104));
            map.put("delivery", delivery.getText().toString().trim());
            map.put("registerLift", intValue(register, 18));
            map.put("energy", intValue(energy, 50));
            map.put("projection", intValue(projection, 40));
            map.put("chestWeight", intValue(chestWeight, -20));
            map.put("brightness", intValue(brightness, 18));
            map.put("adlibDensity", intValue(adlibDensity, 38));
            map.put("fx", fxCode.getText().toString().trim());
            map.put("sections", parsePerformanceSections(lyrics));
            map.put("stems", new JSONArray(STEMS));
            performanceMap.setText(map.toString(2));
            saveState("SP3 PERFORMANCE MAP BUILT");
        } catch (Exception e) {
            status.setText("MAP ERROR: " + e.getMessage());
        }
    }

    private JSONArray parsePerformanceSections(String lyrics) throws Exception {
        JSONArray sections = new JSONArray();
        JSONObject section = null;
        JSONArray bars = null;
        int barIndex = 0;
        String[] lines = lyrics.split("\\r?\\n");
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("[") && line.endsWith("]")) {
                section = new JSONObject();
                bars = new JSONArray();
                section.put("name", line.substring(1, line.length() - 1));
                section.put("bars", bars);
                sections.put(section);
                barIndex = 0;
                continue;
            }
            if (section == null) {
                section = new JSONObject().put("name", "Unlabeled");
                bars = new JSONArray();
                section.put("bars", bars);
                sections.put(section);
            }
            barIndex++;
            JSONObject bar = new JSONObject();
            bar.put("index", barIndex);
            bar.put("text", line);
            bar.put("pocket", pocketFor(barIndex));
            bar.put("energy", Math.min(100, Math.max(0, intValue(energy, 50) + ((barIndex % 4 == 0) ? 5 : 0))));
            bar.put("pauseBeforeMs", barIndex % 4 == 0 ? 120 : 0);
            bar.put("pauseAfterMs", barIndex % 2 == 0 ? 80 : 40);
            JSONArray adlibs = new JSONArray();
            if (containsAdlib(line)) adlibs.put("embedded");
            else if (barIndex % 4 == 0) adlibs.put(barIndex % 8 == 0 ? "YEE!" : "AYE!");
            bar.put("adlibs", adlibs);
            JSONArray doubles = new JSONArray();
            String last = lastWord(line);
            if (!last.isEmpty() && barIndex % 4 == 0) doubles.put(last);
            bar.put("doubleWords", doubles);
            JSONArray under = new JSONArray();
            if (!last.isEmpty() && barIndex % 6 == 0) under.put(last);
            bar.put("underlayerWords", under);
            bar.put("echoTail", barIndex % 4 == 0 ? last : "");
            bars.put(bar);
        }
        return sections;
    }

    private void buildRenderPackage() {
        String lyrics = masterLyrics.getText().toString().trim();
        if (lyrics.isEmpty()) {
            status.setText("MASTER LYRICS REQUIRED");
            return;
        }
        if (performanceMap.getText().toString().trim().isEmpty()) buildPerformanceMap();
        try {
            JSONObject pkg = new JSONObject();
            pkg.put("schema", "fme.demonic.song.render.v1");
            pkg.put("title", title.getText().toString());
            pkg.put("brief", brief.getText().toString());
            pkg.put("masterLyrics", lyrics);
            pkg.put("providerMutable", false);
            pkg.put("beatCode", beatCode.getText().toString());
            pkg.put("bpm", intValue(bpm, 104));
            pkg.put("voice", new JSONObject()
                    .put("profileId", voiceProfile.getText().toString())
                    .put("authorizedByCreator", true)
                    .put("delivery", delivery.getText().toString())
                    .put("registerLift", intValue(register, 18))
                    .put("energy", intValue(energy, 50))
                    .put("projection", intValue(projection, 40))
                    .put("chestWeight", intValue(chestWeight, -20))
                    .put("brightness", intValue(brightness, 18)));
            pkg.put("fx", new JSONObject()
                    .put("code", fxCode.getText().toString())
                    .put("adlibDensity", intValue(adlibDensity, 38)));
            String mapText = performanceMap.getText().toString().trim();
            if (!mapText.isEmpty()) pkg.put("performanceMap", new JSONObject(mapText));
            pkg.put("stems", new JSONArray(STEMS));
            pkg.put("output", new JSONObject()
                    .put("format", "wav")
                    .put("sampleRate", 44100)
                    .put("separateLayers", true));
            renderPackage.setText(pkg.toString(2));
            saveState("RENDER PACKAGE BUILT");
        } catch (Exception e) {
            status.setText("PACKAGE ERROR: " + e.getMessage());
        }
    }

    private void renderVoice() {
        DemonicSecureConfig.Config config = DemonicSecureConfig.load(this);
        if (config.voiceEndpoint.isEmpty()) {
            status.setText("CONFIGURE YOUR AUTHORIZED VOICE ENDPOINT FIRST");
            return;
        }
        buildRenderPackage();
        try {
            JSONObject body = new JSONObject(renderPackage.getText().toString());
            JSONObject voice = body.optJSONObject("voice");
            if (voice == null || !voice.optBoolean("authorizedByCreator", false)) {
                status.setText("CREATOR VOICE AUTHORIZATION REQUIRED");
                return;
            }
            status.setText("RENDERING AUTHORIZED SP3 VOICE…");
            postJson(config.voiceEndpoint, config.voiceToken, body, result -> {
                String base64 = result.optString("audioBase64", result.optString("audio_base64", ""));
                if (!base64.isEmpty()) {
                    try {
                        lastVoiceRender = saveAudio(base64, result.optString("extension", "wav"));
                        status.setText("VOICE RENDER COMPLETE · " + lastVoiceRender.getName());
                        saveState("VOICE RENDER SAVED");
                    } catch (Exception e) {
                        status.setText("VOICE SAVE ERROR: " + e.getMessage());
                    }
                } else {
                    String job = result.optString("jobId", result.optString("status", "submitted"));
                    status.setText("VOICE JOB ACCEPTED · " + job);
                }
            });
        } catch (Exception e) {
            status.setText("VOICE PACKAGE ERROR: " + e.getMessage());
        }
    }

    private void sendToDaw() {
        if (lastVoiceRender == null || !lastVoiceRender.exists()) {
            status.setText("RENDER A VOCAL FIRST");
            return;
        }
        SharedPreferences prefs = getSharedPreferences(STUDIO_PREFS, MODE_PRIVATE);
        StudioProject studio = StudioProject.fromJson(prefs.getString(STUDIO_PROJECT_KEY, ""));
        studio.name = title.getText().toString().trim().isEmpty() ? "Demonic SP3 Session" : title.getText().toString().trim();
        studio.bpm = Math.max(50, Math.min(190, intValue(bpm, 104)));
        studio.voicePath = lastVoiceRender.getAbsolutePath();
        studio.voiceStartStep = 0;
        studio.volume[StudioProject.TRACK_VOICE] = 0.92f;
        studio.delay[StudioProject.TRACK_VOICE] = true;
        studio.reverb[StudioProject.TRACK_VOICE] = true;
        prefs.edit().putString(STUDIO_PROJECT_KEY, studio.toJson()).apply();
        saveState("VOCAL SENT TO DEMONIC DAW");
        Intent intent = new Intent(this, StudioActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void showConfigDialog() {
        DemonicSecureConfig.Config current = DemonicSecureConfig.load(this);
        LinearLayout form = column();
        form.setPadding(dp(18), dp(8), dp(18), dp(4));
        EditText lyricEndpoint = dialogField(form, "Lyric AI endpoint", current.lyricEndpoint);
        EditText lyricToken = dialogField(form, "Lyric endpoint token", "");
        EditText voiceEndpoint = dialogField(form, "Voice AI endpoint", current.voiceEndpoint);
        EditText voiceToken = dialogField(form, "Voice endpoint token", "");
        new AlertDialog.Builder(this)
                .setTitle("Demonic AI Service Configuration")
                .setView(form)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        validateEndpoint(lyricEndpoint.getText().toString());
                        validateEndpoint(voiceEndpoint.getText().toString());
                        String lyricKey = lyricToken.getText().toString();
                        String voiceKey = voiceToken.getText().toString();
                        DemonicSecureConfig.Config old = DemonicSecureConfig.load(this);
                        DemonicSecureConfig.save(this, new DemonicSecureConfig.Config(
                                lyricEndpoint.getText().toString(),
                                lyricKey.isEmpty() ? old.lyricToken : lyricKey,
                                voiceEndpoint.getText().toString(),
                                voiceKey.isEmpty() ? old.voiceToken : voiceKey));
                        refreshConfigStatus();
                        status.setText("AI ENDPOINTS SAVED SECURELY");
                    } catch (Exception e) {
                        status.setText("CONFIG ERROR: " + e.getMessage());
                    }
                })
                .setNeutralButton("Clear", (dialog, which) -> {
                    DemonicSecureConfig.clear(this);
                    refreshConfigStatus();
                    status.setText("AI CONFIG CLEARED");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshConfigStatus() {
        DemonicSecureConfig.Config config = DemonicSecureConfig.load(this);
        boolean lyricsReady = !config.lyricEndpoint.isEmpty();
        boolean voiceReady = !config.voiceEndpoint.isEmpty();
        configStatus.setText((lyricsReady ? "LYRICS✓" : "LYRICS○") + " · " + (voiceReady ? "VOICE✓" : "VOICE○"));
        configStatus.setTextColor(lyricsReady && voiceReady ? GREEN : GOLD);
    }

    private interface JsonCallback { void receive(JSONObject result); }

    private void postJson(String endpoint, String token, JSONObject body, JsonCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                validateEndpoint(endpoint);
                connection = (HttpURLConnection) new URL(endpoint).openConnection();
                activeConnection = connection;
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(240000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("X-Demonic-Creator-Mode", "master-first");
                if (token != null && !token.trim().isEmpty()) connection.setRequestProperty("Authorization", "Bearer " + token.trim());
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
                int code = connection.getResponseCode();
                InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
                String raw = readAll(stream);
                if (code < 200 || code >= 300) throw new IllegalStateException("HTTP " + code + " · " + compact(raw));
                JSONObject result = new JSONObject(raw);
                main.post(() -> callback.receive(result));
            } catch (Throwable t) {
                String message = t.getMessage() == null ? "REQUEST FAILED" : t.getMessage();
                main.post(() -> status.setText("AI REQUEST FAILED · " + message));
            } finally {
                if (connection != null) connection.disconnect();
                activeConnection = null;
            }
        });
    }

    private void cancelActive() {
        HttpURLConnection connection = activeConnection;
        if (connection != null) connection.disconnect();
        activeConnection = null;
        status.setText("ACTIVE REQUEST CANCELLED · MASTER PRESERVED");
    }

    private File saveAudio(String encoded, String extension) throws Exception {
        byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
        File dir = new File(getFilesDir(), "demonic_song_ai");
        if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Could not create song render directory.");
        String safeTitle = title.getText().toString().replaceAll("[^A-Za-z0-9_-]+", "_");
        if (safeTitle.isEmpty()) safeTitle = "SP3";
        File file = new File(dir, safeTitle + "_SP3_" + System.currentTimeMillis() + "." + extension.replaceAll("[^A-Za-z0-9]", ""));
        try (FileOutputStream out = new FileOutputStream(file)) { out.write(bytes); }
        return file;
    }

    private void previewLastRender() {
        if (lastVoiceRender == null || !lastVoiceRender.exists()) {
            status.setText("NO RENDERED VOCAL TO PREVIEW");
            return;
        }
        try {
            if (previewPlayer != null) { previewPlayer.release(); previewPlayer = null; }
            previewPlayer = new MediaPlayer();
            previewPlayer.setDataSource(lastVoiceRender.getAbsolutePath());
            previewPlayer.setOnPreparedListener(MediaPlayer::start);
            previewPlayer.setOnCompletionListener(mp -> status.setText("VOCAL PREVIEW COMPLETE"));
            previewPlayer.prepareAsync();
            status.setText("LOADING VOCAL PREVIEW…");
        } catch (Exception e) {
            status.setText("PREVIEW ERROR: " + e.getMessage());
        }
    }

    private void savePackageFile() {
        buildRenderPackage();
        try {
            File dir = new File(getFilesDir(), "demonic_song_ai");
            if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Could not create package directory.");
            File file = new File(dir, "render_package_" + System.currentTimeMillis() + ".json");
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(renderPackage.getText().toString().getBytes(StandardCharsets.UTF_8));
            }
            status.setText("PACKAGE SAVED · " + file.getAbsolutePath());
        } catch (Exception e) {
            status.setText("PACKAGE SAVE ERROR: " + e.getMessage());
        }
    }

    private void saveState(String message) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString("title", title.getText().toString())
                .putString("brief", brief.getText().toString())
                .putString("beatCode", beatCode.getText().toString())
                .putString("bpm", bpm.getText().toString())
                .putString("delivery", delivery.getText().toString())
                .putString("register", register.getText().toString())
                .putString("energy", energy.getText().toString())
                .putString("projection", projection.getText().toString())
                .putString("chestWeight", chestWeight.getText().toString())
                .putString("brightness", brightness.getText().toString())
                .putString("adlibDensity", adlibDensity.getText().toString())
                .putString("fxCode", fxCode.getText().toString())
                .putString("voiceProfile", voiceProfile.getText().toString())
                .putString("masterLyrics", masterLyrics.getText().toString())
                .putString("performanceMap", performanceMap.getText().toString())
                .putString("renderPackage", renderPackage.getText().toString())
                .putString("lastVoicePath", lastVoiceRender == null ? "" : lastVoiceRender.getAbsolutePath())
                .apply();
        status.setText(message);
    }

    private void restoreState() {
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        title.setText(p.getString("title", "Untitled SP3 Song"));
        brief.setText(p.getString("brief", "Funny grimy Richmond hyphy song about fake people talking too much."));
        beatCode.setText(p.getString("beatCode", "FME_SP3_B10225_104BPM_HYPHYFUNK_808BOUNCE"));
        bpm.setText(p.getString("bpm", "104"));
        delivery.setText(p.getString("delivery", "SP3_BAY_PLAYFUL_MOB_V2"));
        register.setText(p.getString("register", "18"));
        energy.setText(p.getString("energy", "50"));
        projection.setText(p.getString("projection", "40"));
        chestWeight.setText(p.getString("chestWeight", "-20"));
        brightness.setText(p.getString("brightness", "18"));
        adlibDensity.setText(p.getString("adlibDensity", "38"));
        fxCode.setText(p.getString("fxCode", "ECHO_1/4_DELAY_25%FEEDBACK_20%WET"));
        voiceProfile.setText(p.getString("voiceProfile", "SP3"));
        masterLyrics.setText(p.getString("masterLyrics", ""));
        performanceMap.setText(p.getString("performanceMap", ""));
        renderPackage.setText(p.getString("renderPackage", ""));
        String path = p.getString("lastVoicePath", "");
        if (!path.isEmpty()) {
            File file = new File(path);
            if (file.exists()) lastVoiceRender = file;
        }
        status.setText("AUTOSAVE RESTORED");
    }

    private void validateEndpoint(String endpoint) {
        String value = endpoint == null ? "" : endpoint.trim();
        if (value.isEmpty()) throw new IllegalArgumentException("Endpoint required.");
        if (!(value.startsWith("https://") || value.startsWith("http://127.0.0.1") || value.startsWith("http://localhost"))) {
            throw new IllegalArgumentException("Use HTTPS or a local loopback endpoint.");
        }
    }

    private static String readAll(InputStream stream) throws Exception {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) out.append(line).append('\n');
            return out.toString().trim();
        }
    }

    private static String compact(String value) {
        if (value == null) return "";
        String clean = value.replace('\n', ' ').replace('\r', ' ').trim();
        return clean.length() > 300 ? clean.substring(0, 300) + "…" : clean;
    }

    private String pocketFor(int barIndex) {
        switch (barIndex % 4) {
            case 0: return "delayed";
            case 1: return "loose";
            case 2: return "behind";
            default: return "elastic";
        }
    }

    private static boolean containsAdlib(String line) { return line.contains("(") && line.contains(")"); }

    private static String lastWord(String line) {
        String clean = line.replaceAll("\\([^)]*\\)", "").replaceAll("[^A-Za-z0-9'’-]+", " ").trim();
        if (clean.isEmpty()) return "";
        String[] parts = clean.split("\\s+");
        return parts[parts.length - 1];
    }

    private static int intValue(EditText field, int fallback) {
        try { return Integer.parseInt(field.getText().toString().trim()); }
        catch (Exception ignored) { return fallback; }
    }

    private void copy(String value) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Demonic Song AI", value));
        status.setText("COPIED");
    }

    private EditText field(LinearLayout parent, String label, String value, boolean multiline) {
        parent.addView(text(label, 11, MUTED, true));
        EditText input = new EditText(this);
        input.setText(value);
        input.setTextColor(WHITE);
        input.setHintTextColor(MUTED);
        input.setTextSize(13);
        input.setTypeface(Typeface.MONOSPACE);
        input.setBackgroundColor(PANEL);
        input.setPadding(dp(10), dp(7), dp(10), dp(7));
        input.setSingleLine(!multiline);
        input.setMinHeight(dp(multiline ? 86 : 46));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(3), 0, dp(8));
        parent.addView(input, params);
        return input;
    }

    private EditText dialogField(LinearLayout parent, String label, String value) {
        parent.addView(text(label, 12, Color.DKGRAY, true));
        EditText input = new EditText(this);
        input.setText(value);
        input.setSingleLine(true);
        parent.addView(input, new LinearLayout.LayoutParams(-1, dp(54)));
        return input;
    }

    private EditText largeField(String hint, int minHeight) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextColor(WHITE);
        input.setHintTextColor(MUTED);
        input.setTextSize(13);
        input.setTypeface(Typeface.MONOSPACE);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setBackgroundColor(PANEL);
        input.setPadding(dp(10), dp(9), dp(10), dp(9));
        input.setMinHeight(dp(minHeight));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(4), 0, dp(8));
        input.setLayoutParams(params);
        return input;
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(44));
        params.setMargins(dp(3), 0, dp(3), 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button actionButton(String label, int color, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(12);
        button.setTextColor(color == PANEL_2 ? WHITE : BG);
        button.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        button.setBackgroundColor(color);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(48));
        params.setMargins(0, dp(4), 0, dp(4));
        button.setLayoutParams(params);
        return button;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onPause() {
        saveState("AUTOSAVED");
        super.onPause();
    }

    @Override protected void onDestroy() {
        cancelActive();
        executor.shutdownNow();
        if (previewPlayer != null) previewPlayer.release();
        previewPlayer = null;
        super.onDestroy();
    }
}