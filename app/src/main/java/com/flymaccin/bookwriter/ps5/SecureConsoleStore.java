package com.flymaccin.bookwriter.ps5;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class SecureConsoleStore {
  private static final String ALIAS="fme_ps5_console_key";
  private final SharedPreferences prefs;
  public SecureConsoleStore(Context c){prefs=c.getSharedPreferences("fme_ps5_secure",Context.MODE_PRIVATE);}
  private SecretKey key() throws Exception {
    KeyStore ks=KeyStore.getInstance("AndroidKeyStore"); ks.load(null);
    if(!ks.containsAlias(ALIAS)){
      KeyGenerator kg=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
      kg.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
      kg.generateKey();
    }
    return ((KeyStore.SecretKeyEntry)ks.getEntry(ALIAS,null)).getSecretKey();
  }
  public void put(String name,String value) throws Exception {
    Cipher c=Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE,key());
    String blob=Base64.encodeToString(c.getIV(),Base64.NO_WRAP)+"."+Base64.encodeToString(c.doFinal(value.getBytes(StandardCharsets.UTF_8)),Base64.NO_WRAP);
    prefs.edit().putString(name,blob).apply();
  }
  public String get(String name) throws Exception {
    String blob=prefs.getString(name,null); if(blob==null)return null; String[] p=blob.split("\\.",2);
    Cipher c=Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(p[0],Base64.NO_WRAP)));
    return new String(c.doFinal(Base64.decode(p[1],Base64.NO_WRAP)),StandardCharsets.UTF_8);
  }
}
