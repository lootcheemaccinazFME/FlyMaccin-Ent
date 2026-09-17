package com.flymaccin.bookwriter;

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

final class SecureVoiceStore {
    private static final String ALIAS = "demonic_voice_endpoint_key";
    private static final String PREFS = "demonic_voice_secure";
    private static final String ENDPOINT = "endpoint";
    private static final String TOKEN = "token";
    private static final String IV = "iv";

    private SecureVoiceStore() {}

    static void save(Context context, String endpoint, String token) throws Exception {
        String payload = (endpoint == null ? "" : endpoint.trim()) + "\n" + (token == null ? "" : token);
        SecretKey key = getOrCreateKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(TOKEN, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(IV, Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP))
                .apply();
    }

    static String[] load(Context context) throws Exception {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String encoded = p.getString(TOKEN, null);
        String iv = p.getString(IV, null);
        if (encoded == null || iv == null) return null;
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        SecretKey key = (SecretKey) ks.getKey(ALIAS, null);
        if (key == null) return null;
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)));
        String payload = new String(cipher.doFinal(Base64.decode(encoded, Base64.NO_WRAP)), StandardCharsets.UTF_8);
        int split = payload.indexOf('\n');
        if (split < 0) return new String[]{payload, ""};
        return new String[]{payload.substring(0, split), payload.substring(split + 1)};
    }

    static boolean has(Context context) {
        try {
            String[] cfg = load(context);
            return cfg != null && cfg.length > 0 && cfg[0] != null && !cfg[0].trim().isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    static void clear(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }

    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        if (ks.containsAlias(ALIAS)) return (SecretKey) ks.getKey(ALIAS, null);
        KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        kg.init(new KeyGenParameterSpec.Builder(ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return kg.generateKey();
    }
}