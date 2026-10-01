package com.flymaccin.demonicaistudio;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Chunked offline renderer, PCM WAV writer and MediaStore publisher. */
final class StudioRenderer {
    private static final int RATE = AudioEngine.SAMPLE_RATE;
    private static final int CHUNK_FRAMES = 4096;
    private static final int MAX_SECONDS = 600;
    private static final String[] TRACK_NAMES = {"Piano", "Guitar", "Drums", "Voice"};

    static final class Result {
        final File master;
        final List<File> stems;

        Result(File master, List<File> stems) {
            this.master = master;
            this.stems = stems;
        }
    }

    Result render(Context context, StudioProject project, boolean includeStems) throws Exception {
        int baseStepFrames = (int) Math.round(60.0 / project.bpm / 4.0 * RATE);
        int swingFrames = baseStepFrames * project.swing / 100;
        int sequenceFrames = stepOffset(StudioProject.STEPS, baseStepFrames, swingFrames);
        List<Event> events = buildEvents(project, baseStepFrames, swingFrames);
        int frames = sequenceFrames;
        for (Event event : events) frames = Math.max(frames, event.start + event.samples.length);
        frames = Math.max(1, frames);
        if (frames > RATE * MAX_SECONDS)
            throw new IllegalStateException("Session is longer than the 10-minute WAV export limit");

        File root = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (root == null) root = context.getFilesDir();
        root = new File(root, "DemonicRenders");
        if (!root.isDirectory() && !root.mkdirs())
            throw new IllegalStateException("Cannot create render folder");
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String base = safeName(project.name) + "_" + stamp;
        File master = new File(root, base + "_MASTER.wav");
        WavWriter masterWriter = new WavWriter(master, RATE, 2);
        WavWriter[] stemWriters = new WavWriter[StudioProject.TRACK_COUNT];
        try {
            if (includeStems) {
                for (int track = 0; track < stemWriters.length; track++)
                    stemWriters[track] = new WavWriter(new File(root, base + "_" + TRACK_NAMES[track] + ".wav"), RATE, 2);
            }
            for (int start = 0; start < frames; start += CHUNK_FRAMES) {
                int count = Math.min(CHUNK_FRAMES, frames - start);
                float[][] tracks = new float[StudioProject.TRACK_COUNT][count * 2];
                for (Event event : events) {
                    int overlapStart = Math.max(start, event.start);
                    int overlapEnd = Math.min(start + count, event.start + event.samples.length);
                    if (overlapStart >= overlapEnd) continue;
                    int localStart = overlapStart - start;
                    int sourceStart = overlapStart - event.start;
                    float volume = project.volume[event.track];
                    float pan = project.pan[event.track];
                    float left = volume * (pan > 0 ? 1f - pan : 1f);
                    float right = volume * (pan < 0 ? 1f + pan : 1f);
                    for (int frame = 0; frame < overlapEnd - overlapStart; frame++) {
                        float sample = event.samples[sourceStart + frame];
                        tracks[event.track][(localStart + frame) * 2] += sample * left;
                        tracks[event.track][(localStart + frame) * 2 + 1] += sample * right;
                    }
                }
                float[] mix = new float[count * 2];
                for (int track = 0; track < StudioProject.TRACK_COUNT; track++) {
                    if (stemWriters[track] != null) stemWriters[track].write(tracks[track], count, false);
                    if (project.trackAudible(track)) {
                        for (int sample = 0; sample < mix.length; sample++) mix[sample] += tracks[track][sample];
                    }
                }
                masterWriter.write(mix, count, true);
            }
            masterWriter.finish();
            for (WavWriter writer : stemWriters) if (writer != null) writer.finish();
        } catch (Exception error) {
            masterWriter.closeQuietly();
            for (WavWriter writer : stemWriters) if (writer != null) writer.closeQuietly();
            master.delete();
            for (int track = 0; track < stemWriters.length; track++) {
                if (stemWriters[track] != null) stemWriters[track].file.delete();
            }
            throw error;
        }

        ArrayList<File> stems = new ArrayList<>();
        if (includeStems) {
            for (int track = 0; track < stemWriters.length; track++)
                stems.add(stemWriters[track].file);
        }
        return new Result(master, stems);
    }

