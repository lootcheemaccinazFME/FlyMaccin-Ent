package com.flymaccin.demonicaistudio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Small offline PCM synthesizer shared by the instrument screens and transport. */
final class AudioEngine {
    static final int SAMPLE_RATE = 44100;
    private static final int BLOCK_FRAMES = 512;
    private final Object lock = new Object();
    private final List<Voice> voices = new ArrayList<>();
    private volatile boolean running;
    private volatile long renderedFrames;
    private Thread outputThread;

    void start() {
        synchronized (lock) {
            if (outputThread != null && outputThread.isAlive()) return;
            running = true;
            outputThread = new Thread(this::renderAudio, "studio-audio-output");
            outputThread.setPriority(Thread.MAX_PRIORITY);
            outputThread.start();
        }
    }

    void playPiano(int midi, int durationMs, float volume, float pan, boolean reverb, boolean delay) {
        enqueue(InstrumentSynth.piano(midi, durationMs), volume, pan, reverb, delay, 0);
    }

    void playGuitar(int midi, float volume, float pan, boolean reverb, boolean delay) {
        enqueue(InstrumentSynth.guitar(midi, 1350), volume * 0.82f, pan, reverb, delay, 0);
    }

    void playGuitarChord(int[] midiNotes, boolean downStroke, float volume, float pan,
                         boolean reverb, boolean delay) {
        for (int index = 0; index < midiNotes.length; index++) {
            int note = midiNotes[downStroke ? index : midiNotes.length - index - 1];
            enqueue(InstrumentSynth.guitar(note, 1250), volume * 0.76f, pan,
                    reverb, delay, index * SAMPLE_RATE * 28L / 1000L);
        }
    }

    void playDrum(int lane, float volume, float pan, boolean reverb, boolean delay) {
        enqueue(InstrumentSynth.drum(lane), volume, pan, reverb, delay, 0);
    }

