package io.github.brainage04.brainagehud.hud;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;
import static io.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

import io.github.brainage04.brainagehud.config.hud.basic.PositionHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class PositionHud implements BasicCoreHudElement<PositionHudConfig> {
    // Minecraft yaw 0 faces south (+Z) and increases clockwise: 90 is west, 180/-180 north, -90
    // east
    private static final String[] YAW_LABEL = {
        "S (+Z)",
        "SW (-X, +Z)",
        "W (-X)",
        "NW (-X, -Z)",
        "N (-Z)",
        "NE (+X, -Z)",
        "E (+X)",
        "SE (+X, +Z)"
    };

    public static String getYawString(float yaw) {
        float wrapped = ((yaw + 22.5F) % 360.0F + 360.0F) % 360.0F;

        return YAW_LABEL[(int) (wrapped / 45.0F)];
    }

    /**
     * Words in the names of Hypixel SkyBlock's farming tools, which are not vanilla axes or hoes.
     */
    private static final Pattern FARMING_TOOL_NAME =
            Pattern.compile("\\b(axe|hoe|chopper|dicer|cutter|knife)\\b", Pattern.CASE_INSENSITIVE);

    private static boolean isFarmingTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ItemTags.AXES) || stack.is(ItemTags.HOES)) return true;
        return isFarmingToolName(stack.getHoverName().getString());
    }

    /** Whole words only, so that a pickaxe does not count as an axe. */
    static boolean isFarmingToolName(String name) {
        return FARMING_TOOL_NAME.matcher(name).find();
    }

    /**
     * The rendered/total sections at the start of the debug screen's chunk line. Sodium (0.9.3)
     * replaces vanilla's view area with a one-section stub (IgnoringViewArea) and overwrites
     * sectionStatistics() and countRenderedSections() (LevelExtractorMixin), so only that line has
     * the real total with every renderer; vanilla's ViewArea.size() reads 1 under Sodium.
     */
    private static final Pattern SECTION_COUNTS = Pattern.compile("C: \\d+/\\d+");

    /** A line without the counts is shown as it is. */
    static @Nullable String getCCounter(@Nullable String sectionStatistics, boolean smartCull) {
        if (sectionStatistics == null) return null;
        Matcher counts = SECTION_COUNTS.matcher(sectionStatistics);
        if (!counts.lookingAt()) return sectionStatistics;
        return counts.group() + (smartCull ? " (s)" : "");
    }

    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return lines;

        if (getElementConfig().showPosition) {
            String x =
                    "X: %s"
                            .formatted(
                                    roundDecimalPlaces(
                                            player.getX(),
                                            getElementConfig().positionDecimalPlaces));
            String y =
                    "Y: %s"
                            .formatted(
                                    roundDecimalPlaces(
                                            player.getY(),
                                            getElementConfig().positionDecimalPlaces));
            String z =
                    "Z: %s"
                            .formatted(
                                    roundDecimalPlaces(
                                            player.getZ(),
                                            getElementConfig().positionDecimalPlaces));

            if (getElementConfig().showChunkPosition) {
                x += " [%d]".formatted(player.blockPosition().getX() & 15);
                y += " [%d]".formatted(player.blockPosition().getY() & 15);
                z += " [%d]".formatted(player.blockPosition().getZ() & 15);
            }

            lines.add(x);
            lines.add(y);
            lines.add(z);
        }

        var levelExtractor = Minecraft.getInstance().levelExtractor;

        if (getElementConfig().cCounter) {
            String cCounter =
                    getCCounter(
                            levelExtractor.sectionStatistics(), Minecraft.getInstance().smartCull);
            if (cCounter != null) {
                lines.add(cCounter);
            }
        }

        if (getElementConfig().eCounter) {
            lines.add(levelExtractor.entityStatistics());
        }

        if (getElementConfig().showDirection) {
            Entity entity = Minecraft.getInstance().getCameraEntity();
            if (entity == null) return lines;

            float yaw = Mth.wrapDegrees(entity.getYRot());

            String yawString = getYawString(yaw);

            float pitch = Mth.wrapDegrees(entity.getXRot());

            if (getElementConfig().showRotation
                    && (!getElementConfig().rotationOnlyWithFarmingTool
                            || isFarmingTool(player.getMainHandItem()))) {
                yawString +=
                        " (%s / %s)"
                                .formatted(
                                        roundDecimalPlaces(
                                                yaw, getElementConfig().rotationDecimalPlaces),
                                        roundDecimalPlaces(
                                                pitch, getElementConfig().rotationDecimalPlaces));
                // the stored yaw keeps winding past ±180 as the player turns
                float trueYaw = entity.getYRot();
                if (getElementConfig().showTrueYaw && (trueYaw < -180.0F || trueYaw >= 180.0F)) {
                    yawString +=
                            " [%s]"
                                    .formatted(
                                            roundDecimalPlaces(
                                                    trueYaw,
                                                    getElementConfig().rotationDecimalPlaces));
                }
            }

            lines.add(yawString);
        }

        if (getElementConfig().showLight) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return lines;

            lines.add(
                    "Light: %s sky, %s block"
                            .formatted(
                                    level.getBrightness(LightLayer.SKY, player.blockPosition()),
                                    level.getBrightness(LightLayer.BLOCK, player.blockPosition())));
        }

        if (getElementConfig().showBiome) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return lines;

            Holder<Biome> biome = level.getBiome(player.blockPosition());

            biome.unwrapKey()
                    .ifPresent(
                            biomeRegistryKey ->
                                    lines.add(
                                            Component.literal("Biome: ")
                                                    .append(
                                                            Component.translatable(
                                                                    "biome.%s.%s"
                                                                            .formatted(
                                                                                    biomeRegistryKey
                                                                                            .identifier()
                                                                                            .getNamespace(),
                                                                                    biomeRegistryKey
                                                                                            .identifier()
                                                                                            .getPath())))));
        }

        return lines;
    }

    @Override
    public PositionHudConfig getElementConfig() {
        return getConfig().positionHudConfig;
    }
}
