from pathlib import Path
import re
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else 'demonic-ai-studio-hut')
main = root / 'app/src/main/java/com/flymaccin/bookwriter/MainActivity.java'
service = root / 'app/src/main/java/com/flymaccin/bookwriter/BackgroundPlaybackService.java'
manifest = root / 'app/src/main/AndroidManifest.xml'
gradle = root / 'app/build.gradle.kts'

main.write_text(r'''package com.flymaccin.bookwriter;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.app.ShortcutInfo;
import android.app.ShortcutManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TRUSTED_URL = "file:///android_asset/index.html";
    private static final String PREFS = "demonic_multitask";
    private static final String PREF_AUTO_PIP = "auto_pip";
    private static final String PREF_BACKGROUND = "background_mode";

    private WebView webView;
    private Button multitaskButton;
    private SharedPreferences prefs;
    private boolean autoPipEnabled;
    private boolean backgroundEnabled;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        autoPipEnabled = prefs.getBoolean(PREF_AUTO_PIP, true);
        backgroundEnabled = prefs.getBoolean(PREF_BACKGROUND, false);

        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        multitaskButton = new Button(this);
        multitaskButton.setText("⋮");
        multitaskButton.setContentDescription("Home, split screen, background, and picture in picture options");
        multitaskButton.setTextSize(22f);
        multitaskButton.setMinWidth(dp(54));
        multitaskButton.setMinHeight(dp(48));
        multitaskButton.setAlpha(0.90f);
        FrameLayout.LayoutParams menuLp = new FrameLayout.LayoutParams(dp(58), dp(52), Gravity.TOP | Gravity.END);
        menuLp.setMargins(dp(8), dp(8), dp(8), dp(8));
        root.addView(multitaskButton, menuLp);
        setContentView(root);

        WebView.setWebContentsDebuggingEnabled(false);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(false);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setSupportMultipleWindows(false);
        s.setSavePassword(false);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            private boolean trusted(Uri u) {
                return u != null
                        && "file".equalsIgnoreCase(u.getScheme())
                        && "/android_asset/index.html".equals(u.getPath());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !trusted(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return !TRUSTED_URL.equals(url);
            }
        });

        webView.addJavascriptInterface(new BookwriterBridge(this, webView), "AndroidBridge");
        webView.loadUrl(TRUSTED_URL);
        multitaskButton.setOnClickListener(this::showMultitaskMenu);

        if (backgroundEnabled) startBackgroundMode();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void showMultitaskMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, "Add to Home Screen");
        menu.getMenu().add(0, 2, 1, isInMultiWindowModeCompat() ? "Open Another App Beside This" : "Split Screen");
        menu.getMenu().add(0, 3, 2, "Picture in Picture Now");
        menu.getMenu().add(0, 4, 3, autoPipEnabled ? "Auto PiP When Leaving: ON" : "Auto PiP When Leaving: OFF");
        menu.getMenu().add(0, 5, 4, backgroundEnabled ? "Background Mode: ON" : "Background Mode: OFF");
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: requestHomeShortcut(); return true;
                case 2: openSplitScreenOption(); return true;
                case 3: enterPipNow(); return true;
                case 4: toggleAutoPip(); return true;
                case 5: toggleBackgroundMode(); return true;
                default: return false;
            }
        });
        menu.show();
    }

    private boolean isInMultiWindowModeCompat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode();
    }

    private void requestHomeShortcut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager manager = getSystemService(ShortcutManager.class);
            if (manager != null && manager.isRequestPinShortcutSupported()) {
                Intent launch = new Intent(this, MainActivity.class)
                        .setAction(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER);
                ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "demonic-home")
                        .setShortLabel("Demonic Studio")
                        .setLongLabel("Demonic AI Studio Hut")
                        .setIcon(Icon.createWithResource(this, android.R.drawable.sym_def_app_icon))
                        .setIntent(launch)
                        .build();
                manager.requestPinShortcut(shortcut, null);
                Toast.makeText(this, "Home-screen shortcut request sent.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Toast.makeText(this, "Your launcher does not support pinned shortcuts. The installed app icon can still be added from the app drawer.", Toast.LENGTH_LONG).show();
    }

    private void openSplitScreenOption() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(this, "Split screen requires Android 7 or newer.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isInMultiWindowModeCompat()) {
            Toast.makeText(this, "Split screen is enabled for Demonic Studio. Open Android Recents, tap the app icon, then choose Split screen.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            Intent chooser = Intent.createChooser(launcher, "Choose app for the other side");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(chooser);
        } catch (Throwable t) {
            Toast.makeText(this, "Use the other split-screen pane to pick the second app.", Toast.LENGTH_LONG).show();
        }
    }

    private void enterPipNow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(this, "Picture in Picture requires Android 8 or newer.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            PictureInPictureParams params = new PictureInPictureParams.Builder()
                    .setAspectRatio(new Rational(16, 9))
                    .build();
            enterPictureInPictureMode(params);
        } catch (Throwable t) {
            Toast.makeText(this, "Picture in Picture is disabled by this device or system setting.", Toast.LENGTH_LONG).show();
        }
    }

    private void toggleAutoPip() {
        autoPipEnabled = !autoPipEnabled;
        prefs.edit().putBoolean(PREF_AUTO_PIP, autoPipEnabled).apply();
        Toast.makeText(this, "Auto PiP " + (autoPipEnabled ? "enabled" : "disabled") + ".", Toast.LENGTH_SHORT).show();
    }

    private void toggleBackgroundMode() {
        backgroundEnabled = !backgroundEnabled;
        prefs.edit().putBoolean(PREF_BACKGROUND, backgroundEnabled).apply();
        if (backgroundEnabled) startBackgroundMode(); else stopBackgroundMode();
        Toast.makeText(this, "Background mode " + (backgroundEnabled ? "enabled" : "disabled") + ".", Toast.LENGTH_SHORT).show();
    }

    private void startBackgroundMode() {
        Intent i = new Intent(this, BackgroundPlaybackService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i); else startService(i);
    }

    private void stopBackgroundMode() {
        stopService(new Intent(this, BackgroundPlaybackService.class));
    }

    @Override protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (autoPipEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isInPictureInPictureMode()) {
            enterPipNow();
        }
    }

    @Override public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, android.content.res.Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (multitaskButton != null) multitaskButton.setVisibility(isInPictureInPictureMode ? View.GONE : View.VISIBLE);
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("AndroidBridge");
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
''', encoding='utf-8')

