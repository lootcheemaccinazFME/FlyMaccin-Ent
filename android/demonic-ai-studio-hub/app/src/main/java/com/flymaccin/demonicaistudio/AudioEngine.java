package com.flymaccin.demonicaistudio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import java.util.Random;

final class AudioEngine {
    private static final int SAMPLE_RATE = 44100;
    private final Random random = new Random();

    void playPiano(double frequency, int durationMs) {
        playOscillator(frequency, durationMs, 0.58, 0);
    }

    void playGuitarChord(double[] frequencies) {
        for (double frequency : frequencies) {
            playOscillator(frequency, 900, 0.24, 1);
        }
    }

    void playDrum(int lane) {
        new Thread(() -> {
            int durationMs = lane == 0 ? 260 : lane == 1 ? 190 : 100;
            int count = SAMPLE_RATE * durationMs / 1000;
            short[] pcm = new short[count];
            double phase = 0;
            for (int i = 0; i < count; i++) {
                double t = i / (double) SAMPLE_RATE;
                double envelope = Math.exp(-t * (lane == 0 ? 18 : 28));
                double sample;
                if (lane == 0) {
                    double f = 150 - 105 * Math.min(1, t * 8);
                    phase += 2 * Math.PI * f / SAMPLE_RATE;
                    sample = Math.sin(phase) * envelope;
                } else if (lane == 1) {
                    sample = (random.nextDouble() * 2 - 1) * envelope * 0.8
                            + Math.sin(2 * Math.PI * 185 * t) * envelope * 0.25;
                } else if (lane == 2) {
                    sample = (random.nextDouble() * 2 - 1) * envelope * 0.55;
                } else {
                    sample = Math.sin(2 * Math.PI * 520 * t) * envelope * 0.45;
                }
                pcm[i] = (short) (sample * Short.MAX_VALUE * 0.75);
            }
            playPcm(pcm);
        }, "demonic-drum").start();
    }

    private void playOscillator(double frequency, int durationMs, double gain, int voice) {
        new Thread(() -> {
            int count = SAMPLE_RATE * durationMs / 1000;
            short[] pcm = new short[count];
            for (int i = 0; i < count; i++) {
                double t = i / (double) SAMPLE_RATE;
                double attack = Math.min(1, t * 45);
                double release = Math.max(0, 1 - t / (durationMs / 1000.0));
                double envelope = attack * Math.pow(release, voice == 0 ? 0.65 : 1.7);
                double fundamental = Math.sin(2 * Math.PI * frequency * t);
                double harmonic = Math.sin(2 * Math.PI * frequency * 2 * t) * 0.24;
                double sample;
                if (voice == 1) {
                    sample = Math.tanh((fundamental + harmonic) * 1.8) * envelope;
                } else {
                    sample = (fundamental + harmonic * 0.45) * envelope;
                }
                pcm[i] = (short) (sample * Short.MAX_VALUE * gain);
            }
            playPcm(pcm);
        }, "demonic-synth").start();
    }

    private void playPcm(short[] pcm) {
        AudioTrack track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(pcm.length * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build();
        try {
            track.write(pcm, 0, pcm.length);
            track.play();
            Thread.sleep(Math.max(100, pcm.length * 1000L / SAMPLE_RATE + 50));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            track.stop();
            track.release();
        }
    }
}
