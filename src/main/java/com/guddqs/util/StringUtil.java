package com.guddqs.util;

/**
 * @author wq
 * @date 2018/10/30
 * @description jd-plus
 */
public class StringUtil {

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String format(String str, Object... args) {
        for (Object arg : args) {
            str = str.replace("%s", arg.toString());
        }
        return str;
    }

}
