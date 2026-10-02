package com.flymaccin.demonictv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

    private GradientDrawable panelBg(int color, float radius, int strokeColor) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(radius);
        if (strokeColor != Color.TRANSPARENT) bg.setStroke(1, strokeColor);
        return bg;
    }

    private Button button(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setPadding(18, 10, 18, 10);
        b.setBackground(panelBg(Color.rgb(88, 7, 16), 18f, Color.rgb(170, 18, 34)));
        b.setOnClickListener(l);
        return b;
    }

    private Button navButton(String s, View.OnClickListener l) {
        Button b = button(s, l);
        b.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        b.setBackground(panelBg(Color.rgb(22, 12, 16), 16f, Color.rgb(74, 24, 32)));
        return b;
    }

    private void buildUi() {
        final int BLACK = Color.rgb(5, 4, 7);
        final int PANEL = Color.rgb(15, 10, 14);
        final int PANEL_2 = Color.rgb(24, 12, 17);
        final int RED = Color.rgb(180, 20, 36);

        root = new FrameLayout(this);
        root.setBackgroundColor(BLACK);

        LinearLayout app = new LinearLayout(this);
        app.setOrientation(LinearLayout.VERTICAL);
        root.addView(app, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(18, 8, 18, 8);
        header.setBackgroundColor(Color.rgb(9, 6, 9));
        TextView brand = text("DEMONIC TV", 22);
        brand.setTextColor(Color.rgb(235, 55, 65));
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        TextView clock = text("FME MEDIA CENTER", 12);
        clock.setTextColor(Color.LTGRAY);
        header.addView(clock);
        app.addView(header, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        app.addView(body, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.VERTICAL);
        nav.setPadding(10, 12, 10, 12);
        nav.setBackgroundColor(Color.rgb(10, 7, 10));
        nav.addView(navButton("▣  Live TV", v -> selectSource(TvSource.FREE_TV)));
        nav.addView(navButton("⌕  Browser / TV", v -> selectSource(TvSource.BROWSER)));
        nav.addView(navButton("＋  Import File", v -> importFile()));
        nav.addView(navButton("▶  My Movies", v -> refreshMovieLibrary()));
        nav.addView(navButton("☷  Playlists", v -> showPlaylists()));
        nav.addView(navButton("★  Favorites", v -> showFavorites()));
        nav.addView(navButton("⚙  Settings", v -> showSettings()));
        body.addView(nav, new LinearLayout.LayoutParams(230, -1));

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setPadding(12, 12, 12, 12);
        body.addView(center, new LinearLayout.LayoutParams(0, -1, 1));

        stage = new FrameLayout(this);
        stage.setBackground(panelBg(Color.BLACK, 18f, Color.rgb(92, 18, 30)));
        video = new SurfaceView(this);
        stage.addView(video, new FrameLayout.LayoutParams(-1, -1));

        movieView = new VideoView(this);
        movieView.setVisibility(View.GONE);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(movieView);
        movieView.setMediaController(mediaController);
        stage.addView(movieView, new FrameLayout.LayoutParams(-1, -1));

        watermark = text("DEMONIC TV\nSelect Live TV, a saved channel, or one of My Movies", 17);
        watermark.setGravity(Gravity.CENTER);
        watermark.setTextColor(Color.rgb(170, 150, 155));
        stage.addView(watermark, new FrameLayout.LayoutParams(-1, -1));
        center.addView(stage, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout playerBar = new LinearLayout(this);
        playerBar.setGravity(Gravity.CENTER_VERTICAL);
        playerBar.setPadding(8, 8, 8, 8);
        playerBar.setBackground(panelBg(PANEL_2, 16f, Color.rgb(82, 24, 34)));
        playerBar.addView(button("⏮", v -> { if (movieView != null) movieView.seekTo(Math.max(0, movieView.getCurrentPosition()-10000)); }));
        playerBar.addView(button("▶ / ❚❚", v -> {
            if (movieView != null && movieView.getVisibility() == View.VISIBLE) {
                if (movieView.isPlaying()) movieView.pause(); else movieView.start();
            }
        }));
        playerBar.addView(button("⏭", v -> { if (movieView != null) movieView.seekTo(movieView.getCurrentPosition()+10000); }));
        playerBar.addView(button("FULL", v -> setMode(DisplayMode.FULLSCREEN)));
        playerBar.addView(button("PiP", v -> enterTvPip()));
        playerBar.addView(button("AUDIO", v -> toast("Audio track controls")));
        playerBar.addView(button("SUB", v -> toast("Subtitle controls")));
        center.addView(playerBar, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setPadding(10, 12, 10, 12);
        right.setBackgroundColor(PANEL);
        TextView guideTitle = text("LIVE TV GUIDE", 16);
        guideTitle.setTextColor(Color.rgb(235, 55, 65));
        right.addView(guideTitle);
        right.addView(button("+ ADD CHANNEL", v -> addChannel()));
        right.addView(button("EPG / GUIDE", v -> showGuide()));
        right.addView(button("DISCOVER PS5", v -> receiver.discover()));
        right.addView(button("PAIR", v -> receiver.beginPairing()));
        right.addView(button("CONNECT", v -> receiver.connect()));
        right.addView(button("DISCONNECT", v -> receiver.disconnect()));
        body.addView(right, new LinearLayout.LayoutParams(250, -1));

        movieLibrary = new LinearLayout(this);
        movieLibrary.setOrientation(LinearLayout.HORIZONTAL);
        movieLibrary.setPadding(10, 6, 10, 6);
        movieLibrary.setBackgroundColor(Color.rgb(9, 6, 9));
        app.addView(movieLibrary, new LinearLayout.LayoutParams(-1, -2));
        loadMovieLibrary();
        loadChannelLibrary();

        status = text("Ready • My Movies + Live TV + PS5", 13);
        status.setTextColor(Color.LTGRAY);
        status.setBackgroundColor(Color.rgb(8, 5, 8));
        app.addView(status, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);

        video.getHolder().addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder h) { receiver.attachSurface(h.getSurface()); }
            public void surfaceChanged(SurfaceHolder h,int f,int w,int he) { receiver.attachSurface(h.getSurface()); }
            public void surfaceDestroyed(SurfaceHolder h) { receiver.detachSurface(); }
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
