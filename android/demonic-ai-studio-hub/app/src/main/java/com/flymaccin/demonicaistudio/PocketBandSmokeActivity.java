package com.flymaccin.demonicaistudio;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public final class PocketBandSmokeActivity extends Activity {
    private static final String TAG = "PocketBandSmoke";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        new Thread(this::runSmoke, "PocketBandSmoke").start();
    }

    private void runSmoke() {
        try {
            File sample = new File(getCacheDir(), "pocketband_smoke_C4.wav");
            writeTone(sample, 261.626, 350);
            PocketBandInstrument instrument = new PocketBandInstrument(sample, PocketBandInstrument.detect(sample), sample.getName());
            if (!instrument.exists() || instrument.format != PocketBandInstrument.Format.WAV)
                throw new IllegalStateException("Instrument load failed");

            AudioEngine engine = new AudioEngine();
            engine.playSample(sample, 261.626, 329.628, 0.20f, -0.15f, true, true);

            StudioProject project = new StudioProject();
            project.name = "PocketBand_Semantic_Smoke";
            project.bpm = 104;
            project.addFrequency(StudioProject.TRACK_PIANO, 0, 261.626);
            project.addFrequency(StudioProject.TRACK_PIANO, 4, 329.628);
            project.setFrequencies(StudioProject.TRACK_GUITAR, 8, new double[]{196.000, 246.942, 293.665});
            project.drums[0][0] = true;
            project.drums[1][4] = true;
            project.drums[2][2] = true;
            project.drums[2][6] = true;
            project.volume[0] = 0.72f;
            project.pan[0] = -0.20f;
            project.volume[2] = 0.84f;
            project.pan[2] = 0.18f;
            project.reverb[0] = true;
            project.delay[2] = true;

            StudioRenderer.Result result = new StudioRenderer().render(this, project, false);
            validateWav(result.master);
            Log.i(TAG, "POCKETBAND_RENDER_PASS path=" + result.master.getAbsolutePath()
                    + " bytes=" + result.master.length());

            MediaPlayer player = new MediaPlayer();
            player.setDataSource(result.master.getAbsolutePath());
            player.setOnPreparedListener(mp -> {
                try {
                    if (mp.getDuration() <= 0) throw new IllegalStateException("Export has zero duration");
                    mp.setOnCompletionListener(done -> {
                        Log.i(TAG, "POCKETBAND_SMOKE_PASS durationMs=" + done.getDuration());
                        done.release();
                        finish();
                    });
                    mp.start();
                    Log.i(TAG, "POCKETBAND_PLAYBACK_STARTED durationMs=" + mp.getDuration());
                } catch (Throwable failure) {
                    fail(failure, mp);
                }
            });
            player.setOnErrorListener((mp, what, extra) -> {
                fail(new IllegalStateException("MediaPlayer error " + what + "/" + extra), mp);
                return true;
            });
            player.prepareAsync();
        } catch (Throwable failure) {
            fail(failure, null);
        }
    }

    private void fail(Throwable failure, MediaPlayer player) {
        if (player != null) try { player.release(); } catch (Throwable ignored) {}
        Log.e(TAG, "POCKETBAND_SMOKE_FAIL", failure);
        finish();
    }

    private static void validateWav(File file) throws Exception {
        if (!file.exists() || file.length() <= 44) throw new IllegalStateException("Rendered WAV missing/empty");
        byte[] header = new byte[12];
        try (FileInputStream input = new FileInputStream(file)) {
            if (input.read(header) != header.length) throw new IllegalStateException("Short WAV header");
        }
        String riff = new String(header, 0, 4, java.nio.charset.StandardCharsets.US_ASCII);
        String wave = new String(header, 8, 4, java.nio.charset.StandardCharsets.US_ASCII);
        if (!"RIFF".equals(riff) || !"WAVE".equals(wave)) throw new IllegalStateException("Invalid WAV header");
    }

    private static void writeTone(File file, double frequency, int durationMs) throws Exception {
        final int rate = 44100;
        int frames = rate * durationMs / 1000;
        int dataBytes = frames * 2;
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            out.write(new byte[]{'R','I','F','F'}); writeInt(out, 36 + dataBytes);
            out.write(new byte[]{'W','A','V','E','f','m','t',' '}); writeInt(out, 16);
            writeShort(out, 1); writeShort(out, 1); writeInt(out, rate); writeInt(out, rate * 2);
            writeShort(out, 2); writeShort(out, 16); out.write(new byte[]{'d','a','t','a'}); writeInt(out, dataBytes);
            for (int i = 0; i < frames; i++) {
                double env = Math.max(0, 1.0 - i / (double) frames);
                short sample = (short)(Math.sin(2 * Math.PI * frequency * i / rate) * env * 9000);
                writeShort(out, sample);
            }
        }
    }

    private static void writeInt(BufferedOutputStream out, int v) throws Exception {
        out.write(v & 255); out.write((v >> 8) & 255); out.write((v >> 16) & 255); out.write((v >> 24) & 255);
    }
    private static void writeShort(BufferedOutputStream out, int v) throws Exception {
        out.write(v & 255); out.write((v >> 8) & 255);
    }
}
