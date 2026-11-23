package io.github.brainage04.brainagehud.test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.brainage04.brainagehud.screen.WaypointEditScreen;
import io.github.brainage04.brainagehud.screen.WaypointsScreen;
import io.github.brainage04.minecraftmodconventions.ClientGameTestServers;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;

/**
 * Checks the waypoint screens' rows: the four action buttons share one width, one row and one gap, and the row is
 * centred on the Done button; the edit screen's fields share one left edge and one width, and its Save and Cancel
 * pair spans the same edges with one width and one gap. Both screens are checked at the default size, at the
 * 320-unit floor and at a large size. When {@value #DIRECTORY_ENV} names a directory, it also writes each screen's
 * geometry and a screenshot there, so a guide-line overlay can be drawn from the layout the game computed.
 */
@SuppressWarnings("UnstableApiUsage")
public final class GridlinesClientGameTest implements FabricClientGameTest {
    public static final String DIRECTORY_ENV = "BRAINAGEHUD_GRIDLINES_DIR";

    private static final int BUTTON_GAP = 4;

    @Override
    public void runTest(ClientGameTestContext context) {
        String directory = System.getenv(DIRECTORY_ENV);
        String name = directory == null ? "" : directory.strip();
        Path output = name.isEmpty() ? null : Path.of(name);
        Properties serverProperties = ClientGameTestServers.flatServerProperties();
        ClientGameTestServers.withDedicatedServer(
                context,
                serverProperties,
                "BrainageHUD gridlines GameTest",
                server -> {
                    ClientGameTestServers.assertClientWorldAndPlayerAvailable(context);
                    context.waitTicks(20);
                    int[] original = context.computeOnClient(
                            client -> new int[] {
                                client.getWindow().getWidth(), client.getWindow().getHeight()
                            });
                    int originalScale = context.computeOnClient(
                            client -> client.options.guiScale().get());
                    // The GUI width never falls below 320 (Window.MIN_WINDOW_WIDTH), so that is the floor to check.
                    for (int[] size : new int[][] {{original[0], original[1]}, {320, 240}, {1280, 720}}) {
                        context.getInput().resizeWindow(size[0], size[1]);
                        context.waitTicks(5);
                        context.runOnClient(client -> client.gui.setScreen(new WaypointsScreen(null)));
                        context.waitForScreen(WaypointsScreen.class);
                        context.waitTicks(10);
                        if (size[0] == original[0]) {
                            capture(context, output, "waypoints-screen");
                        }
                        assertWaypointsColumn(context);
                        context.runOnClient(client -> client.gui.setScreen(new WaypointEditScreen(null, null)));
                        context.waitForScreen(WaypointEditScreen.class);
                        context.waitTicks(10);
                        if (size[0] == original[0]) {
                            capture(context, output, "waypoint-edit-screen");
                        }
                        assertEditColumn(context);
                    }
                    context.getInput().resizeWindow(original[0], original[1]);
                    context.runOnClient(client -> {
                        client.options.guiScale().set(originalScale);
                        client.resizeGui();
                    });
                    context.waitTicks(5);
                });
    }

