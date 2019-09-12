package com.dramascript.dlibrary.security;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
 * Cread By DramaScript on 2019/3/5
 */
public class AesEncrypt {

    private static AesEncrypt instance;

    private AesEncrypt() {
    }

    public static AesEncrypt getInstance() {
        if (instance == null) {
            synchronized (AesEncrypt.class) {
                if (instance == null) {
                    instance = new AesEncrypt();
                }
            }
        }
        return instance;
    }

    // 解密
    public static String decryptShoot(String src, String key) {
        try {
            // 判断Key是否正确
            if (key == null || key.length() != 32) return null;
            byte[] raw = key.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            IvParameterSpec iv = new IvParameterSpec(new byte[cipher.getBlockSize()]);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = Base64.decode(src, Base64.NO_WRAP);// 先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
                | BadPaddingException | IllegalArgumentException ignored) {
            return null;
        }
    }

    //解密
    public static String decrypt(String sSrc, String key, String ivs) {
        try {
            byte[] raw = key.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivs.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = Base64.decode(sSrc, Base64.NO_WRAP);// 先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original, "utf-8");

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            return null;
        }
    }

    //解密
    public String decrypt(byte[] key, byte[] source) {
        if (key == null || key.length != 32) return null;
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");//"算法/模式/补码方式"
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(source));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
                | BadPaddingException | NoSuchProviderException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    //加密
    public byte[] encrypt(byte[] key, String source) {
        return encrypt(key, source.getBytes());
    }

    //加密
    public byte[] encrypt(byte[] key, byte[] source) {
        if (key == null || key.length != 32) return null;
        byte[] arr = null;
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");//"算法/模式/补码方式"
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            arr = cipher.doFinal(source);
        } catch (IllegalArgumentException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | BadPaddingException | NoSuchProviderException
                | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return arr;
    }
}
