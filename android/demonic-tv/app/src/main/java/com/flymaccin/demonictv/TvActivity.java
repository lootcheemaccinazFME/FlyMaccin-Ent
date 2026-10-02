package com.flymaccin.demonictv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
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
    private static final int REQUEST_IMPORT_MEDIA = 4101;

    private FrameLayout root, stage;
    private TextView status, watermark;
    private SurfaceView video;
    private Ps5Receiver receiver;
    private MediaPlayer importedPlayer;
    private Uri importedUri;
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
        top.addView(button("IMPORT FILE", v -> importFile()));
        for (TvSource s : TvSource.values()) top.addView(button(s.name().replace('_',' '), v -> selectSource(s)));
        shell.addView(top, new LinearLayout.LayoutParams(-1,-2));

        stage = new FrameLayout(this); stage.setBackgroundColor(Color.BLACK);
        video = new SurfaceView(this);
        stage.addView(video, new FrameLayout.LayoutParams(-1,-1));
        watermark = text("PS5 RECEIVER SURFACE",16); watermark.setGravity(Gravity.CENTER);
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
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (importedPlayer != null) {
                    float level = progress / 100f;
                    importedPlayer.setVolume(level, level);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        controls.addView(volume,new LinearLayout.LayoutParams(220,-2));
        shell.addView(controls,new LinearLayout.LayoutParams(-1,-2));

        status = text("PS5: idle • import ready • capture into DAW: OFF",14);
        shell.addView(status,new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);

        video.getHolder().addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder h) {
                receiver.attachSurface(h.getSurface());
                if (importedPlayer != null) importedPlayer.setDisplay(h);
            }
            public void surfaceChanged(SurfaceHolder h,int f,int w,int he) {
                receiver.attachSurface(h.getSurface());
                if (importedPlayer != null) importedPlayer.setDisplay(h);
            }
            public void surfaceDestroyed(SurfaceHolder h) {
                receiver.detachSurface();
                if (importedPlayer != null) importedPlayer.setDisplay(null);
            }
        });
    }

    private void importFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*", "audio/*"});
        startActivityForResult(intent, REQUEST_IMPORT_MEDIA);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_IMPORT_MEDIA || resultCode != RESULT_OK || data == null || data.getData() == null) return;

        Uri uri = data.getData();
        int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            getContentResolver().takePersistableUriPermission(uri, flags & Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // The current read grant remains valid for this activity session.
        }

        importedUri = uri;
        playImportedFile(uri);
    }

    private void playImportedFile(Uri uri) {
        releaseImportedPlayer();
        receiver.detachSurface();

        importedPlayer = new MediaPlayer();
        try {
            importedPlayer.setDataSource(this, uri);
            importedPlayer.setDisplay(video.getHolder());
            importedPlayer.setOnPreparedListener(player -> {
                watermark.setVisibility(View.GONE);
                player.start();
                status.setText("Imported: " + displayName(uri) + " • playing in Demonic TV");
            });
            importedPlayer.setOnCompletionListener(player ->
                status.setText("Imported: " + displayName(uri) + " • playback complete"));
            importedPlayer.setOnErrorListener((player, what, extra) -> {
                status.setText("Import playback error • code " + what);
                return true;
            });
            status.setText("Importing: " + displayName(uri) + "…");
            importedPlayer.prepareAsync();
        } catch (Exception e) {
            status.setText("Import failed: " + e.getClass().getSimpleName());
            releaseImportedPlayer();
            receiver.attachSurface(video.getHolder().getSurface());
        }
    }

    private String displayName(Uri uri) {
        String name = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) name = cursor.getString(0);
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return name != null ? name : "local media";
    }

    private void releaseImportedPlayer() {
        if (importedPlayer != null) {
            try { importedPlayer.stop(); } catch (Exception ignored) {}
            importedPlayer.release();
            importedPlayer = null;
        }
        if (watermark != null) watermark.setVisibility(View.VISIBLE);
    }

    private void selectSource(TvSource source) {
        releaseImportedPlayer();
        receiver.attachSurface(video.getHolder().getSurface());
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
        status.setText("Display: "+m+" • "+(importedUri != null ? "local media available" : "PS5 receiver remains resident"));
    }

    @Override public boolean dispatchKeyEvent(KeyEvent e) {
        if (importedPlayer == null && receiver.sendControllerEvent(e)) return true;
        return super.dispatchKeyEvent(e);
    }

    @Override public void onState(Ps5Receiver.State s,String detail) {
        runOnUiThread(() -> {
            if (importedPlayer == null) status.setText("PS5: "+s+" • "+detail+" • DAW capture OFF");
        });
    }

    @Override protected void onDestroy() {
        releaseImportedPlayer();
        receiver.detachSurface();
        super.onDestroy();
    }
}
