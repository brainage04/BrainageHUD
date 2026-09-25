package io.github.brainage04.brainagehud.test;

import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.event.ModClickEvents;
import io.github.brainage04.brainagehud.hud.FishingHud;
import io.github.brainage04.brainagehud.hud.FoodHud;
import io.github.brainage04.brainagehud.hud.NetworkHud;
import io.github.brainage04.brainagehud.hud.PositionHud;
import io.github.brainage04.brainagehud.hud.ProjectileHud;
import io.github.brainage04.brainagehud.hud.StatusEffectHud;
import io.github.brainage04.brainagehud.hud.custom.EnchantInfoHud;
import io.github.brainage04.brainagehud.screen.WaypointsScreen;
import io.github.brainage04.brainagehud.util.ConfigUtils;
import io.github.brainage04.brainagehud.waypoint.Waypoint;
import io.github.brainage04.brainagehud.waypoint.WaypointActions;
import io.github.brainage04.brainagehud.waypoint.WaypointStore;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.hud.core.HudElementEditor;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.HudRendererLibConfig;
import io.github.brainage04.fabricmoddingconventions.ClientGameTestRecorder;
import io.github.brainage04.fabricmoddingconventions.ClientGameTestServers;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.block.Blocks;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public final class BrainageHUDClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		Properties serverProperties = ClientGameTestServers.flatServerProperties();

		ClientGameTestServers.withDedicatedServer(context, serverProperties, "BrainageHUD visual showcase GameTest", server -> { ShowcaseConfigSnapshot configSnapshot = null;
			try {
				server.runOnServer(minecraftServer -> preparePlayer(
						minecraftServer.getPlayerList().getPlayers().getFirst()));
				ClientGameTestServers.assertClientWorldAndPlayerAvailable(context);
				context.waitTicks(40);

				context.runOnClient(client -> client.gui.setScreen(new HudElementEditor()));
				context.waitTicks(5);
				// leaving the editor with Escape reloads the configs from disk, replacing every config object,
				// so it runs before the showcase configures them
				context.runOnClient(client -> ((HudElementEditor) client.gui.screen()).closeWithoutSaving());
				context.waitTicks(5);
				context.runOnClient(client -> assertEditorKnowsEveryElement());
				configSnapshot = context.computeOnClient(client -> configureAndAssertShowcase());
				ClientGameTestRecorder.startRecording(context);

				ClientGameTestRecorder.showStep(
						context,
						"position-and-counters",
						"Position and corrected C-counter",
						"The top-left HUD shows the fixed player position, chunk coordinates, rendered-section C-counter, entity counter, and direction."
				);
				context.waitTicks(80);

				ClientGameTestRecorder.showStep(
						context,
						"performance-and-network",
						"Basic performance HUDs",
						"Performance, network, reach, and toggle-sprint HUDs remain visible against the local dedicated-server fixture."
				);
				context.waitTicks(60);
				// the dedicated server has reported its game time every second for several seconds by now
				context.runOnClient(client -> assertNetworkHudShowsServerTps());

				ClientGameTestRecorder.showStep(
						context,
						"custom-huds",
						"Armor and keystrokes HUDs",
						"The equipped diamond armor and main-hand tool populate Armor Info while the WASD, space, and mouse controls populate Keystrokes."
				);
				context.waitTicks(40);
				// three separate attack clicks, well within one second, whatever the frame rate
				for (int click = 0; click < 3; click++) {
					context.getInput().pressKey(options -> options.keyAttack);
				}
				context.runOnClient(client -> assertAttackClicksCounted(3));
				context.waitTicks(60);

				ClientGameTestRecorder.showStep(
						context,
						"enchant-info",
						"Enchant info HUD",
						"The enchanted pickaxe in the main hand populates Enchant Info with its enchantments and the ones still missing from it."
				);
				context.waitTicks(60);
				context.runOnClient(client -> assertEnchantInfoHudShowsHeldItem());
				System.out.println("[STDOUT]: Enchant info HUD screenshot: " + context.takeScreenshot("enchant-info-hud"));

				context.runOnClient(client -> assertModKeysHaveTheirOwnCategory());

				ClientGameTestRecorder.showStep(
						context,
						"status-effects",
						"Status Effect HUD",
						"The Status Effect HUD lists Speed II with its time left and the infinite Regeneration, each with its full-size effect icon on the left and the name above the time left; with Show Vanilla Status Effects off the game's icons disappear and the top-right HUDs move back up."
				);
				context.runOnClient(client -> assertHudLines("Status Effect", new StatusEffectHud().getLines(), List.of()));
				server.runOnServer(minecraftServer -> {
					ServerPlayer player = minecraftServer.getPlayerList().getPlayers().getFirst();
					// just under 84 seconds, so it reads 1:23 for the next 19 ticks
					player.addEffect(new MobEffectInstance(MobEffects.SPEED, 1679, 1));
					player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, MobEffectInstance.INFINITE_DURATION));
				});
				context.waitTicks(5);
				context.runOnClient(client -> {
					assertHudLines("Status Effect", new StatusEffectHud().getLines(), List.of("Speed II: 1:23", "Regeneration: Infinite"));
					assertTopRightShift(libraryConfig().adjustTopRightElementsWithStatusEffectsAmount);
				});
				System.out.println("[STDOUT]: Status Effect HUD screenshot: " + context.takeScreenshot("status-effect-hud"));
				System.out.println("[STDOUT]: Status Effect HUD large screenshot: " + takeLargeScreenshot(context, "status-effect-hud-large"));
				context.runOnClient(client -> {
					ConfigUtils.getConfig().statusEffectHudConfig.showDurations = false;
					assertHudLines("Status Effect", new StatusEffectHud().getLines(), List.of("Speed II", "Regeneration"));
				});
				System.out.println("[STDOUT]: Status Effect HUD without durations large screenshot: " + takeLargeScreenshot(context, "status-effect-hud-no-durations-large"));
				context.runOnClient(client -> {
					ConfigUtils.getConfig().statusEffectHudConfig.showDurations = true;
					libraryConfig().showVanillaStatusEffects = false;
					assertTopRightShift(0);
				});
				System.out.println("[STDOUT]: Vanilla status effects hidden screenshot: " + context.takeScreenshot("vanilla-status-effects-hidden"));
				context.runOnClient(client -> libraryConfig().showVanillaStatusEffects = true);
				server.runOnServer(minecraftServer -> minecraftServer.getPlayerList().getPlayers().getFirst().removeAllEffects());
				context.waitTicks(5);

				ClientGameTestRecorder.showStep(
						context,
						"inventory-trackers",
						"Motion, entity and inventory trackers",
						"Under their bold headers, the Projectile HUD counts 96 arrows over two slots and 16 ender pearls, and the Food HUD lists the bread and steak in inventory order."
				);
				context.runOnClient(client -> {
					assertHudLines("Projectile", new ProjectileHud().getLines(), List.of("Projectiles:", "Arrows: 96 [64, 32]", "Ender Pearls: 16"));
					assertHudLines("Food", new FoodHud().getLines(), List.of("Food:", "Bread: 12", "Steak: 5"));
				});
				System.out.println("[STDOUT]: Inventory trackers screenshot: " + context.takeScreenshot("inventory-trackers"));

				ClientGameTestRecorder.showStep(
						context,
						"waypoints",
						"Waypoints",
						"The Create Waypoint key saves a waypoint where the player stands and the Manage Waypoints key opens the waypoint screen."
				);
				context.getInput().pressKey(KeyMapping.get("key.brainagehud.createWaypoint"));
				context.waitTicks(20);
				context.runOnClient(client -> assertQuickWaypointCreated());
				context.getInput().pressKey(KeyMapping.get("key.brainagehud.manageWaypoints"));
				context.waitTicks(40);
				context.runOnClient(client -> {
					if (!(client.gui.screen() instanceof WaypointsScreen)) {
						throw new AssertionError("Expected the Manage Waypoints key to open the waypoint screen, but the screen is " + client.gui.screen() + ".");
					}
				});
				// the World Centre is listed first; selecting it allows Hide but not Edit or Delete
				clickAt(context, context.computeOnClient(client -> client.getWindow().getGuiScaledWidth() / 2), 40);
				context.runOnClient(client -> assertButtons(client, Map.of("Edit", false, "Delete", false, "Hide", true)));
				System.out.println("[STDOUT]: Waypoints screen screenshot: " + context.takeScreenshot("waypoints-screen"));
				clickButton(context, "Hide");
				context.runOnClient(client -> {
					if (ConfigUtils.getConfig().waypointConfig.showWorldCentre) {
						throw new AssertionError("Expected Hide on the World Centre to turn Show World Centre off.");
					}
					assertButtons(client, Map.of("Show", true));
				});
				clickButton(context, "Show");
				context.runOnClient(client -> {
					if (!ConfigUtils.getConfig().waypointConfig.showWorldCentre) {
						throw new AssertionError("Expected Show on the World Centre to turn Show World Centre back on.");
					}
				});
				context.runOnClient(client -> client.gui.setScreen(null));
				context.waitTicks(20);

				ClientGameTestRecorder.showStep(
						context,
						"chat-feedback",
						"Chat feedback",
						"Every message starts with a grey [BrainageHUD] prefix: the Create Waypoint key's confirmation is green, wrong /waypoints input gets a red usage error, and /waypoints list sends one white header followed by unprefixed lines."
				);
				Set<Component> chat = new LinkedHashSet<>();
				// the filter sees every chat message, including those already shown, whenever the chat is laid out again
				context.runOnClient(client -> client.gui.hud.getChat().setVisibleMessageFilter(message -> {
					chat.add(message.content());
					return true;
				}));
				runClientCommand(context, "waypoints add");
				runClientCommand(context, "waypoints add Base 1 two 3");
				runClientCommand(context, "waypoints list");
				context.waitTicks(10);
				context.runOnClient(client -> {
					client.gui.hud.getChat().setVisibleMessageFilter(message -> true);
					assertChatMessage(chat, "[BrainageHUD] Created waypoint Waypoint 1 at 12, -60, -9.", "Created waypoint ", ChatFormatting.GREEN);
					assertChatMessage(chat, "[BrainageHUD] Usage: /waypoints add <name> [<x> <y> <z>]", "Usage: ", ChatFormatting.RED);
					assertChatMessage(chat, "[BrainageHUD] X, Y and Z must be whole numbers.", "X, Y and Z", ChatFormatting.RED);
					assertChatMessage(chat, "[BrainageHUD] Waypoints:", "Waypoints:", ChatFormatting.WHITE);
					assertChatMessage(chat, " - Waypoint 1 12, -60, -9 (Overworld)", " - ", ChatFormatting.WHITE);
				});
				System.out.println("[STDOUT]: Chat feedback screenshot: " + context.takeScreenshot("chat-feedback"));

				ClientGameTestRecorder.showStep(
						context,
						"waypoints-in-world",
						"Waypoints in the world",
						"From twelve blocks away the new waypoint shows its beam, gem, ground pulse and name label; a waypoint 300 blocks away shows a beam and its distance, and the white World Centre beacon stands at 0, 63, 0. Every beam spans the whole height of the world."
				);
				context.runOnClient(client -> WaypointActions.create("Far Base", new BlockPos(100, -60, 300)));
				server.runOnServer(minecraftServer -> {
					ServerPlayer player = minecraftServer.getPlayerList().getPlayers().getFirst();
					player.teleportTo(12.5D, -60.0D, -20.5D);
					player.setYRot(0.0F);
					player.setXRot(0.0F);
				});
				context.waitTicks(60);
				System.out.println("[STDOUT]: Waypoints in world screenshot: " + context.takeScreenshot("waypoints-in-world"));
				context.runOnClient(client -> WaypointActions.remove("Far Base"));
				server.runOnServer(minecraftServer -> minecraftServer.getPlayerList().getPlayers().getFirst().teleportTo(12.5D, -60.0D, -8.5D));
				context.waitTicks(20);

				ClientGameTestRecorder.showStep(
						context,
						"fishing",
						"Fishing HUD",
						"The Fishing HUD stays empty until the bobber is cast, then reports open water and possible treasure for a bobber in a wide, deep pool."
				);
				context.runOnClient(client -> assertFishingHudLines(List.of()));
				context.getInput().pressKey(options -> options.keyHotbarSlots[1]);
				context.waitTicks(5);
				context.getInput().pressKey(options -> options.keyUse);
				context.waitTicks(60);
				context.runOnClient(client -> assertFishingHudLines(List.of("Open water: yes", "Treasure: possible")));
				System.out.println("[STDOUT]: Fishing HUD screenshot: " + context.takeScreenshot("fishing-hud"));
			} finally {
				if (configSnapshot != null) {
					ShowcaseConfigSnapshot snapshotToRestore = configSnapshot;
					context.runOnClient(client -> snapshotToRestore.restore());
				}
			} });
	}

	/**
	 * The current GUI at GUI scale 4 in a window four times the GUI's size, so the HUDs sit where they do in the
	 * normal screenshots and every GUI pixel is a 4x4 block that can be checked when zoomed in; the window size and
	 * GUI scale are put back afterwards.
	 */
	private static Path takeLargeScreenshot(ClientGameTestContext context, String name) {
		int[] original = context.computeOnClient(client -> new int[] {
				client.getWindow().getWidth(),
				client.getWindow().getHeight(),
				client.options.guiScale().get(),
				client.getWindow().getGuiScaledWidth(),
				client.getWindow().getGuiScaledHeight()
		});
		// the GUI scale option only accepts scales the current window fits, so the window grows first
		context.getInput().resizeWindow(original[3] * 4, original[4] * 4);
		context.runOnClient(client -> {
			client.options.guiScale().set(4);
			client.resizeGui();
			Window window = client.getWindow();
			if (window.getGuiScale() != 4 || window.getGuiScaledWidth() != original[3] || window.getGuiScaledHeight() != original[4]) {
				throw new AssertionError("Expected a %dx%d GUI at scale 4 for the large screenshot, got %dx%d at scale %d".formatted(
						original[3], original[4], window.getGuiScaledWidth(), window.getGuiScaledHeight(), window.getGuiScale()));
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

	private static void preparePlayer(ServerPlayer player) {
		player.getInventory().clearContent();
		HolderLookup.RegistryLookup<Enchantment> enchantments = player.level().registryAccess()
				.lookupOrThrow(Registries.ENCHANTMENT);
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
			throw new AssertionError("Expected BrainageHUD's client initializer to register every HUD before recording.");
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

			assertPositionHudShowsFixture();
			return snapshot;
		} catch (RuntimeException | Error exception) {
			snapshot.restore();
			throw exception;
		}
	}

	private static final class ShowcaseConfigSnapshot {
		private final ModConfig config;
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

		private ShowcaseConfigSnapshot(ModConfig config) {
			this.config = config;
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
		}
	}

	/** Clicks at GUI-scaled coordinates. */
	private static void clickAt(ClientGameTestContext context, int x, int y) {
		int scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		context.getInput().setCursorPos(x * scale + scale / 2.0D, y * scale + scale / 2.0D);
		context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		context.waitTicks(2);
	}

	private static void clickButton(ClientGameTestContext context, String label) {
		Button button = context.computeOnClient(client -> findButton(client, label));
		clickAt(context, button.getX() + button.getWidth() / 2, button.getY() + button.getHeight() / 2);
	}

	private static Button findButton(Minecraft client, String label) {
		return client.gui.screen().children().stream()
				.filter(child -> child instanceof Button button && button.getMessage().getString().equals(label))
				.map(Button.class::cast)
				.findFirst()
				.orElseThrow(() -> new AssertionError("Expected a " + label + " button on " + client.gui.screen() + "."));
	}

	private static void assertButtons(Minecraft client, Map<String, Boolean> expectedActive) {
		expectedActive.forEach((label, active) -> {
			if (findButton(client, label).active != active) {
				throw new AssertionError("Expected the " + label + " button to be " + (active ? "active" : "inactive") + " with the World Centre selected.");
			}
		});
	}

	private static void assertEditorKnowsEveryElement() {
		for (var element : HudRenderer.REGISTERED_ELEMENTS) {
			int id = element.getElementConfig().getCoreSettings().elementId;
			if (!HudElementEditor.CORE_SETTINGS_ELEMENTS.containsKey(id)) {
				throw new AssertionError("Expected the element editor to know " + element.getElementConfig().getCoreSettings().elementName
						+ " (ID " + id + ") after reloading the configs, but it only knows " + HudElementEditor.CORE_SETTINGS_ELEMENTS.keySet() + ".");
			}
		}
	}

	private static void assertModKeysHaveTheirOwnCategory() {
		KeyMapping.Category brainageHud = HudRendererLib.getKeyCategory(BrainageHUD.MOD_ID);
		for (String key : List.of("key.brainagehud.openConfig", "key.brainagehud.createWaypoint", "key.brainagehud.manageWaypoints", "key.brainagehud.inventoryStats")) {
			KeyMapping.Category category = KeyMapping.get(key).getCategory();
			if (category != brainageHud) {
				throw new AssertionError("Expected " + key + " in BrainageHUD's key category, but it is in " + category.id() + ".");
			}
		}
		if (KeyMapping.get("key.hudrendererlib.openConfig").getCategory() != HudRendererLib.KEY_CATEGORY) {
			throw new AssertionError("Expected HudRendererLib's own config key to stay in HudRendererLib's category.");
		}
		String title = brainageHud.label().getString();
		if (!title.equals("BrainageHUD")) {
			throw new AssertionError("Expected BrainageHUD's key category to be titled \"BrainageHUD\", but it is \"" + title + "\".");
		}
	}

	private static void assertQuickWaypointCreated() {
		List<Waypoint> waypoints = WaypointStore.current()
				.orElseThrow(() -> new AssertionError("Expected waypoints to be available in a world."));
		if (waypoints.size() != 1 || !waypoints.getFirst().name.equals("Waypoint 1")) {
			throw new AssertionError("Expected the Create Waypoint key to create \"Waypoint 1\", but the waypoints are " + waypoints.stream().map(waypoint -> waypoint.name).toList() + ".");
		}
		BlockPos pos = waypoints.getFirst().pos();
		if (!pos.equals(new BlockPos(12, -60, -9))) {
			throw new AssertionError("Expected \"Waypoint 1\" where the player stands, at 12, -60, -9, but it is at " + pos.toShortString() + ".");
		}
	}

	/** Runs a command as if typed in chat; Fabric runs client commands without sending them to the server. */
	private static void runClientCommand(ClientGameTestContext context, String command) {
		context.runOnClient(client -> client.getConnection().sendCommand(command));
	}

	/** Asserts that the chat shows {@code expected} and that its part starting with {@code bodyStart} has {@code colour}. */
	private static void assertChatMessage(Set<Component> chat, String expected, String bodyStart, ChatFormatting colour) {
		Component message = chat.stream()
				.filter(line -> line.getString().equals(expected))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Expected the chat to show \"" + expected + "\", but it shows " + chat.stream().map(Component::getString).toList() + "."));
		TextColor[] bodyColour = new TextColor[1];
		message.visit((style, text) -> {
			if (text.startsWith(bodyStart)) {
				bodyColour[0] = style.getColor();
				return Optional.of(true);
			}
			return Optional.empty();
		}, Style.EMPTY);
		if (!TextColor.fromLegacyFormat(colour).equals(bodyColour[0])) {
			throw new AssertionError("Expected \"" + expected + "\" in " + colour + ", but its colour is " + bodyColour[0] + ".");
		}
	}

	private static void assertHudLines(String hud, List<Component> actual, List<String> expected) {
		List<String> lines = actual.stream().map(Component::getString).toList();
		if (!lines.equals(expected)) {
			throw new AssertionError("Expected the " + hud + " HUD to show " + expected + ", but got " + lines + ".");
		}
	}

	private static HudRendererLibConfig libraryConfig() {
		return io.github.brainage04.hudrendererlib.util.ConfigUtils.getConfig();
	}

	/** Asserts that a top-right element (the Keystrokes HUD) sits {@code expectedShift} pixels below its configured place. */
	private static void assertTopRightShift(int expectedShift) {
		CoreSettings keystrokes = ConfigUtils.getConfig().keystrokesHudConfig.coreSettings;
		int unshifted = keystrokes.y + HudRenderer.getYOffset(keystrokes, 0);
		int shift = HudRenderer.getPosY(keystrokes, 0) - unshifted;
		if (shift != expectedShift) {
			throw new AssertionError("Expected top-right elements to be shifted down by " + expectedShift + " with Show Vanilla Status Effects "
					+ (libraryConfig().showVanillaStatusEffects ? "on" : "off") + ", but they are shifted by " + shift + ".");
		}
	}

	private static void assertFishingHudLines(List<String> expected) {
		List<String> lines = new FishingHud().getLines().stream().map(line -> line.getString()).toList();
		if (!lines.equals(expected)) {
			throw new AssertionError("Expected the Fishing HUD to show " + expected + ", but got " + lines + ".");
		}
	}

	private static void assertPositionHudShowsFixture() {
		List<String> lines = new PositionHud().getLines().stream()
				.map(line -> line.getString())
				.toList();
		boolean hasPosition = lines.stream().anyMatch(line -> line.startsWith("X: 12.5"));
		boolean hasCCounter = lines.stream().anyMatch(line -> line.startsWith("C: "));
		// the fixture player faces yaw 0, which is south
		boolean facesSouth = lines.stream().anyMatch(line -> line.startsWith("S (+Z)"));

		if (!hasPosition || !hasCCounter || !facesSouth) {
			throw new AssertionError("Expected Position HUD to show the fixture position, rendered-section C-counter and south-facing direction, but got " + lines + ".");
		}
	}

	private static void assertNetworkHudShowsServerTps() {
		List<String> lines = new NetworkHud().getLines().stream()
				.map(line -> line.getString())
				.toList();
		String tpsLine = lines.stream()
				.filter(line -> line.startsWith("TPS: "))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Expected Network HUD to show TPS, but got " + lines + "."));
		double tps;
		try {
			tps = Double.parseDouble(tpsLine.substring("TPS: ".length()).replace(',', '.'));
		} catch (NumberFormatException exception) {
			throw new AssertionError("Expected a measured TPS after several server time reports, but got " + lines + ".", exception);
		}

		// an idle local server ticks at its 20 TPS target; the lower bound allows for a slow CI machine
		if (tps < 10.0D || tps > 20.0D) {
			throw new AssertionError("Expected the idle dedicated server to measure close to 20 TPS, but got " + lines + ".");
		}
	}

	private static void assertAttackClicksCounted(int expectedClicks) {
		int clicks = ModClickEvents.getAttackClicksPerSecond();
		if (clicks != expectedClicks) {
			throw new AssertionError("Expected " + expectedClicks + " attack clicks in the last second, but counted " + clicks + ".");
		}
	}

	private static void assertEnchantInfoHudShowsHeldItem() {
		List<String> lines = new EnchantInfoHud().getLines().stream()
				.map(line -> line.getString())
				.toList();
		boolean hasEfficiency = lines.stream().anyMatch(line -> line.startsWith("Efficiency"));
		boolean hasMissingHeader = lines.contains("Missing:");
		boolean hasConflictGroup = lines.stream()
				.anyMatch(line -> line.contains("Silk Touch") && line.contains("Fortune"));

		if (!hasEfficiency || !hasMissingHeader || !hasConflictGroup) {
			throw new AssertionError("Expected Enchant Info HUD to show the held pickaxe's enchantments plus the ones missing from it, but got " + lines + ".");
		}
	}

	private static ResourceKey<Enchantment> enchantmentKey(String enchantmentId) {
		return ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(enchantmentId));
	}
}
