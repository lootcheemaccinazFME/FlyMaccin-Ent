package com.flymaccin.demonicaistudio;

final class UnifiedTransport {
    static final int PPQ = 960;
    private double bpm = 96.0;
    private int numerator = 4;
    private int denominator = 4;
    private long tick = 0;
    private long loopStart = 0;
    private long loopEnd = PPQ * 16L;
    private boolean looping = true;
    private int snapDivision = 16;

    double bpm() { return bpm; }
    long tick() { return tick; }
    void setBpm(double value) { bpm = Math.max(30.0, Math.min(300.0, value)); }
    void setTimeSignature(int num, int den) { numerator = Math.max(1, num); denominator = Math.max(1, den); }
    void seek(long value) { tick = Math.max(0, value); }
    void setLoop(long start, long end, boolean enabled) {
        loopStart = Math.max(0, start);
        loopEnd = Math.max(loopStart + 1, end);
        looping = enabled;
    }
    void setSnapDivision(int division) { snapDivision = Math.max(1, division); }
    long snap(long value) {
        long grid = Math.max(1, PPQ * 4L / snapDivision);
        return Math.round(value / (double) grid) * grid;
    }
    long advanceFrames(int frames, int sampleRate) {
        double quarterNotes = frames / (double) sampleRate * bpm / 60.0;
        tick += Math.max(0, Math.round(quarterNotes * PPQ));
        if (looping && tick >= loopEnd) tick = loopStart + (tick - loopEnd);
        return tick;
    }
    long millisForTicks(long ticks) { return Math.round((ticks / (double) PPQ) * 60000.0 / bpm); }
    int numerator() { return numerator; }
    int denominator() { return denominator; }
}