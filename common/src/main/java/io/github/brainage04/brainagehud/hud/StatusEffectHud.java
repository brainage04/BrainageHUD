package io.github.brainage04.brainagehud.hud;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import com.google.common.collect.Ordering;
import io.github.brainage04.brainagehud.config.hud.basic.StatusEffectHudConfig;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ElementCorners;
import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import io.github.brainage04.hudrendererlib.util.TextList;
import io.github.brainage04.hudrendererlib.util.TextWrapper;
import java.util.ArrayList;
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

public class StatusEffectHud implements CoreHudElement<StatusEffectHudConfig> {
    /**
     * The vanilla effect sprite's size: exactly two text lines, so the name and time left span the
     * icon.
     */
    private static final int ICON_SIZE = 18;

    private static final int ICON_TEXT_OFFSET = ICON_SIZE + 4;

    /**
     * The sprite's art sits a pixel low in its 18x18 frame, so drawing it this much higher centres
     * the art on the text.
     */
    private static final int ICON_RAISE = 1;

    public TextList getLines() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return new TextList();
        return getLines(player, getSortedEffects(player));
    }

    /** The max width wraps the text lines only; icon entries keep each name whole. */
    @Override
    public boolean supportsMaxWidth() {
        return !getElementConfig().showIcons;
    }

    @Override
    public void render(GuiGraphicsExtractor drawContext, DeltaTracker renderTickCounter) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        Font renderer = Minecraft.getInstance().font;
        StatusEffectHudConfig config = getElementConfig();
        List<MobEffectInstance> effects = getSortedEffects(player);
        if (!config.showIcons) {
            // getLines already wrapped the lines at the max width
            HudRenderer.renderElement(
                    renderer, drawContext, getLines(player, effects), config.coreSettings, 0);
            return;
        }
        if (effects.isEmpty()) return;

        CoreSettings coreSettings = config.coreSettings;
        int elementPadding = HudRendererLib.getPadding(coreSettings);
        float tickRate = player.level().tickRateManager().tickrate();

        List<Entry> entries = new ArrayList<>(effects.size());
        int textWidth = 0;
        int elementHeight = elementPadding;
        for (MobEffectInstance effect : effects) {
            List<FormattedCharSequence> rows = new ArrayList<>();
            rows.add(getEffectName(effect).getVisualOrderText());
            if (config.showDurations) {
                rows.add(Component.literal(formatDuration(effect, tickRate)).getVisualOrderText());
            }
            int entryTextWidth = 0;
            for (FormattedCharSequence row : rows) {
                entryTextWidth = Math.max(entryTextWidth, renderer.width(row));
            }
            Entry entry = new Entry(effect, rows, entryTextWidth);
            entries.add(entry);
            textWidth = Math.max(textWidth, entryTextWidth);
            elementHeight += entry.height(renderer) + elementPadding;
        }

        int elementWidth = ICON_TEXT_OFFSET + textWidth;
        int posX = HudRenderer.getPosX(coreSettings, elementWidth);
        int posY = HudRenderer.getPosY(coreSettings, elementHeight);

        ElementCorners paddedCorners =
                HudRenderer.getCornersWithPadding(
                        posX, posY, posX + elementWidth, posY + elementHeight, coreSettings);
        // with too little padding the raised first icon would stick out above the backdrop, so the
        // top then covers it
        paddedCorners.top = Math.min(paddedCorners.top, posY - ICON_RAISE);
        ElementCorners corners = HudRenderer.setElementBounds(coreSettings, paddedCorners);
        HudRenderer.renderBackdrop(drawContext, corners, coreSettings);

        int textColour = HudRendererLib.getTextColour(coreSettings);
        boolean textShadows = HudRendererLib.getTextShadows(coreSettings);
        int entryPosY = posY;
        for (Entry entry : entries) {
            int entryPosX =
                    HudRenderer.alignContentX(
                            coreSettings, posX, elementWidth, ICON_TEXT_OFFSET + entry.textWidth());
            // effects that hide their icon keep its column, so their text lines up with the rest
            if (entry.effect().showIcon()) {
                drawContext.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        Hud.getMobEffectSprite(entry.effect().getEffect()),
                        entryPosX,
                        entryPosY - ICON_RAISE,
                        ICON_SIZE,
                        ICON_SIZE);
            }
            // text shorter than the icon (the name alone) is centred on it; the +1 rounds towards
            // the glyphs'
            // shadow row, which puts as many empty rows above the text as below it
            int textHeight = renderer.lineHeight * entry.rows().size();
            int rowPosY = entryPosY + Math.max(0, (ICON_SIZE - textHeight + 1) / 2);
            for (FormattedCharSequence row : entry.rows()) {
                drawContext.text(
                        renderer,
                        row,
                        entryPosX + ICON_TEXT_OFFSET,
                        rowPosY,
                        textColour,
                        textShadows);
                rowPosY += renderer.lineHeight;
            }
            entryPosY += entry.height(renderer) + elementPadding;
        }
    }

    private record Entry(
            MobEffectInstance effect, List<FormattedCharSequence> rows, int textWidth) {
        int height(Font renderer) {
            return Math.max(ICON_SIZE, renderer.lineHeight * rows.size());
        }
    }

    private static List<MobEffectInstance> getSortedEffects(LocalPlayer player) {
        return Ordering.natural().sortedCopy(player.getActiveEffects());
    }

    private TextList getLines(LocalPlayer player, List<MobEffectInstance> effects) {
        TextList lines = new TextList();
        boolean showDurations = getElementConfig().showDurations;
        int maxWidth = HudRendererLib.getMaxWidth(getElementConfig().coreSettings);
        float tickRate = player.level().tickRateManager().tickrate();
        for (MobEffectInstance effect : effects) {
            List<Component> parts = new ArrayList<>(2);
            parts.add(getEffectName(effect));
            if (showDurations) parts.add(Component.literal(formatDuration(effect, tickRate)));
            lines.addAll(
                    TextWrapper.wrapParts(
                            Component.empty(),
                            parts,
                            ": ",
                            maxWidth,
                            TextWrapper.widthOf(Minecraft.getInstance().font)));
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
