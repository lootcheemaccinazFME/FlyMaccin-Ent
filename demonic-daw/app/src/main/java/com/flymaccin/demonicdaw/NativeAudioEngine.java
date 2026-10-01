package com.flymaccin.demonicdaw;

public final class NativeAudioEngine {
    static { System.loadLibrary("demonic_audio"); }
    private NativeAudioEngine() {}

    public static native boolean nativeStart();
    public static native void nativeStop();
    public static native int nativeLoadSoundFont(String path);
    public static native void nativeClearSampleBank();
    public static native void nativeClearSampleChannel(int channel);
    public static native boolean nativeAddWavRegion(String path, int channel, int loKey, int hiKey, int loVel, int hiVel,
            int keyCenter, float volumeDb, float pan, float tuneCents, int transpose,
            int loopMode, int loopStart, int loopEnd, int offset, int end, float releaseSeconds);
    public static native void nativeNoteOn(int channel, int key, int velocity);
    public static native void nativeNoteOff(int channel, int key);
    public static native void nativeProgramSelect(int channel, int soundFontId, int bank, int preset);
    public static native void nativeCc(int channel, int controller, int value);
    public static native void nativePitchBend(int channel, int value);
    public static native void nativeSetGain(float gain);
    public static native void nativeSetChannelMix(int channel, float gain, float pan, boolean mute, boolean solo);
    public static native void nativeSetReverb(float room, float damp, float width, float level);
    public static native void nativeSetChorus(int voices, float level, float speed, float depth);
}
