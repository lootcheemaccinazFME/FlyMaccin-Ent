package com.flymaccin.bookwriter.ps5;
public final class Ps5ProtocolBridge {
  public static final class RegistrationResult {
    public final boolean ok; public final String message, registKey, rpKey, rpKeyType;
    RegistrationResult(boolean ok,String message,String registKey,String rpKey,String rpKeyType){
      this.ok=ok;this.message=message;this.registKey=registKey;this.rpKey=rpKey;this.rpKeyType=rpKeyType;
    }
  }
  public RegistrationResult register(Ps5RegistrationRequest request){
    String error=request.validate();
    if(error!=null)return new RegistrationResult(false,error,null,null,null);
    return new RegistrationResult(false,"Chiaki registration core not linked yet",null,null,null);
  }
  public Ps5Wake.Result wake(String host,String registKey){return Ps5Wake.request(host,registKey);}
  public String connect(){return "Chiaki session core not linked yet";}
}
