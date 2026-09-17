package io.github.brainage04.brainagehud.test;

import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.hud.PositionHud;
import io.github.brainage04.brainagehud.hud.custom.EnchantInfoHud;
import io.github.brainage04.brainagehud.util.ConfigUtils;
import io.github.brainage04.fabricmoddingconventions.ClientGameTestRecorder;
import io.github.brainage04.fabricmoddingconventions.ClientGameTestServers;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Properties;

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

				ClientGameTestRecorder.showStep(
						context,
						"custom-huds",
						"Armor and keystrokes HUDs",
						"The equipped diamond armor and main-hand tool populate Armor Info while the WASD, space, and mouse controls populate Keystrokes."
				);
				context.waitTicks(100);

				ClientGameTestRecorder.showStep(
						context,
						"enchant-info",
						"Enchant info HUD",
						"The enchanted pickaxe in the main hand populates Enchant Info with its enchantments and the ones still missing from it."
				);
				context.waitTicks(60);
				context.runOnClient(client -> assertEnchantInfoHudShowsHeldItem());
				System.out.println("[STDOUT]: Enchant info HUD screenshot: " + context.takeScreenshot("enchant-info-hud"));
			} finally {
				if (configSnapshot != null) {
					ShowcaseConfigSnapshot snapshotToRestore = configSnapshot;
					context.runOnClient(client -> snapshotToRestore.restore());
				}
				;
			} });
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

			if (!config.positionHudConfig.coreSettings.enabled
					|| !config.positionHudConfig.cCounter
					|| !config.armourInfoHudConfig.coreSettings.enabled
					|| !config.armourInfoHudConfig.showMainHand
					|| !config.keystrokesHudConfig.coreSettings.enabled
					|| !config.keystrokesHudConfig.showWasd) {
				throw new AssertionError("Expected the basic and custom HUD showcase registrations to expose enabled configuration.");
			}

			assertPositionHudShowsCorrectedCounters();
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
		}
	}

	private static void assertPositionHudShowsCorrectedCounters() {
		PositionHud positionHud = new PositionHud();
		boolean hasPosition = positionHud.getLines().stream()
				.map(line -> line.getString())
				.anyMatch(line -> line.startsWith("X: 12.5"));
		boolean hasCCounter = positionHud.getLines().stream()
				.map(line -> line.getString())
				.anyMatch(line -> line.startsWith("C: "));

		if (!hasPosition || !hasCCounter) {
			throw new AssertionError("Expected Position HUD to show the fixture position and corrected rendered-section C-counter.");
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
