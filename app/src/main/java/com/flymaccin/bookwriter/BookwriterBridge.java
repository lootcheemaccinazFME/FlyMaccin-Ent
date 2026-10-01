package com.flymaccin.bookwriter;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookwriterBridge {
    private final WebView webView;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile HttpURLConnection active;

    BookwriterBridge(Context context, WebView webView) {
        this.context = context.getApplicationContext();
        this.webView = webView;
    }

    @JavascriptInterface
    public void setOpenAIKey(String key) {
        try {
            SecureKeyStore.save(context, key == null ? "" : key.trim());
            callback("settings", true, "OpenAI key saved securely on this device.");
        } catch (Exception e) {
            callback("settings", false, e.getMessage() == null ? "Could not save OpenAI key." : e.getMessage());
        }
    }

    @JavascriptInterface
    public boolean hasOpenAIKey() {
        try {
            String key = SecureKeyStore.load(context);
            return key != null && !key.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @JavascriptInterface
    public void clearOpenAIKey() {
        SecureKeyStore.clear(context);
    }

    @JavascriptInterface
    public void saveVoiceConfig(String id, String endpoint, String token) {
        try {
            String clean = endpoint == null ? "" : endpoint.trim();
            if (!(clean.startsWith("https://") || clean.startsWith("http://127.0.0.1") || clean.startsWith("http://localhost"))) {
                throw new IllegalArgumentException("Voice endpoint must use HTTPS, localhost, or 127.0.0.1.");
            }
            SecureVoiceStore.save(context, clean, token == null ? "" : token);
            callback(id, true, "Voice configuration saved.");
        } catch (Throwable t) {
            callback(id, false, t.getMessage() == null ? "Could not save voice configuration." : t.getMessage());
        }
    }

    @JavascriptInterface
    public boolean hasVoiceConfig() {
        return SecureVoiceStore.has(context);
    }

    @JavascriptInterface
    public void clearVoiceConfig(String id) {
        SecureVoiceStore.clear(context);
        callback(id, true, "Voice configuration cleared.");
    }

    @JavascriptInterface
    public void renderAuthorizedVoice(String id, String renderPackageJson) {
        executor.execute(() -> {
            try {
                String[] cfg = SecureVoiceStore.load(context);
                if (cfg == null || cfg.length < 1 || cfg[0] == null || cfg[0].trim().isEmpty()) {
                    throw new IllegalStateException("Configure the authorized voice endpoint first.");
                }
                JSONObject request = new JSONObject(renderPackageJson == null ? "{}" : renderPackageJson);
                JSONObject voice = request.optJSONObject("voice");
                if (voice == null || !voice.optBoolean("authorizedByCreator", false)) {
                    throw new IllegalArgumentException("Voice render refused: creator authorization flag is required.");
                }
                String profile = voice.optString("profileId", "").trim();
                if (profile.isEmpty()) throw new IllegalArgumentException("Voice profile ID is required.");

                HttpURLConnection connection = (HttpURLConnection) new URL(cfg[0]).openConnection();
                active = connection;
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(240000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                if (cfg.length > 1 && cfg[1] != null && !cfg[1].trim().isEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer " + cfg[1].trim());
                }
                connection.setRequestProperty("X-Demonic-Voice-Profile", profile);
                connection.setRequestProperty("X-Creator-Authorization", "true");

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(request.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = connection.getResponseCode();
                InputStream in = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
                String raw = readAll(in).trim();
                if (code < 200 || code >= 300) {
                    throw new IllegalStateException("Voice endpoint failed (" + code + "): " + compact(raw));
                }
                if (raw.isEmpty()) throw new IllegalStateException("Voice endpoint returned an empty response.");
                new JSONObject(raw); // validate contract; response may contain audioBase64 or jobId/status.
                callback(id, true, raw);
            } catch (Throwable t) {
                callback(id, false, t.getMessage() == null ? "Voice render failed" : t.getMessage());
            } finally {
                HttpURLConnection connection = active;
                if (connection != null) connection.disconnect();
                active = null;
            }
        });
    }

    @JavascriptInterface
    public void cancelGeneration() {
        HttpURLConnection connection = active;
        if (connection != null) connection.disconnect();
        active = null;
    }

    @JavascriptInterface
    public void generate(String id, String prompt, String model) {
        executor.execute(() -> {
            try {
                String key = SecureKeyStore.load(context);
                if (key == null || key.trim().isEmpty()) {
                    throw new IllegalStateException("Configure your OpenAI API key in Settings first.");
                }

                String chosen = (model == null || model.trim().isEmpty())
                        ? "gpt-5.6-terra"
                        : model.trim();

                HttpURLConnection connection = (HttpURLConnection)
                        new URL("https://api.openai.com/v1/responses").openConnection();
                active = connection;
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(180000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + key);

                JSONObject body = new JSONObject()
                        .put("model", chosen)
                        .put("input", prompt == null ? "" : prompt)
                        .put("store", false);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = connection.getResponseCode();
                InputStream in = code >= 200 && code < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();
                String raw = readAll(in);
                if (code < 200 || code >= 300) {
                    throw new IllegalStateException(error(code, raw));
                }

                String text = extract(new JSONObject(raw));
                if (text.isEmpty()) {
                    throw new IllegalStateException("OpenAI returned no text output.");
                }
                callback(id, true, text);
            } catch (Throwable t) {
                callback(id, false, t.getMessage() == null ? "Generation failed" : t.getMessage());
            } finally {
                HttpURLConnection connection = active;
                if (connection != null) connection.disconnect();
                active = null;
            }
        });
    }

    private static String extract(JSONObject json) {
        StringBuilder out = new StringBuilder();
        JSONArray output = json.optJSONArray("output");
        if (output == null) return "";
        for (int i = 0; i < output.length(); i++) {
            JSONObject item = output.optJSONObject(i);
            if (item == null) continue;
            JSONArray content = item.optJSONArray("content");
            if (content == null) continue;
            for (int k = 0; k < content.length(); k++) {
                JSONObject part = content.optJSONObject(k);
                if (part != null && "output_text".equals(part.optString("type"))) {
                    String text = part.optString("text", "");
                    if (!text.isEmpty()) {
                        if (out.length() > 0) out.append("\n\n");
                        out.append(text);
                    }
                }
            }
        }
        return out.toString().trim();
    }

    private static String readAll(InputStream in) throws IOException {
        if (in == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) out.append(line).append('\n');
            return out.toString();
        }
    }

    private static String compact(String raw) {
        if (raw == null) return "";
        String c = raw.replace('\n', ' ').replace('\r', ' ').trim();
        return c.length() > 400 ? c.substring(0, 400) + "…" : c;
    }

    private static String error(int code, String raw) {
        try {
            JSONObject e = new JSONObject(raw).optJSONObject("error");
            if (e != null && !e.optString("message", "").isEmpty()) {
                return "OpenAI request failed (" + code + "): " + e.optString("message");
            }
        } catch (Exception ignored) {}
        if (code == 401) return "OpenAI authentication failed (401). Check the API key.";
        if (code == 403) return "OpenAI access refused (403). Check project/model permissions.";
        if (code == 429) return "OpenAI rate or quota limit reached (429). Your draft is preserved.";
        if (code >= 500) return "OpenAI service error (" + code + "). Your draft is preserved.";
        return "OpenAI request failed (" + code + ").";
    }

    private void callback(String id, boolean ok, String payload) {
        String js = "window.__fmeAndroidResult&&window.__fmeAndroidResult("
                + JSONObject.quote(id) + "," + ok + "," + JSONObject.quote(payload) + ");";
        webView.post(() -> webView.evaluateJavascript(js, null));
    }
}
