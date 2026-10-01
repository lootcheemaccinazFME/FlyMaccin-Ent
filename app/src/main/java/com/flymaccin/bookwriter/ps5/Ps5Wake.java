package com.flymaccin.bookwriter.ps5;
public final class Ps5Wake {
  private Ps5Wake(){}
  public static Result request(String host,String registKey){
    if(host==null||host.isBlank())return new Result(false,"Missing console host");
    if(registKey==null||registKey.isBlank())return new Result(false,"Missing registration key");
    return new Result(false,"Native Chiaki-compatible wake packet encoder not integrated yet");
  }
  public static final class Result{public final boolean sent;public final String message;Result(boolean s,String m){sent=s;message=m;}}
}
