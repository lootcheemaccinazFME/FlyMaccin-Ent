package com.flymaccin.demonictv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.VideoView;
import android.widget.MediaController;
import android.app.PictureInPictureParams;
import android.app.AlertDialog;
import android.widget.EditText;
import android.util.Rational;
import android.widget.Toast;
import android.content.SharedPreferences;
import java.util.LinkedHashSet;
import java.util.Set;

public final class TvActivity extends Activity implements Ps5Receiver.Listener {
    private static final int REQUEST_IMPORT_MEDIA = 4101;

    private FrameLayout root, stage;
    private TextView status, watermark;
    private SurfaceView video;
    private VideoView movieView;
    private Ps5Receiver receiver;
    private Uri importedUri;
    private LinearLayout movieLibrary;
    private final Set<String> savedMovies = new LinkedHashSet<>();
    private final Set<String> recentMovies = new LinkedHashSet<>();
    private final Set<String> savedChannels = new LinkedHashSet<>();
    private final Set<String> favoriteChannels = new LinkedHashSet<>();
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
        top.addView(button("LIVE TV", v -> selectSource(TvSource.FREE_TV)));
        top.addView(button("BROWSER / TV", v -> selectSource(TvSource.BROWSER)));
        top.addView(button("IMPORT FILE", v -> importFile()));
        top.addView(button("PLAYLISTS", v -> showPlaylists()));
        top.addView(button("FAVORITES", v -> showFavorites()));
        top.addView(button("SETTINGS", v -> showSettings()));
        for (TvSource s : TvSource.values()) top.addView(button(s.name().replace('_',' '), v -> selectSource(s)));
        shell.addView(top, new LinearLayout.LayoutParams(-1,-2));

        stage = new FrameLayout(this); stage.setBackgroundColor(Color.BLACK);
        video = new SurfaceView(this);
        stage.addView(video, new FrameLayout.LayoutParams(-1,-1));
        movieView = new VideoView(this);
        movieView.setVisibility(View.GONE);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(movieView);
        movieView.setMediaController(mediaController);
        stage.addView(movieView, new FrameLayout.LayoutParams(-1,-1));
        watermark = text("PS5 RECEIVER SURFACE",16); watermark.setGravity(Gravity.CENTER);
        stage.addView(watermark,new FrameLayout.LayoutParams(-1,-1));
        shell.addView(stage,new LinearLayout.LayoutParams(-1,0,1));

