package com.xtrinity;

import java.util.Arrays;
import java.util.Objects;

public class Utils {
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static Number parseNumber(String strNumber) {
        Number number = null;
        try {
            number = Integer.parseInt(strNumber);
        } catch (NumberFormatException ex) {
            number = Double.parseDouble(strNumber);
        }

        return number;
    }

    public static boolean evaluateBySign(String v1, String v2, String sign) {
        int result = v1.compareTo(v2);
        switch (sign) {
            case ">":
                return result > 0;
            case "<":
                return result < 0;
            case "<>":
                return result != 0;
            case "=":
                return result == 0;

            default:
                return false;
        }
    }

    public static boolean evaluateBySign(Double v1, Double v2, String sign) {
        switch (sign) {
            case ">":
                return v1 > v2;
            case "<":
                return v1 < v2;
            case "<>":
                return !Objects.equals(v1, v2);
            case "=":
                return Objects.equals(v1, v2);

            default:
                return false;
        }
    }

    public static boolean evaluateBySign(Integer v1, Integer v2, String sign) {
        return evaluateBySign(Double.valueOf(v1), Double.valueOf(v2), sign);
    }

    public static String[] cleanDoubleQuotes(String[] strings) {
        return Arrays.stream(strings).map(s -> {
            if (s.startsWith("\"") && s.endsWith("\"")) {
                return s.substring(1, s.length() - 1);
            }
            return s;
        }).toArray(String[]::new);
    }
}
