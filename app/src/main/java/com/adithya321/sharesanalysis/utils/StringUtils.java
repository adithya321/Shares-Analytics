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

    public static String getName(String string) {
        String name = "";
        int j = 0;
        while (j < string.length() && string.charAt(j) != '-') j++;
        j++;
        while (j < string.length()) {
            name += string.charAt(j);
            j++;
        }
        if (name.length() == 0) return string;
        else return name;
    }
}
