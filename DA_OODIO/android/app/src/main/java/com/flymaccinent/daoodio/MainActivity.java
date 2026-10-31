package com.flymaccinent.daoodio;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import org.json.JSONArray;
import org.json.JSONObject;

public final class MainActivity extends Activity {
  private LinearLayout page;
  private final String[] challenges = {
    "Write four bars about waking up in the Bay.",
    "Write eight bars with internal rhymes and a character entrance.",
    "Write a hook that a whole crowd can repeat.",
    "Tell a story in sixteen lines. Show a choice and its consequence."
  };
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
    v.setText(text); v.setTextSize(size); v.setTextColor(Color.WHITE);
    v.setPadding(0, 12, 0, 18); page.addView(v);
  }
  private void action(String label, Runnable run) {
    Button b = new Button(this); b.setText(label);
    b.setOnClickListener(v -> run.run()); page.addView(b);
  }
  private SharedPreferences store() { return getSharedPreferences("daoodio_writer_v1", MODE_PRIVATE); }
  private JSONArray entries() {
    try { return new JSONArray(store().getString("entries", "[]")); }
    catch (Exception ignored) { return new JSONArray(); }
  }
  private void home() {
    page.removeAllViews();
    heading("DA OODIO UNIVERSAL", 28);
    heading("Android foundation • v0.2 alpha", 16);
    action("Writer Games • playable alpha", this::writer);
    action("Writing Library • saved locally", this::library);
    for (String module : new String[]{"DAW Studio", "Beat Lab", "Remix Lab", "Mic Arena", "Arcade"}) {
      action(module + " • pending", () -> {
        page.removeAllViews(); heading(module, 26);
        heading("This module is not integrated yet. Audio and gameplay features are not claimed.", 16);
        action("Back to Home", this::home);
      });
    }
  }
  private void writer() {
    page.removeAllViews(); heading("WRITER GAMES", 26);
    heading("Write your own bars. Your words stay yours.", 16);
    int index = store().getInt("challenge", 0) % challenges.length;
    heading("CHALLENGE " + (index + 1) + ": " + challenges[index], 17);
    EditText title = new EditText(this); title.setSingleLine(true);
    title.setHint("Title"); title.setText(store().getString("draft_title", ""));
    page.addView(title);
    EditText lyrics = new EditText(this); lyrics.setMinLines(9);
    lyrics.setGravity(android.view.Gravity.TOP);
    lyrics.setHint("Write your lyrics here...");
    lyrics.setText(store().getString("draft_lyrics", "")); page.addView(lyrics);
    action("Save draft", () -> {
      store().edit().putString("draft_title", title.getText().toString())
        .putString("draft_lyrics", lyrics.getText().toString()).apply();
      heading("Draft saved on this device.", 14);
    });
    action("Complete and save to Library", () -> {
      String body = lyrics.getText().toString().trim();
      if (body.isEmpty()) { heading("Write something before saving.", 14); return; }
      try {
        JSONObject item = new JSONObject();
        item.put("title", title.getText().toString().trim());
        item.put("lyrics", body); item.put("challenge", challenges[index]);
        item.put("savedAt", System.currentTimeMillis());
        JSONArray all = entries(); all.put(item);
        store().edit().putString("entries", all.toString())
          .remove("draft_title").remove("draft_lyrics").apply();
        library();
      } catch (Exception e) { heading("Could not save entry.", 14); }
    });
    action("Next challenge", () -> {
      store().edit().putInt("challenge", (index + 1) % challenges.length)
        .putString("draft_title", title.getText().toString())
        .putString("draft_lyrics", lyrics.getText().toString()).apply();
      writer();
    });
    action("Back to Home", this::home);
  }
  private void library() {
    page.removeAllViews(); heading("WRITING LIBRARY", 26);
    JSONArray all = entries();
    heading(all.length() + " saved writings", 16);
    for (int i = all.length() - 1; i >= 0; i--) {
      JSONObject item = all.optJSONObject(i); if (item == null) continue;
      String title = item.optString("title", "").trim();
      heading(title.isEmpty() ? "Untitled writing" : title, 20);
      heading(item.optString("lyrics"), 15);
    }
    action("New writing", this::writer);
    action("Back to Home", this::home);
  }
}
