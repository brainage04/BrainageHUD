package io.github.brainage04.brainagehud.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StringUtilsTest {
    @Test
    void labelsWriteAcronymsInCapitals() {
        assertEquals("Show WASD", StringUtils.pascalCaseToHumanReadable("showWasd"));
        assertEquals("TPS Decimal Places", StringUtils.pascalCaseToHumanReadable("tpsDecimalPlaces"));
        assertEquals("Blacklisted Enchantment IDs", StringUtils.pascalCaseToHumanReadable("blacklistedEnchantmentIds"));
        assertEquals("C Counter", StringUtils.pascalCaseToHumanReadable("cCounter"));
        assertEquals("X", StringUtils.pascalCaseToHumanReadable("x"));
    }
}
