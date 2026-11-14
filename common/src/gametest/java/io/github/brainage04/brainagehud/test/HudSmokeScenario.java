package io.github.brainage04.brainagehud.test;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.event.ModClickEvents;
import io.github.brainage04.brainagehud.hud.ReachHud;
import io.github.brainage04.brainagehud.util.ConfigUtils;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ElementCorners;
import io.github.brainage04.hudrendererlib.hud.core.HudElementEditor;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A loader-independent smoke run of the HUD in a client that has a world open. */
public final class HudSmokeScenario {
    public interface Driver {
        void onClient(Consumer<Minecraft> action);

        void waitTicks(int ticks);
    }

    private static final int PRESS = 1;
    private static final int RELEASE = 0;

    private HudSmokeScenario() {}

    /** Runs every step, then fails with each failed step's error attached. */
    public static void run(Driver driver) {
        driver.onClient(
                client -> {
                    check(
                            client.player != null && client.level != null,
                            "The client has no world or player.");
                    if (client.gui.hud.isHidden()) client.gui.hud.toggle();
                    assertConfigLoaded();
                    enableOnly(positionAndNetwork());
                });
        List<Runnable> steps =
                List.of(
                        () -> assertTwoElementsRender(driver),
                        () -> assertAttackClicksCounted(driver),
                        () -> driver.onClient(HudSmokeScenario::assertReachUpdatesOnAClick),
                        () -> assertPingsOnlyWhileNetworkHudIsOn(driver));
        List<Throwable> failures = new ArrayList<>();
        for (Runnable step : steps) {
            try {
                step.run();
            } catch (Throwable failure) {
                failures.add(failure);
            }
        }
        if (!failures.isEmpty()) {
            AssertionError error =
                    new AssertionError(
                            failures.size() + " of " + steps.size() + " HUD smoke steps failed");
            failures.forEach(error::addSuppressed);
            throw error;
        }
    }

    private static List<CoreSettings> positionAndNetwork() {
        ModConfig config = ConfigUtils.getConfig();
        return List.of(
                config.positionHudConfig.coreSettings, config.networkHudConfig.coreSettings);
    }

    private static void assertConfigLoaded() {
        ModConfig config = ConfigUtils.getConfig();
        check(config != null, "The HUD config did not load.");
        for (var element : HudRenderer.REGISTERED_ELEMENTS) {
            check(
                    element.getElementConfig() != null
                            && element.getElementConfig().getCoreSettings() != null,
                    "A registered HUD element has no loaded config: " + element + ".");
        }
    }

    private static void enableOnly(List<CoreSettings> enabled) {
        for (var element : HudRenderer.REGISTERED_ELEMENTS) {
            CoreSettings settings = element.getElementConfig().getCoreSettings();
            settings.enabled = enabled.contains(settings);
        }
        HudElementEditor.populateCoreSettingsElements();
        for (CoreSettings settings : enabled) {
            ElementCorners corners =
                    HudElementEditor.CORE_SETTINGS_ELEMENTS.get(settings.elementId).corners;
            corners.left = -1;
            corners.top = -1;
            corners.right = -1;
            corners.bottom = -1;
        }
    }

    /** Every frame that draws an element records its bounds in the editor's element map. */
    private static void assertTwoElementsRender(Driver driver) {
        driver.waitTicks(10);
        driver.onClient(
                client -> {
                    for (CoreSettings settings : positionAndNetwork()) {
                        ElementCorners corners =
                                HudElementEditor.CORE_SETTINGS_ELEMENTS.get(settings.elementId)
                                        .corners;
                        check(
                                corners.right > corners.left && corners.bottom > corners.top,
                                "The enabled "
                                        + settings.elementName
                                        + " HUD drew nothing in 10 ticks.");
                    }
                });
    }

