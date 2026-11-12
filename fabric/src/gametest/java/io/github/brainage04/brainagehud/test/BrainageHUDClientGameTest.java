package io.github.brainage04.brainagehud.test;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.event.ModClickEvents;
import io.github.brainage04.brainagehud.event.ModTooltipEvents;
import io.github.brainage04.brainagehud.hud.FishingHud;
import io.github.brainage04.brainagehud.hud.FoodHud;
import io.github.brainage04.brainagehud.hud.NetworkHud;
import io.github.brainage04.brainagehud.hud.PositionHud;
import io.github.brainage04.brainagehud.hud.ProjectileHud;
import io.github.brainage04.brainagehud.hud.StatusEffectHud;
import io.github.brainage04.brainagehud.hud.custom.EnchantInfoHud;
import io.github.brainage04.brainagehud.screen.WaypointEditScreen;
import io.github.brainage04.brainagehud.screen.WaypointsScreen;
import io.github.brainage04.brainagehud.util.ConfigUtils;
import io.github.brainage04.brainagehud.waypoint.Waypoint;
import io.github.brainage04.brainagehud.waypoint.WaypointActions;
import io.github.brainage04.brainagehud.waypoint.WaypointStore;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.hudrendererlib.config.core.ElementCorners;
import io.github.brainage04.hudrendererlib.config.core.HudRendererLibConfig;
import io.github.brainage04.hudrendererlib.hud.core.HudElementEditor;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import io.github.brainage04.minecraftmodconventions.ClientGameTestRecorder;
import io.github.brainage04.minecraftmodconventions.ClientGameTestRecordingSession;
import io.github.brainage04.minecraftmodconventions.ClientGameTestServers;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.sdl.SDLVideo;

@SuppressWarnings("UnstableApiUsage")
public final class BrainageHUDClientGameTest implements FabricClientGameTest {
    /** Set by the build on the client GameTest run that installs Sodium. */
    private static final String SODIUM_RUN_PROPERTY = "brainagehud.gametest.sodium";

    private static final Pattern DEBUG_SECTION_COUNTS = Pattern.compile("C: (\\d+)/(\\d+)");

    @Override
    public void runTest(ClientGameTestContext context) {
        Properties serverProperties = ClientGameTestServers.flatServerProperties();
        context.runOnClient(
                client -> {
                    boolean recording = ClientGameTestRecordingSession.isEnabled();
                    Window window = client.getWindow();
                    window.setWindowed(recording ? 1920 : 854, recording ? 1080 : 480);
                    SDLVideo.SDL_SetWindowPosition(window.handle(), 0, 0);
                    client.options.fullscreen().set(false);
                    client.options.guiScale().set(recording ? 2 : 1);
                    client.resizeGui();
                });
        context.waitTicks(5);

        ClientGameTestServers.withDedicatedServer(
                context,
                serverProperties,
                "BrainageHUD visual showcase GameTest",
                server -> {
                    ShowcaseConfigSnapshot configSnapshot = null;
                    try {
                        server.runOnServer(
                                minecraftServer ->
                                        preparePlayer(
                                                minecraftServer
                                                        .getPlayerList()
                                                        .getPlayers()
                                                        .getFirst()));
                        ClientGameTestServers.assertClientWorldAndPlayerAvailable(context);
                        context.waitTicks(40);

                        context.runOnClient(client -> client.gui.setScreen(new HudElementEditor()));
                        context.waitTicks(5);
                        // leaving the editor with Escape reloads the configs from disk, replacing
                        // every config object,
                        // so it runs before the showcase configures them
                        context.runOnClient(
                                client ->
                                        ((HudElementEditor) client.gui.screen())
                                                .closeWithoutSaving());
                        context.waitTicks(5);
                        context.runOnClient(client -> assertEditorKnowsEveryElement());
                        context.runOnClient(client -> assertDefaultKeys());
                        configSnapshot =
                                context.computeOnClient(client -> configureAndAssertShowcase());
                        ClientGameTestRecorder.startRecording(context);

                        ClientGameTestRecorder.showStep(
                                context,
                                "position-and-counters",
                                "Position and corrected C-counter",
                                "The top-left HUD shows the fixed player position, chunk coordinates, rendered-section C-counter, entity counter, and direction.");
                        context.waitTicks(80);
                        context.runOnClient(
                                BrainageHUDClientGameTest::assertCountersMatchDebugScreen);

                        ClientGameTestRecorder.showStep(
                                context,
                                "performance-and-network",
                                "Basic performance HUDs",
                                "Performance, network, reach, and toggle-sprint HUDs remain visible against the local dedicated-server fixture.");
                        context.waitTicks(60);
                        // the dedicated server has reported its game time every second for several
                        // seconds by now
                        context.runOnClient(client -> assertNetworkHudShowsServerTps());

                        ClientGameTestRecorder.showStep(
                                context,
                                "custom-huds",
                                "Armor and keystrokes HUDs",
                                "The equipped diamond armor and main-hand tool populate Armor Info while the WASD, space, and mouse controls populate Keystrokes.");
                        context.waitTicks(40);
                        for (int click = 0; click < 3; click++) {
                            context.getInput().pressKey(options -> options.keyAttack);
                        }
                        context.runOnClient(client -> assertAttackClicksCounted(3));
                        context.waitTicks(60);

                        ClientGameTestRecorder.showStep(
                                context,
                                "enchant-info",
                                "Enchant info HUD",
                                "The enchanted pickaxe in the main hand populates Enchant Info with its enchantments and the ones still missing from it.");
                        context.waitTicks(60);
                        context.runOnClient(client -> assertEnchantInfoHudShowsHeldItem());
                        System.out.println(
                                "[STDOUT]: Enchant info HUD screenshot: "
                                        + context.takeScreenshot("enchant-info-hud"));
                        exerciseOwnersEnchantPanel(context, server);
                        exerciseEnchantMaxWidth(context, server);

                        ClientGameTestRecorder.showStep(
                                context,
                                "status-effects",
                                "Status Effect HUD",
                                "The Status Effect HUD lists Speed II with its time left and the infinite Regeneration, each with its full-size effect icon on the left and the name above the time left. By default the game's own icons are hidden while the HUD is on and the top-right HUDs stay put; with Hide Vanilla Status Effects off the icons return and the top-right HUDs move down; with Show Vanilla Status Effects off the icons disappear again and the top-right HUDs move back up.");
                        context.runOnClient(
                                client ->
                                        assertHudLines(
                                                "Status Effect",
                                                new StatusEffectHud().getLines(),
                                                List.of()));
                        server.runOnServer(
                                minecraftServer -> {
                                    ServerPlayer player =
                                            minecraftServer.getPlayerList().getPlayers().getFirst();
                                    // just under 84 seconds, so it reads 1:23 for the next 19 ticks
                                    player.addEffect(
                                            new MobEffectInstance(MobEffects.SPEED, 1679, 1));
                                    player.addEffect(
                                            new MobEffectInstance(
                                                    MobEffects.REGENERATION,
                                                    MobEffectInstance.INFINITE_DURATION));
                                });
                        context.waitTicks(5);
                        context.runOnClient(
                                client -> {
                                    assertHudLines(
                                            "Status Effect",
                                            new StatusEffectHud().getLines(),
                                            List.of("Speed II: 1:23", "Regeneration: Infinite"));
                                    assertTopRightShift(0);
                                });
                        System.out.println(
                                "[STDOUT]: Status Effect HUD screenshot: "
                                        + context.takeScreenshot("status-effect-hud"));
                        if (!ClientGameTestRecordingSession.isEnabled()) {
                            System.out.println(
                                    "[STDOUT]: Status Effect HUD large screenshot: "
                                            + takeLargeScreenshot(
                                                    context, "status-effect-hud-large"));
                        }
                        context.runOnClient(
                                client -> {
                                    ConfigUtils.getConfig().statusEffectHudConfig.showDurations =
                                            false;
                                    assertHudLines(
                                            "Status Effect",
                                            new StatusEffectHud().getLines(),
                                            List.of("Speed II", "Regeneration"));
                                });
                        context.waitTicks(40);
                        if (!ClientGameTestRecordingSession.isEnabled()) {
                            System.out.println(
                                    "[STDOUT]: Status Effect HUD without durations large screenshot: "
                                            + takeLargeScreenshot(
                                                    context,
                                                    "status-effect-hud-no-durations-large"));
                        }
                        context.runOnClient(
                                client -> {
                                    ConfigUtils.getConfig().statusEffectHudConfig.showDurations =
                                            true;
                                    ConfigUtils.getConfig()
                                                    .statusEffectHudConfig
                                                    .hideVanillaStatusEffects =
                                            false;
                                    assertTopRightShift(
                                            libraryConfig()
                                                    .adjustTopRightElementsWithStatusEffectsAmount);
                                });
                        context.waitTicks(40);
                        System.out.println(
                                "[STDOUT]: Vanilla status effects shown screenshot: "
                                        + context.takeScreenshot("vanilla-status-effects-shown"));
                        context.runOnClient(
                                client -> {
                                    libraryConfig().showVanillaStatusEffects = false;
                                    assertTopRightShift(0);
                                });
                        context.waitTicks(40);
                        System.out.println(
                                "[STDOUT]: Vanilla status effects hidden screenshot: "
                                        + context.takeScreenshot("vanilla-status-effects-hidden"));
                        context.runOnClient(
                                client -> {
                                    libraryConfig().showVanillaStatusEffects = true;
                                    ConfigUtils.getConfig()
                                                    .statusEffectHudConfig
                                                    .hideVanillaStatusEffects =
                                            true;
                                });
                        server.runOnServer(
                                minecraftServer ->
                                        minecraftServer
                                                .getPlayerList()
                                                .getPlayers()
                                                .getFirst()
                                                .removeAllEffects());
                        context.waitTicks(5);

                        ClientGameTestRecorder.showStep(
                                context,
                                "inventory-trackers",
                                "Motion, entity and inventory trackers",
                                "Under their bold headers, the Projectile HUD counts 96 arrows over two slots and 16 ender pearls, and the Food HUD lists the bread and steak in inventory order.");
                        context.runOnClient(
                                client -> {
                                    assertHudLines(
                                            "Projectile",
                                            new ProjectileHud().getLines(),
                                            List.of(
                                                    "Projectiles:",
                                                    "Arrows: 96 [64, 32]",
                                                    "Ender Pearls: 16"));
                                    assertHudLines(
                                            "Food",
                                            new FoodHud().getLines(),
                                            List.of("Food:", "Bread: 12", "Steak: 5"));
                                });
                        context.waitTicks(60);
                        System.out.println(
                                "[STDOUT]: Inventory trackers screenshot: "
                                        + context.takeScreenshot("inventory-trackers"));

                        exerciseEnchantCommands(context);
                        exerciseInventoryStats(context);

                        ClientGameTestRecorder.showStep(
                                context,
                                "waypoints",
                                "Waypoints",
                                "The Create Waypoint key saves a waypoint where the player stands and the Manage Waypoints key opens the waypoint screen.");
                        context.getInput()
                                .pressKey(KeyMapping.get("key.brainagehud.createWaypoint"));
                        context.waitTicks(20);
                        context.runOnClient(client -> assertQuickWaypointCreated());
                        context.getInput()
                                .pressKey(KeyMapping.get("key.brainagehud.manageWaypoints"));
                        context.waitTicks(40);
                        context.runOnClient(
                                client -> {
                                    if (!(client.gui.screen() instanceof WaypointsScreen)) {
                                        throw new AssertionError(
                                                "Expected the Manage Waypoints key to open the waypoint screen, but the screen is "
                                                        + client.gui.screen()
                                                        + ".");
                                    }
                                });
                        clickAt(
                                context,
                                context.computeOnClient(
                                        client -> client.getWindow().getGuiScaledWidth() / 2),
                                40);
                        context.runOnClient(
                                client ->
                                        assertButtons(
                                                client,
                                                Map.of(
                                                        "Edit", false, "Delete", false, "Hide",
                                                        true)));
                        System.out.println(
                                "[STDOUT]: Waypoints screen screenshot: "
                                        + context.takeScreenshot("waypoints-screen"));
                        clickButton(context, "Hide");
                        context.runOnClient(
                                client -> {
                                    if (ConfigUtils.getConfig().waypointConfig.showWorldCentre) {
                                        throw new AssertionError(
                                                "Expected Hide on the World Centre to turn Show World Centre off.");
                                    }
                                    assertButtons(client, Map.of("Show", true));
                                });
                        clickButton(context, "Show");
                        context.runOnClient(
                                client -> {
                                    if (!ConfigUtils.getConfig().waypointConfig.showWorldCentre) {
                                        throw new AssertionError(
                                                "Expected Show on the World Centre to turn Show World Centre back on.");
                                    }
                                });
                        context.runOnClient(client -> client.gui.setScreen(null));
                        context.waitTicks(20);

                        ClientGameTestRecorder.showStep(
                                context,
                                "chat-feedback",
                                "Chat feedback",
                                "Every message starts with a grey [BrainageHUD] prefix: the Create Waypoint key's confirmation is green, wrong /waypoints input gets a red usage error, and /waypoints list sends one white header followed by unprefixed lines.");
                        Set<Component> chat = new LinkedHashSet<>();
                        // the filter sees every chat message, including those already shown,
                        // whenever the chat is laid out again
                        context.runOnClient(
                                client ->
                                        client.gui
                                                .hud
                                                .getChat()
                                                .setVisibleMessageFilter(
                                                        message -> {
                                                            chat.add(message.content());
                                                            return true;
                                                        }));
                        runClientCommand(context, "waypoints add");
                        runClientCommand(context, "waypoints add Base 1 two 3");
                        runClientCommand(context, "waypoints list");
                        context.waitTicks(10);
                        context.runOnClient(
                                client -> {
                                    client.gui
                                            .hud
                                            .getChat()
                                            .setVisibleMessageFilter(message -> true);
                                    assertChatMessage(
                                            chat,
                                            "[BrainageHUD] Created waypoint Waypoint 1 at 12, -60, -9.",
                                            "Created waypoint ",
                                            ChatFormatting.GREEN);
                                    assertChatMessage(
                                            chat,
                                            "[BrainageHUD] Usage: /waypoints add <name> [<x> <y> <z>]",
                                            "Usage: ",
                                            ChatFormatting.RED);
                                    assertChatMessage(
                                            chat,
                                            "[BrainageHUD] X, Y and Z must be whole numbers.",
                                            "X, Y and Z",
                                            ChatFormatting.RED);
                                    assertChatMessage(
                                            chat,
                                            "[BrainageHUD] Waypoints:",
                                            "Waypoints:",
                                            ChatFormatting.WHITE);
                                    assertChatMessage(
                                            chat,
                                            " - Waypoint 1 12, -60, -9 (Overworld)",
                                            " - ",
                                            ChatFormatting.WHITE);
                                });
                        System.out.println(
                                "[STDOUT]: Chat feedback screenshot: "
                                        + context.takeScreenshot("chat-feedback"));
                        exerciseWaypointCommands(context);

                        ClientGameTestRecorder.showStep(
                                context,
                                "waypoints-in-world",
                                "Waypoints in the world",
                                "From twelve blocks away the new waypoint shows its beam, gem, ground pulse and name label; a waypoint 300 blocks away shows a beam and its distance, and the white World Centre beacon stands at 0, 63, 0. Every beam spans the whole height of the world.");
                        context.runOnClient(
                                client ->
                                        WaypointActions.create(
                                                "Far Base", new BlockPos(100, -60, 300)));
                        server.runOnServer(
                                minecraftServer -> {
                                    ServerPlayer player =
                                            minecraftServer.getPlayerList().getPlayers().getFirst();
                                    player.teleportTo(12.5D, -60.0D, -20.5D);
                                    player.setYRot(0.0F);
                                    player.setXRot(0.0F);
                                });
                        context.waitTicks(60);
                        System.out.println(
                                "[STDOUT]: Waypoints in world screenshot: "
                                        + context.takeScreenshot("waypoints-in-world"));
                        context.runOnClient(client -> WaypointActions.remove("Far Base"));
                        server.runOnServer(
                                minecraftServer ->
                                        minecraftServer
                                                .getPlayerList()
                                                .getPlayers()
                                                .getFirst()
                                                .teleportTo(12.5D, -60.0D, -8.5D));
                        context.waitTicks(20);

                        ClientGameTestRecorder.showStep(
                                context,
                                "fishing",
                                "Fishing HUD",
                                "The Fishing HUD stays empty until the bobber is cast, then reports open water, possible treasure and the chances of fish, treasure and junk for a bobber in a wide, deep pool.");
                        context.runOnClient(client -> assertFishingHudLines(List.of()));
                        context.getInput().pressKey(options -> options.keyHotbarSlots[1]);
                        context.waitTicks(5);
                        context.getInput().pressKey(options -> options.keyUse);
                        context.waitTicks(60);
                        context.runOnClient(
                                client ->
                                        assertFishingHudLines(
                                                List.of(
                                                        "Open water: yes",
                                                        "Treasure: possible",
                                                        "Chances: Fish 85%, Treasure 5%, Junk 10%")));
                        System.out.println(
                                "[STDOUT]: Fishing HUD screenshot: "
                                        + context.takeScreenshot("fishing-hud"));
                        exerciseFullbright(context, server);
                    } finally {
                        if (configSnapshot != null) {
                            ShowcaseConfigSnapshot snapshotToRestore = configSnapshot;
                            context.runOnClient(client -> snapshotToRestore.restore());
                        }
                    }
                });
    }

