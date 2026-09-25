package io.github.brainage04.brainagehud.hud;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** The bobber sits in the water block at y = 0 in every scene. */
class FishingHudTest {
    private static final BlockPos HOOK = BlockPos.ZERO;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // a headless test loads no tags and leaves the fluid registry unfrozen; binding the water
        // tag (and every other requested tag to empty) and then freezing binds each fluid's tags
        MappedRegistry<Fluid> fluids = (MappedRegistry<Fluid>) BuiltInRegistries.FLUID;
        fluids.bindAllTagsToEmpty();
        fluids.bindTags(Map.of(FluidTags.WATER, List.of(
                Fluids.WATER.builtInRegistryHolder(), Fluids.FLOWING_WATER.builtInRegistryHolder())));
        fluids.freeze();
    }

    @Test
    void poolFiveWideAndTwoDeepIsOpenWater() {
        Scene scene = new Scene().fill(-1, 0, Blocks.WATER.defaultBlockState(), 2);

        assertTrue(FishingHud.isOpenWater(scene, HOOK));
    }

    @Test
    void lilyPadsCountAsOpenAbove() {
        Scene scene = new Scene().fill(-1, 0, Blocks.WATER.defaultBlockState(), 2).fill(1, 1, Blocks.LILY_PAD.defaultBlockState(), 2);

        assertTrue(FishingHud.isOpenWater(scene, HOOK));
    }

    @Test
    void singleBlockOfWaterIsNotOpenWater() {
        // the reported case: one water block in a hole in the ground
        Scene scene = new Scene().fill(-1, 0, Blocks.DIRT.defaultBlockState(), 2).set(HOOK, Blocks.WATER.defaultBlockState());

        assertFalse(FishingHud.isOpenWater(scene, HOOK));
    }

    @Test
    void waterOneBlockDeepIsNotOpenWater() {
        Scene scene = new Scene().fill(-1, -1, Blocks.STONE.defaultBlockState(), 2).fill(0, 0, Blocks.WATER.defaultBlockState(), 2);

        assertFalse(FishingHud.isOpenWater(scene, HOOK));
    }

    @Test
    void poolNarrowerThanFiveIsNotOpenWater() {
        Scene scene = new Scene()
                .fill(-1, 0, Blocks.STONE.defaultBlockState(), 2)
                .fill(-1, 0, Blocks.WATER.defaultBlockState(), 1);

        assertFalse(FishingHud.isOpenWater(scene, HOOK));
    }

    @Test
    void blockTwoAboveTheBobberBlocksOpenWater() {
        Scene scene = new Scene().fill(-1, 0, Blocks.WATER.defaultBlockState(), 2).set(new BlockPos(2, 2, 2), Blocks.OAK_LEAVES.defaultBlockState());

        assertFalse(FishingHud.isOpenWater(scene, HOOK));
    }

    @Test
    void waterAboveAnAirPocketIsNotOpenWater() {
        Scene scene = new Scene().fill(-1, 2, Blocks.WATER.defaultBlockState(), 2);
        assertTrue(FishingHud.isOpenWater(scene, HOOK), "water continuing upwards is still open water");

        scene.fill(1, 1, Blocks.AIR.defaultBlockState(), 2);
        assertFalse(FishingHud.isOpenWater(scene, HOOK));
    }

    @Test
    void chancesInOpenWaterWithoutLuckAreTheLootTableWeights() {
        // fish 85, treasure 5, junk 10 out of 100
        assertArrayEquals(new int[] {85, 5, 10}, FishingHud.getChances(true, 0));
    }

    @Test
    void chancesOutsideOpenWaterLeaveOutTreasure() {
        // fish 85 and junk 10 out of 95: 89.47% and 10.53%, so the leftover point goes to junk
        assertArrayEquals(new int[] {89, 0, 11}, FishingHud.getChances(false, 0));
    }

    @Test
    void luckOfTheSeaThreeShiftsWeightFromFishAndJunkToTreasure() {
        // fish 82, treasure 11, junk 4 out of 97: 84.54%, 11.34% and 4.12%
        assertArrayEquals(new int[] {85, 11, 4}, FishingHud.getChances(true, 3));
        // fish 82 and junk 4 out of 86: 95.35% and 4.65%
        assertArrayEquals(new int[] {95, 0, 5}, FishingHud.getChances(false, 3));
    }

    @Test
    void chancesAlwaysAddUpToOneHundred() {
        for (int tenths = -100; tenths <= 400; tenths++) {
            for (boolean openWater : new boolean[] {true, false}) {
                int[] chances = FishingHud.getChances(openWater, tenths / 10.0F);
                assertEquals(100, chances[0] + chances[1] + chances[2], "luck " + tenths / 10.0F + ", open water " + openWater);
            }
        }
    }

    /** Air everywhere except the blocks set. */
    private static final class Scene implements BlockGetter {
        private final Map<BlockPos, BlockState> blocks = new HashMap<>();

        /** Fills the layers {@code fromY..toY} within {@code radius} of the bobber's column. */
        Scene fill(int fromY, int toY, BlockState state, int radius) {
            for (int y = fromY; y <= toY; y++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        set(new BlockPos(x, y, z), state);
                    }
                }
            }
            return this;
        }

        Scene set(BlockPos pos, BlockState state) {
            blocks.put(pos, state);
            return this;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public int getHeight() {
            return 384;
        }

        @Override
        public int getMinY() {
            return -64;
        }
    }
}
