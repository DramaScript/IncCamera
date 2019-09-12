package com.dramascript.dlibrary.utils;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Cread By DramaScript on 2019/3/19
 */
public class StringCheckUtils {

    /**
     * 匹配密码格式(包含大小写英文，阿拉伯数字， 6-12位)
     */
    public static final String PASSWORD_LEGAL = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,12}$";

    /**
     * 匹配手机号
     */
    public static final String PHONE_LEGAL = "^(13[0-9]|14[0-9]|15[0-9]|17[0-9]|18[0-9])\\d{8}$";

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str) || "null".equals(str);
    }

    /**
     * 检查输入的字符占几个字节
     * @param isUp false 小于几个字节  true大于几个字节
     * @param size  字节大小上线
     * @param content 输入的内容
     * @return
     */
    public static boolean checkStringSize(boolean isUp,int size,String content){
        int length = 0;
        try {
            length = content.getBytes("GB2312").length;// 汉子就是2个字节了
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            length =0;
        }
        if (length!=0){
            if (isUp){
                if (length>size){
                    return true;
                }else {
                    return false;
                }
            }else {
                if (length<size){
                    return true;
                }else {
                    return false;
                }
            }
        }else {
            return false;
        }
    }

    /**
     * 判断密码是否6-16位，包含数字但不是纯数字，除去特殊字符，并且包含大写字母和小写字母
     */
    public static boolean checkPwd(String pwd) {
        if (isNumber(pwd)){
            return false;
        }
        if (!isContainNumber(pwd)){
            return false;
        }
        if (isHanzi(pwd)){
            return false;
        }
        if (pwd.length()<6||pwd.length()>16){
            return false;
        }
        if (containSpace(pwd)){
            return false;
        }
        if (EmojiFilter.containsEmoji(pwd)){
            return false;
        }
        if (!isUpLowCase(pwd)){
            return false;
        }
        return true;
    }

    /**
     * 判断字符串中是否包括了大写和小写字母一起，前提是已经判断了里面包含了字母
     * @param str
     * @return
     */
    public static boolean isUpLowCase(String str){
        boolean isUpCase = false;//定义一个boolean值，用来表示是否包含大字母
        boolean isLowCase = false;//定义一个boolean值，用来表示是否包含小字母
        for (int i = 0; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i))) {
                isUpCase = true;
            }
            if (Character.isLowerCase(str.charAt(i))) {
                isLowCase = true;
            }
        }
        return isLowCase&&isUpCase;
    }

    /**
     * 判断是否含有特殊字符
     *
     * @param str
     * @return true为包含，false为不包含
     */
    public static boolean isSpecialChar(String str) {
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find()&&!EmojiFilter.containsEmoji(str);
    }

    /**
     * 判断是否含有特殊字符 英文输入法的
     *
     * @param str
     * @return true为包含，false为不包含
     */
    public static boolean isSpecialCharForEnglish(String str) {
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~-]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find()&&!EmojiFilter.containsEmoji(str);
    }

    /**
     * 判断输入的内容为金额
     */
    public static boolean isMoney(String msg) {
        if (isEmpty(msg)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$");
        Matcher isNum = pattern.matcher(msg);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 判断字符串是否为邮箱
     */
    public static boolean isEmail(String str) {
        Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher mc = pattern.matcher(str);
        return mc.matches();
    }

    /**
     * 验证电话号码  简单判断  最好服务器判断
     * @param phoneNumber
     * @return
     */
    public static boolean isMobileNO(String phoneNumber) {
        Pattern p = Pattern.compile("^1[0-9]{10}$");
        Matcher m = p.matcher(phoneNumber);
        if (m.matches() == true) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断List是否为空
     */
    public static boolean listIsNullOrEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 判断map是否为空
     */
    public static boolean mapIsNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty() || map.size() == 0;
    }

    /**
     * 判断字符是否纯数字
     * @param str
     * @return
     */
    public static boolean isNumber(String str){
        return str.matches("[0-9]+");
    }

    /**
     * 判断字符中是否包含数字
     * @param company
     * @return
     */
    public static boolean isContainNumber(String company) {
        Pattern p = Pattern.compile("[0-9]");
        Matcher m = p.matcher(company);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 判断用户输入的内容是否是汉字
     */
    public static boolean isHanzi(String text) {
        int len = 0;
        char[] charText = text.toCharArray();
        for (int i = 0; i < charText.length; i++) {
            if (isChinese(charText[i])) {
                len += 2;
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 包括空格判断
     *
     * @param input
     * @return
     */
    public static boolean containSpace(CharSequence input) {
        return Pattern.compile("\\s+").matcher(input).find();
    }

}