    void release() {
        Thread thread;
        synchronized (lock) {
            running = false;
            voices.clear();
            thread = outputThread;
        }
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(1500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        synchronized (lock) {
            if (outputThread == thread && (thread == null || !thread.isAlive())) outputThread = null;
        }
    }

    private void enqueue(float[] samples, float volume, float pan, boolean reverb,
                         boolean delay, long frameDelay) {
        if (!running) start();
        float[] effected = InstrumentSynth.effects(samples, reverb, delay);
        float left = volume * (pan > 0 ? 1f - pan : 1f);
        float right = volume * (pan < 0 ? 1f + pan : 1f);
        synchronized (lock) {
            voices.add(new Voice(effected, left, right, renderedFrames + frameDelay));
        }
    }

    private void renderAudio() {
        AudioTrack track = null;
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
            int minBytes = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            if (minBytes <= 0) throw new IllegalStateException("Audio output is unavailable");
            track = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build())
                    .setBufferSizeInBytes(Math.max(minBytes * 2, BLOCK_FRAMES * 8))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
            if (track.getState() != AudioTrack.STATE_INITIALIZED)
                throw new IllegalStateException("Audio output could not start");
            track.play();
            float[] mix = new float[BLOCK_FRAMES * 2];
            short[] pcm = new short[BLOCK_FRAMES * 2];
            while (running) {
                java.util.Arrays.fill(mix, 0f);
                long blockStart = renderedFrames;
                renderedFrames = blockStart + BLOCK_FRAMES;
                synchronized (lock) {
                    Iterator<Voice> iterator = voices.iterator();
                    while (iterator.hasNext()) {
                        Voice voice = iterator.next();
                        long start = Math.max(0, voice.startFrame - blockStart);
                        if (voice.startFrame >= blockStart + BLOCK_FRAMES) continue;
                        for (int frame = (int) start; frame < BLOCK_FRAMES; frame++) {
                            int source = (int) (blockStart + frame - voice.startFrame);
                            if (source >= voice.samples.length) break;
                            float sample = voice.samples[source];
                            mix[frame * 2] += sample * voice.left;
                            mix[frame * 2 + 1] += sample * voice.right;
                        }
                        if (blockStart + BLOCK_FRAMES - voice.startFrame >= voice.samples.length)
                            iterator.remove();
                    }
                }
                for (int index = 0; index < mix.length; index++)
                    pcm[index] = (short) (Math.max(-1f, Math.min(1f, mix[index])) * 32767);
                int offset = 0;
                while (running && offset < pcm.length) {
                    int written = track.write(pcm, offset, pcm.length - offset, AudioTrack.WRITE_BLOCKING);
                    if (written < 0) throw new IllegalStateException("Audio write failed: " + written);
                    if (written == 0) continue;
                    offset += written;
                }
            }
        } catch (Exception ignored) {
            // Audio is optional on devices without a usable output route.
        } finally {
            if (track != null) {
                try { track.pause(); } catch (Exception ignored) { }
                try { track.flush(); } catch (Exception ignored) { }
                track.release();
            }
            synchronized (lock) {
                voices.clear();
                running = false;
                if (outputThread == Thread.currentThread()) outputThread = null;
            }
        }
    }

    private static final class Voice {
        final float[] samples;
        final float left;
        final float right;
        final long startFrame;

        Voice(float[] samples, float left, float right, long startFrame) {
            this.samples = samples;
            this.left = left;
            this.right = right;
            this.startFrame = startFrame;
        }
    }

    /** Pure synthesis routines also drive the deterministic offline WAV renderer. */
    static final class InstrumentSynth {
        private InstrumentSynth() { }

        static float[] piano(int midi, int durationMs) {
            double frequency = StudioProject.frequencyForMidi(midi);
            int count = SAMPLE_RATE * durationMs / 1000;
            float[] samples = new float[count];
            for (int index = 0; index < count; index++) {
                double time = index / (double) SAMPLE_RATE;
                double envelope = Math.min(1, time * 55)
                        * Math.pow(Math.max(0, 1 - time / (durationMs / 1000.0)), 0.7);
                double wave = Math.sin(2 * Math.PI * frequency * time)
                        + 0.28 * Math.sin(4 * Math.PI * frequency * time)
                        + 0.10 * Math.sin(6 * Math.PI * frequency * time);
                samples[index] = (float) (wave * envelope * 0.52);
            }
            return samples;
        }

        static float[] guitar(int midi, int durationMs) {
            double frequency = StudioProject.frequencyForMidi(midi);
            int count = SAMPLE_RATE * durationMs / 1000;
            int period = Math.max(2, Math.min(SAMPLE_RATE, (int) (SAMPLE_RATE / frequency)));
            float[] ring = new float[period];
            Random random = new Random(210903L + midi * 31L);
            for (int index = 0; index < period; index++) ring[index] = random.nextFloat() * 2f - 1f;
            float[] samples = new float[count];
            for (int index = 0; index < count; index++) {
                int slot = index % period;
                float value = ring[slot];
                ring[slot] = 0.498f * (value + ring[(slot + 1) % period]);
                samples[index] = (float) (value * Math.exp(-index / (double) SAMPLE_RATE * 1.45) * 0.64);
            }
            return samples;
        }

        static float[] drum(int lane) {
            lane = Math.max(0, Math.min(3, lane));
            int durationMs = lane == 0 ? 360 : lane == 1 ? 230 : lane == 2 ? 110 : 170;
            int count = SAMPLE_RATE * durationMs / 1000;
            float[] samples = new float[count];
            double phase = 0;
            Random random = new Random(57721L + lane * 7919L);
            for (int index = 0; index < count; index++) {
                double time = index / (double) SAMPLE_RATE;
                double envelope = Math.exp(-time * (lane == 0 ? 14 : lane == 1 ? 24 : 42));
                if (lane == 0) {
                    double frequency = 155 - 105 * Math.min(1, time * 7);
                    phase += 2 * Math.PI * frequency / SAMPLE_RATE;
                    samples[index] = (float) (Math.sin(phase) * envelope);
                } else if (lane == 1) {
                    samples[index] = (float) (((random.nextDouble() * 2 - 1) * 0.72
                            + Math.sin(2 * Math.PI * 185 * time) * 0.24) * envelope);
                } else if (lane == 2) {
                    samples[index] = (float) ((random.nextDouble() * 2 - 1) * envelope * 0.5);
                } else {
                    samples[index] = (float) ((Math.sin(2 * Math.PI * 520 * time) * 0.48
                            + (random.nextDouble() * 2 - 1) * 0.13) * envelope);
                }
            }
            return samples;
        }

        static float[] effects(float[] source, boolean reverb, boolean delay) {
            if (!reverb && !delay) return source;
            int maximumTail = SAMPLE_RATE * (delay ? 1 : 0) + SAMPLE_RATE / 5;
            float[] result = new float[source.length + maximumTail];
            System.arraycopy(source, 0, result, 0, source.length);
            if (delay) echo(result, SAMPLE_RATE * 27 / 100, 0.25f);
            if (reverb) {
                echo(result, SAMPLE_RATE * 61 / 1000, 0.16f);
                echo(result, SAMPLE_RATE * 97 / 1000, 0.10f);
            }
            return result;
        }

        private static void echo(float[] samples, int offset, float gain) {
            for (int index = offset; index < samples.length; index++)
                samples[index] += samples[index - offset] * gain;
        }
    }
}
