package com.flymaccin.demonicaistudio;

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

final class DemonicSecureConfig {
    private static final String ALIAS = "demonic_song_ai_config_key";
    private static final String PREFS = "demonic_song_ai_secure";
    private static final String VALUE = "payload";
    private static final String IV = "iv";

    static final class Config {
        final String lyricEndpoint;
        final String lyricToken;
        final String voiceEndpoint;
        final String voiceToken;
        Config(String lyricEndpoint, String lyricToken, String voiceEndpoint, String voiceToken) {
            this.lyricEndpoint = lyricEndpoint;
            this.lyricToken = lyricToken;
            this.voiceEndpoint = voiceEndpoint;
            this.voiceToken = voiceToken;
        }
    }

    private DemonicSecureConfig() {}

    static void save(Context context, Config config) throws Exception {
        String payload = safe(config.lyricEndpoint) + "\n" + safe(config.lyricToken) + "\n"
                + safe(config.voiceEndpoint) + "\n" + safe(config.voiceToken);
        SecretKey key = getOrCreateKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(VALUE, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(IV, Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP))
                .apply();
    }

    static Config load(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String encoded = prefs.getString(VALUE, null);
            String iv = prefs.getString(IV, null);
            if (encoded == null || iv == null) return new Config("", "", "", "");
            KeyStore store = KeyStore.getInstance("AndroidKeyStore");
            store.load(null);
            SecretKey key = (SecretKey) store.getKey(ALIAS, null);
            if (key == null) return new Config("", "", "", "");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)));
            String raw = new String(cipher.doFinal(Base64.decode(encoded, Base64.NO_WRAP)), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\n", -1);
            return new Config(part(parts,0), part(parts,1), part(parts,2), part(parts,3));
        } catch (Exception ignored) {
            return new Config("", "", "", "");
        }
    }

    static void clear(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }

    private static String part(String[] parts, int index) { return index < parts.length ? parts[index] : ""; }
    private static String safe(String value) { return value == null ? "" : value.trim(); }

    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        if (store.containsAlias(ALIAS)) return (SecretKey) store.getKey(ALIAS, null);
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return generator.generateKey();
    }
}