    private static Path takeLargeScreenshot(ClientGameTestContext context, String name) {
        int[] original =
                context.computeOnClient(
                        client ->
                                new int[] {
                                    client.getWindow().getWidth(),
                                    client.getWindow().getHeight(),
                                    client.options.guiScale().get(),
                                    client.getWindow().getGuiScaledWidth(),
                                    client.getWindow().getGuiScaledHeight()
                                });
        // the GUI scale option only accepts scales the current window fits, so the window grows
        // first
        context.getInput().resizeWindow(original[3] * 4, original[4] * 4);
        context.runOnClient(
                client -> {
                    client.options.guiScale().set(4);
                    client.resizeGui();
                    Window window = client.getWindow();
                    if (window.getGuiScale() != 4
                            || window.getGuiScaledWidth() != original[3]
                            || window.getGuiScaledHeight() != original[4]) {
                        throw new AssertionError(
                                "Expected a %dx%d GUI at scale 4 for the large screenshot, got %dx%d at scale %d"
                                        .formatted(
                                                original[3],
                                                original[4],
                                                window.getGuiScaledWidth(),
                                                window.getGuiScaledHeight(),
                                                window.getGuiScale()));
                    }
                });
        context.waitTick();
        try {
            return context.takeScreenshot(name);
        } finally {
            context.runOnClient(client -> client.options.guiScale().set(original[2]));
            context.getInput().resizeWindow(original[0], original[1]);
            context.waitTick();
        }
    }

