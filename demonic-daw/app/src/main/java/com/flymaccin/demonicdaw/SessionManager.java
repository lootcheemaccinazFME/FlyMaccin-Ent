package com.flymaccin.demonicdaw;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import org.json.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;

public final class SessionManager {
  private static final long LIFE=8L*60*60*1000, IDLE=30L*60*1000;
  private static final String KEY_ALIAS="demonic_control_sessions_v1";
  private static final Set<String> ALLOWED=new HashSet<>(Arrays.asList("READ","EDIT","RECORD","RENDER","FILE","PUBLISH"));
  private final SharedPreferences prefs; private final SecureRandom rng=new SecureRandom(); private final KeyStore ks;
  public SessionManager(Context c){
    prefs=c.getSharedPreferences("demonic_control_sessions",Context.MODE_PRIVATE);
    try{ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);ensureKey();}catch(Exception e){throw new IllegalStateException("KEYSTORE_INIT_FAILED",e);}
  }
  public synchronized String requestPairing(String client,JSONArray requested)throws Exception{
    JSONArray scopes=validated(requested);String id=UUID.randomUUID().toString();long now=System.currentTimeMillis();
    JSONObject p=new JSONObject().put("id",id).put("client",client==null?"Controller":client).put("requestedScopes",scopes).put("createdAt",now).put("approved",false);
    prefs.edit().putString("p."+id,p.toString()).commit();
    return p.toString();
  }
  public synchronized String approvePairing(String pendingId,JSONArray approved)throws Exception{
    String raw=prefs.getString("p."+pendingId,null);if(raw==null)throw new SecurityException("PAIRING_NOT_FOUND");
    JSONObject p=new JSONObject(raw);JSONArray requested=p.getJSONArray("requestedScopes"),scopes=intersection(requested,validated(approved));
    if(scopes.length()==0)throw new SecurityException("PAIRING_NO_SCOPES_APPROVED");
    byte[] b=new byte[32];rng.nextBytes(b);String token=android.util.Base64.encodeToString(b,android.util.Base64.NO_WRAP|android.util.Base64.URL_SAFE);
    String id=UUID.randomUUID().toString();long now=System.currentTimeMillis();
    JSONObject secret=new JSONObject().put("token",token);
    JSONObject x=new JSONObject().put("id",id).put("client",p.optString("client","Controller")).put("secret",encrypt(secret.toString())).put("scopes",scopes)
      .put("createdAt",now).put("lastSeenAt",now).put("expiresAt",now+LIFE).put("revoked",false).put("localOnly",true);
    prefs.edit().putString("s."+id,x.toString()).remove("p."+pendingId).commit();
    return new JSONObject().put("id",id).put("token",token).put("scopes",scopes).put("expiresAt",now+LIFE).put("localOnly",true).toString();
  }
  /** Compatibility entry point now creates a pending request only. It never grants a session. */
  public synchronized String pair(String client,JSONArray requested)throws Exception{return requestPairing(client,requested);}
  public synchronized JSONObject authorize(String id,String token,String scope)throws Exception{
    if(!ALLOWED.contains(scope))throw new SecurityException("SCOPE_INVALID");
    String raw=prefs.getString("s."+id,null);if(raw==null)throw new SecurityException("SESSION_NOT_FOUND");JSONObject x=new JSONObject(raw);long now=System.currentTimeMillis();
    if(x.optBoolean("revoked"))throw new SecurityException("SESSION_REVOKED");if(now>x.optLong("expiresAt")||now-x.optLong("lastSeenAt")>IDLE)throw new SecurityException("SESSION_EXPIRED");
    JSONObject secret=new JSONObject(decrypt(x.getString("secret")));if(!constantTime(secret.optString("token"),token))throw new SecurityException("INVALID_TOKEN");
    if(!contains(x.optJSONArray("scopes"),scope))throw new SecurityException("SCOPE_DENIED");
    x.put("lastSeenAt",now);prefs.edit().putString("s."+id,x.toString()).commit();return publicView(x);
  }
  public synchronized boolean revoke(String id)throws Exception{String raw=prefs.getString("s."+id,null);if(raw==null)return false;JSONObject x=new JSONObject(raw).put("revoked",true).put("revokedAt",System.currentTimeMillis());return prefs.edit().putString("s."+id,x.toString()).commit();}
  public synchronized int revokeAll()throws Exception{int n=0;SharedPreferences.Editor e=prefs.edit();for(String k:prefs.getAll().keySet())if(k.startsWith("s.")){JSONObject x=new JSONObject(String.valueOf(prefs.getAll().get(k))).put("revoked",true).put("revokedAt",System.currentTimeMillis());e.putString(k,x.toString());n++;}e.commit();return n;}
  private JSONArray validated(JSONArray in){JSONArray out=new JSONArray();if(in!=null)for(int i=0;i<in.length();i++){String s=in.optString(i);if(ALLOWED.contains(s)&&!contains(out,s))out.put(s);}return out;}
  private JSONArray intersection(JSONArray a,JSONArray b){JSONArray out=new JSONArray();for(int i=0;i<a.length();i++){String s=a.optString(i);if(contains(b,s))out.put(s);}return out;}
  private static boolean contains(JSONArray a,String s){if(a!=null)for(int i=0;i<a.length();i++)if(s.equals(a.optString(i)))return true;return false;}
  private JSONObject publicView(JSONObject x)throws Exception{return new JSONObject().put("id",x.getString("id")).put("client",x.optString("client")).put("scopes",x.optJSONArray("scopes")).put("createdAt",x.optLong("createdAt")).put("lastSeenAt",x.optLong("lastSeenAt")).put("expiresAt",x.optLong("expiresAt")).put("revoked",x.optBoolean("revoked")).put("localOnly",x.optBoolean("localOnly",true));}
  private void ensureKey()throws Exception{if(ks.containsAlias(KEY_ALIAS))return;KeyGenerator g=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());g.generateKey();}
  private SecretKey key()throws Exception{return ((KeyStore.SecretKeyEntry)ks.getEntry(KEY_ALIAS,null)).getSecretKey();}
  private String encrypt(String plain)throws Exception{Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());byte[] iv=c.getIV(),ct=c.doFinal(plain.getBytes(StandardCharsets.UTF_8));return b64(iv)+"."+b64(ct);}
  private String decrypt(String packed)throws Exception{String[]p=packed.split("\\.",2);if(p.length!=2)throw new SecurityException("SESSION_SECRET_INVALID");Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,unb64(p[0])));return new String(c.doFinal(unb64(p[1])),StandardCharsets.UTF_8);}
  private static String b64(byte[]b){return android.util.Base64.encodeToString(b,android.util.Base64.NO_WRAP|android.util.Base64.URL_SAFE);}
  private static byte[] unb64(String s){return android.util.Base64.decode(s,android.util.Base64.NO_WRAP|android.util.Base64.URL_SAFE);}
  private static boolean constantTime(String a,String b){if(a==null||b==null)return false;byte[]x=a.getBytes(StandardCharsets.UTF_8),y=b.getBytes(StandardCharsets.UTF_8);int d=x.length^y.length;for(int i=0;i<Math.max(x.length,y.length);i++)d|=(i<x.length?x[i]:0)^(i<y.length?y[i]:0);return d==0;}
}
