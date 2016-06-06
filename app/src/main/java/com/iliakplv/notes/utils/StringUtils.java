package com.iliakplv.notes.utils;

public final class StringUtils {

    private StringUtils() {
        throw new AssertionError("Instance creation not allowed!");
    }

    public static String getNotNull(String s) {
        return s != null ? s : "";
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String normalizeString(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }

    public static String wrapWithEmptyLines(String string) {
        return "\n" + string + "\n";
    }

    public static boolean equals(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        } else if (s1 != null) {
            return s1.equals(s2);
        } else { // s2 != null
            return s2.equals(s1);
        }
    }

}
