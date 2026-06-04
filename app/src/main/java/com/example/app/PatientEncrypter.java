package com.example.app;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import android.util.Base64;


// PatientEncrypter class using AES-256-GCM
public class PatientEncrypter {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256, TAG_BIT = 128, IV_BYTE = 12;

    public static SecretKey generateKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(KEY_SIZE);
        return kg.generateKey();
    }

    public static String encrypt(String plain, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_BYTE];
        new SecureRandom().nextBytes(iv); // Unique IV for every encryption
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BIT, iv));
        byte[] cipherText = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
        ByteBuffer bb = ByteBuffer.allocate(iv.length + cipherText.length);
        return Base64.encodeToString(bb.put(iv).put(cipherText).array(), Base64.DEFAULT);
    }

    public static String decrypt(String encrypted, SecretKey key) throws Exception {
        byte[] combined = Base64.decode(encrypted, Base64.DEFAULT);
        ByteBuffer bb = ByteBuffer.wrap(combined);
        byte[] iv = new byte[IV_BYTE];
        bb.get(iv);
        byte[] cipherText = new byte[bb.remaining()];
        bb.get(cipherText);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BIT, iv));
        return new String(c.doFinal(cipherText), StandardCharsets.UTF_8);
    }
}
