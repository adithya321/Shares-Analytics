package com.adithya321.sharesanalysis.utils;

public class StringUtils {
    public static String getCode(String name) {
        String code = "";
        int j = 0;
        while (j < name.length() && name.charAt(j) != ' ') {
            code += name.charAt(j);
            j++;
        }
        return code;
    }
}
