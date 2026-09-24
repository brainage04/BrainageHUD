package io.github.brainage04.brainagehud.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.DecimalFormatSymbols;
import org.junit.jupiter.api.Test;

class MathUtilsTest {
    // the HUD formats with the player's locale, so the expectations use its decimal separator
    private static final char SEPARATOR = DecimalFormatSymbols.getInstance().getDecimalSeparator();

    @Test
    void keepsFractionAtWorldBorderCoordinates() {
        assertEquals("29999984" + SEPARATOR + "37", MathUtils.roundDecimalPlaces(29_999_984.37D, 2));
        assertEquals("-1000000" + SEPARATOR + "3", MathUtils.roundDecimalPlaces(-1_000_000.34D, 1));
    }

    @Test
    void padsToExactlyTheRequestedPlaces() {
        assertEquals("1" + SEPARATOR + "50", MathUtils.roundDecimalPlaces(1.5D, 2));
    }

    @Test
    void zeroPlacesHasNoTrailingSeparator() {
        assertEquals("5", MathUtils.roundDecimalPlaces(5.4D, 0));
    }

    @Test
    void outOfRangePlacesAreClampedInsteadOfThrowing() {
        assertEquals("5", MathUtils.roundDecimalPlaces(5.4D, -1));
        assertEquals(
                "1" + SEPARATOR + "0".repeat(MathUtils.MAX_DECIMAL_PLACES),
                MathUtils.roundDecimalPlaces(1.0D, MathUtils.MAX_DECIMAL_PLACES + 5));
    }
}