    static Uri publish(Context context, File source) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, source.getName());
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav");
        if (Build.VERSION.SDK_INT >= 29) {
            values.put(MediaStore.Audio.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_MUSIC + "/DemonicAIStudio");
            values.put(MediaStore.Audio.Media.IS_PENDING, 1);
        }
        Uri uri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) throw new IllegalStateException("Media library rejected the WAV export");
        try (InputStream input = new FileInputStream(source);
             java.io.OutputStream output = context.getContentResolver().openOutputStream(uri)) {
            if (output == null) throw new IllegalStateException("Cannot open exported media file");
            byte[] buffer = new byte[32768];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            output.flush();
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues ready = new ContentValues();
                ready.put(MediaStore.Audio.Media.IS_PENDING, 0);
                context.getContentResolver().update(uri, ready, null, null);
            }
            return uri;
        } catch (Exception error) {
            context.getContentResolver().delete(uri, null, null);
            throw error;
        }
    }

    private static List<Event> buildEvents(StudioProject project, int stepFrames, int swingFrames)
            throws Exception {
        ArrayList<Event> events = new ArrayList<>();
        for (int step = 0; step < StudioProject.STEPS; step++) {
            int start = stepOffset(step, stepFrames, swingFrames);
            for (int midi : project.midiNotesAt(StudioProject.TRACK_PIANO, step)) {
                events.add(new Event(StudioProject.TRACK_PIANO, start,
                        AudioEngine.InstrumentSynth.effects(
                                AudioEngine.InstrumentSynth.piano(midi, 850),
                                project.reverb[0], project.delay[0])));
            }
            int[] guitar = project.midiNotesAt(StudioProject.TRACK_GUITAR, step);
            for (int note = 0; note < guitar.length; note++) {
                events.add(new Event(StudioProject.TRACK_GUITAR,
                        start + note * RATE * 28 / 1000,
                        AudioEngine.InstrumentSynth.effects(
                                AudioEngine.InstrumentSynth.guitar(guitar[note], 1250),
                                project.reverb[1], project.delay[1])));
            }
            for (int lane = 0; lane < 4; lane++) {
                if (project.drums[lane][step]) {
                    events.add(new Event(StudioProject.TRACK_DRUMS, start,
                            AudioEngine.InstrumentSynth.effects(AudioEngine.InstrumentSynth.drum(lane),
                                    project.reverb[2], project.delay[2])));
                }
            }
        }
        if (!project.voicePath.isEmpty()) {
            File voice = new File(project.voicePath);
            if (voice.isFile()) {
                float[] samples = readVoice(voice);
                events.add(new Event(StudioProject.TRACK_VOICE,
                        stepOffset(project.voiceStartStep, stepFrames, swingFrames),
                        AudioEngine.InstrumentSynth.effects(samples, project.reverb[3], project.delay[3])));
            }
        }
        return events;
    }

    private static float[] readVoice(File file) throws Exception {
        try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
            if (input.length() < 44) throw new IllegalArgumentException("Voice take is not a valid WAV file");
            if (!"RIFF".equals(readFourCc(input)) || !"WAVE".equals(readFourCcAt(input, 8)))
                throw new IllegalArgumentException("Voice take is not a RIFF WAV file");
            long dataOffset = -1;
            long dataLength = 0;
            int channels = 0;
            int rate = 0;
            int bits = 0;
            int encoding = 0;
            long cursor = 12;
            while (cursor + 8 <= input.length()) {
                input.seek(cursor);
                String id = readFourCc(input);
                long size = readUnsignedIntLE(input);
                long body = cursor + 8;
                if (body + size > input.length()) throw new IllegalArgumentException("Truncated voice WAV");
                if ("fmt ".equals(id) && size >= 16) {
                    input.seek(body);
                    encoding = readUnsignedShortLE(input);
                    channels = readUnsignedShortLE(input);
                    rate = (int) readUnsignedIntLE(input);
                    input.seek(body + 14);
                    bits = readUnsignedShortLE(input);
                } else if ("data".equals(id)) {
                    dataOffset = body;
                    dataLength = size;
                }
                cursor = body + size + (size & 1);
            }
            if (encoding != 1 || bits != 16 || (channels != 1 && channels != 2) || rate != RATE)
                throw new IllegalArgumentException("Voice take must be 44.1 kHz PCM16 mono or stereo");
            if (dataOffset < 0) throw new IllegalArgumentException("Voice WAV contains no audio data");
            long frames = dataLength / (channels * 2L);
            if (frames > (long) RATE * MAX_SECONDS)
                throw new IllegalArgumentException("Voice take exceeds the 10-minute export limit");
            float[] result = new float[(int) frames];
            input.seek(dataOffset);
            for (int frame = 0; frame < result.length; frame++) {
                int left = readSignedShortLE(input);
                if (channels == 2) {
                    int right = readSignedShortLE(input);
                    result[frame] = (left + right) / 65536f;
                } else result[frame] = left / 32768f;
            }
            return result;
        }
    }

    private static int stepOffset(int step, int baseFrames, int swingFrames) {
        return step * baseFrames + (step % 2 == 1 ? swingFrames : 0);
    }

    private static String safeName(String name) {
        String safe = name == null ? "" : name.trim().replaceAll("[^A-Za-z0-9_-]+", "_");
        return safe.isEmpty() ? "Demonic_Session" : safe.substring(0, Math.min(48, safe.length()));
    }

    private static String readFourCc(RandomAccessFile file) throws Exception {
        byte[] value = new byte[4];
        file.readFully(value);
        return new String(value, "US-ASCII");
    }

    private static String readFourCcAt(RandomAccessFile file, long offset) throws Exception {
        file.seek(offset);
        return readFourCc(file);
    }

    private static int readUnsignedShortLE(RandomAccessFile file) throws Exception {
        int low = file.read();
        int high = file.read();
        if (low < 0 || high < 0) throw new IllegalArgumentException("Truncated WAV header");
        return low | (high << 8);
    }

    private static int readSignedShortLE(RandomAccessFile file) throws Exception {
        return (short) readUnsignedShortLE(file);
    }

    private static long readUnsignedIntLE(RandomAccessFile file) throws Exception {
        long a = file.read();
        long b = file.read();
        long c = file.read();
        long d = file.read();
        if (d < 0) throw new IllegalArgumentException("Truncated WAV header");
        return a | (b << 8) | (c << 16) | (d << 24);
    }

    private static final class Event {
        final int track;
        final int start;
        final float[] samples;

        Event(int track, int start, float[] samples) {
            this.track = track;
            this.start = start;
            this.samples = samples;
        }
    }

    private static final class WavWriter {
        final File file;
        private final RandomAccessFile output;
        private final int channels;
        private long dataBytes;

        WavWriter(File file, int sampleRate, int channels) throws Exception {
            this.file = file;
            this.channels = channels;
            output = new RandomAccessFile(file, "rw");
            output.setLength(0);
            output.write(new byte[44]);
        }

        void write(float[] samples, int frames, boolean limit) throws Exception {
            byte[] bytes = new byte[frames * channels * 2];
            int target = 0;
            for (int index = 0; index < frames * channels; index++) {
                float value = samples[index];
                if (limit) value = (float) (value / (1.0 + Math.abs(value) * 0.45));
                int sample = (int) (Math.max(-1f, Math.min(1f, value)) * 32767);
                bytes[target++] = (byte) sample;
                bytes[target++] = (byte) (sample >>> 8);
            }
            if (dataBytes + bytes.length > 0x7fffffffL - 36)
                throw new IllegalStateException("WAV export exceeds the PCM format size limit");
            output.write(bytes);
            dataBytes += bytes.length;
        }

        void finish() throws Exception {
            output.seek(0);
            output.writeBytes("RIFF");
            writeIntLE(output, (int) (36 + dataBytes));
            output.writeBytes("WAVEfmt ");
            writeIntLE(output, 16);
            writeShortLE(output, 1);
            writeShortLE(output, channels);
            writeIntLE(output, RATE);
            writeIntLE(output, RATE * channels * 2);
            writeShortLE(output, channels * 2);
            writeShortLE(output, 16);
            output.writeBytes("data");
            writeIntLE(output, (int) dataBytes);
            output.close();
        }

        void closeQuietly() {
            try { output.close(); } catch (Exception ignored) { }
        }

        private static void writeIntLE(RandomAccessFile target, int value) throws Exception {
            target.write(value & 255);
            target.write((value >>> 8) & 255);
            target.write((value >>> 16) & 255);
            target.write((value >>> 24) & 255);
        }

        private static void writeShortLE(RandomAccessFile target, int value) throws Exception {
            target.write(value & 255);
            target.write((value >>> 8) & 255);
        }
    }
}
