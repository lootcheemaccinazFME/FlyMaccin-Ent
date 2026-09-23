package com.flymaccin.bookwriter.ps5;
public final class Ps5Session {
  public enum State { UNREGISTERED, READY, CONNECTING, CONNECTED, FAILED }
  private State state=State.UNREGISTERED;
  public State state(){return state;}
  public void registrationAvailable(){state=State.READY;}
  public void connecting(){state=State.CONNECTING;}
  public void connected(){state=State.CONNECTED;}
  public void failed(){state=State.FAILED;}
}