service.write_text(r'''package com.flymaccin.bookwriter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class BackgroundPlaybackService extends Service {
    private static final String CHANNEL_ID = "demonic_background";
    private static final int NOTIFICATION_ID = 2401;

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        Intent open = new Intent(this, MainActivity.class)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pending = PendingIntent.getActivity(this, 0, open, flags);
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        Notification notification = builder
                .setContentTitle("Demonic AI Studio Hut")
                .setContentText("Background mode is active")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pending)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Demonic background mode",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Keeps Demonic AI Studio Hut available while you use other apps.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }
}
''', encoding='utf-8')

m = manifest.read_text(encoding='utf-8')
if 'android.permission.FOREGROUND_SERVICE' not in m:
    m = m.replace('<uses-permission android:name="android.permission.INTERNET" />',
                  '<uses-permission android:name="android.permission.INTERNET" />\n    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />\n    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />\n    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />')
if 'BackgroundPlaybackService' not in m:
    m = m.replace('<activity android:name=".BrowserActivity" android:exported="false" />',
                  '<service android:name=".BackgroundPlaybackService" android:exported="false" android:foregroundServiceType="mediaPlayback" />\n        <activity android:name=".BrowserActivity" android:exported="false" />')
m = m.replace('android:configChanges="orientation|screenSize|keyboardHidden"',
              'android:configChanges="orientation|screenSize|smallestScreenSize|screenLayout|keyboardHidden|density|uiMode"\n            android:resizeableActivity="true"\n            android:supportsPictureInPicture="true"')
manifest.write_text(m, encoding='utf-8')

g = gradle.read_text(encoding='utf-8')
g = re.sub(r'versionName\s*=\s*"2\.1\.0"', 'versionName = "2.1.1"', g)
vm = re.search(r'versionCode\s*=\s*(\d+)', g)
if vm:
    old = int(vm.group(1))
    g = g[:vm.start(1)] + str(max(old + 1, 211)) + g[vm.end(1):]
gradle.write_text(g, encoding='utf-8')

print('Patched MainActivity, BackgroundPlaybackService, AndroidManifest, and version metadata.')