        movieLibrary = new LinearLayout(this);
        movieLibrary.setOrientation(LinearLayout.HORIZONTAL);
        shell.addView(movieLibrary, new LinearLayout.LayoutParams(-1,-2));
        loadMovieLibrary();
        loadChannelLibrary();

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
                // VideoView follows the device media volume.
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
                // Imported movies render through the VideoView.
            }
            public void surfaceChanged(SurfaceHolder h,int f,int w,int he) {
                receiver.attachSurface(h.getSurface());
                // Imported movies render through the VideoView.
            }
            public void surfaceDestroyed(SurfaceHolder h) {
                receiver.detachSurface();
                // Imported movies render through the VideoView.
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
        saveMovie(uri);
        playImportedFile(uri);
    }

    private void saveMovie(Uri uri) {
        savedMovies.add(uri.toString());
        getSharedPreferences("demonic_tv_movies", MODE_PRIVATE)
            .edit().putStringSet("movies", new LinkedHashSet<>(savedMovies)).apply();
        refreshMovieLibrary();
    }

    private void loadMovieLibrary() {
        SharedPreferences prefs = getSharedPreferences("demonic_tv_movies", MODE_PRIVATE);
        savedMovies.clear();
        recentMovies.clear();
        savedMovies.addAll(prefs.getStringSet("movies", new LinkedHashSet<>()));
        recentMovies.addAll(prefs.getStringSet("recent", new LinkedHashSet<>()));
        refreshMovieLibrary();
    }

    private void refreshMovieLibrary() {
        if (movieLibrary == null) return;
        movieLibrary.removeAllViews();
        movieLibrary.addView(text("MY MOVIES", 14));
        movieLibrary.addView(button("+ ADD CHANNEL", v -> addChannel()));
        movieLibrary.addView(button("EPG / GUIDE", v -> showGuide()));
        movieLibrary.addView(button("FULLSCREEN", v -> setMode(DisplayMode.FULLSCREEN)));
        movieLibrary.addView(button("PiP", v -> enterTvPip()));
        movieLibrary.addView(button("AUDIO", v -> toast("Audio track controls")));
        movieLibrary.addView(button("SUBTITLES", v -> toast("Subtitle controls")));
        if (!recentMovies.isEmpty()) movieLibrary.addView(text("RECENT", 12));
        for (String recent : recentMovies) {
            Uri uri = Uri.parse(recent);
            movieLibrary.addView(button("▶ " + displayName(uri), v -> playImportedFile(uri)));
        }
        if (!savedMovies.isEmpty()) movieLibrary.addView(text("LIBRARY", 12));
        for (String saved : savedMovies) {
            Uri uri = Uri.parse(saved);
            movieLibrary.addView(button(displayName(uri), v -> playImportedFile(uri)));
            movieLibrary.addView(button("REMOVE", v -> removeMovie(uri)));
        }
    }

    private void playImportedFile(Uri uri) {
        stopMoviePlayback();
        receiver.detachSurface();
        importedUri = uri;
        recentMovies.remove(uri.toString());
        recentMovies.add(uri.toString());
        while (recentMovies.size() > 8) recentMovies.remove(recentMovies.iterator().next());
        getSharedPreferences("demonic_tv_movies", MODE_PRIVATE).edit()
            .putStringSet("recent", new LinkedHashSet<>(recentMovies)).apply();
        video.setVisibility(View.GONE);
        watermark.setVisibility(View.GONE);
        movieView.setVisibility(View.VISIBLE);
        movieView.setVideoURI(uri);
        movieView.setOnPreparedListener(player -> {
            int resumeMs = getSharedPreferences("demonic_tv_resume", MODE_PRIVATE)
                .getInt(uri.toString(), 0);
            if (resumeMs > 5000 && resumeMs < player.getDuration() - 5000) {
                movieView.seekTo(resumeMs);
                status.setText("Resuming: " + displayName(uri));
            } else {
                status.setText("Now playing: " + displayName(uri));
            }
            movieView.start();
        });
        movieView.setOnCompletionListener(player ->
            status.setText("Movie finished: " + displayName(uri)));
        movieView.setOnErrorListener((player, what, extra) -> {
            status.setText("Can't play this movie format • try MP4 (H.264/AAC)");
            return true;
        });
        status.setText("Loading movie: " + displayName(uri));
        movieView.requestFocus();
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

    private void removeMovie(Uri uri) {
        savedMovies.remove(uri.toString());
        recentMovies.remove(uri.toString());
        getSharedPreferences("demonic_tv_movies", MODE_PRIVATE).edit()
            .putStringSet("movies", new LinkedHashSet<>(savedMovies))
            .putStringSet("recent", new LinkedHashSet<>(recentMovies)).apply();
        getSharedPreferences("demonic_tv_resume", MODE_PRIVATE).edit()
            .remove(uri.toString()).apply();
        if (uri.equals(importedUri)) stopMoviePlayback();
        refreshMovieLibrary();
        status.setText("Removed from My Movies • original file untouched");
    }

    private void saveResumePosition() {
        if (movieView != null && movieView.getVisibility() == View.VISIBLE && importedUri != null) {
            int position = movieView.getCurrentPosition();
            if (position > 0) getSharedPreferences("demonic_tv_resume", MODE_PRIVATE)
                .edit().putInt(importedUri.toString(), position).apply();
        }
    }

    private void stopMoviePlayback() {
        saveResumePosition();
        if (movieView != null) {
            try { movieView.stopPlayback(); } catch (Exception ignored) {}
            movieView.setVisibility(View.GONE);
        }
        if (video != null) video.setVisibility(View.VISIBLE);
        if (watermark != null) watermark.setVisibility(View.VISIBLE);
    }

    private void releaseImportedPlayer() {
        stopMoviePlayback();
    }

    private void loadChannelLibrary() {
        SharedPreferences prefs = getSharedPreferences("demonic_tv_channels", MODE_PRIVATE);
        savedChannels.clear();
        favoriteChannels.clear();
        savedChannels.addAll(prefs.getStringSet("channels", new LinkedHashSet<>()));
        favoriteChannels.addAll(prefs.getStringSet("favorites", new LinkedHashSet<>()));
    }

    private void persistChannels() {
        getSharedPreferences("demonic_tv_channels", MODE_PRIVATE).edit()
            .putStringSet("channels", new LinkedHashSet<>(savedChannels))
            .putStringSet("favorites", new LinkedHashSet<>(favoriteChannels))
            .apply();
    }

    private void showPlaylists() {
        movieLibrary.removeAllViews();
        movieLibrary.addView(text("PLAYLISTS / CHANNELS", 14));
        movieLibrary.addView(button("+ ADD CHANNEL", v -> addChannel()));
        for (String channel : savedChannels) {
            String[] parts = channel.split("\\|", 2);
            String name = parts[0];
            String url = parts.length > 1 ? parts[1] : "";
            movieLibrary.addView(button(name, v -> openChannel(name, url)));
            movieLibrary.addView(button(favoriteChannels.contains(channel) ? "★" : "☆", v -> toggleFavorite(channel)));
        }
        status.setText("PLAYLISTS • " + savedChannels.size() + " saved channels");
    }

    private void showFavorites() {
        movieLibrary.removeAllViews();
        movieLibrary.addView(text("FAVORITES", 14));
        for (String channel : favoriteChannels) {
            String[] parts = channel.split("\\|", 2);
            String name = parts[0];
            String url = parts.length > 1 ? parts[1] : "";
            movieLibrary.addView(button("★ " + name, v -> openChannel(name, url)));
        }
        status.setText("FAVORITES • " + favoriteChannels.size() + " channels");
    }

    private void showSettings() {
        status.setText("SETTINGS • playback, display, audio, subtitles");
        toast("Demonic TV settings");
    }

    private void addChannel() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        final EditText name = new EditText(this);
        name.setHint("Channel name");
        final EditText url = new EditText(this);
        url.setHint("https:// provider or legal stream URL");
        form.addView(name);
        form.addView(url);
        new AlertDialog.Builder(this)
            .setTitle("Add TV Channel")
            .setView(form)
            .setPositiveButton("SAVE", (dialog, which) -> {
                String n = name.getText().toString().trim();
                String u = url.getText().toString().trim();
                if (n.isEmpty() || !(u.startsWith("https://") || u.startsWith("http://"))) {
                    toast("Enter a channel name and valid web address");
                    return;
                }
                savedChannels.add(n + "|" + u);
                persistChannels();
                showPlaylists();
            })
            .setNegativeButton("CANCEL", null)
            .show();
    }

    private void openChannel(String name, String url) {
        if (url == null || url.isEmpty()) return;
        Uri uri = Uri.parse(url);
        String lower = url.toLowerCase();
        boolean directMedia = lower.contains(".m3u8") || lower.endsWith(".mp4") || lower.endsWith(".3gp") || lower.endsWith(".webm");
        if (!directMedia) {
            status.setText("CHANNEL • " + name + " • provider page");
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return;
        }

        stopMoviePlayback();
        receiver.detachSurface();
        video.setVisibility(View.GONE);
        watermark.setVisibility(View.GONE);
        movieView.setVisibility(View.VISIBLE);
        movieView.setVideoURI(uri);
        movieView.setOnPreparedListener(player -> {
            status.setText("LIVE • " + name);
            movieView.start();
        });
        movieView.setOnErrorListener((player, what, extra) -> {
            status.setText("Stream needs provider/browser playback • " + name);
            stopMoviePlayback();
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
        });
        status.setText("TUNING • " + name);
        movieView.requestFocus();
    }

    private void toggleFavorite(String channel) {
        if (favoriteChannels.contains(channel)) favoriteChannels.remove(channel);
        else favoriteChannels.add(channel);
        persistChannels();
        showPlaylists();
    }

    private void showGuide() {
        movieLibrary.removeAllViews();
        movieLibrary.addView(text("LIVE TV GUIDE", 14));
        movieLibrary.addView(button("FREE TV GUIDE", v ->
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://watch.plex.tv/live-tv")))));
        for (String channel : savedChannels) {
            String[] parts = channel.split("\\|", 2);
            String name = parts[0];
            String url = parts.length > 1 ? parts[1] : "";
            movieLibrary.addView(button(name + " • WATCH", v -> openChannel(name, url)));
        }
        status.setText("EPG / GUIDE • " + savedChannels.size() + " saved channels • provider schedules where available");
    }

    private void enterTvPip() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            PictureInPictureParams params = new PictureInPictureParams.Builder()
                .setAspectRatio(new Rational(16, 9)).build();
            enterPictureInPictureMode(params);
        } else {
            setMode(DisplayMode.PIP);
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void selectSource(TvSource source) {
        releaseImportedPlayer();
        if (source == TvSource.FREE_TV) {
            Intent liveTv = new Intent(Intent.ACTION_VIEW, Uri.parse("https://watch.plex.tv/live-tv"));
            status.setText("LIVE TV • free legal channel guide");
            startActivity(liveTv);
            return;
        }
        if (source == TvSource.BROWSER) {
            Intent browserTv = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/"));
            status.setText("BROWSER / TV • opening web video");
            startActivity(browserTv);
            return;
        }
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
        if (movieView == null || movieView.getVisibility() != View.VISIBLE) { if (receiver.sendControllerEvent(e)) return true; }
        return super.dispatchKeyEvent(e);
    }

    @Override public void onState(Ps5Receiver.State s,String detail) {
        runOnUiThread(() -> {
            if (movieView == null || movieView.getVisibility() != View.VISIBLE) status.setText("PS5: "+s+" • "+detail+" • DAW capture OFF");
        });
    }

    @Override protected void onPause() {
        saveResumePosition();
        super.onPause();
    }

    @Override protected void onDestroy() {
        releaseImportedPlayer();
        receiver.detachSurface();
        super.onDestroy();
    }
}
