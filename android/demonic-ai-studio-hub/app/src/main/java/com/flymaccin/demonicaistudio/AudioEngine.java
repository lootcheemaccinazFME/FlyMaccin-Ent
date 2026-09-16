package com.flymaccin.demonicaistudio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import java.util.Random;

final class AudioEngine {
    static final int SAMPLE_RATE = 44100;
    private final Random random = new Random();

    void playPiano(double frequency, int durationMs) {
        playPiano(frequency, durationMs, 0.8f, 0f, false, false);
    }

    void playPiano(double frequency, int durationMs, float volume, float pan, boolean reverb, boolean delay) {
        start("demonic-piano", () -> playBuffer(piano(frequency, durationMs), volume, pan, reverb, delay));
    }

    void playGuitarNote(double frequency, float volume, float pan, boolean reverb, boolean delay) {
        start("demonic-guitar", () -> playBuffer(pluck(frequency, 1200), volume, pan, reverb, delay));
    }

    void playGuitarChord(double[] frequencies) {
        playGuitarChord(frequencies, true, 0.82f, 0f, false, false);
    }

    void playGuitarChord(double[] frequencies, boolean downStroke, float volume, float pan,
                         boolean reverb, boolean delay) {
        for (int index = 0; index < frequencies.length; index++) {
            int source = downStroke ? index : frequencies.length - index - 1;
            final double frequency = frequencies[source];
            final long wait = index * 32L;
            start("demonic-strum", () -> {
                try { Thread.sleep(wait); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                playBuffer(pluck(frequency, 1350), volume * 0.72f, pan, reverb, delay);
            });
        }
    }

    void playDrum(int lane) {
        playDrum(lane, 0.9f, 0f, false, false);
    }

    void playDrum(int lane, float volume, float pan, boolean reverb, boolean delay) {
        start("demonic-drum", () -> playBuffer(drum(lane), volume, pan, reverb, delay));
    }

    private float[] piano(double frequency, int durationMs) {
        int count = SAMPLE_RATE * durationMs / 1000;
        float[] pcm = new float[count];
        for (int i = 0; i < count; i++) {
            double t = i / (double) SAMPLE_RATE;
            double attack = Math.min(1, t * 55);
            double release = Math.max(0, 1 - t / (durationMs / 1000.0));
            double envelope = attack * Math.pow(release, 0.7);
            double fundamental = Math.sin(2 * Math.PI * frequency * t);
            double second = Math.sin(2 * Math.PI * frequency * 2 * t) * 0.28;
            double third = Math.sin(2 * Math.PI * frequency * 3 * t) * 0.10;
            pcm[i] = (float) ((fundamental + second + third) * envelope * 0.62);
        }
        return pcm;
    }

    private float[] pluck(double frequency, int durationMs) {
        int count = SAMPLE_RATE * durationMs / 1000;
        int period = Math.max(2, (int) (SAMPLE_RATE / Math.max(35, frequency)));
        float[] ring = new float[period];
        for (int index = 0; index < period; index++) ring[index] = random.nextFloat() * 2f - 1f;
        float[] pcm = new float[count];
        for (int index = 0; index < count; index++) {
            int slot = index % period;
            float value = ring[slot];
            ring[slot] = 0.498f * (value + ring[(slot + 1) % period]);
            double t = index / (double) SAMPLE_RATE;
            pcm[index] = (float) (value * Math.exp(-t * 1.25) * 0.78);
        }
        return pcm;
    }

    private float[] drum(int lane) {
        int durationMs = lane == 0 ? 320 : lane == 1 ? 210 : lane == 2 ? 105 : 160;
        int count = SAMPLE_RATE * durationMs / 1000;
        float[] pcm = new float[count];
        double phase = 0;
        for (int i = 0; i < count; i++) {
            double t = i / (double) SAMPLE_RATE;
            double envelope = Math.exp(-t * (lane == 0 ? 15 : lane == 1 ? 25 : 42));
            double sample;
            if (lane == 0) {
                double frequency = 160 - 112 * Math.min(1, t * 8);
                phase += 2 * Math.PI * frequency / SAMPLE_RATE;
                sample = Math.sin(phase) * envelope;
            } else if (lane == 1) {
                sample = (random.nextDouble() * 2 - 1) * envelope * 0.82
                        + Math.sin(2 * Math.PI * 185 * t) * envelope * 0.26;
            } else if (lane == 2) {
                sample = (random.nextDouble() * 2 - 1) * envelope * 0.52;
            } else {
                sample = Math.sin(2 * Math.PI * 520 * t) * envelope * 0.52
                        + (random.nextDouble() * 2 - 1) * envelope * 0.16;
            }
            pcm[i] = (float) sample;
        }
        return pcm;
    }

    private void playBuffer(float[] mono, float volume, float pan, boolean reverb, boolean delay) {
        float[] processed = mono.clone();
        if (delay) addEcho(processed, (int) (SAMPLE_RATE * 0.27), 0.28f);
        if (reverb) {
            addEcho(processed, (int) (SAMPLE_RATE * 0.063), 0.18f);
            addEcho(processed, (int) (SAMPLE_RATE * 0.101), 0.12f);
        }
        float leftGain = volume * (pan <= 0 ? 1f : 1f - pan);
        float rightGain = volume * (pan >= 0 ? 1f : 1f + pan);
        short[] stereo = new short[processed.length * 2];
        for (int index = 0; index < processed.length; index++) {
            stereo[index * 2] = sample(processed[index] * leftGain);
            stereo[index * 2 + 1] = sample(processed[index] * rightGain);
        }
        AudioTrack track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build())
                .setBufferSizeInBytes(stereo.length * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build();
        try {
            track.write(stereo, 0, stereo.length);
            track.play();
            Thread.sleep(Math.max(100, processed.length * 1000L / SAMPLE_RATE + 40));
        } catch (Exception error) {
            if (error instanceof InterruptedException) Thread.currentThread().interrupt();
        } finally {
            try { if (track.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) track.stop(); } catch (Exception ignored) { }
            track.release();
        }
    }

    private static void addEcho(float[] samples, int offset, float gain) {
        for (int index = offset; index < samples.length; index++)
            samples[index] += samples[index - offset] * gain;
    }

    private static short sample(float value) {
        return (short) (Math.max(-1f, Math.min(1f, value)) * Short.MAX_VALUE);
    }

    private static void start(String name, Runnable action) {
        new Thread(action, name).start();
    }
}
