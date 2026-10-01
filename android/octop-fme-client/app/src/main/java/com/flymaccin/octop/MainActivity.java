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

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        root.setBackgroundColor(Color.rgb(16,16,20));

        TextView title = new TextView(this);
        title.setText("OCTOP FME");
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        title.setPadding(0,16,0,8);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("FlyMaccin-Ent Intelligence Control Plane");
        sub.setTextColor(Color.LTGRAY);
        root.addView(sub);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        endpoint = new EditText(this);
        endpoint.setSingleLine(true);
        endpoint.setText("http://10.0.2.2:8088");
        endpoint.setTextColor(Color.WHITE);
        endpoint.setHintTextColor(Color.GRAY);
        bar.addView(endpoint, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Button connect = new Button(this);
        connect.setText("CONNECT");
        connect.setOnClickListener(v -> openEndpoint());
        bar.addView(connect);
        root.addView(bar);

        TextView bridge = new TextView(this);
        bridge.setText("FME Bridge • Agents • Memory • Knowledge • Plugins • Automation");
        bridge.setTextColor(Color.rgb(139,233,253));
        bridge.setPadding(0,8,0,8);
        root.addView(bridge);

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient());
        root.addView(web, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        setContentView(root);
    }

    private void openEndpoint() {
        String url = endpoint.getText().toString().trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
        web.loadUrl(url);
    }

    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
