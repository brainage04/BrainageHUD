package io.github.brainage04.brainagehud.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Ported from GetEnchantInfo's {@code EnchantmentUtilsTest}. */
class EnchantmentUtilsTest {
    @Test
    void keepsTransitiveConflictsAsSeparatePairs() {
        List<Set<String>> conflicts =
                EnchantmentUtils.conflictPairs(List.of(Pair.of("a", "b"), Pair.of("b", "c")));

        assertEquals(List.of(Set.of("a", "b"), Set.of("b", "c")), conflicts);
    }
}
