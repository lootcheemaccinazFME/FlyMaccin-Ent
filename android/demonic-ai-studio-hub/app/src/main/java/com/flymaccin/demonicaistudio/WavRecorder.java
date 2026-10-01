package com.flymaccin.demonicaistudio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

/** Microphone capture to a standards-compliant local mono PCM WAV file. */
final class WavRecorder {
    private static final int SAMPLE_RATE = 44100;
    private final Object lock = new Object();
    private volatile boolean running;
    private volatile String lastError = "";
    private AudioRecord audioRecord;
    private Thread captureThread;
    private File output;

    boolean start(File destination) {
        synchronized (lock) {
            if (running || (captureThread != null && captureThread.isAlive())) return false;
            AudioRecord created = null;
            try {
                int minimum = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                if (minimum <= 0) throw new IllegalStateException("Microphone input is unavailable");
                int bufferBytes = Math.max(minimum * 2, SAMPLE_RATE / 5);
                File parent = destination.getParentFile();
                if (parent == null || (!parent.isDirectory() && !parent.mkdirs()))
                    throw new IllegalStateException("Cannot create take folder");
                created = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferBytes);
                if (created.getState() != AudioRecord.STATE_INITIALIZED)
                    throw new IllegalStateException("Microphone could not be initialized");
                output = destination;
                lastError = "";
                audioRecord = created;
                created.startRecording();
                running = true;
                AudioRecord active = created;
                captureThread = new Thread(() -> capture(active, bufferBytes), "studio-microphone-capture");
                captureThread.start();
                return true;
            } catch (Exception error) {
                running = false;
                lastError = describe(error);
                if (created != null) created.release();
                audioRecord = null;
                captureThread = null;
                return false;
            }
        }
    }

    File stop() {
        Thread worker;
        AudioRecord source;
        File completed;
        synchronized (lock) {
            running = false;
            worker = captureThread;
            source = audioRecord;
            completed = output;
        }
        if (source != null) {
            try { source.stop(); } catch (Exception ignored) { }
        }
        if (worker != null) {
            try {
                worker.join(5000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        synchronized (lock) {
            if (worker != null && worker.isAlive()) {
                lastError = "Microphone did not stop";
                return null;
            }
            if (captureThread == worker) captureThread = null;
            if (audioRecord == source) audioRecord = null;
        }
        return completed != null && completed.isFile() && completed.length() > 44 ? completed : null;
    }

    boolean isRunning() {
        return running;
    }

    String getLastError() {
        return lastError;
    }

    private void capture(AudioRecord source, int bufferBytes) {
        long dataBytes = 0;
        byte[] buffer = new byte[bufferBytes];
        File destination;
        synchronized (lock) {
            destination = output;
        }
        try (FileOutputStream stream = new FileOutputStream(destination)) {
            stream.write(new byte[44]);
            while (running) {
                int count = source.read(buffer, 0, buffer.length);
                if (count > 0) {
                    if (dataBytes + count > 0x7fffffffL - 36)
                        throw new IllegalStateException("Take is too large for a WAV file");
                    stream.write(buffer, 0, count);
                    dataBytes += count;
                } else if (count < 0 && running) {
                    throw new IllegalStateException("Microphone read failed (" + count + ")");
                }
            }
            stream.flush();
            writeHeader(destination, dataBytes);
        } catch (Exception error) {
            lastError = describe(error);
            running = false;
            if (destination != null) destination.delete();
        } finally {
            source.release();
            synchronized (lock) {
                if (audioRecord == source) audioRecord = null;
            }
        }
    }

    private static void writeHeader(File file, long dataBytes) throws Exception {
        try (RandomAccessFile target = new RandomAccessFile(file, "rw")) {
            target.seek(0);
            target.writeBytes("RIFF");
            writeIntLE(target, (int) (36 + dataBytes));
            target.writeBytes("WAVEfmt ");
            writeIntLE(target, 16);
            writeShortLE(target, 1);
            writeShortLE(target, 1);
            writeIntLE(target, SAMPLE_RATE);
            writeIntLE(target, SAMPLE_RATE * 2);
            writeShortLE(target, 2);
            writeShortLE(target, 16);
            target.writeBytes("data");
            writeIntLE(target, (int) dataBytes);
        }
    }

    private static void writeIntLE(RandomAccessFile target, int value) throws Exception {
        target.write(value & 0xff);
        target.write((value >>> 8) & 0xff);
        target.write((value >>> 16) & 0xff);
        target.write((value >>> 24) & 0xff);
    }

    private static void writeShortLE(RandomAccessFile target, int value) throws Exception {
        target.write(value & 0xff);
        target.write((value >>> 8) & 0xff);
    }

    private static String describe(Exception error) {
        String detail = error.getMessage();
        return error.getClass().getSimpleName() + (detail == null ? "" : ": " + detail);
    }
}
