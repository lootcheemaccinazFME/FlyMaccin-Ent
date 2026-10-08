package com.flymaccin.bookwriter.ps5;
import android.content.Context;
import java.util.*;
public final class Ps5Phase1Manager {
  private final SecureConsoleStore store;
  public Ps5Phase1Manager(Context c){store=new SecureConsoleStore(c);}
  public List<Ps5Console> discover(int timeoutMs) throws Exception {
    List<Ps5Console> out=new ArrayList<>();
    for(String r:Ps5Discovery.scan(timeoutMs)){int n=r.indexOf('\n');out.add(new Ps5Console(n>0?r.substring(0,n):r,n>0?r.substring(n+1):""));}
    return out;
  }
  public void saveRegistration(String host,String accountId,String registKey,String rpKey,String rpKeyType) throws Exception {
    store.put("host",host); store.put("account_id",accountId); store.put("regist_key",registKey); store.put("rp_key",rpKey); store.put("rp_key_type",rpKeyType);
  }
  public boolean hasRegistration(){try{return store.get("host")!=null&&store.get("regist_key")!=null&&store.get("rp_key")!=null;}catch(Exception e){return false;}}
  public String host(){try{return store.get("host");}catch(Exception e){return null;}}
  public String registrationStatus(){return hasRegistration()?"REGISTERED CREDENTIAL SET PRESENT":"REGISTRATION REQUIRED";}
  public String wakeStatus(){return hasRegistration()?"READY FOR NATIVE WAKE CORE":"BLOCKED: REGISTER FIRST";}
  public String sessionStatus(){return hasRegistration()?"READY FOR SESSION CORE":"BLOCKED: REGISTER FIRST";}
}
