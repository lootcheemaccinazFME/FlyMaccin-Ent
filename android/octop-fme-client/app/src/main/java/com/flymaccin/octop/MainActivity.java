package com.flymaccin.octop;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;

public final class MainActivity extends Activity {
    private WebView web;
    private EditText endpoint;
    private LinearLayout home;
    private FrameLayout content;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        showHome();
    }

    private TextView text(String value, float size, int color) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setPadding(12, 12, 12, 12);
        return v;
    }

    private Button module(String label, String route) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setOnClickListener(v -> openRoute(route));
        return b;
    }

    private void showHome() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        root.setBackgroundColor(Color.rgb(16,16,20));

        TextView title = text("FLYMACCIN", 30, Color.WHITE);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title);

        TextView sub = text("ONE APP • ONE APK • FME CONTROL CENTER", 13, Color.LTGRAY);
        sub.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(sub);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        endpoint = new EditText(this);
        endpoint.setSingleLine(true);
        endpoint.setText("http://10.0.2.2:8088");
        endpoint.setTextColor(Color.WHITE);
        endpoint.setHintTextColor(Color.GRAY);
        endpoint.setHint("FME / Octop endpoint");
        bar.addView(endpoint, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Button connect = new Button(this);
        connect.setText("CONNECT");
        connect.setOnClickListener(v -> openRoute(""));
        bar.addView(connect);
        root.addView(bar);

        ScrollView scroll = new ScrollView(this);
        home = new LinearLayout(this);
        home.setOrientation(LinearLayout.VERTICAL);

        home.addView(text("FME MODULES", 16, Color.rgb(139,233,253)));
        home.addView(module("🎛 Studio / DAW", "/studio"));
        home.addView(module("🧠 AI / Octop", "/"));
        home.addView(module("📚 Library", "/library"));
        home.addView(module("🌐 Browser + Downloader", "/browser"));
        home.addView(module("📺 Media / Mini TV", "/media"));
        home.addView(module("🧩 Plugins / Connectors", "/plugins"));
        home.addView(module("⚙ Automation", "/automation"));
        home.addView(module("💾 Memory + Knowledge", "/memory"));
        home.addView(module("🎹 PocketBand Engine", "/pocketband"));

        TextView bridge = text("FME Bridge • Agents • Memory • Knowledge • Plugins • Automation • Native Audio", 12, Color.LTGRAY);
        home.addView(bridge);

        scroll.addView(home);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);
    }

    private void openRoute(String route) {
        String base = endpoint == null ? "" : endpoint.getText().toString().trim();
        if (base.isEmpty()) base = "http://10.0.2.2:8088";
        if (!base.startsWith("http://") && !base.startsWith("https://")) base = "http://" + base;
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(Color.rgb(16,16,20));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        Button back = new Button(this);
        back.setText("FME HOME");
        back.setOnClickListener(v -> showHome());
        nav.addView(back);
        TextView routeName = text(route.isEmpty() ? "AI / OCTOP" : route.toUpperCase(), 14, Color.WHITE);
        nav.addView(routeName, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        shell.addView(nav);

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClient());
        shell.addView(web, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(shell);
        web.loadUrl(base + route);
    }

    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else showHome();
    }
}
