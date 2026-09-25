package io.github.brainage04.brainagehud.util;

import java.util.Locale;
import java.util.Set;

public class StringUtils {
    /** Words written in capitals in labels, e.g. "Show FPS" rather than "Show Fps". */
    private static final Set<String> ACRONYMS = Set.of("HUD", "FPS", "RAM", "CPU", "GPU", "TPS", "WASD", "ID", "CPS");

    public static char toUpperCase(char c) {
        if (c >= 'a' && c <= 'z') {
            return (char) (c & ~32); // Bitwise uppercase (a -> A)
        }

        return c;
    }

    /**
     * Turns a camel case name into a Title Case label with its acronyms in capitals:
     * {@code showWasd} becomes "Show WASD" and {@code blacklistedEnchantmentIds} "Blacklisted
     * Enchantment IDs".
     */
    public static String pascalCaseToHumanReadable(String input) {
        StringBuilder output = new StringBuilder(input.length() + 8);
        int wordStart = 0;

        for (int i = 1; i <= input.length(); i++) {
            if (i == input.length() || (input.charAt(i) >= 'A' && input.charAt(i) <= 'Z')) {
                if (wordStart > 0) output.append(' ');
                output.append(word(input.substring(wordStart, i)));
                wordStart = i;
            }
        }

        return output.toString();
    }

    private static String word(String word) {
        String upperCase = word.toUpperCase(Locale.ROOT);
        if (ACRONYMS.contains(upperCase)) return upperCase;
        // plural acronyms keep a lower case s: "IDs"
        if (upperCase.endsWith("S") && ACRONYMS.contains(upperCase.substring(0, upperCase.length() - 1))) {
            return upperCase.substring(0, upperCase.length() - 1) + "s";
        }

        return toUpperCase(word.charAt(0)) + word.substring(1);
    }
}
