package br.com.amedigital.weather.api.utils;

public final class StringUtils {

    private StringUtils() {
    }

    public static boolean isNotEmpty(String text) {
        return !isEmpty(text);
    }

    public static boolean isEmpty(String text) {
        if (text == null) return true;

        if (text.equalsIgnoreCase("null")) return true;

        return text.trim().isEmpty();
    }

}
