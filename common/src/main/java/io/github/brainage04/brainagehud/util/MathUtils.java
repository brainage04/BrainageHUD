package io.github.brainage04.brainagehud.util;

import java.text.DecimalFormat;

public class MathUtils {
    /** The largest number of decimal places any HUD option accepts. */
    public static final int MAX_DECIMAL_PLACES = 6;

    // one format per precision, built on first use; HUD formatting only runs on the render thread
    private static final DecimalFormat[] FORMATS = new DecimalFormat[MAX_DECIMAL_PLACES + 1];

    /**
     * Formats {@code input} with exactly {@code decimalPlaces} fractional digits, clamped to
     * {@code [0, MAX_DECIMAL_PLACES]}. The value is formatted as a {@code double}, so large
     * coordinates keep their fractional part.
     */
    public static String roundDecimalPlaces(double input, int decimalPlaces) {
        int places = Math.clamp(decimalPlaces, 0, MAX_DECIMAL_PLACES);
        DecimalFormat format = FORMATS[places];
        if (format == null) {
            format = new DecimalFormat();
            format.setGroupingUsed(false);
            format.setMinimumFractionDigits(places);
            format.setMaximumFractionDigits(places);
            FORMATS[places] = format;
        }

        return format.format(input);
    }
}
