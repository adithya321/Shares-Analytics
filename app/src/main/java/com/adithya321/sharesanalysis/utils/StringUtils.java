/*
 * Shares Analysis
 * Copyright (C) 2016  Adithya J
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

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
