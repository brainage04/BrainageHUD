package io.github.brainage04.brainagehud.hud;

import com.google.common.collect.Ordering;
import io.github.brainage04.brainagehud.config.hud.basic.StatusEffectHudConfig;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ElementCorners;
import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import io.github.brainage04.hudrendererlib.util.TextList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

/**
 * The player's active effects, one per line in the order the inventory lists them, with their time left.
 * With Show Icons on, each line has the effect's icon to its left.
 */
public class StatusEffectHud implements CoreHudElement<StatusEffectHudConfig> {
    /** The effect sprite is drawn at one text line's height, so rows stay as far apart as text-only ones. */
    private static final int ICON_SIZE = 9;
    private static final int ICON_TEXT_OFFSET = ICON_SIZE + 2;

    public TextList getLines() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return new TextList();
        return getLines(player, getSortedEffects(player));
    }

    @Override
    public void render(GuiGraphicsExtractor drawContext, DeltaTracker renderTickCounter) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        Font renderer = Minecraft.getInstance().font;
        StatusEffectHudConfig config = getElementConfig();
        List<MobEffectInstance> effects = getSortedEffects(player);
        TextList lines = getLines(player, effects);
        if (!config.showIcons) {
            HudRenderer.renderElement(renderer, drawContext, lines, config.coreSettings);
            return;
        }
        if (lines.isEmpty()) return;

        // laid out like HudRenderer.renderElement, with the text moved right to make room for the icon
        CoreSettings coreSettings = config.coreSettings;
        int elementPadding = HudRendererLib.getPadding(coreSettings);
        int maxWidth = HudRendererLib.getMaxWidth(coreSettings);
        int wrapWidth = Math.max(1, maxWidth - ICON_TEXT_OFFSET);

        List<List<FormattedCharSequence>> wrappedLines = lines.stream()
                .map(line -> maxWidth > 0 ? renderer.split(line, wrapWidth) : List.of(line.getVisualOrderText()))
                .toList();
        int rowCount = 0;
        int textWidth = 0;
        for (List<FormattedCharSequence> rows : wrappedLines) {
            rowCount += Math.max(1, rows.size());
            for (FormattedCharSequence row : rows) {
                textWidth = Math.max(textWidth, renderer.width(row));
            }
        }

        int lineHeight = renderer.lineHeight + elementPadding;
        int elementWidth = ICON_TEXT_OFFSET + textWidth;
        int elementHeight = lineHeight * rowCount + elementPadding;
        int posX = HudRenderer.getPosX(coreSettings, elementWidth);
        int posY = HudRenderer.getPosY(coreSettings, elementHeight);

        ElementCorners corners = HudRenderer.setElementBounds(
                coreSettings,
                HudRenderer.getCornersWithPadding(posX, posY, posX + elementWidth, posY + elementHeight, coreSettings)
        );
        HudRenderer.renderBackdrop(drawContext, corners, coreSettings);

        int textColour = HudRendererLib.getTextColour(coreSettings);
        boolean textShadows = HudRendererLib.getTextShadows(coreSettings);
        int row = 0;
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance effect = effects.get(i);
            List<FormattedCharSequence> rows = wrappedLines.get(i);
            for (int j = 0; j < Math.max(1, rows.size()); j++, row++) {
                int rowWidth = j < rows.size() ? renderer.width(rows.get(j)) : 0;
                int rowPosX = HudRenderer.alignContentX(coreSettings, posX, elementWidth, ICON_TEXT_OFFSET + rowWidth);
                int rowPosY = posY + lineHeight * row;

                // effects that hide their icon keep the gap, so their text lines up with the rest
                if (j == 0 && effect.showIcon()) {
                    drawContext.blitSprite(
                            RenderPipelines.GUI_TEXTURED,
                            Hud.getMobEffectSprite(effect.getEffect()),
                            rowPosX,
                            rowPosY,
                            ICON_SIZE,
                            ICON_SIZE
                    );
                }
                if (j < rows.size()) {
                    drawContext.text(renderer, rows.get(j), rowPosX + ICON_TEXT_OFFSET, rowPosY, textColour, textShadows);
                }
            }
        }
    }

    private static List<MobEffectInstance> getSortedEffects(LocalPlayer player) {
        return Ordering.natural().sortedCopy(player.getActiveEffects());
    }

    /** One line per effect, in the order of {@code effects}. */
    private TextList getLines(LocalPlayer player, List<MobEffectInstance> effects) {
        TextList lines = new TextList();
        boolean showDurations = getElementConfig().showDurations;
        float tickRate = player.level().tickRateManager().tickrate();
        for (MobEffectInstance effect : effects) {
            MutableComponent line = getEffectName(effect);
            if (showDurations) line.append(": ").append(formatDuration(effect, tickRate));
            lines.add(line);
        }
        return lines;
    }

    /** The name the inventory gives the effect: its level is shown from II to X. */
    private static MutableComponent getEffectName(MobEffectInstance effect) {
        MutableComponent name = effect.getEffect().value().getDisplayName().copy();
        int amplifier = effect.getAmplifier();
        if (amplifier >= 1 && amplifier <= 9) {
            name.append(" ").append(Component.translatable("enchantment.level." + (amplifier + 1)));
        }
        return name;
    }

    private static String formatDuration(MobEffectInstance effect, float tickRate) {
        if (effect.isInfiniteDuration()) return "Infinite";
        return formatDuration(Mth.floor(effect.getDuration() / tickRate));
    }

    /** "m:ss", or "h:mm:ss" from an hour. */
    public static String formatDuration(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = totalSeconds / 60 % 60;
        int seconds = totalSeconds % 60;
        if (hours > 0) return "%d:%02d:%02d".formatted(hours, minutes, seconds);
        return "%d:%02d".formatted(minutes, seconds);
    }

    @Override
    public StatusEffectHudConfig getElementConfig() {
        return getConfig().statusEffectHudConfig;
    }
}
