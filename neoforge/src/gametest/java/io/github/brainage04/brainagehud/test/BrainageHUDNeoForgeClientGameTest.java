package io.github.brainage04.brainagehud.test;

import io.github.brainage04.brainagehud.BrainageHUD;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * NeoForge has no client GameTest framework, so this drives {@link HudSmokeScenario} from the
 * client tick in the {@code :neoforge:runClientGameTest} run: it creates a flat world, runs the
 * scenario off the client thread and logs a pass marker the build checks for.
 */
@EventBusSubscriber(modid = BrainageHUD.MOD_ID, value = Dist.CLIENT)
public final class BrainageHUDNeoForgeClientGameTest {
    private static final String WORLD_DIRECTORY = "brainagehud-client-gametest";
    private static final long WORLD_OPEN_DEADLINE_TICKS = 6000;
    private static final AtomicLong TICKS = new AtomicLong();
    private static boolean worldRequested;
    private static boolean started;

    private BrainageHUDNeoForgeClientGameTest() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("brainagehud.clientGameTest")) return;
        TICKS.incrementAndGet();
        Minecraft client = Minecraft.getInstance();
        try {
            if (!started && TICKS.get() > WORLD_OPEN_DEADLINE_TICKS) {
                throw new AssertionError("The test world did not open in 5 minutes.");
            }
            if (!worldRequested && client.gui.screen() != null) {
                worldRequested = true;
                LevelSettings settings =
                        new LevelSettings(
                                "BrainageHUD GameTest",
                                GameType.CREATIVE,
                                new LevelSettings.DifficultySettings(
                                        Difficulty.PEACEFUL, false, false),
                                true,
                                WorldDataConfiguration.DEFAULT);
                client.createWorldOpenFlows()
                        .createFreshLevel(
                                WORLD_DIRECTORY,
                                settings,
                                new WorldOptions(0L, false, false),
                                registries ->
                                        registries
                                                .lookupOrThrow(Registries.WORLD_PRESET)
                                                .getOrThrow(WorldPresets.FLAT)
                                                .value()
                                                .createWorldDimensions(),
                                client.gui.screen());
            }
            if (!started
                    && client.player != null
                    && client.level != null
                    && client.gui.screen() == null) {
                started = true;
                Thread.ofVirtual().name("brainagehud-client-gametest").start(() -> run(client));
            }
        } catch (Throwable failure) {
            fail(failure);
        }
    }

    private static void run(Minecraft client) {
        try {
            HudSmokeScenario.run(
                    new HudSmokeScenario.Driver() {
                        @Override
                        public void onClient(Consumer<Minecraft> action) {
                            CompletableFuture<Void> completed = new CompletableFuture<>();
                            client.execute(
                                    () -> {
                                        try {
                                            action.accept(client);
                                            completed.complete(null);
                                        } catch (Throwable failure) {
                                            completed.completeExceptionally(failure);
                                        }
                                    });
                            try {
                                completed.get(30, TimeUnit.SECONDS);
                            } catch (Exception failure) {
                                throw new AssertionError("Client action failed", failure);
                            }
                        }

                        @Override
                        public void waitTicks(int ticks) {
                            long target = TICKS.get() + ticks;
                            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
                            while (TICKS.get() < target) {
                                if (System.nanoTime() >= deadline) {
                                    throw new AssertionError("Client ticks stalled");
                                }
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException failure) {
                                    Thread.currentThread().interrupt();
                                    throw new AssertionError("Client test interrupted", failure);
                                }
                            }
                        }
                    });
            BrainageHUD.LOGGER.info("BRAINAGEHUD_NEOFORGE_CLIENT_GAMETEST_PASS");
            client.execute(client::stop);
        } catch (Throwable failure) {
            fail(failure);
        }
    }

    private static void fail(Throwable failure) {
        BrainageHUD.LOGGER.error("BRAINAGEHUD_NEOFORGE_CLIENT_GAMETEST_FAIL", failure);
        System.exit(1);
    }
}
