package com.flymaccin.bookwriter.ps5;
public final class Ps5Native {
  static { System.loadLibrary("fme_ps5"); }
  public native String nativeStatus();
}
