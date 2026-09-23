package com.flymaccin.bookwriter.ps5;

public final class Ps5Native {
  static { System.loadLibrary("fme_ps5"); }

  public interface RegistrationCallback {
    void onRegistrationResult(boolean ok, String message, String registKey, String rpKey, String rpKeyType);
  }

  public native String nativeStatus();
  public native boolean hasChiaki();
  public native boolean mediaPipelineReady();
  public native boolean startRegistration(String host, String accountId, int pin, RegistrationCallback callback);
}