    /**
     * The owner's case of 12 Nov 2026 (A-0751): an Iron Sword on a server that registers Skyport's
     * enchantments (this GameTest mod's datapack, {@code data/skyport}), with the compact Enchant
     * Info HUD and the Date/Time HUD at their default places, in a 1920x1080 window at GUI scale 3.
     * The panel must end before the clock. It resizes the window, so like the other resizing checks
     * it runs only outside recordings.
     */
    private static void exerciseOwnersEnchantPanel(
            ClientGameTestContext context,
            net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext server) {
        if (ClientGameTestRecordingSession.isEnabled()) return;
        int[] original =
                context.computeOnClient(
                        client ->
                                new int[] {
                                    client.getWindow().getWidth(),
                                    client.getWindow().getHeight(),
                                    client.options.guiScale().get(),
                                    client.player.getInventory().getSelectedSlot()
                                });
        ModConfig config = context.computeOnClient(client -> ConfigUtils.getConfig());
        ModConfig defaults = new ModConfig();
        List<CoreSettings> placed =
                List.of(
                        config.enchantInfoHudConfig.coreSettings,
                        config.dateTimeHudConfig.coreSettings,
                        config.keystrokesHudConfig.coreSettings);
        List<CoreSettings> defaultPlaces =
                List.of(
                        defaults.enchantInfoHudConfig.coreSettings,
                        defaults.dateTimeHudConfig.coreSettings,
                        defaults.keystrokesHudConfig.coreSettings);
        List<HudPlacement> placements = placed.stream().map(HudPlacement::new).toList();
        boolean compactMode = config.enchantInfoHudConfig.compactMode;
        boolean maxWidthOverride =
                config.enchantInfoHudConfig.coreSettings.elementOverrides.maxWidth.enabled;
        server.runOnServer(
                minecraftServer ->
                        minecraftServer
                                .getPlayerList()
                                .getPlayers()
                                .getFirst()
                                .getInventory()
                                .setItem(8, new ItemStack(Items.IRON_SWORD)));
        try {
            // the recorder's step panel in the top right would cover the clock and the panel
            context.runOnClient(client -> setRecorderPanelHidden(true));
            context.getInput().resizeWindow(1920, 1080);
            context.runOnClient(
                    client -> {
                        client.options.guiScale().set(3);
                        client.resizeGui();
                        if (client.getWindow().getGuiScale() != 3) {
                            throw new AssertionError(
                                    "Expected GUI scale 3 in a 1920x1080 window, got "
                                            + client.getWindow().getGuiScale());
                        }
                        client.player.getInventory().setSelectedSlot(8);
                        for (int i = 0; i < placed.size(); i++) {
                            new HudPlacement(defaultPlaces.get(i)).applyTo(placed.get(i));
                        }
                        config.enchantInfoHudConfig.compactMode = true;
                        config.enchantInfoHudConfig.coreSettings.elementOverrides.maxWidth.enabled =
                                false;
                    });
            context.waitTicks(10);
            System.out.println(
                    "[STDOUT]: Owner's Enchant Info panel at 1920x1080, GUI scale 3, screenshot: "
                            + context.takeScreenshot("enchant-owner-1080p-gui3"));
            context.runOnClient(
                    client -> {
                        ElementCorners enchant = bounds(config.enchantInfoHudConfig.coreSettings);
                        ElementCorners clock = bounds(config.dateTimeHudConfig.coreSettings);
                        System.out.printf(
                                "[STDOUT]: Owner's Enchant Info panel: x %d to %d, %d lines; clock"
                                        + " from x %d; screen %d GUI pixels wide%n",
                                enchant.left,
                                enchant.right,
                                new EnchantInfoHud().getLines().size(),
                                clock.left,
                                HudRenderer.getScaledWidth());
                        if (enchant.right >= clock.left) {
                            throw new AssertionError(
                                    "Expected the Enchant Info panel at its default place to end"
                                            + " before the Date/Time HUD at x %d, but it ends at x %d"
                                                    .formatted(clock.left, enchant.right));
                        }
                    });
        } finally {
            context.runOnClient(
                    client -> {
                        placements.forEach(HudPlacement::restore);
                        setRecorderPanelHidden(false);
                        config.enchantInfoHudConfig.compactMode = compactMode;
                        config.enchantInfoHudConfig.coreSettings.elementOverrides.maxWidth.enabled =
                                maxWidthOverride;
                        client.player.getInventory().setSelectedSlot(original[3]);
                        client.options.guiScale().set(original[2]);
                    });
            server.runOnServer(
                    minecraftServer ->
                            minecraftServer
                                    .getPlayerList()
                                    .getPlayers()
                                    .getFirst()
                                    .getInventory()
                                    .setItem(8, ItemStack.EMPTY));
            context.getInput().resizeWindow(original[0], original[1]);
            context.runOnClient(Minecraft::resizeGui);
            context.waitTicks(5);
        }
    }

    /** The compact Iron Sword panel on a server with Skyport's enchantments, with Max Width off. */
    private static final List<String> SKYPORT_IRON_SWORD_LINES =
            List.of(
                    "Iron Sword",
                    "Missing: Blinding, Bloodlust, Crippling, Dread, Drifting, Eclipse, Enfeebling,"
                            + " Famine, Fire Aspect, Infestation, Insight, Looting, Mending, Oozing,"
                            + " Plague, Pyre, Sapping, Siphon, Soulbound, Stormcaller, Sweeping Edge,"
                            + " Unbreaking, Updraft, Venom, Vertigo, Warden's Wrath, Warding,"
                            + " Withering",
                    "Annihilation / Bane of Illagers / Bane of the Deep / Bane of the End / Bane of"
                            + " the Nether / Duelist / Sharpness",
                    "Cataclysm / Volatile");

    /** The gap the max width leaves between the Enchant Info panel and its neighbour. */
    private static final int MAX_WIDTH_GAP = 6;

