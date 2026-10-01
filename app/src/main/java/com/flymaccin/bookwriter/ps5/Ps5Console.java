package com.flymaccin.bookwriter.ps5;
public final class Ps5Console {
  public final String host, raw;
  public Ps5Console(String host,String raw){this.host=host;this.raw=raw;}
  public boolean standby(){return raw.toLowerCase().contains("standby");}
  public boolean ready(){return raw.toLowerCase().contains("ready");}
}
