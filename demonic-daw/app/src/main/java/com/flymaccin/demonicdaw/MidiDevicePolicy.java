package com.flymaccin.demonicdaw;
public final class MidiDevicePolicy {
 public static int clampChannel(int channel){if(channel<0||channel>15)throw new IllegalArgumentException("MIDI_CHANNEL");return channel;}
 public static int clampData7(int value){return Math.max(0,Math.min(127,value));}
 public static long normalizeTimestampNanos(long timestamp,long now){return timestamp<=0?now:Math.max(now-5_000_000L,timestamp);}
 public static boolean isMusicalCategory(int channel){return false;} // channel numbers never imply instrument category
 private MidiDevicePolicy(){}
}