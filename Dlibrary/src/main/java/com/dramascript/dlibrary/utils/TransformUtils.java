package com.dramascript.dlibrary.utils;

import android.text.TextUtils;

import java.math.BigDecimal;

/*
 * Cread By DramaScript on 2019/3/19
 */
public class TransformUtils {

    /**
     * 保留double类型小数后两位，不四舍五入，直接取小数后两位 比如：10.1269 返回：10.12
     *
     * @param doubleValue
     * @return
     */
    public static String calculateProfit(double doubleValue) {
        // 保留4位小数
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.0000");
        String result = df.format(doubleValue);

        // 截取第一位
        String index = result.substring(0, 1);

        if (".".equals(index)) {
            result = "0" + result;
        }

        // 获取小数 . 号第一次出现的位置
        int inde = firstIndexOf(result, ".");

        // 字符串截断
        return result.substring(0, inde + 3);
    }

    /**
     * 查找字符串pattern在str中第一次出现的位置
     *
     * @param str
     * @param pattern
     * @return
     */
    public static int firstIndexOf(String str, String pattern) {
        for (int i = 0; i < (str.length() - pattern.length()); i++) {
            int j = 0;
            while (j < pattern.length()) {
                if (str.charAt(i + j) != pattern.charAt(j))
                    break;
                j++;
            }
            if (j == pattern.length())
                return i;
        }
        return -1;
    }

    public static int string2Int(String str){
        if (TextUtils.isEmpty(str)){
            return 0;
        }
        try {
            return Integer.valueOf(str);
        }catch (Exception e){
            return 0;
        }
    }

    public static double string2Double(String str){
        if (TextUtils.isEmpty(str)){
            return 0;
        }
        try {
            return Double.valueOf(str);
        }catch (Exception e){
            return 0;
        }
    }


    public static float string2Float(String str){
        if (TextUtils.isEmpty(str)){
            return 0;
        }
        try {
            return Float.valueOf(str);
        }catch (Exception e){
            return 0;
        }
    }

    public static long string2Long(String str){
        if (TextUtils.isEmpty(str)){
            return 0;
        }
        try {
            return Long.valueOf(str);
        }catch (Exception e){
            return 0;
        }
    }

    /**
     * double保留两位小数
     * @param d
     * @param carry 是否进位
     * @return
     */
    public static double doubleKeep2(double d,boolean carry){
        if (d==0){
            return 0;
        }
        try {
            BigDecimal a = null;
            if (carry){
                a = new BigDecimal(d).setScale(2,BigDecimal.ROUND_UP);
                return a.doubleValue();
            }else {
                a = new BigDecimal(d).setScale(2,BigDecimal.ROUND_DOWN);
                return a.doubleValue();
            }
        }catch (Exception e){
            return 0.00;
        }
    }

    /**
     * float保留两位小数
     * @param carry 是否进位
     * @param d
     * @return
     */
    public static float floatKeep2(float d,boolean carry){
        if (d==0){
            return 0;
        }
        try {
            BigDecimal a = null;
            if (carry){
                a = new BigDecimal(d).setScale(2,BigDecimal.ROUND_UP);
                return a.floatValue();
            }else {
                a = new BigDecimal(d).setScale(2,BigDecimal.ROUND_DOWN);
                return a.floatValue();
            }
        }catch (Exception e){
            return 0.00f;
        }
    }

    /**
     * String中double保留两位小数
     * @param d
     * @return
     */
    public static String doubleKeep2ToString(double d){
        try {
            return String.format("%.2f",d);
        }catch (Exception e){
            return "0.00";
        }
    }

    /**
     * String中double不保留小数
     * @param d
     * @return
     */
    public static String doubleKeep0ToString(double d){
        try {
            return String.format("%.0f",d);
        }catch (Exception e){
            return "0";
        }
    }

}
