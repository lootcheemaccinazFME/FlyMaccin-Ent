package com.flymaccin.demonicdaw;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.*;
import java.security.SecureRandom;
import java.util.*;

public final class SessionManager {
  private static final long LIFE=8L*60*60*1000, IDLE=30L*60*1000;
  private final SharedPreferences prefs; private final SecureRandom rng=new SecureRandom();
  public SessionManager(Context c){prefs=c.getSharedPreferences("demonic_control_sessions",Context.MODE_PRIVATE);}
  public synchronized String pair(String client,JSONArray requested)throws Exception{
    Set<String> allowed=new HashSet<>(Arrays.asList("READ","EDIT","RECORD","RENDER","FILE","PUBLISH"));JSONArray scopes=new JSONArray();
    for(int i=0;i<requested.length();i++){String s=requested.optString(i);if(allowed.contains(s))scopes.put(s);}
    byte[] b=new byte[32];rng.nextBytes(b);String token=android.util.Base64.encodeToString(b,android.util.Base64.NO_WRAP|android.util.Base64.URL_SAFE);
    String id=UUID.randomUUID().toString();long now=System.currentTimeMillis();
    JSONObject x=new JSONObject().put("id",id).put("client",client).put("token",token).put("scopes",scopes).put("createdAt",now).put("lastSeenAt",now).put("expiresAt",now+LIFE).put("revoked",false);
    prefs.edit().putString("s."+id,x.toString()).apply();return x.toString();
  }
  public synchronized JSONObject authorize(String id,String token,String scope)throws Exception{
    String raw=prefs.getString("s."+id,null);if(raw==null)throw new SecurityException("SESSION_NOT_FOUND");JSONObject x=new JSONObject(raw);long now=System.currentTimeMillis();
    if(x.optBoolean("revoked"))throw new SecurityException("SESSION_REVOKED");if(now>x.optLong("expiresAt")||now-x.optLong("lastSeenAt")>IDLE)throw new SecurityException("SESSION_EXPIRED");
    if(!constantTime(x.optString("token"),token))throw new SecurityException("INVALID_TOKEN");boolean ok=false;JSONArray a=x.optJSONArray("scopes");if(a!=null)for(int i=0;i<a.length();i++)if(scope.equals(a.optString(i)))ok=true;if(!ok)throw new SecurityException("SCOPE_DENIED");
    x.put("lastSeenAt",now);prefs.edit().putString("s."+id,x.toString()).apply();return x;
  }
  public synchronized boolean revoke(String id)throws Exception{String raw=prefs.getString("s."+id,null);if(raw==null)return false;JSONObject x=new JSONObject(raw).put("revoked",true);prefs.edit().putString("s."+id,x.toString()).apply();return true;}
  private static boolean constantTime(String a,String b){if(a==null||b==null)return false;byte[]x=a.getBytes(),y=b.getBytes();int d=x.length^y.length;for(int i=0;i<Math.max(x.length,y.length);i++)d|=(i<x.length?x[i]:0)^(i<y.length?y[i]:0);return d==0;}
}