    private static void capture(ClientGameTestContext context, Path output, String name) {
        if (output == null) {
            return;
        }
        try {
            Files.createDirectories(output);
            Files.writeString(
                    output.resolve(name + ".json"), geometry(context), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError("Could not write the gridlines geometry", exception);
        }
        context.takeScreenshot(
                TestScreenshotOptions.of(name).disableCounterPrefix().withDestinationDir(output));
    }

    /**
     * The four action buttons share one width, one y and one gap, and the Done button is centred on the same
     * column as the row above it.
     */
    private static void assertWaypointsColumn(ClientGameTestContext context) {
        context.runOnClient(
                client -> {
                    List<Button> buttons = buttons(client.gui.screen());
                    List<Button> row = buttons.stream()
                            .filter(button -> !button.getMessage().getString().equals("Done"))
                            .toList();
                    if (row.size() != 4) {
                        throw new AssertionError("Expected 4 action buttons, found " + row.size());
                    }
                    Button first = row.getFirst();
                    for (Button button : row) {
                        if (button.getWidth() != first.getWidth()) {
                            throw new AssertionError("'" + button.getMessage().getString() + "' is "
                                    + button.getWidth() + " px wide, '" + first.getMessage().getString()
                                    + "' is " + first.getWidth());
                        }
                        if (button.getY() != first.getY()) {
                            throw new AssertionError("'" + button.getMessage().getString() + "' sits at y "
                                    + button.getY() + ", '" + first.getMessage().getString() + "' at "
                                    + first.getY());
                        }
                    }
                    for (int index = 1; index < row.size(); index++) {
                        Button previous = row.get(index - 1);
                        Button current = row.get(index);
                        int gap = current.getX() - (previous.getX() + previous.getWidth());
                        if (gap != BUTTON_GAP) {
                            throw new AssertionError("Gap before '" + current.getMessage().getString()
                                    + "' is " + gap + " px, expected " + BUTTON_GAP);
                        }
                    }
                    Button done = buttons.stream()
                            .filter(button -> button.getMessage().getString().equals("Done"))
                            .findFirst()
                            .orElseThrow(() -> new AssertionError("No Done button"));
                    int rowCentre = first.getX() + (row.getLast().getX() + row.getLast().getWidth()
                            - first.getX()) / 2;
                    int doneCentre = done.getX() + done.getWidth() / 2;
                    if (Math.abs(rowCentre - doneCentre) > 1) {
                        throw new AssertionError("The action row is centred at " + rowCentre
                                + " px and Done at " + doneCentre);
                    }
                    for (Button button : buttons) {
                        if (button.getX() < 0
                                || button.getX() + button.getWidth() > client.gui.screen().width) {
                            throw new AssertionError("'" + button.getMessage().getString() + "' spans "
                                    + button.getX() + " to " + (button.getX() + button.getWidth())
                                    + " px, the screen is " + client.gui.screen().width + " wide");
                        }
                    }
                });
    }

    /**
     * The edit screen's fields and its buttons share one column: the three coordinate fields together span the
     * name field, and the Save and Cancel pair spans the same width on the same edges.
     */
    private static void assertEditColumn(ClientGameTestContext context) {
        context.runOnClient(
                client -> {
                    List<AbstractWidget> widgets = new ArrayList<>();
                    collect(client.gui.screen(), widgets);
                    List<EditBox> boxes = widgets.stream()
                            .filter(EditBox.class::isInstance)
                            .map(EditBox.class::cast)
                            .toList();
                    if (boxes.size() != 4) {
                        throw new AssertionError("Expected 4 fields, found " + boxes.size());
                    }
                    EditBox name = boxes.getFirst();
                    int nameLeft = name.getX();
                    int nameRight = name.getX() + name.getWidth();
                    EditBox firstCoordinate = boxes.get(1);
                    EditBox lastCoordinate = boxes.getLast();
                    if (firstCoordinate.getX() != nameLeft) {
                        throw new AssertionError("The coordinate row starts at " + firstCoordinate.getX()
                                + " px, the name field at " + nameLeft);
                    }
                    int coordinateRight = lastCoordinate.getX() + lastCoordinate.getWidth();
                    if (coordinateRight != nameRight) {
                        throw new AssertionError("The coordinate row ends at " + coordinateRight
                                + " px, the name field at " + nameRight);
                    }
                    List<Button> buttons = widgets.stream()
                            .filter(Button.class::isInstance)
                            .map(Button.class::cast)
                            .toList();
                    if (buttons.size() != 2) {
                        throw new AssertionError("Expected 2 buttons, found " + buttons.size());
                    }
                    Button save = buttons.getFirst();
                    Button cancel = buttons.getLast();
                    if (save.getWidth() != cancel.getWidth()) {
                        throw new AssertionError("Save is " + save.getWidth() + " px wide, Cancel "
                                + cancel.getWidth());
                    }
                    int buttonGap = cancel.getX() - (save.getX() + save.getWidth());
                    if (buttonGap != BUTTON_GAP) {
                        throw new AssertionError("The gap between Save and Cancel is " + buttonGap
                                + " px, expected " + BUTTON_GAP);
                    }
                    if (save.getX() != nameLeft) {
                        throw new AssertionError("Save starts at " + save.getX() + " px, the fields at "
                                + nameLeft);
                    }
                    int cancelRight = cancel.getX() + cancel.getWidth();
                    if (cancelRight != nameRight) {
                        throw new AssertionError("Cancel ends at " + cancelRight + " px, the fields at "
                                + nameRight);
                    }
                    for (Button button : buttons) {
                        if (button.getX() < 0
                                || button.getX() + button.getWidth() > client.gui.screen().width) {
                            throw new AssertionError("'" + button.getMessage().getString() + "' spans "
                                    + button.getX() + " to " + (button.getX() + button.getWidth())
                                    + " px, the screen is " + client.gui.screen().width + " wide");
                        }
                    }
                });
    }

    private static List<Button> buttons(ContainerEventHandler parent) {
        List<AbstractWidget> widgets = new ArrayList<>();
        collect(parent, widgets);
        return widgets.stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
    }

    private static void collect(ContainerEventHandler parent, List<? super AbstractWidget> out) {
        for (var listener : parent.children()) {
            if (listener instanceof AbstractWidget widget) {
                out.add(widget);
                if (widget instanceof ContainerEventHandler nested) {
                    collect(nested, out);
                }
            }
        }
    }

    private static String geometry(ClientGameTestContext context) {
        return context.computeOnClient(
                client -> {
                    JsonObject root = new JsonObject();
                    root.addProperty("guiWidth", client.gui.screen().width);
                    root.addProperty("guiHeight", client.gui.screen().height);
                    root.addProperty("guiScale", client.getWindow().getGuiScale());
                    root.addProperty("windowWidth", client.getWindow().getWidth());
                    root.addProperty("windowHeight", client.getWindow().getHeight());
                    List<AbstractWidget> widgets = new ArrayList<>();
                    collect(client.gui.screen(), widgets);
                    JsonArray array = new JsonArray();
                    for (AbstractWidget widget : widgets) {
                        JsonObject entry = new JsonObject();
                        entry.addProperty("type", widget.getClass().getSimpleName());
                        entry.addProperty("x", widget.getX());
                        entry.addProperty("y", widget.getY());
                        entry.addProperty("width", widget.getWidth());
                        entry.addProperty("height", widget.getHeight());
                        entry.addProperty("message", widget.getMessage().getString());
                        array.add(entry);
                    }
                    root.add("widgets", array);
                    return new GsonBuilder().setPrettyPrinting().create().toJson(root);
                });
    }
}
