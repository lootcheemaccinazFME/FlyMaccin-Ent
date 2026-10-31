package com.flymaccinent.daoodio;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ScrollView;

public final class MainActivity extends Activity {
  private LinearLayout page;
  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    ScrollView scroll = new ScrollView(this);
    page = new LinearLayout(this);
    page.setOrientation(LinearLayout.VERTICAL);
    page.setPadding(28, 48, 28, 28);
    page.setBackgroundColor(Color.rgb(13, 11, 22));
    scroll.addView(page);
    setContentView(scroll);
    home();
  }
  private void heading(String text, int size) {
    TextView v = new TextView(this);
    v.setText(text);
    v.setTextSize(size);
    v.setTextColor(Color.WHITE);
    v.setPadding(0, 12, 0, 18);
    page.addView(v);
  }
  private void action(String label, Runnable run) {
    Button b = new Button(this);
    b.setText(label);
    b.setOnClickListener(v -> run.run());
    page.addView(b);
  }
  private void home() {
    page.removeAllViews();
    heading("DA OODIO UNIVERSAL", 28);
    heading("Android foundation • v0.1 alpha", 16);
    heading("Module registry (integration pending)", 18);
    for (String module : new String[]{"DAW Studio", "Writer Games", "Beat Lab", "Remix Lab", "Mic Arena", "Arcade", "Library"}) {
      action(module + " • pending", () -> {
        page.removeAllViews();
        heading(module, 26);
        heading("This module is not integrated yet. No recording, audio or game functionality is claimed.", 16);
        action("Back to Home", this::home);
      });
    }
  }
}
