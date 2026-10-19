package io.github.brainage04.brainagehud.test;

import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.event.ModClickEvents;
import io.github.brainage04.brainagehud.hud.FishingHud;
import io.github.brainage04.brainagehud.event.ModTooltipEvents;
import io.github.brainage04.brainagehud.hud.FoodHud;
import io.github.brainage04.brainagehud.hud.NetworkHud;
import io.github.brainage04.brainagehud.hud.PositionHud;
import io.github.brainage04.brainagehud.hud.ProjectileHud;
import io.github.brainage04.brainagehud.hud.StatusEffectHud;
import io.github.brainage04.brainagehud.hud.custom.EnchantInfoHud;
import io.github.brainage04.brainagehud.screen.WaypointsScreen;
import io.github.brainage04.brainagehud.screen.WaypointEditScreen;
import io.github.brainage04.brainagehud.util.ConfigUtils;
import io.github.brainage04.brainagehud.waypoint.Waypoint;
import io.github.brainage04.brainagehud.waypoint.WaypointActions;
import io.github.brainage04.brainagehud.waypoint.WaypointStore;
import io.github.brainage04.hudrendererlib.hud.core.HudElementEditor;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.HudRendererLibConfig;
import io.github.brainage04.fabricmoddingconventions.ClientGameTestRecorder;
import io.github.brainage04.fabricmoddingconventions.ClientGameTestServers;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import net.minecraft.ChatFormatting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.Minecraft;
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

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import javax.imageio.ImageIO;
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

				ClientGameTestRecorder.showStep(
						context,
						"status-effects",
						"Status Effect HUD",
						"The Status Effect HUD lists Speed II with its time left and the infinite Regeneration, each with its full-size effect icon on the left and the name above the time left. By default the game's own icons are hidden while the HUD is on and the top-right HUDs stay put; with Hide Vanilla Status Effects off the icons return and the top-right HUDs move down; with Show Vanilla Status Effects off the icons disappear again and the top-right HUDs move back up."
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
					assertTopRightShift(0);
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
					ConfigUtils.getConfig().statusEffectHudConfig.hideVanillaStatusEffects = false;
					assertTopRightShift(libraryConfig().adjustTopRightElementsWithStatusEffectsAmount);
				});
				System.out.println("[STDOUT]: Vanilla status effects shown screenshot: " + context.takeScreenshot("vanilla-status-effects-shown"));
				context.runOnClient(client -> {
					libraryConfig().showVanillaStatusEffects = false;
					assertTopRightShift(0);
				});
				System.out.println("[STDOUT]: Vanilla status effects hidden screenshot: " + context.takeScreenshot("vanilla-status-effects-hidden"));
				context.runOnClient(client -> {
					libraryConfig().showVanillaStatusEffects = true;
					ConfigUtils.getConfig().statusEffectHudConfig.hideVanillaStatusEffects = true;
				});
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
				context.waitTicks(60);

				exerciseEnchantCommands(context);
				exerciseInventoryStats(context);
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
				exerciseWaypointCommands(context);

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
						"The Fishing HUD stays empty until the bobber is cast, then reports open water, possible treasure and the chances of fish, treasure and junk for a bobber in a wide, deep pool."
				);
				context.runOnClient(client -> assertFishingHudLines(List.of()));
				context.getInput().pressKey(options -> options.keyHotbarSlots[1]);
				context.waitTicks(5);
				context.getInput().pressKey(options -> options.keyUse);
				context.waitTicks(60);
				context.runOnClient(client -> assertFishingHudLines(List.of("Open water: yes", "Treasure: possible", "Chances: Fish 85%, Treasure 5%, Junk 10%")));
				System.out.println("[STDOUT]: Fishing HUD screenshot: " + context.takeScreenshot("fishing-hud"));
				exerciseFullbright(context, server);
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

	private static void exerciseEnchantCommands(ClientGameTestContext context) {
		context.runOnClient(client -> assertTooltipHighlighting(client));
		ClientGameTestRecorder.showStep(context, "enchant-commands", "Enchantment commands",
				"The held pickaxe and a named mace list their enchantments; lookup accepts IDs, names and partial names, and blacklist edits give success and duplicate warnings.");
		Set<Component> chat = new LinkedHashSet<>();
		List<String> original = context.computeOnClient(client ->
				List.copyOf(ConfigUtils.getConfig().enchantInfoConfig.blacklistedEnchantmentIds));
		context.runOnClient(client -> client.gui.hud.getChat().setVisibleMessageFilter(message -> {
			chat.add(message.content());
			return true;
		}));
		try {
			commandAndCheck(context, chat, "getenchants", "[BrainageHUD] Acceptable enchants for Diamond Pickaxe:");
			commandAndCheck(context, chat, "getenchants minecraft:mace", "[BrainageHUD] Acceptable enchants for Mace:");
			commandAndCheck(context, chat, "getenchantinfo minecraft:fortune", "ID: minecraft:fortune");
			commandAndCheck(context, chat, "getenchantinfo \"Fortune\"", "[BrainageHUD] Enchant info for Fortune:");
			commandAndCheck(context, chat, "getenchantinfo \"protect\"", "[BrainageHUD] No exact match found. Potential matches:");
			commandAndCheck(context, chat, "getenchantinfo \"not an enchantment\"", "[BrainageHUD] No potential matches found!");
			context.runOnClient(client -> ConfigUtils.getConfig().enchantInfoConfig.blacklistedEnchantmentIds.remove("minecraft:unbreaking"));
			commandAndCheck(context, chat, "blacklistedenchants add minecraft:unbreaking", "[BrainageHUD] Unbreaking is now blacklisted.");
			commandAndCheck(context, chat, "blacklistedenchants add minecraft:unbreaking", "[BrainageHUD] Unbreaking is already blacklisted!");
			commandAndCheck(context, chat, "blacklistedenchants query", " - Unbreaking");
			commandAndCheck(context, chat, "blacklistedenchants remove minecraft:unbreaking", "[BrainageHUD] Unbreaking is no longer blacklisted.");
			commandAndCheck(context, chat, "blacklistedenchants remove minecraft:unbreaking", "[BrainageHUD] Unbreaking is not blacklisted!");
		} finally {
			context.runOnClient(client -> {
				client.gui.hud.getChat().setVisibleMessageFilter(message -> true);
				ConfigUtils.getConfig().enchantInfoConfig.blacklistedEnchantmentIds = new java.util.ArrayList<>(original);
				ConfigUtils.saveConfig();
			});
		}
	}

	private static void commandAndCheck(ClientGameTestContext context, Set<Component> chat, String command, String expected) {
		context.runOnClient(client -> {
			chat.clear();
			client.gui.hud.getChat().clearMessages(false);
		});
		runClientCommand(context, command);
		context.waitTicks(5);
		context.runOnClient(client -> {
			if (chat.stream().noneMatch(line -> line.getString().equals(expected))) {
				throw new AssertionError("Command /" + command + " did not print " + expected + "; got " + chat);
			}
		});
		context.waitTicks(35);
		context.takeScreenshot(command.replaceAll("[^a-zA-Z0-9]+", "-"));
	}

	private static void exerciseInventoryStats(ClientGameTestContext context) {
		ClientGameTestRecorder.showStep(context, "inventory-stats", "Inventory Stats includes armour",
				"The Inventory Stats key lists every occupied slot, including all four armour slots and the off hand.");
		Set<Component> chat = new LinkedHashSet<>();
		KeyMapping key = KeyMapping.get("key.brainagehud.inventoryStats");
		String previousKey = context.computeOnClient(client -> key.saveString());
		context.runOnClient(client -> {
			key.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_I));
			KeyMapping.resetMapping();
			client.gui.hud.getChat().clearMessages(false);
			client.gui.hud.getChat().setVisibleMessageFilter(message -> {
				chat.add(message.content());
				return true;
			});
		});
		try {
			context.getInput().pressKey(key);
			context.waitTicks(10);
			context.runOnClient(client -> {
				for (String expected : List.of("Feet: 1 × minecraft:diamond_boots", "Legs: 1 × minecraft:diamond_leggings",
						"Chest: 1 × minecraft:diamond_chestplate", "Head: 1 × minecraft:diamond_helmet",
						"Off hand: 1 × minecraft:shield")) {
					if (chat.stream().noneMatch(line -> line.getString().equals(expected))) {
						throw new AssertionError("Inventory Stats omitted " + expected + "; got " + chat);
					}
				}
			});
			context.waitTicks(60);
			context.takeScreenshot("inventory-stats-armour");
		} finally {
			context.runOnClient(client -> {
				key.setKey(InputConstants.getKey(previousKey));
				KeyMapping.resetMapping();
				client.gui.hud.getChat().setVisibleMessageFilter(message -> true);
			});
		}
	}

	private static void exerciseFullbright(ClientGameTestContext context,
			net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext server) {
		server.runOnServer(minecraftServer -> {
			ServerPlayer player = minecraftServer.getPlayerList().getPlayers().getFirst();
			for (BlockPos pos : BlockPos.betweenClosed(37, -53, -3, 43, -47, 3)) {
				boolean wall = pos.getX() == 37 || pos.getX() == 43 || pos.getY() == -53 || pos.getY() == -47
						|| pos.getZ() == -3 || pos.getZ() == 3;
				player.level().setBlockAndUpdate(pos, wall ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
			}
			player.teleportTo(40.5, -52, 0.5);
			player.setYRot(0);
			player.setXRot(0);
		});
		context.waitTicks(60);
		float original = context.computeOnClient(client -> ConfigUtils.getConfig().qualityOfLifeConfig.fullbright);
		try {
			double[] brightness = new double[3];
			int[] amounts = {0, 1, -1};
			for (int index = 0; index < amounts.length; index++) {
				int amount = amounts[index];
				ClientGameTestRecorder.showStep(context, "fullbright-" + amount, "Fullbright " + amount,
						"A sealed, unlit stone room shows actual terrain lighting: normal at 0, fully lit at 1, black at -1.");
				runClientCommand(context, "fullbright " + amount);
				context.waitTicks(80);
				brightness[index] = centreBrightness(context.takeScreenshot("fullbright-" + amount));
			}
			if (brightness[1] < brightness[0] + 30 || brightness[2] > brightness[1] / 4) {
				throw new AssertionError("Fullbright did not change terrain lighting: " + java.util.Arrays.toString(brightness));
			}
		} finally {
			context.runOnClient(client -> {
				ConfigUtils.getConfig().qualityOfLifeConfig.fullbright = original;
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
			List<Component> lines = new java.util.ArrayList<>(List.of(pickaxe.getHoverName(),
					Enchantment.getFullname(efficiency, 3), Enchantment.getFullname(mending, 1),
					Component.literal("Mending lore")));
			config.highlightMaxLevelEnchants = false;
			ModTooltipEvents.onItemTooltip(pickaxe, lines);
			if (lines.get(2).getStyle().isBold()) throw new AssertionError("Disabled tooltip highlighting still bolded Mending.");
			config.highlightMaxLevelEnchants = true;
			ModTooltipEvents.onItemTooltip(pickaxe, lines);
			if (!lines.get(2).getStyle().isBold() || lines.get(0).getStyle().isBold()
					|| lines.get(1).getStyle().isBold() || lines.get(3).getStyle().isBold()) {
				throw new AssertionError("Tooltip highlighting changed the wrong item lines.");
			}
			ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
			ItemEnchantments.Mutable stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
			stored.set(mending, 1);
			book.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
			List<Component> bookLines = new java.util.ArrayList<>(List.of(book.getHoverName(), Enchantment.getFullname(mending, 1)));
			ModTooltipEvents.onItemTooltip(book, bookLines);
			if (!bookLines.get(1).getStyle().isBold()) throw new AssertionError("Stored enchantments in books were not highlighted.");
		} finally {
			config.highlightMaxLevelEnchants = original;
		}
	}

	private static void exerciseWaypointCommands(ClientGameTestContext context) {
		assertWaypointEditSurvivesResize(context);
		ClientGameTestRecorder.showStep(context, "waypoint-commands", "Waypoint command persistence",
				"A quoted name and explicit coordinates are saved, a case-insensitive duplicate is rejected, and removal updates the file.");
		Set<Component> chat = new LinkedHashSet<>();
		context.runOnClient(client -> client.gui.hud.getChat().setVisibleMessageFilter(message -> {
			chat.add(message.content());
			return true;
		}));
		try {
			commandAndCheck(context, chat, "waypoints add \"Quoted Base\" 1 -60 3", "[BrainageHUD] Created waypoint Quoted Base at 1, -60, 3.");
			context.runOnClient(client -> assertSavedWaypoint(client, "Quoted Base", true));
			commandAndCheck(context, chat, "waypoints add \"quoted base\"", "[BrainageHUD] A waypoint called \"quoted base\" already exists.");
			commandAndCheck(context, chat, "waypoints remove \"QUOTED BASE\"", "[BrainageHUD] Removed waypoint Quoted Base.");
			context.runOnClient(client -> assertSavedWaypoint(client, "Quoted Base", false));
			commandAndCheck(context, chat, "waypoints remove \"Quoted Base\"", "[BrainageHUD] There is no waypoint called \"Quoted Base\".");
		} finally {
			context.runOnClient(client -> client.gui.hud.getChat().setVisibleMessageFilter(message -> true));
		}
	}

	private static void assertSavedWaypoint(Minecraft client, String name, boolean present) {
		Path file = client.gameDirectory.toPath().resolve("config/brainagehud/waypoints.json");
		try {
			var saved = com.google.gson.JsonParser.parseString(Files.readString(file)).getAsJsonObject()
					.getAsJsonArray(WaypointStore.currentWorldKey().orElseThrow());
			boolean found = false;
			for (var entry : saved) {
				var waypoint = entry.getAsJsonObject();
				if (waypoint.get("name").getAsString().equals(name)) {
					found = true;
					if (waypoint.get("x").getAsInt() != 1 || waypoint.get("y").getAsInt() != -60 || waypoint.get("z").getAsInt() != 3) {
						throw new AssertionError("Saved waypoint has the wrong position: " + waypoint);
					}
				}
			}
			if (found != present) throw new AssertionError("Saved waypoint presence was " + found + ", expected " + present);
		} catch (IOException exception) {
			throw new AssertionError("Cannot read saved waypoints", exception);
		}
	}

	private static void assertWaypointEditSurvivesResize(ClientGameTestContext context) {
		int[] original = context.computeOnClient(client -> new int[] {client.getWindow().getWidth(), client.getWindow().getHeight()});
		List<String> values = List.of("Unsubmitted Base", "123", "bad input", "-456");
		context.runOnClient(client -> {
			client.gui.setScreen(new WaypointEditScreen(null, null));
			var boxes = client.gui.screen().children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).toList();
			for (int index = 0; index < values.size(); index++) boxes.get(index).setValue(values.get(index));
		});
		try {
			context.getInput().resizeWindow(original[0] + 80, original[1] + 40);
			context.waitTicks(2);
			context.runOnClient(client -> {
				var boxes = client.gui.screen().children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).toList();
				if (!boxes.stream().map(EditBox::getValue).toList().equals(values)) {
					throw new AssertionError("Resizing discarded unsubmitted waypoint edits.");
				}
				boxes.getFirst().setValue("");
			});
			context.getInput().resizeWindow(original[0], original[1]);
			context.waitTicks(2);
			context.runOnClient(client -> {
				var boxes = client.gui.screen().children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).toList();
				if (!boxes.stream().map(EditBox::getValue).toList().equals(List.of("", "123", "bad input", "-456"))) {
					throw new AssertionError("Resizing replaced an intentionally blank waypoint name.");
				}
			});
		} finally {
			context.runOnClient(client -> client.gui.setScreen(null));
			context.getInput().resizeWindow(original[0], original[1]);
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
			config.statusEffectHudConfig.coreSettings.enabled = true;
			config.statusEffectHudConfig.showDurations = true;
			config.statusEffectHudConfig.hideVanillaStatusEffects = true;
			libraryConfig().showVanillaStatusEffects = true;
			config.waypointConfig.showWorldCentre = true;
			config.qualityOfLifeConfig.fullbright = 0;
			config.foodHudConfig.showSlotCounts = false;

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
		private final boolean statusEnabled;
		private final boolean statusDurations;
		private final boolean hideVanillaEffects;
		private final boolean showVanillaEffects;
		private final boolean worldCentre;
		private final float fullbright;
		private final boolean foodSlotCounts;

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
			ConfigUtils.saveConfig();
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
					+ (libraryConfig().showVanillaStatusEffects ? "on" : "off") + " and the Status Effect HUD's Hide Vanilla Status Effects "
					+ (ConfigUtils.getConfig().statusEffectHudConfig.hideVanillaStatusEffects ? "on" : "off") + ", but they are shifted by " + shift + ".");
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
