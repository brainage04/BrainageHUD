package io.github.brainage04.brainagehud.util;

import java.text.DecimalFormat;

public class MathUtils {
    // from https://stackoverflow.com/a/10873851
    public static boolean isBetween(float x, float lower, float upper) {
        return lower <= x && x <= upper;
    }

    public static String roundDecimalPlaces(float input, int decimalPlaces) {
        DecimalFormat decimalFormat = new DecimalFormat("0." + "0".repeat(decimalPlaces));

        return decimalFormat.format(input);
    }

    public static String roundDecimalPlaces(double input, int decimalPlaces) {
        return roundDecimalPlaces((float) input, decimalPlaces);
    }
}