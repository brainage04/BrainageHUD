package io.github.brainage04.brainagehud.test;

import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.util.ConfigUtils;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import org.lwjgl.glfw.GLFW;

import java.util.Properties;

@SuppressWarnings("UnstableApiUsage")
public class BrainageHUDClientGameTest implements FabricClientGameTest {
	private static final TestScreenshotComparisonAlgorithm TEMPLATE_MATCH = TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.005F);

	@Override
	public void runTest(ClientGameTestContext context) {
		Properties serverProperties = new Properties();
		serverProperties.setProperty("simulation-distance", "5");
		serverProperties.setProperty("view-distance", "2");

		try (TestDedicatedServerContext server = context.worldBuilder().createServer(serverProperties);
			 TestServerConnection ignored = server.connect()) {
			ConfigUtils.getConfig().dateTimeHudConfig.coreSettings.enabled = false;

			context.getInput().resizeWindow(1280, 720);
			context.waitTicks(200);
			context.takeScreenshot("brainagehud_world_loaded");
			assertContainsTemplate(context, "brainagehud_biome_label", "actual_brainagehud_biome_label", 0, 120, 240, 120);
			assertContainsTemplate(context, "brainagehud_movement_label", "actual_brainagehud_movement_label", 0, 650, 240, 70);
			assertContainsTemplate(context, "brainagehud_keystrokes_idle", "actual_brainagehud_keystrokes_idle", 1080, 0, 200, 260);

			context.getInput().holdKey(GLFW.GLFW_KEY_W);
			context.getInput().holdKey(GLFW.GLFW_KEY_A);
			context.getInput().holdKey(GLFW.GLFW_KEY_SPACE);
			context.getInput().holdMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
			context.waitTicks(10);
			context.takeScreenshot("brainagehud_keystrokes_pressed");
			assertContainsTemplate(context, "brainagehud_keystrokes_pressed", "actual_brainagehud_keystrokes_pressed", 1080, 0, 200, 260);
			context.getInput().releaseKey(GLFW.GLFW_KEY_W);
			context.getInput().releaseKey(GLFW.GLFW_KEY_A);
			context.getInput().releaseKey(GLFW.GLFW_KEY_SPACE);
			context.getInput().releaseMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);

			context.setScreen(() -> AutoConfigClient.getConfigScreen(ModConfig.class, null).get());
			context.waitTicks(20);
			context.takeScreenshot("brainagehud_config_screen");
			assertContainsTemplate(context, "brainagehud_config_title", "actual_brainagehud_config_title", 350, 0, 600, 80);
		}
	}

	private static void assertContainsTemplate(ClientGameTestContext context, String template, String actualFileName, int x, int y, int width, int height) {
		context.assertScreenshotContains(TestScreenshotComparisonOptions.of(template)
				.withRegion(x, y, width, height)
				.withGrayscale()
				.withAlgorithm(TEMPLATE_MATCH)
				.saveWithFileName(actualFileName)
				.disableCounterPrefix());
	}
}
