package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.PositionHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;
import static io.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

public class PositionHud implements BasicCoreHudElement<PositionHudConfig> {
    private static final String[] YAW_LABEL = {
            "S (+Z)",          // 0  : [-22.5,  22.5)
            "SW (-X, +Z)",     // 1  :  [22.5,  67.5)
            "W (-X)",          // 2  :  [67.5,  112.5)
            "NW (-X, -Z)",     // 3  :  [112.5, 157.5)
            "N (-Z)",          // 4  :  [157.5, 180) / [-180,-157.5)
            "NE (+X, -Z)",     // 5  : [-157.5, -112.5)
            "E (+X)",          // 6  : [-112.5, -67.5)
            "SE (+X, +Z)"      // 7  : [-67.5,  -22.5)
    };

    public static String getYawString(float yaw) {
        // move normalised range from [-180, 180) to [0, 360)
        float wrapped = yaw + 180;

        // rotate by half a sector (45 / 2 = 22.5)
        wrapped = (wrapped + 22.5f) % 360f;

        // calculate index and return label
        int index = (int) (wrapped / 45f);

        return YAW_LABEL[index];
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

        LevelRenderer worldRenderer = Minecraft.getInstance().levelRenderer;

        if (getElementConfig().cCounter) {
            // taken from net.minecraft.client.renderer.WorldRenderer
            int completedChunks = worldRenderer.countRenderedSections();
            int totalChunks = (int) worldRenderer.getTotalSections();

            lines.add("C: %d/%d%s".formatted(
                    completedChunks,
                    totalChunks,
                    Minecraft.getInstance().smartCull ? " (s)" : ""
            ));
        }

        if (getElementConfig().eCounter) {
            lines.add(worldRenderer.getEntityStatistics());
        }

        if (getElementConfig().showDirection) {
            // taken from net.minecraft.client.gui.hud.DebugHud
            Entity entity = Minecraft.getInstance().getCameraEntity();
            if (entity == null) return lines;

            float yaw = Mth.wrapDegrees(entity.getYRot());

            String yawString = getYawString(yaw);

            float pitch = Mth.wrapDegrees(entity.getXRot());

            if (getElementConfig().showRotation) {
                yawString += " (%s / %s)".formatted(
                        roundDecimalPlaces(yaw, getElementConfig().rotationDecimalPlaces),
                        roundDecimalPlaces(pitch, getElementConfig().rotationDecimalPlaces)
                );
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
