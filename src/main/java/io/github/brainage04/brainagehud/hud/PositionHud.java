package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.PositionHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

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

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return lines;

        if (getElementConfig().showPosition) {
            String x = "X: %s".formatted(roundDecimalPlaces(player.getX(), getElementConfig().positionDecimalPlaces));
            String y = "Y: %s".formatted(roundDecimalPlaces(player.getY(), getElementConfig().positionDecimalPlaces));
            String z = "Z: %s".formatted(roundDecimalPlaces(player.getZ(), getElementConfig().positionDecimalPlaces));

            if (getElementConfig().showChunkPosition) {
                x += " [%d]".formatted(player.getBlockPos().getX() & 15);
                y += " [%d]".formatted(player.getBlockPos().getY() & 15);
                z += " [%d]".formatted(player.getBlockPos().getZ() & 15);
            }

            lines.add(x);
            lines.add(y);
            lines.add(z);
        }

        WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;

        if (getElementConfig().cCounter) {
            // taken from net.minecraft.client.renderer.WorldRenderer
            int completedChunks = worldRenderer.getCompletedChunkCount();
            int totalChunks = (int) worldRenderer.getChunkCount();

            lines.add("C: %d/%d%s".formatted(
                    completedChunks,
                    totalChunks,
                    MinecraftClient.getInstance().chunkCullingEnabled ? " (s)" : ""
            ));
        }

        if (getElementConfig().eCounter) {
            // taken from net.minecraft.client.renderer.WorldRenderer
            lines.add("E: %d/%d".formatted(
                    worldRenderer.renderedEntitiesCount,
                    player.clientWorld.getRegularEntityCount()
            ));
        }

        if (getElementConfig().showDirection) {
            // taken from net.minecraft.client.gui.hud.DebugHud
            Entity entity = MinecraftClient.getInstance().getCameraEntity();
            if (entity == null) return lines;

            float yaw = MathHelper.wrapDegrees(entity.getYaw());

            String yawString = getYawString(yaw);

            float pitch = MathHelper.wrapDegrees(entity.getPitch());

            if (getElementConfig().showRotation) {
                yawString += " (%s / %s)".formatted(
                        roundDecimalPlaces(yaw, getElementConfig().rotationDecimalPlaces),
                        roundDecimalPlaces(pitch, getElementConfig().rotationDecimalPlaces)
                );
            }

            lines.add(yawString);
        }

        if (getElementConfig().showLight) {
            lines.add("Light: %s sky, %s block".formatted(
                player.clientWorld.getLightLevel(LightType.SKY, player.getBlockPos()),
                player.clientWorld.getLightLevel(LightType.BLOCK, player.getBlockPos())
            ));
        }

        if (getElementConfig().showBiome) {
            RegistryEntry<Biome> biome = player.clientWorld.getBiome(player.getBlockPos());

            biome.getKey().ifPresent(biomeRegistryKey ->
                    lines.add(Text.literal("Biome: ")
                            .append(Text.translatable("biome.%s.%s".formatted(
                                    biomeRegistryKey.getValue().getNamespace(),
                                    biomeRegistryKey.getValue().getPath()
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