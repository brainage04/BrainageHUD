package com.github.brainage04.brainagehud.util;

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

    // from https://stackoverflow.com/a/9354899
    public static byte getBit(int number, int position) {
        return (byte) ((number >> position) & 1);
    }

    public static boolean isBitOn(int number, int position) {
        return getBit(number, position) == 1;
    }
}