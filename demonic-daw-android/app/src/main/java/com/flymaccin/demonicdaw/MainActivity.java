package com.flymaccin.demonicdaw;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String DAW_URL = "https://demonic-daw-lootchee.lootcheemaccinaz.chatgpt.site";
    private static final int FILE_CHOOSER_REQUEST = 7001;
    private static final int AUDIO_PERMISSION_REQUEST = 7002;
    private static final int STORAGE_PERMISSION_REQUEST = 7003;

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private FrameLayout root;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private String pendingDownloadUrl;
    private String pendingDownloadUserAgent;
    private String pendingDownloadDisposition;
    private String pendingDownloadMime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        addInstrumentButton();
        configureWebView();

        if (savedInstanceState == null) {
            webView.loadUrl(DAW_URL);
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private void addInstrumentButton() {
        Button instruments = new Button(this);
        instruments.setText("GET INSTRUMENTS");
        instruments.setTextSize(11f);
        instruments.setAllCaps(false);
        instruments.setTextColor(Color.WHITE);
        instruments.setBackgroundColor(Color.rgb(84, 0, 120));
        instruments.setPadding(18, 0, 18, 0);
        instruments.setOnClickListener(v -> showInstrumentHub());

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                52);
        lp.gravity = Gravity.TOP | Gravity.END;
        lp.setMargins(12, 12, 12, 0);
        root.addView(instruments, lp);
    }

    private void showInstrumentHub() {
        String html = "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<style>body{background:#08060c;color:#f7f2ff;font-family:sans-serif;padding:18px}"
                + "h1{color:#d873ff;margin-top:52px}h2{color:#ffcf62}"
                + ".card{display:block;background:#17111f;border:1px solid #4f2c66;border-radius:14px;padding:16px;margin:12px 0;color:white;text-decoration:none}"
                + ".tag{color:#bfa6ca;font-size:13px}.warn{background:#2a1a08;padding:12px;border-radius:12px;color:#ffd88a}"
                + "button{padding:12px 16px;border:0;border-radius:10px;background:#68288c;color:white}</style></head><body>"
                + "<h1>Demonic Internet Instruments</h1>"
                + "<p>Browse legal free/open instrument libraries and sample sources. Downloads go to <b>Downloads/DemonicDAW</b>.</p>"
                + "<div class='warn'>Always check each library's license before commercial release. Free download does not automatically mean royalty-free.</div>"
                + "<h2>Open / free instrument directories</h2>"
                + "<a class='card' href='https://sfzinstruments.github.io/'><b>SFZ Instruments Directory</b><br><span class='tag'>Pianos, drums, bass, guitars, synths, strings, orchestra and more. Licenses vary by library.</span></a>"
                + "<a class='card' href='https://sfzinstruments.github.io/synthesizers/'><b>Free Synth Instruments</b><br><span class='tag'>Includes CC0 and MIT-licensed synth libraries.</span></a>"
                + "<a class='card' href='https://sfzinstruments.github.io/percussion/'><b>Percussion & Drums</b><br><span class='tag'>Free and open percussion libraries, including CC0/CC-BY options.</span></a>"
                + "<a class='card' href='https://sfzinstruments.github.io/pianos/'><b>Pianos & Keys</b><br><span class='tag'>Free and commercial piano libraries are listed; verify license before use.</span></a>"
                + "<a class='card' href='https://sfzinstruments.github.io/orchestra/'><b>Orchestra</b><br><span class='tag'>Free orchestral libraries such as Sonatina and community editions.</span></a>"
                + "<a class='card' href='https://freesound.org/'><b>Freesound</b><br><span class='tag'>Creative Commons samples and one-shots. Account/login may be required for downloads.</span></a>"
                + "<h2>Supported imports</h2><p>WAV, MP3, OGG, FLAC, SFZ, SF2 and ZIP packs can be downloaded. WAV/MP3/OGG/FLAC can feed the current sample-vault workflow directly. SFZ/SF2 playback requires a sampler engine in Demonic; until that engine is added, those files are stored for future instrument loading.</p>"
                + "<p><a class='card' href='demonic://daw'><b>← Back to Demonic DAW</b></a></p>"
                + "</body></html>";
        webView.loadDataWithBaseURL("https://demonic.local/", html, "text/html", "UTF-8", null);
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url == null) return false;
                if (url.equals("demonic://daw")) {
                    view.loadUrl(DAW_URL);
                    return true;
                }
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url);
                    return true;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception ignored) {}
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallbackNew,
                                             FileChooserParams fileChooserParams) {
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = filePathCallbackNew;

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                        "audio/*", "video/*", "application/octet-stream", "application/zip"
                });

                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                } catch (ActivityNotFoundException e) {
                    filePathCallback = null;
                    Toast.makeText(MainActivity.this, "No file picker available", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    if (request == null) return;
                    boolean wantsAudio = false;
                    for (String resource : request.getResources()) {
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) {
                            wantsAudio = true;
                            break;
                        }
                    }

                    if (wantsAudio && checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_REQUEST);
                        request.deny();
                        return;
                    }
                    request.grant(request.getResources());
                });
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                webView.setVisibility(View.GONE);
                root.addView(customView, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            @Override
            public void onHideCustomView() {
                hideCustomView();
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (url == null || !(url.startsWith("https://") || url.startsWith("http://"))) {
                Toast.makeText(MainActivity.this, "Unsupported download link", Toast.LENGTH_SHORT).show();
                return;
            }

            pendingDownloadUrl = url;
            pendingDownloadUserAgent = userAgent;
            pendingDownloadDisposition = contentDisposition;
            pendingDownloadMime = mimetype;

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
            } else {
                startPendingDownload();
            }
        });
    }

    private void startPendingDownload() {
        if (pendingDownloadUrl == null) return;
        try {
            String filename = URLUtil.guessFileName(
                    pendingDownloadUrl,
                    pendingDownloadDisposition,
                    pendingDownloadMime);
            filename = filename.replaceAll("[^a-zA-Z0-9._() -]", "_");

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pendingDownloadUrl));
            request.setTitle(filename);
            request.setDescription("Demonic DAW instrument/sample download");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);
            if (pendingDownloadMime != null && !pendingDownloadMime.isEmpty()) {
                request.setMimeType(pendingDownloadMime);
            }
            if (pendingDownloadUserAgent != null && !pendingDownloadUserAgent.isEmpty()) {
                request.addRequestHeader("User-Agent", pendingDownloadUserAgent);
            }
            String cookies = CookieManager.getInstance().getCookie(pendingDownloadUrl);
            if (cookies != null && !cookies.isEmpty()) {
                request.addRequestHeader("Cookie", cookies);
            }
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "DemonicDAW/" + filename);

            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            manager.enqueue(request);
            Toast.makeText(this, "Downloading to Downloads/DemonicDAW", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            pendingDownloadUrl = null;
            pendingDownloadUserAgent = null;
            pendingDownloadDisposition = null;
            pendingDownloadMime = null;
        }
    }

    private void hideCustomView() {
        if (customView == null) return;
        root.removeView(customView);
        customView = null;
        webView.setVisibility(View.VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
            customViewCallback = null;
        }
    }

    @Override
    protected void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPendingDownload();
            } else {
                Toast.makeText(this, "Storage permission is required for downloads on this Android version", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_CHOOSER_REQUEST || filePathCallback == null) return;

        Uri[] results = null;
        if (resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                results = new Uri[count];
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    results[i] = uri;
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                }
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                results = new Uri[]{uri};
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}
            }
        }

        filePathCallback.onReceiveValue(results);
        filePathCallback = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            hideCustomView();
            return;
        }
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