    /** Presses the attack button through the mouse handler, as a player's click arrives. */
    private static void assertAttackClicksCounted(Driver driver) {
        driver.onClient(
                client -> {
                    InputConstants.Key attack =
                            InputConstants.getKey(client.options.keyAttack.saveString());
                    check(
                            attack.getType() == InputConstants.Type.MOUSE,
                            "The attack key is not a mouse button: " + attack + ".");
                    MouseButtonInfo button = new MouseButtonInfo(attack.getValue(), 0);
                    long window = client.getWindow().handle();
                    try {
                        for (int click = 0; click < 3; click++) {
                            client.mouseHandler.onButton(window, button, PRESS);
                            client.mouseHandler.onButton(window, button, RELEASE);
                        }
                        int clicks = ModClickEvents.getAttackClicksPerSecond();
                        check(
                                clicks == 3,
                                "Expected 3 attack clicks in the last second, but counted "
                                        + clicks
                                        + ".");
                    } finally {
                        while (client.options.keyAttack.consumeClick()) {
                            // the clicks above must not attack on the next tick
                        }
                    }
                });
    }

    /**
     * With Update On Attack Click, a click pressed and released between two frames (low frame
     * rates) must still update the Reach HUD: the attack key is never seen down.
     */
    private static void assertReachUpdatesOnAClick(Minecraft client) {
        var reach = ConfigUtils.getConfig().reachHudConfig;
        boolean updateOnAttackClick = reach.updateOnAttackClick;
        HitResult target = client.hitResult;
        try {
            reach.updateOnAttackClick = true;
            ReachHud hud = new ReachHud();
            BlockPos below = client.player.blockPosition().below();
            client.hitResult =
                    new BlockHitResult(Vec3.atCenterOf(below), Direction.UP, below, false);
            check(!hud.getLines().isEmpty(), "The Reach HUD shows nothing for a block in reach.");
            client.hitResult = BlockHitResult.miss(Vec3.atCenterOf(below), Direction.UP, below);
            check(!hud.getLines().isEmpty(), "The Reach HUD updated without an attack click.");
            KeyMapping.click(InputConstants.getKey(client.options.keyAttack.saveString()));
            check(
                    hud.getLines().isEmpty(),
                    "The Reach HUD missed an attack click pressed and released between two"
                            + " frames.");
        } finally {
            while (client.options.keyAttack.consumeClick()) {
                // the click above must not attack on the next tick
            }
            client.hitResult = target;
            reach.updateOnAttackClick = updateOnAttackClick;
        }
    }

    /**
     * The Network HUD's pings go out only while the HUD is on: with it off, and the debug network
     * chart closed, the game's ping log stays empty.
     */
    private static void assertPingsOnlyWhileNetworkHudIsOn(Driver driver) {
        boolean[] previous = new boolean[2];
        driver.onClient(
                client -> {
                    var network = ConfigUtils.getConfig().networkHudConfig;
                    previous[0] = network.coreSettings.enabled;
                    previous[1] = network.showPing;
                    network.coreSettings.enabled = false;
                    network.showPing = true;
                });
        try {
            // a pong already in flight would otherwise land in the emptied log
            driver.waitTicks(5);
            driver.onClient(client -> client.getDebugOverlay().getPingLogger().reset());
            driver.waitTicks(45);
            driver.onClient(
                    client -> {
                        int pongs = client.getDebugOverlay().getPingLogger().size();
                        check(
                                pongs == 0,
                                "The disabled Network HUD sent pings: "
                                        + pongs
                                        + " pongs came back in 45 ticks.");
                        ConfigUtils.getConfig().networkHudConfig.coreSettings.enabled = true;
                    });
            driver.waitTicks(45);
            driver.onClient(
                    client ->
                            check(
                                    client.getDebugOverlay().getPingLogger().size() > 0,
                                    "The Network HUD showing ping sent no pings in 45 ticks."));
        } finally {
            driver.onClient(
                    client -> {
                        var network = ConfigUtils.getConfig().networkHudConfig;
                        network.coreSettings.enabled = previous[0];
                        network.showPing = previous[1];
                    });
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
