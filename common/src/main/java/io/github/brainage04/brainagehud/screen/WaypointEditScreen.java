package io.github.brainage04.brainagehud.screen;

import io.github.brainage04.brainagehud.waypoint.Waypoint;
import io.github.brainage04.brainagehud.waypoint.WaypointActions;
import io.github.brainage04.brainagehud.waypoint.WaypointStore;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** Creates a waypoint, prefilled with the player's position, or edits an existing one. */
public class WaypointEditScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFFA0A0A0;
    private static final int RED = 0xFFFF5555;
    private static final int FIELD_WIDTH = 200;
    private static final int COORDINATE_WIDTH = 64;

    private final Screen parent;
    @Nullable private final Waypoint waypoint;
    private EditBox nameBox;
    private EditBox xBox;
    private EditBox yBox;
    private EditBox zBox;
    @Nullable private String error;

    public WaypointEditScreen(Screen parent, @Nullable Waypoint waypoint) {
        super(Component.literal(waypoint == null ? "Create Waypoint" : "Edit Waypoint"));
        this.parent = parent;
        this.waypoint = waypoint;
    }

    @Override
    protected void init() {
        int left = (width - FIELD_WIDTH) / 2;

        nameBox = addRenderableWidget(new EditBox(font, left, 60, FIELD_WIDTH, 20, Component.literal("Name")));
        nameBox.setMaxLength(WaypointActions.MAX_NAME_LENGTH);
        xBox = addRenderableWidget(new EditBox(font, left, 104, COORDINATE_WIDTH, 20, Component.literal("X")));
        yBox = addRenderableWidget(new EditBox(font, left + (FIELD_WIDTH - COORDINATE_WIDTH) / 2, 104, COORDINATE_WIDTH, 20, Component.literal("Y")));
        zBox = addRenderableWidget(new EditBox(font, left + FIELD_WIDTH - COORDINATE_WIDTH, 104, COORDINATE_WIDTH, 20, Component.literal("Z")));

        if (nameBox.getValue().isEmpty()) fillInitialValues();

        addRenderableWidget(Button.builder(Component.literal("Save"), button -> save())
                .bounds(width / 2 - 102, height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
                .bounds(width / 2 + 2, height - 28, 100, 20).build());

        setInitialFocus(nameBox);
    }

    private void fillInitialValues() {
        if (waypoint != null) {
            nameBox.setValue(waypoint.name);
            setPosition(waypoint.pos());
            return;
        }

        nameBox.setValue(WaypointStore.nextQuickName(WaypointStore.current().orElse(List.of())));
        if (minecraft.player != null) setPosition(minecraft.player.blockPosition());
    }

    private void setPosition(BlockPos pos) {
        xBox.setValue(Integer.toString(pos.getX()));
        yBox.setValue(Integer.toString(pos.getY()));
        zBox.setValue(Integer.toString(pos.getZ()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 20, WHITE);
        graphics.text(font, "Name", nameBox.getX(), nameBox.getY() - 11, GREY);
        graphics.text(font, "X", xBox.getX(), xBox.getY() - 11, GREY);
        graphics.text(font, "Y", yBox.getX(), yBox.getY() - 11, GREY);
        graphics.text(font, "Z", zBox.getX(), zBox.getY() - 11, GREY);
        if (error != null) graphics.centeredText(font, error, width / 2, 140, RED);
    }

    private void save() {
        String name = nameBox.getValue().strip();
        Optional<BlockPos> pos = parsePosition();
        Optional<String> problem = WaypointActions.validateName(name);
        if (problem.isPresent()) {
            error = problem.get();
            return;
        }
        if (pos.isEmpty()) {
            error = "X, Y and Z must be whole numbers.";
            return;
        }

        if (waypoint == null) {
            // reports its own problems, e.g. a duplicate name, in chat
            if (WaypointActions.create(name, pos.get()).isEmpty()) {
                error = "Could not create the waypoint; see chat.";
                return;
            }
        } else {
            List<Waypoint> waypoints = WaypointStore.current().orElse(List.of());
            Optional<Waypoint> sameName = WaypointStore.find(waypoints, name);
            if (sameName.isPresent() && sameName.get() != waypoint) {
                error = "Another waypoint is already called \"%s\".".formatted(name);
                return;
            }

            waypoint.name = name;
            waypoint.x = pos.get().getX();
            waypoint.y = pos.get().getY();
            waypoint.z = pos.get().getZ();
            WaypointStore.save();
        }

        onClose();
    }

    private Optional<BlockPos> parsePosition() {
        try {
            return Optional.of(new BlockPos(
                    Integer.parseInt(xBox.getValue().strip()),
                    Integer.parseInt(yBox.getValue().strip()),
                    Integer.parseInt(zBox.getValue().strip())));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
