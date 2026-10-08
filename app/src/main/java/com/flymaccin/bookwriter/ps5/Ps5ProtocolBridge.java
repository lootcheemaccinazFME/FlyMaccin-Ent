package com.flymaccin.bookwriter.ps5;

public final class Ps5ProtocolBridge {
  public interface RegistrationCallback { void onResult(RegistrationResult result); }

  public static final class RegistrationResult {
    public final boolean ok;
    public final String message, registKey, rpKey, rpKeyType;
    RegistrationResult(boolean ok,String message,String registKey,String rpKey,String rpKeyType){
      this.ok=ok; this.message=message; this.registKey=registKey; this.rpKey=rpKey; this.rpKeyType=rpKeyType;
    }
  }

  private final Ps5Native nativeCore = new Ps5Native();

  public boolean registerAsync(Ps5RegistrationRequest request, RegistrationCallback callback){
    String error=request.validate();
    if(error!=null){
      callback.onResult(new RegistrationResult(false,error,null,null,null));
      return false;
    }
    final int pin;
    try { pin=Integer.parseInt(request.pin); }
    catch(NumberFormatException e){
      callback.onResult(new RegistrationResult(false,"Invalid Link Device PIN",null,null,null));
      return false;
    }
    boolean started=nativeCore.startRegistration(request.host,request.accountId,pin,
      (ok,message,registKey,rpKey,rpKeyType)->callback.onResult(
        new RegistrationResult(ok,message,registKey,rpKey,rpKeyType)));
    if(!started) callback.onResult(new RegistrationResult(false,
      "Native registration did not start. Check PSN AccountID format and native core.",null,null,null));
    return started;
  }

  public Ps5Wake.Result wake(String host,String registKey){return Ps5Wake.request(host,registKey);}
  public String connect(){return "Chiaki session core not linked yet";}
}
