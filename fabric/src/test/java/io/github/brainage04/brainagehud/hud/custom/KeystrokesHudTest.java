package io.github.brainage04.brainagehud.hud.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KeystrokesHudTest {
    /**
     * The LMB and RMB boxes are 31 wide ({@code (int) (20 * 1.5 + 2 / 2)} in
     * {@code getKeyStrokesItems}), so a label of odd width leaves an odd leftover, and the old
     * {@code width / 2 - textWidth / 2} put it a pixel left of centre.
     */
    @Test
    void centresAnOddLeftoverToTheRight() {
        assertEquals(7, KeystrokesHud.centredTextX(0, 31, 18));
        assertEquals(107, KeystrokesHud.centredTextX(100, 31, 18));
    }

    @Test
    void centresAnEvenLeftoverExactly() {
        assertEquals(5, KeystrokesHud.centredTextX(0, 20, 10));
        assertEquals(0, KeystrokesHud.centredTextX(0, 20, 20));
    }

    /** No box leaves more slack on the left of the label than on its right. */
    @Test
    void neverLeavesMoreSlackOnTheLeft() {
        for (int boxWidth = 1; boxWidth <= 64; boxWidth++) {
            for (int textWidth = 0; textWidth <= boxWidth; textWidth++) {
                int x = KeystrokesHud.centredTextX(0, boxWidth, textWidth);
                int left = x;
                int right = boxWidth - (x + textWidth);
                assertTrue(
                        left <= right,
                        "box " + boxWidth + " text " + textWidth + " left " + left + " right " + right);
                assertTrue(
                        right - left <= 1,
                        "box " + boxWidth + " text " + textWidth + " left " + left + " right " + right);
            }
        }
    }
}
