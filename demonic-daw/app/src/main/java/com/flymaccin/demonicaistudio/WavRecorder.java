package com.flymaccin.demonicaistudio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

final class WavRecorder {
    private static final int SAMPLE_RATE = 44100;
    private AudioRecord recorder;
    private Thread worker;
    private volatile boolean running;
    private File output;
    private String lastError = "";

    boolean start(File file) {
        if (running) return false;
        try {
            int minimum = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            int bufferSize = Math.max(minimum, SAMPLE_RATE / 5) * 2;
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) throw new IllegalStateException("Microphone unavailable");
            output = file;
            lastError = "";
            recorder.startRecording();
            running = true;
            worker = new Thread(() -> capture(bufferSize), "demonic-wav-recorder");
            worker.start();
            return true;
        } catch (Exception error) {
            lastError = error.getClass().getSimpleName() + ": " + error.getMessage();
            release();
            return false;
        }
    }

    File stop() {
        running = false;
        try {
            if (recorder != null) recorder.stop();
        } catch (Exception ignored) {
        }
        if (worker != null) {
            try { worker.join(1800); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
        release();
        return output != null && output.exists() && output.length() > 44 ? output : null;
    }

    boolean isRunning() { return running; }
    String getLastError() { return lastError; }

    private void capture(int bufferSize) {
        long dataBytes = 0;
        byte[] buffer = new byte[bufferSize];
        try (FileOutputStream stream = new FileOutputStream(output)) {
            stream.write(new byte[44]);
            while (running) {
                int count = recorder.read(buffer, 0, buffer.length);
                if (count > 0) {
                    stream.write(buffer, 0, count);
                    dataBytes += count;
                }
            }
            stream.flush();
            writeHeader(output, dataBytes);
        } catch (Exception error) {
            lastError = error.getClass().getSimpleName() + ": " + error.getMessage();
            if (output != null) output.delete();
        }
    }

    private void release() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        worker = null;
    }

    private static void writeHeader(File file, long dataBytes) throws Exception {
        try (RandomAccessFile target = new RandomAccessFile(file, "rw")) {
            target.seek(0);
            target.writeBytes("RIFF");
            writeIntLE(target, (int) (36 + dataBytes));
            target.writeBytes("WAVEfmt ");
            writeIntLE(target, 16);
            writeShortLE(target, (short) 1);
            writeShortLE(target, (short) 1);
            writeIntLE(target, SAMPLE_RATE);
            writeIntLE(target, SAMPLE_RATE * 2);
            writeShortLE(target, (short) 2);
            writeShortLE(target, (short) 16);
            target.writeBytes("data");
            writeIntLE(target, (int) dataBytes);
        }
    }

    private static void writeIntLE(RandomAccessFile file, int value) throws Exception {
        file.write(value & 0xff);
        file.write((value >> 8) & 0xff);
        file.write((value >> 16) & 0xff);
        file.write((value >> 24) & 0xff);
    }

    private static void writeShortLE(RandomAccessFile file, short value) throws Exception {
        file.write(value & 0xff);
        file.write((value >> 8) & 0xff);
    }
}
