package com.flymaccin.lootcheerom;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class DreyvenActivity extends Activity {
    private LinearLayout list;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(13, 14, 25));
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(26, 32, 26, 32);
        scroll.addView(list);
        title("DREYVEN", 34, 0xfff2ca65);
        title("FME UNIVERSAL APP HUB", 18, Color.WHITE);
        title("1 APP  •  0 LAUNCHERS  •  1 APK", 14, 0xffbbbbbb);
        room("Universal Emulator", true, "Launch the built-in emulator engine.");
        room("Music & DAW Studio", true, "Built-in offline 16-step beat lab.");
        room("Demonic AI Studio", false, "AI engine integration is in progress.");
        room("LOOTCHEE OS", false, "Workflow engine integration is in progress.");
        room("FME Games", false, "Game engines are not yet certified.");
        room("Gummie Bear Academy", true, "Offline fourth-grade math lab.");
        setContentView(scroll);
    }
    private void title(String s, int sp, int color) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(sp); v.setTextColor(color);
        v.setPadding(6, 12, 6, 12); v.setGravity(Gravity.CENTER_HORIZONTAL);
        list.addView(v);
    }
    private void room(String name, boolean ready, String message) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(name + (ready ? "   •   OPEN" : "   •   COMING SOON"));
        list.addView(b);
        b.setOnClickListener(v -> {
            if (ready) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                new android.app.AlertDialog.Builder(this)
                    .setTitle(name + " • Coming Soon")
                    .setMessage(message)
                    .setPositiveButton("OK", null).show();
            }
        });
    }
}
