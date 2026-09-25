package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.FishingHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

/**
 * Shows whether the thrown bobber is in open water, which decides whether treasure can be caught, and the chance
 * of catching fish, treasure or junk.
 *
 * <p>The server decides open water ({@code FishingHook.calculateOpenWater}) but never sends the
 * result, so the client's {@link FishingHook#isOpenWaterFishing()} is always {@code true}. This
 * element repeats the server's check against the client's copy of the world instead.
 */
public class FishingHud implements BasicCoreHudElement<FishingHudConfig> {
    private static final String NOT_IN_WATER = "Bobber: not in water";
    private static final String OPEN_WATER = "Open water: yes";
    private static final String NOT_OPEN_WATER = "Open water: no (needs 5x5 water, 2 deep, open above)";
    private static final String TREASURE_POSSIBLE = "Treasure: possible";
    private static final String TREASURE_IMPOSSIBLE = "Treasure: impossible";

    // the weight and quality of each entry in vanilla's gameplay/fishing loot table
    private static final int FISH_WEIGHT = 85;
    private static final int FISH_QUALITY = -1;
    private static final int TREASURE_WEIGHT = 5;
    private static final int TREASURE_QUALITY = 2;
    private static final int JUNK_WEIGHT = 10;
    private static final int JUNK_QUALITY = -2;

    private enum Layer {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID
    }

    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        LocalPlayer player = Minecraft.getInstance().player;
        FishingHook hook = player == null ? null : player.fishing;
        // nothing to report until a bobber is out
        if (hook == null) return lines;

        BlockPos hookPos = hook.blockPosition();
        if (!hook.level().getFluidState(hookPos).is(FluidTags.WATER)) {
            lines.add(NOT_IN_WATER);
            return lines;
        }

        boolean openWater = isOpenWater(hook.level(), hookPos);
        lines.add(openWater ? OPEN_WATER : NOT_OPEN_WATER);
        // vanilla's fishing loot table only rolls treasure for a hook in open water
        lines.add(openWater ? TREASURE_POSSIBLE : TREASURE_IMPOSSIBLE);

        int[] chances = getChances(openWater, getLuck(player));
        lines.add("Chances: Fish %d%%, Treasure %d%%, Junk %d%%".formatted(chances[0], chances[1], chances[2]));
        return lines;
    }

    /** The luck the server rolls the catch with: the rod's Luck of the Sea level plus the player's luck attribute. */
    private static float getLuck(LocalPlayer player) {
        // the rod that cast the bobber, in the hand vanilla checks first
        ItemStack rod = player.getMainHandItem().is(Items.FISHING_ROD) ? player.getMainHandItem() : player.getOffhandItem();
        int luckOfTheSea = player.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.LUCK_OF_THE_SEA)
                .map(enchantment -> rod.getEnchantments().getLevel(enchantment))
                .orElse(0);
        return luckOfTheSea + player.getLuck();
    }

    /**
     * The chance of catching fish, treasure and junk, in that order, as whole percentages that add up to 100 (all
     * 0 when every weight is 0, where vanilla catches nothing). Like vanilla's loot pool each entry weighs
     * {@code floor(weight + quality * luck)}, at least 0, and treasure only counts in open water.
     */
    public static int[] getChances(boolean openWater, float luck) {
        int[] weights = {
                getWeight(FISH_WEIGHT, FISH_QUALITY, luck),
                openWater ? getWeight(TREASURE_WEIGHT, TREASURE_QUALITY, luck) : 0,
                getWeight(JUNK_WEIGHT, JUNK_QUALITY, luck)
        };
        int totalWeight = weights[0] + weights[1] + weights[2];
        int[] chances = new int[weights.length];
        if (totalWeight == 0) return chances;

        // largest remainder rounding: round every share down, then give the leftover points to the largest
        // remainders, earlier categories first on a tie
        long[] remainders = new long[weights.length];
        int leftover = 100;
        for (int i = 0; i < weights.length; i++) {
            long scaled = weights[i] * 100L;
            chances[i] = (int) (scaled / totalWeight);
            remainders[i] = scaled % totalWeight;
            leftover -= chances[i];
        }
        for (; leftover > 0; leftover--) {
            int largest = 0;
            for (int i = 1; i < remainders.length; i++) {
                if (remainders[i] > remainders[largest]) largest = i;
            }
            chances[largest]++;
            remainders[largest] = -1;
        }
        return chances;
    }

    private static int getWeight(int weight, int quality, float luck) {
        return Math.max(Mth.floor(weight + quality * luck), 0);
    }

    /**
     * The client-side equivalent of {@code FishingHook.calculateOpenWater}: the four 5x5 layers from
     * one block below the bobber to two blocks above it must each be entirely water or entirely
     * clear, water must not sit above a clear layer, and the lowest layer must be water.
     */
    public static boolean isOpenWater(BlockGetter level, BlockPos hookPos) {
        Layer below = Layer.INVALID;

        for (int dy = -1; dy <= 2; dy++) {
            Layer layer = getLayer(level, hookPos.offset(0, dy, 0));
            switch (layer) {
                case INVALID -> {
                    return false;
                }
                case ABOVE_WATER -> {
                    if (below == Layer.INVALID) return false;
                }
                case INSIDE_WATER -> {
                    if (below == Layer.ABOVE_WATER) return false;
                }
            }
            below = layer;
        }

        return true;
    }

    private static Layer getLayer(BlockGetter level, BlockPos centre) {
        Layer layer = null;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                pos.setWithOffset(centre, dx, 0, dz);
                Layer block = getBlockLayer(level, pos);
                if (layer == null) {
                    layer = block;
                } else if (layer != block) {
                    return Layer.INVALID;
                }
            }
        }

        return layer;
    }

    private static Layer getBlockLayer(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.LILY_PAD)) return Layer.ABOVE_WATER;

        FluidState fluid = state.getFluidState();
        if (fluid.is(FluidTags.WATER) && fluid.isSource() && state.getCollisionShape(level, pos).isEmpty()) {
            return Layer.INSIDE_WATER;
        }

        return Layer.INVALID;
    }

    @Override
    public FishingHudConfig getElementConfig() {
        return getConfig().fishingHudConfig;
    }
}
