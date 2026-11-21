package io.github.brainage04.brainagehud.hud.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KeystrokesHudTest {
    /**
     * The LMB and RMB boxes are 31 wide ({@code (int) (20 * 1.5 + 2 / 2)} in
     * {@code getKeyStrokesItems}) and their labels 18, so the leftover is odd and the old
     * {@code width / 2 - textWidth / 2} put the label a pixel left of centre.
     */
    @Test
    void centresAnOddLeftover() {
        assertEquals(7, KeystrokesHud.centredTextX(0, 31, 18));
        assertEquals(107, KeystrokesHud.centredTextX(100, 31, 18));
    }

    @Test
    void centresAnEvenLeftoverExactly() {
        assertEquals(5, KeystrokesHud.centredTextX(0, 20, 10));
        assertEquals(0, KeystrokesHud.centredTextX(0, 20, 20));
    }

    /**
     * The label's advance ends a pixel past its last glyph, so the leftover pixel goes on the left:
     * that centres the ink. The old maths put it on the right, which is the pixel the owner saw.
     */
    @Test
    void putsTheLeftoverPixelOnTheLeft() {
        for (int boxWidth = 1; boxWidth <= 64; boxWidth++) {
            for (int textWidth = 0; textWidth <= boxWidth; textWidth++) {
                int x = KeystrokesHud.centredTextX(0, boxWidth, textWidth);
                int left = x;
                int right = boxWidth - (x + textWidth);
                assertTrue(
                        left >= right,
                        "box " + boxWidth + " text " + textWidth + " left " + left + " right " + right);
                assertTrue(
                        left - right <= 1,
                        "box " + boxWidth + " text " + textWidth + " left " + left + " right " + right);
            }
        }
    }
}
