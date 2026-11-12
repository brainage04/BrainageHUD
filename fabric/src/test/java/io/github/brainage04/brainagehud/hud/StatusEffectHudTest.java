package io.github.brainage04.brainagehud.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.junit.jupiter.api.Test;

class StatusEffectHudTest {
    // widths in characters, so that the rows can be counted by hand; in game they are GUI pixels
    private static final ToIntFunction<FormattedText> CHARACTERS =
            text -> text.getString().length();

    @Test
    void formatsDurationsUnderAnHourAsMinutesAndSeconds() {
        assertEquals("0:00", StatusEffectHud.formatDuration(0));
        assertEquals("0:05", StatusEffectHud.formatDuration(5));
        assertEquals("1:23", StatusEffectHud.formatDuration(83));
        assertEquals("59:59", StatusEffectHud.formatDuration(3599));
    }

    @Test
    void formatsDurationsFromAnHourWithHours() {
        assertEquals("1:00:00", StatusEffectHud.formatDuration(3600));
        assertEquals("1:01:05", StatusEffectHud.formatDuration(3665));
        assertEquals("12:00:00", StatusEffectHud.formatDuration(43200));
    }

    // the icon column is 22 wide, so a max width of 32 leaves 10 for the text
    @Test
    void iconRowsWrapBetweenWordsAtTheMaxWidthLessTheIconColumn() {
        assertEquals(
                List.of("Slow", "Falling"),
                strings(
                        StatusEffectHud.wrapIconRow(
                                Component.literal("Slow Falling"), 32, CHARACTERS)));
        assertEquals(
                List.of("Regeneration"),
                strings(
                        StatusEffectHud.wrapIconRow(
                                Component.literal("Regeneration"), 32, CHARACTERS)));
    }

    @Test
    void iconRowsStayWholeWithoutAMaxWidthOrWhenTheyFit() {
        assertEquals(
                List.of("Slow Falling II"),
                strings(
                        StatusEffectHud.wrapIconRow(
                                Component.literal("Slow Falling II"), 0, CHARACTERS)));
        assertEquals(
                List.of("Speed II"),
                strings(
                        StatusEffectHud.wrapIconRow(
                                Component.literal("Speed II"), 32, CHARACTERS)));
    }

    private static List<String> strings(List<Component> rows) {
        return rows.stream().map(Component::getString).toList();
    }
}
