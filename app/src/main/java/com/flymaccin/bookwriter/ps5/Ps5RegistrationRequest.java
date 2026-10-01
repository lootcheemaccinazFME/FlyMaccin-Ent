package com.flymaccin.bookwriter.ps5;
public final class Ps5RegistrationRequest {
  public final String host, accountId, pin;
  public Ps5RegistrationRequest(String host,String accountId,String pin){
    this.host=host==null?"":host.trim(); this.accountId=accountId==null?"":accountId.trim(); this.pin=pin==null?"":pin.trim();
  }
  public String validate(){
    if(host.isEmpty()) return "Console IP/host is required";
    if(accountId.isEmpty()) return "PSN AccountID is required";
    if(!pin.matches("\\d{8}")) return "PS5 Link Device PIN must be 8 digits";
    return null;
  }
}