    /**
     * The owner's case of 9 and 12 Nov 2026 (A-0293, A-0751): an Iron Sword on a server that
     * registers Skyport's custom enchantments (this GameTest mod's datapack, {@code data/skyport}),
     * the compact Enchant Info panel at its default place in the top left and the Date/Time HUD at
     * its default place in the top right, in a 1920x1080 window at GUI scale 3. With Max Width
     * turned off the panel reaches the clock; at the default max width every line fits it in GUI
     * pixels and the panel ends before the clock; a max width set by dragging the HUD Element
     * Editor's handle wraps it at its separators before the clock. Anchored top right next to the
     * clock, the panel may reach the Position HUD instead, and its max width keeps it clear while
     * its right edge stays where it was. Last, the owner's own GUI size (854x480) at the default.
     * It resizes the window, so like the other resizing checks it runs only outside recordings.
     */
    private static void exerciseEnchantMaxWidth(
            ClientGameTestContext context,
            net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext server) {
        if (ClientGameTestRecordingSession.isEnabled()) return;
        ClientGameTestRecorder.showStep(
                context,
                "enchant-max-width",
                "Enchant Info max width",
                "An Iron Sword with Skyport's enchantments: with Max Width off the panel reaches the clock; the default max width and one dragged in the HUD Element Editor wrap it at its separators, never inside a name, and keep its anchored edge.");
        int[] original =
                context.computeOnClient(
                        client ->
                                new int[] {
                                    client.getWindow().getWidth(),
                                    client.getWindow().getHeight(),
                                    client.options.guiScale().get(),
                                    client.player.getInventory().getSelectedSlot()
                                });
        List<HudPlacement> placements =
                context.computeOnClient(
                        client ->
                                List.of(
                                        new HudPlacement(
                                                ConfigUtils.getConfig()
                                                        .enchantInfoHudConfig
                                                        .coreSettings),
                                        new HudPlacement(
                                                ConfigUtils.getConfig()
                                                        .dateTimeHudConfig
                                                        .coreSettings),
                                        new HudPlacement(
                                                ConfigUtils.getConfig()
                                                        .keystrokesHudConfig
                                                        .coreSettings)));
        server.runOnServer(
                minecraftServer ->
                        minecraftServer
                                .getPlayerList()
                                .getPlayers()
                                .getFirst()
                                .getInventory()
                                .setItem(8, new ItemStack(Items.IRON_SWORD)));
        try {
            // the recorder's step panel in the top right would cover the clock and the panel
            context.runOnClient(client -> setRecorderPanelHidden(true));
            setWindow(context, 1920, 1080, 3);
            context.runOnClient(
                    client -> {
                        ModConfig config = ConfigUtils.getConfig();
                        ModConfig defaults = new ModConfig();
                        client.player.getInventory().setSelectedSlot(8);
                        CoreSettings enchant = config.enchantInfoHudConfig.coreSettings;
                        new HudPlacement(defaults.enchantInfoHudConfig.coreSettings)
                                .applyTo(enchant);
                        turnMaxWidthOff(enchant);
                        config.enchantInfoHudConfig.compactMode = true;
                        new HudPlacement(defaults.dateTimeHudConfig.coreSettings)
                                .applyTo(config.dateTimeHudConfig.coreSettings);
                        new HudPlacement(defaults.keystrokesHudConfig.coreSettings)
                                .applyTo(config.keystrokesHudConfig.coreSettings);
                    });
            context.waitTicks(10);

            // left-anchored, Max Width off: the panel reaches the clock
            int[] leftBefore =
                    context.computeOnClient(
                            client -> {
                                assertHudLines(
                                        "Enchant Info",
                                        new EnchantInfoHud().getLines(),
                                        SKYPORT_IRON_SWORD_LINES);
                                ElementCorners enchant = enchantBounds();
                                ElementCorners clock = bounds(clockSettings());
                                if (enchant.right <= clock.left) {
                                    throw new AssertionError(
                                            "Expected the Enchant Info panel with Max Width off to reach the clock at %d, but it ends at %d"
                                                    .formatted(clock.left, enchant.right));
                                }
                                return new int[] {enchant.left, enchant.right, clock.left};
                            });
            printScreenshot(context, "Enchant max width, left, off", "enchant-max-width-left-off");

            // left-anchored, the default max width: every line fits it and the panel ends before
            // the clock
            context.runOnClient(client -> HudElementEditor.clearMaxWidth(enchantSettings()));
            context.waitTicks(5);
            context.runOnClient(
                    client -> {
                        List<Component> lines = new EnchantInfoHud().getLines();
                        int widest = widestLine(client, lines);
                        if (widest > EnchantInfoHud.DEFAULT_MAX_WIDTH) {
                            throw new AssertionError(
                                    "Expected every Enchant Info line within the default max width %d, but the widest is %d: %s"
                                            .formatted(
                                                    EnchantInfoHud.DEFAULT_MAX_WIDTH,
                                                    widest,
                                                    lines.stream()
                                                            .map(Component::getString)
                                                            .toList()));
                        }
                        assertWrappedBetweenNames(lines);
                        ElementCorners enchant = enchantBounds();
                        if (enchant.left != leftBefore[0] || enchant.right >= leftBefore[2]) {
                            throw new AssertionError(
                                    "Expected the panel at the default max width to keep its left edge %d and end before the clock at %d, got %d to %d"
                                            .formatted(
                                                    leftBefore[0],
                                                    leftBefore[2],
                                                    enchant.left,
                                                    enchant.right));
                        }
                        System.out.printf(
                                "[STDOUT]: Enchant Info at the default max width: x %d to %d, widest line %d, %d lines; clock from x %d%n",
                                enchant.left, enchant.right, widest, lines.size(), leftBefore[2]);
                    });
            printScreenshot(
                    context, "Enchant max width, left, default", "enchant-max-width-left-default");

            // the HUD Element Editor's handle, dragged to just before the clock
            context.runOnClient(client -> client.gui.setScreen(new HudElementEditor()));
            context.waitTicks(5);
            context.runOnClient(
                    client -> {
                        HudElementEditor editor = (HudElementEditor) client.gui.screen();
                        CoreSettings settings = enchantSettings();
                        var element =
                                HudElementEditor.CORE_SETTINGS_ELEMENTS.get(settings.elementId);
                        ElementCorners corners = element.corners;
                        editor.mouseClicked(
                                editorMouse((corners.left + corners.right) / 2.0, corners.top + 3),
                                false);
                        if (editor.selectedElementIndex != settings.elementId) {
                            throw new AssertionError(
                                    "Clicking the Enchant Info panel did not select it in the editor.");
                        }
                        // the handle starts at the default max width, past the widest line
                        int handleX = HudElementEditor.getMaxWidthHandleX(element);
                        int expectedHandleX =
                                corners.right
                                        + EnchantInfoHud.DEFAULT_MAX_WIDTH
                                        - widestLine(client, new EnchantInfoHud().getLines());
                        if (handleX != expectedHandleX) {
                            throw new AssertionError(
                                    "Expected the max width handle at the default max width's edge %d, got %d"
                                            .formatted(expectedHandleX, handleX));
                        }
                        double target = leftBefore[2] - MAX_WIDTH_GAP;
                        editor.mouseClicked(editorMouse(handleX, corners.top + 3), false);
                        editor.mouseDragged(editorMouse(target, corners.top + 3), 0, 0);
                        editor.mouseReleased(editorMouse(target, corners.top + 3));
                        if (!settings.elementOverrides.maxWidth.enabled) {
                            throw new AssertionError(
                                    "Dragging the handle did not turn on the panel's max width.");
                        }
                    });
            // the pointer over the panel, so the editor describes it and its max width
            int[] pointer =
                    context.computeOnClient(
                            client -> {
                                ElementCorners corners = enchantBounds();
                                int scale = client.getWindow().getGuiScale();
                                return new int[] {
                                    (corners.left + corners.right) / 2 * scale,
                                    (corners.bottom - 6) * scale
                                };
                            });
            context.getInput().setCursorPos(pointer[0], pointer[1]);
            context.waitTicks(5);
            printScreenshot(context, "Enchant max width, editor", "enchant-max-width-editor");
            // Save & Close: the max width is written to the config file
            context.runOnClient(client -> ((HudElementEditor) client.gui.screen()).onClose());
            context.waitTicks(5);
            context.runOnClient(
                    client -> {
                        ElementCorners enchant = enchantBounds();
                        if (enchant.left != leftBefore[0]
                                || enchant.right > leftBefore[2] - MAX_WIDTH_GAP) {
                            throw new AssertionError(
                                    "Expected the capped panel to keep its left edge %d and end by %d, got %d to %d"
                                            .formatted(
                                                    leftBefore[0],
                                                    leftBefore[2] - MAX_WIDTH_GAP,
                                                    enchant.left,
                                                    enchant.right));
                        }
                        assertSavedEnchantMaxWidth(
                                client, enchantSettings().elementOverrides.maxWidth.value);
                        assertWrappedBetweenNames(new EnchantInfoHud().getLines());
                    });
            printScreenshot(context, "Enchant max width, left, on", "enchant-max-width-left-on");

            // right-anchored next to the clock, Max Width off: the panel reaches the Position HUD
            context.runOnClient(
                    client -> {
                        CoreSettings settings = enchantSettings();
                        turnMaxWidthOff(settings);
                        settings.elementAnchor = ElementAnchor.TOP_RIGHT;
                        settings.x = leftBefore[2] - MAX_WIDTH_GAP - HudRenderer.getScaledWidth();
                    });
            context.waitTicks(5);
            int[] rightBefore =
                    context.computeOnClient(
                            client -> {
                                ElementCorners enchant = enchantBounds();
                                ElementCorners position =
                                        bounds(
                                                ConfigUtils.getConfig()
                                                        .positionHudConfig
                                                        .coreSettings);
                                if (enchant.right != leftBefore[2] - MAX_WIDTH_GAP) {
                                    throw new AssertionError(
                                            "Expected the right-anchored panel to end at %d, got %d"
                                                    .formatted(
                                                            leftBefore[2] - MAX_WIDTH_GAP,
                                                            enchant.right));
                                }
                                System.out.println(
                                        "[STDOUT]: Right-anchored Enchant Info with Max Width off: %d to %d, Position HUD ends at %d"
                                                .formatted(
                                                        enchant.left,
                                                        enchant.right,
                                                        position.right));
                                return new int[] {enchant.right, position.right};
                            });
            printScreenshot(
                    context, "Enchant max width, right, off", "enchant-max-width-right-off");

            // the max width as the config screen sets it: Style Overrides > Max Width
            context.runOnClient(
                    client -> {
                        CoreSettings settings = enchantSettings();
                        int padding = HudRendererLib.getPadding(settings);
                        settings.elementOverrides.maxWidth.enabled = true;
                        settings.elementOverrides.maxWidth.value =
                                Math.min(
                                        240,
                                        rightBefore[0]
                                                - rightBefore[1]
                                                - MAX_WIDTH_GAP
                                                - padding * 4);
                    });
            context.waitTicks(5);
            context.runOnClient(
                    client -> {
                        ElementCorners enchant = enchantBounds();
                        if (enchant.right != rightBefore[0]
                                || enchant.left < rightBefore[1] + MAX_WIDTH_GAP) {
                            throw new AssertionError(
                                    "Expected the capped right-anchored panel to keep its right edge %d and start after %d, got %d to %d"
                                            .formatted(
                                                    rightBefore[0],
                                                    rightBefore[1] + MAX_WIDTH_GAP,
                                                    enchant.left,
                                                    enchant.right));
                        }
                        assertWrappedBetweenNames(new EnchantInfoHud().getLines());
                    });
            printScreenshot(context, "Enchant max width, right, on", "enchant-max-width-right-on");

            // the owner's GUI size (2560x1440 at GUI scale 3 is 853x480) at the default max width
            context.runOnClient(
                    client -> {
                        CoreSettings settings = enchantSettings();
                        HudElementEditor.clearMaxWidth(settings);
                        new HudPlacement(new ModConfig().enchantInfoHudConfig.coreSettings)
                                .applyTo(settings);
                    });
            setWindow(context, 1708, 960, 2);
            context.waitTicks(5);
            context.runOnClient(
                    client -> {
                        ElementCorners enchant = enchantBounds();
                        ElementCorners clock = bounds(clockSettings());
                        if (enchant.right >= clock.left) {
                            throw new AssertionError(
                                    "Expected the panel to end before the clock at %d at the owner's GUI size, but it ends at %d"
                                            .formatted(clock.left, enchant.right));
                        }
                    });
            printScreenshot(
                    context,
                    "Enchant max width, owner's GUI size, default",
                    "enchant-max-width-owner-gui");
        } finally {
            context.runOnClient(
                    client -> {
                        placements.forEach(HudPlacement::restore);
                        setRecorderPanelHidden(false);
                        HudElementEditor.clearMaxWidth(enchantSettings());
                        client.player.getInventory().setSelectedSlot(original[3]);
                        ConfigUtils.saveConfig();
                    });
            server.runOnServer(
                    minecraftServer ->
                            minecraftServer
                                    .getPlayerList()
                                    .getPlayers()
                                    .getFirst()
                                    .getInventory()
                                    .setItem(8, ItemStack.EMPTY));
            context.runOnClient(client -> client.options.guiScale().set(original[2]));
            context.getInput().resizeWindow(original[0], original[1]);
            context.runOnClient(Minecraft::resizeGui);
            context.waitTicks(5);
        }
    }

    private static final Identifier RECORDER_PANEL =
            Identifier.fromNamespaceAndPath(
                    "minecraftmodconventions", "gametest_recording_feedback");
    private static volatile boolean recorderPanelHidden;
    private static boolean recorderPanelWrapped;

    /**
     * Hides or shows MinecraftModConventions' GameTest step panel, which is drawn in the top right
     * while the recorder environment is on. Runs on the client thread.
     */
    private static void setRecorderPanelHidden(boolean hidden) {
        recorderPanelHidden = hidden;
        if (recorderPanelWrapped) return;
        recorderPanelWrapped = true;
        try {
            HudElementRegistry.replaceElement(
                    RECORDER_PANEL,
                    panel ->
                            (graphics, deltaTracker) -> {
                                if (!recorderPanelHidden) {
                                    panel.extractRenderState(graphics, deltaTracker);
                                }
                            });
        } catch (IllegalArgumentException notRegistered) {
            // the panel is registered only while the recorder environment is on
        }
    }

    /** Resizes the window, then sets the GUI scale, which only accepts scales the window fits. */
    private static void setWindow(ClientGameTestContext context, int width, int height, int scale) {
        context.getInput().resizeWindow(width, height);
        context.runOnClient(
                client -> {
                    client.options.guiScale().set(scale);
                    client.resizeGui();
                    if (client.getWindow().getGuiScale() != scale) {
                        throw new AssertionError(
                                "Expected GUI scale %d in a %dx%d window, got %d"
                                        .formatted(
                                                scale,
                                                width,
                                                height,
                                                client.getWindow().getGuiScale()));
                    }
                });
        context.waitTick();
    }

    private static void printScreenshot(ClientGameTestContext context, String label, String name) {
        System.out.println("[STDOUT]: " + label + " screenshot: " + context.takeScreenshot(name));
    }

    private static CoreSettings enchantSettings() {
        return ConfigUtils.getConfig().enchantInfoHudConfig.coreSettings;
    }

    private static CoreSettings clockSettings() {
        return ConfigUtils.getConfig().dateTimeHudConfig.coreSettings;
    }

    private static ElementCorners enchantBounds() {
        return bounds(enchantSettings());
    }

    /** Max Width as the config screen turns it off: the element's own override, on at 0. */
    private static void turnMaxWidthOff(CoreSettings settings) {
        settings.elementOverrides.maxWidth.enabled = true;
        settings.elementOverrides.maxWidth.value = 0;
    }

    /** The width of the widest line in GUI pixels, as the game's font measures it. */
    private static int widestLine(Minecraft client, List<Component> lines) {
        int widest = 0;
        for (Component line : lines) widest = Math.max(widest, client.font.width(line));
        return widest;
    }

    /** The element's bounds as it was last drawn. */
    private static ElementCorners bounds(CoreSettings settings) {
        ElementCorners corners =
                HudElementEditor.CORE_SETTINGS_ELEMENTS.get(settings.elementId).corners;
        if (corners.right <= corners.left) {
            throw new AssertionError(settings.elementName + " was not drawn.");
        }
        return corners;
    }

