package com.dramascript.dlibrary.security;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * Cread By DramaScript on 2019/3/5
 */
public class MD5 {
    public static String encrypt(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");//MessageDigest专门用于加密的类
            byte[] result = messageDigest.digest(str.getBytes()); //得到加密后的字符组数
            return toHexString(result);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getMd5Hash(String fileName) throws NoSuchAlgorithmException {
        InputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
            byte[] buffer = new byte[1024];
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            int numRead;
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            return toHexString(md5.digest());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String toHexString(byte[] result) {
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            int num = b & 0xff; // 这里的是为了将原本是byte型的数向上提升为int型，从而使得原本的负数转为了正数
            String hex = Integer.toHexString(num); //这里将int型的数直接转换成16进制表示,用16进制参数表示的无符号整数值的字符串表示形式。
            //16进制可能是为1的长度，这种情况下，需要在前面补0，
            if (hex.length() == 1) {
                sb.append(0);
            }
            sb.append(hex);
        }
        //默认是32位加密,若要16位,则sb.toString().substring(8,24)
        return sb.toString();
    }
}
