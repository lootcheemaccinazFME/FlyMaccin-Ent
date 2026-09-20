package com.flymaccin.demonictv;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public final class TvActivity extends Activity implements Ps5Receiver.Listener {
    private FrameLayout root, stage;
    private TextView status;
    private SurfaceView video;
    private Ps5Receiver receiver;
    private DisplayMode mode = DisplayMode.DOCKED;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        receiver = new Ps5Receiver(this);
        receiver.setListener(this);
        buildUi();
    }

    private TextView text(String s, int sp) {
        TextView v = new TextView(this); v.setText(s); v.setTextColor(Color.WHITE); v.setTextSize(sp);
        v.setPadding(18,12,18,12); return v;
    }
    private Button button(String s, View.OnClickListener l) {
        Button b = new Button(this); b.setText(s); b.setOnClickListener(l); return b;
    }
    private void buildUi() {
        root = new FrameLayout(this); root.setBackgroundColor(Color.rgb(8,5,13));
        LinearLayout shell = new LinearLayout(this); shell.setOrientation(LinearLayout.VERTICAL);
        root.addView(shell, new FrameLayout.LayoutParams(-1,-1));

        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(text("DEMONIC TV",22));
        for (TvSource s : TvSource.values()) top.addView(button(s.name().replace('_',' '), v -> selectSource(s)));
        shell.addView(top, new LinearLayout.LayoutParams(-1,-2));

        stage = new FrameLayout(this); stage.setBackgroundColor(Color.BLACK);
        video = new SurfaceView(this);
        stage.addView(video, new FrameLayout.LayoutParams(-1,-1));
        TextView watermark = text("PS5 RECEIVER SURFACE",16); watermark.setGravity(Gravity.CENTER);
        stage.addView(watermark,new FrameLayout.LayoutParams(-1,-1));
        shell.addView(stage,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout controls = new LinearLayout(this); controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(button("DISCOVER",v->receiver.discover()));
        controls.addView(button("PAIR",v->receiver.beginPairing()));
        controls.addView(button("CONNECT",v->receiver.connect()));
        controls.addView(button("DISCONNECT",v->receiver.disconnect()));
        controls.addView(button("DOCK",v->setMode(DisplayMode.DOCKED)));
        controls.addView(button("FULL",v->setMode(DisplayMode.FULLSCREEN)));
        controls.addView(button("PiP",v->setMode(DisplayMode.PIP)));
        controls.addView(button("INPUT LOCK",v->receiver.setControllerLocked(!receiver.isControllerLocked())));
        controls.addView(button("BG AUDIO",v->receiver.setBackgroundAudio(!receiver.isBackgroundAudio())));
        SeekBar volume = new SeekBar(this); volume.setMax(100); volume.setProgress(75);
        controls.addView(volume,new LinearLayout.LayoutParams(220,-2));
        shell.addView(controls,new LinearLayout.LayoutParams(-1,-2));

        status = text("PS5: idle • capture into DAW: OFF",14);
        shell.addView(status,new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);

        video.getHolder().addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder h) { receiver.attachSurface(h.getSurface()); }
            public void surfaceChanged(SurfaceHolder h,int f,int w,int he) { receiver.attachSurface(h.getSurface()); }
            public void surfaceDestroyed(SurfaceHolder h) { receiver.detachSurface(); }
        });
    }
    private void selectSource(TvSource source) {
        status.setText("Source: "+source.name().replace('_',' ')+" • PS5 session preserved");
    }
    private void setMode(DisplayMode m) {
        mode=m;
        ViewGroup.LayoutParams p=stage.getLayoutParams();
        if (m==DisplayMode.PIP) {
            p.width=(int)(getResources().getDisplayMetrics().widthPixels*.38f);
            p.height=(int)(getResources().getDisplayMetrics().heightPixels*.38f);
        } else { p.width=-1; p.height=(m==DisplayMode.FULLSCREEN)?-1:0; }
        stage.setLayoutParams(p);
        status.setText("Display: "+m+" • PS5 receiver remains resident");
    }
    @Override public boolean dispatchKeyEvent(KeyEvent e) {
        if (receiver.sendControllerEvent(e)) return true;
        return super.dispatchKeyEvent(e);
    }
    @Override public void onState(Ps5Receiver.State s,String detail) {
        runOnUiThread(()->status.setText("PS5: "+s+" • "+detail+" • DAW capture OFF"));
    }
}