    private static MouseButtonEvent editorMouse(double x, double y) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0));
    }

    /**
     * Every line of the capped panel breaks at a separator: it ends in "," or " /" unless it ends
     * its section, and every enchantment name appears whole.
     */
    private static void assertWrappedBetweenNames(List<Component> lines) {
        List<String> rendered = lines.stream().map(Component::getString).toList();
        String text = String.join("\n", rendered);
        for (String line : SKYPORT_IRON_SWORD_LINES.subList(1, SKYPORT_IRON_SWORD_LINES.size())) {
            for (String name : line.replace("Missing: ", "").split(", |,$| / | /$")) {
                if (!name.isBlank() && !text.contains(name)) {
                    throw new AssertionError(
                            "Expected \"%s\" whole on one line of %s".formatted(name, rendered));
                }
            }
        }
        if (rendered.size() <= SKYPORT_IRON_SWORD_LINES.size()) {
            throw new AssertionError("Expected the max width to wrap the panel, got " + rendered);
        }
    }

    private static void assertSavedEnchantMaxWidth(Minecraft client, int expected) {
        Path file = client.gameDirectory.toPath().resolve("config/brainagehud.json");
        try {
            var saved =
                    com.google.gson.JsonParser.parseString(Files.readString(file))
                            .getAsJsonObject();
            var maxWidth =
                    saved.getAsJsonObject("enchantInfoHudConfig")
                            .getAsJsonObject("coreSettings")
                            .getAsJsonObject("elementOverrides")
                            .getAsJsonObject("maxWidth");
            if (!maxWidth.get("enabled").getAsBoolean()
                    || maxWidth.get("value").getAsInt() != expected) {
                throw new AssertionError(
                        "Expected the saved Enchant Info max width %d, got %s"
                                .formatted(expected, maxWidth));
            }
        } catch (IOException exception) {
            throw new AssertionError("Could not read " + file, exception);
        }
    }

    private static void exerciseEnchantCommands(ClientGameTestContext context) {
        context.runOnClient(client -> assertTooltipHighlighting(client));
        ClientGameTestRecorder.showStep(
                context,
                "enchant-commands",
                "Enchantment commands",
                "The held pickaxe and a named mace list their enchantments; lookup accepts IDs, names and partial names, and blacklist edits give success and duplicate warnings.");
        Set<Component> chat = new LinkedHashSet<>();
        List<String> original =
                context.computeOnClient(
                        client ->
                                List.copyOf(
                                        ConfigUtils.getConfig()
                                                .enchantInfoConfig
                                                .blacklistedEnchantmentIds));
        context.runOnClient(
                client ->
                        client.gui
                                .hud
                                .getChat()
                                .setVisibleMessageFilter(
                                        message -> {
                                            chat.add(message.content());
                                            return true;
                                        }));
        try {
            commandAndCheck(
                    context,
                    chat,
                    "getenchants",
                    "[BrainageHUD] Acceptable enchants for Diamond Pickaxe:");
            commandAndCheck(
                    context,
                    chat,
                    "getenchants minecraft:mace",
                    "[BrainageHUD] Acceptable enchants for Mace:");
            commandAndCheck(
                    context, chat, "getenchantinfo minecraft:fortune", "ID: minecraft:fortune");
            commandAndCheck(
                    context,
                    chat,
                    "getenchantinfo \"Fortune\"",
                    "[BrainageHUD] Enchant info for Fortune:");
            commandAndCheck(
                    context,
                    chat,
                    "getenchantinfo \"protect\"",
                    "[BrainageHUD] No exact match found. Potential matches:");
            commandAndCheck(
                    context,
                    chat,
                    "getenchantinfo \"not an enchantment\"",
                    "[BrainageHUD] No potential matches found!");
            context.runOnClient(
                    client ->
                            ConfigUtils.getConfig()
                                    .enchantInfoConfig
                                    .blacklistedEnchantmentIds
                                    .remove("minecraft:unbreaking"));
            commandAndCheck(
                    context,
                    chat,
                    "blacklistedenchants add minecraft:unbreaking",
                    "[BrainageHUD] Unbreaking is now blacklisted.");
            commandAndCheck(
                    context,
                    chat,
                    "blacklistedenchants add minecraft:unbreaking",
                    "[BrainageHUD] Unbreaking is already blacklisted!");
            commandAndCheck(context, chat, "blacklistedenchants query", " - Unbreaking");
            commandAndCheck(
                    context,
                    chat,
                    "blacklistedenchants remove minecraft:unbreaking",
                    "[BrainageHUD] Unbreaking is no longer blacklisted.");
            commandAndCheck(
                    context,
                    chat,
                    "blacklistedenchants remove minecraft:unbreaking",
                    "[BrainageHUD] Unbreaking is not blacklisted!");
        } finally {
            context.runOnClient(
                    client -> {
                        client.gui.hud.getChat().setVisibleMessageFilter(message -> true);
                        ConfigUtils.getConfig().enchantInfoConfig.blacklistedEnchantmentIds =
                                new java.util.ArrayList<>(original);
                        ConfigUtils.saveConfig();
                    });
        }
    }

    private static void commandAndCheck(
            ClientGameTestContext context, Set<Component> chat, String command, String expected) {
        String screenshotName = command.replaceAll("[^a-zA-Z0-9]+", "-");
        ClientGameTestRecorder.showStep(
                context,
                "command-" + screenshotName,
                "/" + command,
                "Expanded chat shows the complete command response.");
        context.runOnClient(
                client -> {
                    chat.clear();
                    client.gui.hud.getChat().clearMessages(false);
                });
        runClientCommand(context, command);
        context.waitTicks(5);
        context.runOnClient(
                client -> {
                    if (chat.stream().noneMatch(line -> line.getString().equals(expected))) {
                        throw new AssertionError(
                                "Command /"
                                        + command
                                        + " did not print "
                                        + expected
                                        + "; got "
                                        + chat);
                    }
                });
        context.getInput().pressKey(options -> options.keyChat);
        try {
            context.waitTicks(35);
            context.takeScreenshot(screenshotName);
        } finally {
            context.runOnClient(client -> client.gui.setScreen(null));
        }
    }

    private static void exerciseInventoryStats(ClientGameTestContext context) {
        ClientGameTestRecorder.showStep(
                context,
                "inventory-stats",
                "Inventory Stats includes armour",
                "The Inventory Stats key lists every occupied slot, including all four armour slots and the off hand.");
        Set<Component> chat = new LinkedHashSet<>();
        KeyMapping key = KeyMapping.get("key.brainagehud.inventoryStats");
        String previousKey = context.computeOnClient(client -> key.saveString());
        context.runOnClient(
                client -> {
                    key.setKey(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_I));
                    KeyMapping.resetMapping();
                    client.gui.hud.getChat().clearMessages(false);
                    client.gui
                            .hud
                            .getChat()
                            .setVisibleMessageFilter(
                                    message -> {
                                        chat.add(message.content());
                                        return true;
                                    });
                });
        try {
            context.getInput().pressKey(key);
            context.waitTicks(10);
            context.runOnClient(
                    client -> {
                        for (String expected :
                                List.of(
                                        "Feet: 1 × minecraft:diamond_boots",
                                        "Legs: 1 × minecraft:diamond_leggings",
                                        "Chest: 1 × minecraft:diamond_chestplate",
                                        "Head: 1 × minecraft:diamond_helmet",
                                        "Off hand: 1 × minecraft:shield")) {
                            if (chat.stream()
                                    .noneMatch(line -> line.getString().equals(expected))) {
                                throw new AssertionError(
                                        "Inventory Stats omitted " + expected + "; got " + chat);
                            }
                        }
                    });
            context.getInput().pressKey(options -> options.keyChat);
            context.waitTicks(60);
            context.takeScreenshot("inventory-stats-armour");
        } finally {
            context.runOnClient(
                    client -> {
                        client.gui.setScreen(null);
                        key.setKey(InputConstants.getKey(previousKey));
                        KeyMapping.resetMapping();
                        client.gui.hud.getChat().setVisibleMessageFilter(message -> true);
                    });
        }
    }

    private static void exerciseFullbright(
            ClientGameTestContext context,
            net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext server) {
        context.getInput().pressKey(options -> options.keyUse);
        context.getInput().pressKey(options -> options.keyHotbarSlots[0]);
        context.waitTicks(5);
        server.runOnServer(
                minecraftServer -> {
                    ServerPlayer player = minecraftServer.getPlayerList().getPlayers().getFirst();
                    for (BlockPos pos : BlockPos.betweenClosed(37, -53, -3, 43, -47, 3)) {
                        boolean wall =
                                pos.getX() == 37
                                        || pos.getX() == 43
                                        || pos.getY() == -53
                                        || pos.getY() == -47
                                        || pos.getZ() == -3
                                        || pos.getZ() == 3;
                        player.level()
                                .setBlockAndUpdate(
                                        pos,
                                        wall
                                                ? Blocks.STONE.defaultBlockState()
                                                : Blocks.AIR.defaultBlockState());
                    }
                    player.teleportTo(40.5, -52, 0.5);
                    player.setYRot(0);
                    player.setXRot(0);
                });
        context.waitTicks(60);
        float original =
                context.computeOnClient(
                        client -> ConfigUtils.getConfig().qualityOfLifeConfig.fullbright);
        boolean[] overlays =
                context.computeOnClient(
                        client -> {
                            ModConfig config = ConfigUtils.getConfig();
                            boolean[] originalOverlays = {
                                config.reachHudConfig.coreSettings.enabled,
                                config.waypointConfig.showInWorld
                            };
                            // Measure the stone wall, not Reach HUD text or a waypoint label over
                            // the sampled pixels.
                            config.reachHudConfig.coreSettings.enabled = false;
                            config.waypointConfig.showInWorld = false;
                            return originalOverlays;
                        });
        try {
            double[] brightness = new double[3];
            int[] amounts = {0, 1, -1};
            for (int index = 0; index < amounts.length; index++) {
                int amount = amounts[index];
                ClientGameTestRecorder.showStep(
                        context,
                        "fullbright-" + amount,
                        "Fullbright " + amount,
                        "A sealed, unlit stone room shows actual terrain lighting: normal at 0, fully lit at 1, black at -1.");
                runClientCommand(context, "fullbright " + amount);
                context.waitTicks(80);
                brightness[index] =
                        centreBrightness(context.takeScreenshot("fullbright-" + amount));
            }
            if (brightness[1] < brightness[0] + 30 || brightness[2] > brightness[1] / 4) {
                throw new AssertionError(
                        "Fullbright did not change terrain lighting: "
                                + java.util.Arrays.toString(brightness));
            }
        } finally {
            context.runOnClient(
                    client -> {
                        ConfigUtils.getConfig().qualityOfLifeConfig.fullbright = original;
                        ConfigUtils.getConfig().reachHudConfig.coreSettings.enabled = overlays[0];
                        ConfigUtils.getConfig().waypointConfig.showInWorld = overlays[1];
                        ConfigUtils.saveConfig();
                    });
        }
    }

    private static double centreBrightness(Path screenshot) {
        try {
            var image = ImageIO.read(screenshot.toFile());
            long total = 0;
            for (int y = image.getHeight() / 2 + 20; y < image.getHeight() / 2 + 70; y++) {
                for (int x = image.getWidth() / 2 + 20; x < image.getWidth() / 2 + 70; x++) {
                    int pixel = image.getRGB(x, y);
                    total += ((pixel >> 16) & 255) + ((pixel >> 8) & 255) + (pixel & 255);
                }
            }
            return total / 7500.0;
        } catch (IOException exception) {
            throw new AssertionError("Could not read lighting screenshot " + screenshot, exception);
        }
    }

    private static void assertTooltipHighlighting(Minecraft client) {
        var registry = client.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var mending = registry.getOrThrow(enchantmentKey("minecraft:mending"));
        var efficiency = registry.getOrThrow(enchantmentKey("minecraft:efficiency"));
        var config = ConfigUtils.getConfig().enchantInfoConfig;
        boolean original = config.highlightMaxLevelEnchants;
        try {
            ItemStack pickaxe = client.player.getMainHandItem();
            List<Component> lines =
                    new java.util.ArrayList<>(
                            List.of(
                                    pickaxe.getHoverName(),
                                    Enchantment.getFullname(efficiency, 3),
                                    Enchantment.getFullname(mending, 1),
                                    Component.literal("Mending lore")));
            config.highlightMaxLevelEnchants = false;
            ModTooltipEvents.onItemTooltip(pickaxe, lines);
            if (lines.get(2).getStyle().isBold())
                throw new AssertionError("Disabled tooltip highlighting still bolded Mending.");
            config.highlightMaxLevelEnchants = true;
            ModTooltipEvents.onItemTooltip(pickaxe, lines);
            if (!lines.get(2).getStyle().isBold()
                    || lines.get(0).getStyle().isBold()
                    || lines.get(1).getStyle().isBold()
                    || lines.get(3).getStyle().isBold()) {
                throw new AssertionError("Tooltip highlighting changed the wrong item lines.");
            }
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            ItemEnchantments.Mutable stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            stored.set(mending, 1);
            book.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
            List<Component> bookLines =
                    new java.util.ArrayList<>(
                            List.of(book.getHoverName(), Enchantment.getFullname(mending, 1)));
            ModTooltipEvents.onItemTooltip(book, bookLines);
            if (!bookLines.get(1).getStyle().isBold())
                throw new AssertionError("Stored enchantments in books were not highlighted.");
        } finally {
            config.highlightMaxLevelEnchants = original;
        }
    }

    private static void exerciseWaypointCommands(ClientGameTestContext context) {
        if (!ClientGameTestRecordingSession.isEnabled()) {
            assertWaypointEditSurvivesResize(context);
        }
        ClientGameTestRecorder.showStep(
                context,
                "waypoint-commands",
                "Waypoint command persistence",
                "A quoted name and explicit coordinates are saved, a case-insensitive duplicate is rejected, and removal updates the file.");
        Set<Component> chat = new LinkedHashSet<>();
        context.runOnClient(
                client ->
                        client.gui
                                .hud
                                .getChat()
                                .setVisibleMessageFilter(
                                        message -> {
                                            chat.add(message.content());
                                            return true;
                                        }));
        try {
            commandAndCheck(
                    context,
                    chat,
                    "waypoints add \"Quoted Base\" 1 -60 3",
                    "[BrainageHUD] Created waypoint Quoted Base at 1, -60, 3.");
            context.runOnClient(client -> assertSavedWaypoint(client, "Quoted Base", true));
            commandAndCheck(
                    context,
                    chat,
                    "waypoints add \"quoted base\"",
                    "[BrainageHUD] A waypoint called \"quoted base\" already exists.");
            commandAndCheck(
                    context,
                    chat,
                    "waypoints remove \"QUOTED BASE\"",
                    "[BrainageHUD] Removed waypoint Quoted Base.");
            context.runOnClient(client -> assertSavedWaypoint(client, "Quoted Base", false));
            commandAndCheck(
                    context,
                    chat,
                    "waypoints remove \"Quoted Base\"",
                    "[BrainageHUD] There is no waypoint called \"Quoted Base\".");
        } finally {
            context.runOnClient(
                    client -> client.gui.hud.getChat().setVisibleMessageFilter(message -> true));
        }
    }

    private static void assertSavedWaypoint(Minecraft client, String name, boolean present) {
        Path file = client.gameDirectory.toPath().resolve("config/brainagehud/waypoints.json");
        try {
            var saved =
                    com.google.gson.JsonParser.parseString(Files.readString(file))
                            .getAsJsonObject()
                            .getAsJsonArray(WaypointStore.currentWorldKey().orElseThrow());
            boolean found = false;
            for (var entry : saved) {
                var waypoint = entry.getAsJsonObject();
                if (waypoint.get("name").getAsString().equals(name)) {
                    found = true;
                    if (waypoint.get("x").getAsInt() != 1
                            || waypoint.get("y").getAsInt() != -60
                            || waypoint.get("z").getAsInt() != 3) {
                        throw new AssertionError(
                                "Saved waypoint has the wrong position: " + waypoint);
                    }
                }
            }
            if (found != present)
                throw new AssertionError(
                        "Saved waypoint presence was " + found + ", expected " + present);
        } catch (IOException exception) {
            throw new AssertionError("Cannot read saved waypoints", exception);
        }
    }

    private static void assertWaypointEditSurvivesResize(ClientGameTestContext context) {
        int[] original =
                context.computeOnClient(
                        client ->
                                new int[] {
                                    client.getWindow().getWidth(), client.getWindow().getHeight()
                                });
        List<String> values = List.of("Unsubmitted Base", "123", "bad input", "-456");
        context.runOnClient(
                client -> {
                    client.gui.setScreen(new WaypointEditScreen(null, null));
                    var boxes =
                            client.gui.screen().children().stream()
                                    .filter(EditBox.class::isInstance)
                                    .map(EditBox.class::cast)
                                    .toList();
                    for (int index = 0; index < values.size(); index++)
                        boxes.get(index).setValue(values.get(index));
                });
        try {
            context.getInput().resizeWindow(original[0] + 80, original[1] + 40);
            context.waitTicks(2);
            context.runOnClient(
                    client -> {
                        var boxes =
                                client.gui.screen().children().stream()
                                        .filter(EditBox.class::isInstance)
                                        .map(EditBox.class::cast)
                                        .toList();
                        if (!boxes.stream().map(EditBox::getValue).toList().equals(values)) {
                            throw new AssertionError(
                                    "Resizing discarded unsubmitted waypoint edits.");
                        }
                        boxes.getFirst().setValue("");
                    });
            context.getInput().resizeWindow(original[0], original[1]);
            context.waitTicks(2);
            context.runOnClient(
                    client -> {
                        var boxes =
                                client.gui.screen().children().stream()
                                        .filter(EditBox.class::isInstance)
                                        .map(EditBox.class::cast)
                                        .toList();
                        if (!boxes.stream()
                                .map(EditBox::getValue)
                                .toList()
                                .equals(List.of("", "123", "bad input", "-456"))) {
                            throw new AssertionError(
                                    "Resizing replaced an intentionally blank waypoint name.");
                        }
                    });
        } finally {
            context.runOnClient(client -> client.gui.setScreen(null));
            context.getInput().resizeWindow(original[0], original[1]);
        }
    }

    private static void preparePlayer(ServerPlayer player) {
        player.getInventory().clearContent();
        HolderLookup.RegistryLookup<Enchantment> enchantments =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.enchant(enchantments.getOrThrow(enchantmentKey("minecraft:efficiency")), 3);
        pickaxe.enchant(enchantments.getOrThrow(enchantmentKey("minecraft:unbreaking")), 3);
        pickaxe.enchant(enchantments.getOrThrow(enchantmentKey("minecraft:mending")), 1);
        player.getInventory().setItem(0, pickaxe);
        player.getInventory().setSelectedSlot(0);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        player.getInventory().setItem(1, new ItemStack(Items.FISHING_ROD));
        player.getInventory().setItem(2, new ItemStack(Items.ARROW, 64));
        player.getInventory().setItem(20, new ItemStack(Items.ARROW, 32));
        player.getInventory().setItem(3, new ItemStack(Items.ENDER_PEARL, 16));
        player.getInventory().setItem(4, new ItemStack(Items.BREAD, 12));
        player.getInventory().setItem(21, new ItemStack(Items.COOKED_BEEF, 5));
        // a pool wide and deep enough for open water wherever the cast lands: its water surface is
        // the flat world's grass layer, south of the player who faces south
        for (BlockPos pos : BlockPos.betweenClosed(4, -62, -7, 20, -61, 16)) {
            player.level().setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        }
        player.teleportTo(12.5D, -60.0D, -8.5D);
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        player.setOnGround(true);
    }

    private static ShowcaseConfigSnapshot configureAndAssertShowcase() {
        if (!BrainageHUD.isInitialized()) {
            throw new AssertionError(
                    "Expected BrainageHUD's client initializer to register every HUD before recording.");
        }

        ShowcaseConfigSnapshot snapshot = new ShowcaseConfigSnapshot(ConfigUtils.getConfig());
        try {
            ModConfig config = snapshot.config;
            config.positionHudConfig.coreSettings.enabled = true;
            config.positionHudConfig.showPosition = true;
            config.positionHudConfig.showChunkPosition = true;
            config.positionHudConfig.cCounter = true;
            config.positionHudConfig.eCounter = true;
            config.positionHudConfig.showDirection = true;
            config.positionHudConfig.showRotation = true;
            config.performanceHudConfig.coreSettings.enabled = true;
            config.networkHudConfig.coreSettings.enabled = true;
            config.reachHudConfig.coreSettings.enabled = true;
            config.toggleSprintHudConfig.coreSettings.enabled = true;
            config.armourInfoHudConfig.coreSettings.enabled = true;
            config.armourInfoHudConfig.showArmour = true;
            config.armourInfoHudConfig.showMainHand = true;
            config.armourInfoHudConfig.showItemNames = true;
            config.keystrokesHudConfig.coreSettings.enabled = true;
            config.keystrokesHudConfig.showWasd = true;
            config.keystrokesHudConfig.showSpace = true;
            config.keystrokesHudConfig.showMouseButtons = true;
            config.fishingHudConfig.coreSettings.enabled = true;
            config.motionHudConfig.coreSettings.enabled = true;
            config.entityHudConfig.coreSettings.enabled = true;
            config.projectileHudConfig.coreSettings.enabled = true;
            config.projectileHudConfig.showSlotCounts = true;
            config.foodHudConfig.coreSettings.enabled = true;
            config.waypointConfig.showInWorld = true;
            config.statusEffectHudConfig.coreSettings.enabled = true;
            config.statusEffectHudConfig.showDurations = true;
            config.statusEffectHudConfig.hideVanillaStatusEffects = true;
            libraryConfig().showVanillaStatusEffects = true;
            config.waypointConfig.showWorldCentre = true;
            config.qualityOfLifeConfig.fullbright = 0;
            config.foodHudConfig.showSlotCounts = false;
            // Leave the recorder's top-right title card clear without hiding any showcased element.
            config.dateTimeHudConfig.coreSettings.elementAnchor = ElementAnchor.TOP_LEFT;
            config.dateTimeHudConfig.coreSettings.x = 150;
            config.dateTimeHudConfig.coreSettings.y = 85;
            config.entityHudConfig.coreSettings.elementAnchor = ElementAnchor.TOP_LEFT;
            config.entityHudConfig.coreSettings.x = 5;
            config.entityHudConfig.coreSettings.y = 195;
            config.motionHudConfig.coreSettings.elementAnchor = ElementAnchor.TOP_LEFT;
            config.motionHudConfig.coreSettings.x = 5;
            config.motionHudConfig.coreSettings.y = 265;
            config.keystrokesHudConfig.coreSettings.y = 130;

            assertPositionHudShowsFixture();
            return snapshot;
        } catch (RuntimeException | Error exception) {
            snapshot.restore();
            throw exception;
        }
    }

    private record HudPlacement(CoreSettings settings, int x, int y, ElementAnchor anchor) {
        private HudPlacement(CoreSettings settings) {
            this(settings, settings.x, settings.y, settings.elementAnchor);
        }

        private void restore() {
            applyTo(settings);
        }

        private void applyTo(CoreSettings target) {
            target.x = x;
            target.y = y;
            target.elementAnchor = anchor;
        }
    }

    private static final class ShowcaseConfigSnapshot {
        private final ModConfig config;
        private final List<HudPlacement> placements;
        private final boolean positionEnabled;
        private final boolean showPosition;
        private final boolean showChunkPosition;
        private final boolean cCounter;
        private final boolean eCounter;
        private final boolean showDirection;
        private final boolean showRotation;
        private final boolean performanceEnabled;
        private final boolean networkEnabled;
        private final boolean reachEnabled;
        private final boolean toggleSprintEnabled;
        private final boolean armourInfoEnabled;
        private final boolean showArmour;
        private final boolean showMainHand;
        private final boolean showItemNames;
        private final boolean keystrokesEnabled;
        private final boolean showWasd;
        private final boolean showSpace;
        private final boolean showMouseButtons;
        private final boolean fishingEnabled;
        private final boolean motionEnabled;
        private final boolean entityEnabled;
        private final boolean projectileEnabled;
        private final boolean projectileSlotCounts;
        private final boolean foodEnabled;
        private final boolean waypointEnabled;
        private final boolean statusEnabled;
        private final boolean statusDurations;
        private final boolean hideVanillaEffects;
        private final boolean showVanillaEffects;
        private final boolean worldCentre;
        private final float fullbright;
        private final boolean foodSlotCounts;

        private ShowcaseConfigSnapshot(ModConfig config) {
            this.config = config;
            placements =
                    List.of(
                            new HudPlacement(config.dateTimeHudConfig.coreSettings),
                            new HudPlacement(config.entityHudConfig.coreSettings),
                            new HudPlacement(config.motionHudConfig.coreSettings),
                            new HudPlacement(config.keystrokesHudConfig.coreSettings));
            positionEnabled = config.positionHudConfig.coreSettings.enabled;
            showPosition = config.positionHudConfig.showPosition;
            showChunkPosition = config.positionHudConfig.showChunkPosition;
            cCounter = config.positionHudConfig.cCounter;
            eCounter = config.positionHudConfig.eCounter;
            showDirection = config.positionHudConfig.showDirection;
            showRotation = config.positionHudConfig.showRotation;
            performanceEnabled = config.performanceHudConfig.coreSettings.enabled;
            networkEnabled = config.networkHudConfig.coreSettings.enabled;
            reachEnabled = config.reachHudConfig.coreSettings.enabled;
            toggleSprintEnabled = config.toggleSprintHudConfig.coreSettings.enabled;
            armourInfoEnabled = config.armourInfoHudConfig.coreSettings.enabled;
            showArmour = config.armourInfoHudConfig.showArmour;
            showMainHand = config.armourInfoHudConfig.showMainHand;
            showItemNames = config.armourInfoHudConfig.showItemNames;
            keystrokesEnabled = config.keystrokesHudConfig.coreSettings.enabled;
            showWasd = config.keystrokesHudConfig.showWasd;
            showSpace = config.keystrokesHudConfig.showSpace;
            showMouseButtons = config.keystrokesHudConfig.showMouseButtons;
            fishingEnabled = config.fishingHudConfig.coreSettings.enabled;
            motionEnabled = config.motionHudConfig.coreSettings.enabled;
            entityEnabled = config.entityHudConfig.coreSettings.enabled;
            projectileEnabled = config.projectileHudConfig.coreSettings.enabled;
            projectileSlotCounts = config.projectileHudConfig.showSlotCounts;
            foodEnabled = config.foodHudConfig.coreSettings.enabled;
            waypointEnabled = config.waypointConfig.showInWorld;
            statusEnabled = config.statusEffectHudConfig.coreSettings.enabled;
            statusDurations = config.statusEffectHudConfig.showDurations;
            hideVanillaEffects = config.statusEffectHudConfig.hideVanillaStatusEffects;
            showVanillaEffects = libraryConfig().showVanillaStatusEffects;
            worldCentre = config.waypointConfig.showWorldCentre;
            fullbright = config.qualityOfLifeConfig.fullbright;
            foodSlotCounts = config.foodHudConfig.showSlotCounts;
        }

        private void restore() {
            config.positionHudConfig.coreSettings.enabled = positionEnabled;
            config.positionHudConfig.showPosition = showPosition;
            config.positionHudConfig.showChunkPosition = showChunkPosition;
            config.positionHudConfig.cCounter = cCounter;
            config.positionHudConfig.eCounter = eCounter;
            config.positionHudConfig.showDirection = showDirection;
            config.positionHudConfig.showRotation = showRotation;
            config.performanceHudConfig.coreSettings.enabled = performanceEnabled;
            config.networkHudConfig.coreSettings.enabled = networkEnabled;
            config.reachHudConfig.coreSettings.enabled = reachEnabled;
            config.toggleSprintHudConfig.coreSettings.enabled = toggleSprintEnabled;
            config.armourInfoHudConfig.coreSettings.enabled = armourInfoEnabled;
            config.armourInfoHudConfig.showArmour = showArmour;
            config.armourInfoHudConfig.showMainHand = showMainHand;
            config.armourInfoHudConfig.showItemNames = showItemNames;
            config.keystrokesHudConfig.coreSettings.enabled = keystrokesEnabled;
            config.keystrokesHudConfig.showWasd = showWasd;
            config.keystrokesHudConfig.showSpace = showSpace;
            config.keystrokesHudConfig.showMouseButtons = showMouseButtons;
            config.fishingHudConfig.coreSettings.enabled = fishingEnabled;
            config.motionHudConfig.coreSettings.enabled = motionEnabled;
            config.entityHudConfig.coreSettings.enabled = entityEnabled;
            config.projectileHudConfig.coreSettings.enabled = projectileEnabled;
            config.projectileHudConfig.showSlotCounts = projectileSlotCounts;
            config.foodHudConfig.coreSettings.enabled = foodEnabled;
            config.waypointConfig.showInWorld = waypointEnabled;
            config.statusEffectHudConfig.coreSettings.enabled = statusEnabled;
            config.statusEffectHudConfig.showDurations = statusDurations;
            config.statusEffectHudConfig.hideVanillaStatusEffects = hideVanillaEffects;
            libraryConfig().showVanillaStatusEffects = showVanillaEffects;
            config.waypointConfig.showWorldCentre = worldCentre;
            config.qualityOfLifeConfig.fullbright = fullbright;
            config.foodHudConfig.showSlotCounts = foodSlotCounts;
            placements.forEach(HudPlacement::restore);
            ConfigUtils.saveConfig();
        }
    }

    private static void clickAt(ClientGameTestContext context, int x, int y) {
        int scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
        context.getInput().setCursorPos(x * scale + scale / 2.0D, y * scale + scale / 2.0D);
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.waitTicks(2);
    }

    private static void clickButton(ClientGameTestContext context, String label) {
        Button button = context.computeOnClient(client -> findButton(client, label));
        clickAt(
                context,
                button.getX() + button.getWidth() / 2,
                button.getY() + button.getHeight() / 2);
    }

    private static Button findButton(Minecraft client, String label) {
        return client.gui.screen().children().stream()
                .filter(
                        child ->
                                child instanceof Button button
                                        && button.getMessage().getString().equals(label))
                .map(Button.class::cast)
                .findFirst()
                .orElseThrow(
                        () ->
                                new AssertionError(
                                        "Expected a "
                                                + label
                                                + " button on "
                                                + client.gui.screen()
                                                + "."));
    }

    private static void assertButtons(Minecraft client, Map<String, Boolean> expectedActive) {
        expectedActive.forEach(
                (label, active) -> {
                    if (findButton(client, label).active != active) {
                        throw new AssertionError(
                                "Expected the "
                                        + label
                                        + " button to be "
                                        + (active ? "active" : "inactive")
                                        + " with the World Centre selected.");
                    }
                });
    }

    /**
     * The default keys by name (B, U, K, Numpad Minus, Inventory Stats unbound). Names, not the
     * code constants the mod uses, so a GLFW-era or otherwise wrong code fails here.
     */
    private static void assertDefaultKeys() {
        Map<String, String> expected =
                Map.of(
                        "key.brainagehud.createWaypoint", "key.keyboard.b",
                        "key.brainagehud.manageWaypoints", "key.keyboard.u",
                        "key.brainagehud.toggleEnchantInfoCompact", "key.keyboard.k",
                        "key.brainagehud.openConfig", "key.keyboard.keypad.subtract",
                        "key.brainagehud.inventoryStats", "key.keyboard.unknown");
        expected.forEach(
                (name, keyName) -> {
                    String actual = KeyMapping.get(name).getDefaultKey().getName();
                    if (!actual.equals(keyName)) {
                        throw new AssertionError(
                                name + " defaults to " + actual + ", expected " + keyName);
                    }
                });
    }

    private static void assertEditorKnowsEveryElement() {
        for (var element : HudRenderer.REGISTERED_ELEMENTS) {
            int id = element.getElementConfig().getCoreSettings().elementId;
            if (!HudElementEditor.CORE_SETTINGS_ELEMENTS.containsKey(id)) {
                throw new AssertionError(
                        "Expected the element editor to know "
                                + element.getElementConfig().getCoreSettings().elementName
                                + " (ID "
                                + id
                                + ") after reloading the configs, but it only knows "
                                + HudElementEditor.CORE_SETTINGS_ELEMENTS.keySet()
                                + ".");
            }
        }
    }

    private static void assertQuickWaypointCreated() {
        List<Waypoint> waypoints =
                WaypointStore.current()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                "Expected waypoints to be available in a world."));
        if (waypoints.size() != 1 || !waypoints.getFirst().name.equals("Waypoint 1")) {
            throw new AssertionError(
                    "Expected the Create Waypoint key to create \"Waypoint 1\", but the waypoints are "
                            + waypoints.stream().map(waypoint -> waypoint.name).toList()
                            + ".");
        }
        BlockPos pos = waypoints.getFirst().pos();
        if (!pos.equals(new BlockPos(12, -60, -9))) {
            throw new AssertionError(
                    "Expected \"Waypoint 1\" where the player stands, at 12, -60, -9, but it is at "
                            + pos.toShortString()
                            + ".");
        }
    }

    /**
     * Runs a command as if typed in chat; Fabric runs client commands without sending them to the
     * server.
     */
    private static void runClientCommand(ClientGameTestContext context, String command) {
        context.runOnClient(client -> client.getConnection().sendCommand(command));
    }

    private static void assertChatMessage(
            Set<Component> chat, String expected, String bodyStart, ChatFormatting colour) {
        Component message =
                chat.stream()
                        .filter(line -> line.getString().equals(expected))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                "Expected the chat to show \""
                                                        + expected
                                                        + "\", but it shows "
                                                        + chat.stream()
                                                                .map(Component::getString)
                                                                .toList()
                                                        + "."));
        TextColor[] bodyColour = new TextColor[1];
        message.visit(
                (style, text) -> {
                    if (text.startsWith(bodyStart)) {
                        bodyColour[0] = style.getColor();
                        return Optional.of(true);
                    }
                    return Optional.empty();
                },
                Style.EMPTY);
        if (!TextColor.fromLegacyFormat(colour).equals(bodyColour[0])) {
            throw new AssertionError(
                    "Expected \""
                            + expected
                            + "\" in "
                            + colour
                            + ", but its colour is "
                            + bodyColour[0]
                            + ".");
        }
    }

    private static void assertHudLines(String hud, List<Component> actual, List<String> expected) {
        List<String> lines = actual.stream().map(Component::getString).toList();
        if (!lines.equals(expected)) {
            throw new AssertionError(
                    "Expected the "
                            + hud
                            + " HUD to show "
                            + expected
                            + ", but got "
                            + lines
                            + ".");
        }
    }

    private static HudRendererLibConfig libraryConfig() {
        return io.github.brainage04.hudrendererlib.util.ConfigUtils.getConfig();
    }

    private static void assertTopRightShift(int expectedShift) {
        CoreSettings keystrokes = ConfigUtils.getConfig().keystrokesHudConfig.coreSettings;
        int unshifted = keystrokes.y + HudRenderer.getYOffset(keystrokes, 0);
        int shift = HudRenderer.getPosY(keystrokes, 0) - unshifted;
        if (shift != expectedShift) {
            throw new AssertionError(
                    "Expected top-right elements to be shifted down by "
                            + expectedShift
                            + " with Show Vanilla Status Effects "
                            + (libraryConfig().showVanillaStatusEffects ? "on" : "off")
                            + " and the Status Effect HUD's Hide Vanilla Status Effects "
                            + (ConfigUtils.getConfig()
                                            .statusEffectHudConfig
                                            .hideVanillaStatusEffects
                                    ? "on"
                                    : "off")
                            + ", but they are shifted by "
                            + shift
                            + ".");
        }
    }

    private static void assertFishingHudLines(List<String> expected) {
        List<String> lines =
                new FishingHud().getLines().stream().map(line -> line.getString()).toList();
        if (!lines.equals(expected)) {
            throw new AssertionError(
                    "Expected the Fishing HUD to show " + expected + ", but got " + lines + ".");
        }
    }

    private static void assertPositionHudShowsFixture() {
        List<String> lines =
                new PositionHud().getLines().stream().map(line -> line.getString()).toList();
        boolean hasPosition = lines.stream().anyMatch(line -> line.startsWith("X: 12.5"));
        boolean hasCCounter = lines.stream().anyMatch(line -> line.startsWith("C: "));
        boolean facesSouth = lines.stream().anyMatch(line -> line.startsWith("S (+Z)"));

        if (!hasPosition || !hasCCounter || !facesSouth) {
            throw new AssertionError(
                    "Expected Position HUD to show the fixture position, rendered-section C-counter and south-facing direction, but got "
                            + lines
                            + ".");
        }
    }

    /**
     * The Position HUD's C and E lines read what the debug screen's (F3) chunk and entity lines
     * read, with the renderer this run installs: vanilla's, or Sodium's in {@code
     * runProductionClientGameTestSodium}.
     */
    private static void assertCountersMatchDebugScreen(Minecraft client) {
        boolean sodiumRun = Boolean.getBoolean(SODIUM_RUN_PROPERTY);
        if (FabricLoader.getInstance().isModLoaded("sodium") != sodiumRun) {
            throw new AssertionError(
                    "Expected Sodium to be "
                            + (sodiumRun ? "installed" : "absent")
                            + " in this client GameTest run.");
        }

        List<String> lines =
                new PositionHud().getLines().stream().map(line -> line.getString()).toList();
        String sectionStatistics = client.levelExtractor.sectionStatistics();
        Matcher debugCounts =
                DEBUG_SECTION_COUNTS.matcher(sectionStatistics == null ? "" : sectionStatistics);
        if (!debugCounts.lookingAt() || Integer.parseInt(debugCounts.group(2)) <= 1) {
            throw new AssertionError(
                    "Expected the debug screen's chunk line to count rendered sections out of more than one, but it reads "
                            + sectionStatistics
                            + ".");
        }
        String expectedCCounter = debugCounts.group() + (client.smartCull ? " (s)" : "");
        if (!lines.contains(expectedCCounter)) {
            throw new AssertionError(
                    "Expected the Position HUD's C counter to read \""
                            + expectedCCounter
                            + "\" like the debug screen's \""
                            + sectionStatistics
                            + "\", but the HUD shows "
                            + lines
                            + ".");
        }

        String entityStatistics = client.levelExtractor.entityStatistics();
        if (!lines.contains(entityStatistics)) {
            throw new AssertionError(
                    "Expected the Position HUD's E counter to read \""
                            + entityStatistics
                            + "\" like the debug screen, but the HUD shows "
                            + lines
                            + ".");
        }
    }

    private static void assertNetworkHudShowsServerTps() {
        List<String> lines =
                new NetworkHud().getLines().stream().map(line -> line.getString()).toList();
        String tpsLine =
                lines.stream()
                        .filter(line -> line.startsWith("TPS: "))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                "Expected Network HUD to show TPS, but got "
                                                        + lines
                                                        + "."));
        double tps;
        try {
            tps = Double.parseDouble(tpsLine.substring("TPS: ".length()).replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new AssertionError(
                    "Expected a measured TPS after several server time reports, but got "
                            + lines
                            + ".",
                    exception);
        }

        // an idle local server ticks at its 20 TPS target; the lower bound allows for a slow CI
        // machine
        if (tps < 10.0D || tps > 20.0D) {
            throw new AssertionError(
                    "Expected the idle dedicated server to measure close to 20 TPS, but got "
                            + lines
                            + ".");
        }
    }

    private static void assertAttackClicksCounted(int expectedClicks) {
        int clicks = ModClickEvents.getAttackClicksPerSecond();
        if (clicks != expectedClicks) {
            throw new AssertionError(
                    "Expected "
                            + expectedClicks
                            + " attack clicks in the last second, but counted "
                            + clicks
                            + ".");
        }
    }

    private static void assertEnchantInfoHudShowsHeldItem() {
        List<String> lines =
                new EnchantInfoHud().getLines().stream().map(line -> line.getString()).toList();
        boolean hasEfficiency = lines.stream().anyMatch(line -> line.startsWith("Efficiency"));
        // the header is its own line in the full format and the start of the missing line in the
        // compact one, which is the default
        boolean hasMissingHeader =
                lines.stream()
                        .anyMatch(line -> line.equals("Missing:") || line.startsWith("Missing: "));
        boolean hasConflictGroup =
                lines.stream()
                        .anyMatch(line -> line.contains("Silk Touch") && line.contains("Fortune"));

        if (!hasEfficiency || !hasMissingHeader || !hasConflictGroup) {
            throw new AssertionError(
                    "Expected Enchant Info HUD to show the held pickaxe's enchantments plus the ones missing from it, but got "
                            + lines
                            + ".");
        }
    }

    private static ResourceKey<Enchantment> enchantmentKey(String enchantmentId) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(enchantmentId));
    }
}
