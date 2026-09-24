package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.PositionHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
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

import java.util.regex.Pattern;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;
import static io.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

public class PositionHud implements BasicCoreHudElement<PositionHudConfig> {
    // Minecraft yaw 0 faces south (+Z) and increases clockwise: 90 is west, 180/-180 north, -90 east
    private static final String[] YAW_LABEL = {
            "S (+Z)",          // 0 : centred on 0
            "SW (-X, +Z)",     // 1 : centred on 45
            "W (-X)",          // 2 : centred on 90
            "NW (-X, -Z)",     // 3 : centred on 135
            "N (-Z)",          // 4 : centred on 180 / -180
            "NE (+X, -Z)",     // 5 : centred on 225 / -135
            "E (+X)",          // 6 : centred on 270 / -90
            "SE (+X, +Z)"      // 7 : centred on 315 / -45
    };

    public static String getYawString(float yaw) {
        // shift by half a sector so each label's range starts at a multiple of 45, then wrap to [0, 360)
        float wrapped = ((yaw + 22.5F) % 360.0F + 360.0F) % 360.0F;

        return YAW_LABEL[(int) (wrapped / 45.0F)];
    }

    /** Words in the names of Hypixel SkyBlock's farming tools, which are not vanilla axes or hoes. */
    private static final Pattern FARMING_TOOL_NAME = Pattern.compile("\\b(axe|hoe|chopper|dicer|cutter|knife)\\b", Pattern.CASE_INSENSITIVE);

    /** Vanilla axes and hoes, and items named as farming tools (like SkyBlock's Melon Dicer). */
    private static boolean isFarmingTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ItemTags.AXES) || stack.is(ItemTags.HOES)) return true;
        return isFarmingToolName(stack.getHoverName().getString());
    }

    /** Whole words only, so that a pickaxe does not count as an axe. */
    static boolean isFarmingToolName(String name) {
        return FARMING_TOOL_NAME.matcher(name).find();
    }

    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return lines;

        if (getElementConfig().showPosition) {
            String x = "X: %s".formatted(roundDecimalPlaces(player.getX(), getElementConfig().positionDecimalPlaces));
            String y = "Y: %s".formatted(roundDecimalPlaces(player.getY(), getElementConfig().positionDecimalPlaces));
            String z = "Z: %s".formatted(roundDecimalPlaces(player.getZ(), getElementConfig().positionDecimalPlaces));

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
            // Matches LevelExtractor.sectionStatistics(): its ViewArea is the vanilla C-counter denominator.
            var viewArea = Minecraft.getInstance().levelRenderer.viewArea();
            if (viewArea != null) {
                int completedChunks = levelExtractor.countRenderedSections();
                int totalChunks = viewArea.size();

                lines.add("C: %d/%d%s".formatted(
                        completedChunks,
                        totalChunks,
                        Minecraft.getInstance().smartCull ? " (s)" : ""
                ));
            }
        }

        if (getElementConfig().eCounter) {
            lines.add(levelExtractor.entityStatistics());
        }

        if (getElementConfig().showDirection) {
            // taken from net.minecraft.client.gui.hud.DebugHud
            Entity entity = Minecraft.getInstance().getCameraEntity();
            if (entity == null) return lines;

            float yaw = Mth.wrapDegrees(entity.getYRot());

            String yawString = getYawString(yaw);

            float pitch = Mth.wrapDegrees(entity.getXRot());

            if (getElementConfig().showRotation && (!getElementConfig().rotationOnlyWithFarmingTool || isFarmingTool(player.getMainHandItem()))) {
                yawString += " (%s / %s)".formatted(
                        roundDecimalPlaces(yaw, getElementConfig().rotationDecimalPlaces),
                        roundDecimalPlaces(pitch, getElementConfig().rotationDecimalPlaces)
                );
                // the stored yaw keeps winding past ±180 as the player turns
                float trueYaw = entity.getYRot();
                if (getElementConfig().showTrueYaw && (trueYaw < -180.0F || trueYaw >= 180.0F)) {
                    yawString += " [%s]".formatted(roundDecimalPlaces(trueYaw, getElementConfig().rotationDecimalPlaces));
                }
            }

            lines.add(yawString);
        }

        if (getElementConfig().showLight) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return lines;

            lines.add("Light: %s sky, %s block".formatted(
                level.getBrightness(LightLayer.SKY, player.blockPosition()),
                level.getBrightness(LightLayer.BLOCK, player.blockPosition())
            ));
        }

        if (getElementConfig().showBiome) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return lines;

            Holder<Biome> biome = level.getBiome(player.blockPosition());

            biome.unwrapKey().ifPresent(biomeRegistryKey ->
                    lines.add(Component.literal("Biome: ")
                            .append(Component.translatable("biome.%s.%s".formatted(
                                    biomeRegistryKey.identifier().getNamespace(),
                                    biomeRegistryKey.identifier().getPath()
                            )))
                    )
            );
        }

        return lines;
    }

    @Override
    public PositionHudConfig getElementConfig() {
        return getConfig().positionHudConfig;
    }
}
