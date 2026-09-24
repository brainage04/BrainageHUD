package io.github.brainage04.brainagehud.screen;

import io.github.brainage04.brainagehud.waypoint.Waypoint;
import io.github.brainage04.brainagehud.waypoint.WaypointActions;
import io.github.brainage04.brainagehud.waypoint.WaypointStore;
import io.github.brainage04.brainagehud.waypoint.WorldCentre;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Lists the World Centre and the current world's waypoints, with buttons to add, edit, show/hide
 * and delete them. The World Centre can only be shown or hidden.
 */
public class WaypointsScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFFA0A0A0;
    private static final int BUTTON_WIDTH = 74;
    private static final int BUTTON_GAP = 4;

    @Nullable private final Screen parent;
    @Nullable private List<Waypoint> waypoints;
    private WaypointList list;
    private Button editButton;
    private Button toggleButton;
    private Button deleteButton;

    public WaypointsScreen(@Nullable Screen parent) {
        super(Component.literal("Waypoints"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        waypoints = WaypointStore.current().orElse(null);

        list = addRenderableWidget(new WaypointList(minecraft, width, height - 32 - 64, 32, 24));
        if (waypoints != null && minecraft.level != null) {
            list.add(new WaypointEntry(WorldCentre.in(minecraft.level.dimension().identifier().toString()), true));
            waypoints.forEach(waypoint -> list.add(new WaypointEntry(waypoint, false)));
        }

        int rowWidth = BUTTON_WIDTH * 4 + BUTTON_GAP * 3;
        int left = (width - rowWidth) / 2;
        int rowY = height - 56;
        Button addButton = addRenderableWidget(Button.builder(Component.literal("Add"), button -> edit(null))
                .bounds(left, rowY, BUTTON_WIDTH, 20).build());
        addButton.active = waypoints != null;
        editButton = addRenderableWidget(Button.builder(Component.literal("Edit"), button -> selectedOwn().ifPresent(this::edit))
                .bounds(left + (BUTTON_WIDTH + BUTTON_GAP), rowY, BUTTON_WIDTH, 20).build());
        toggleButton = addRenderableWidget(Button.builder(Component.literal("Hide"), button -> toggleSelected())
                .bounds(left + (BUTTON_WIDTH + BUTTON_GAP) * 2, rowY, BUTTON_WIDTH, 20).build());
        deleteButton = addRenderableWidget(Button.builder(Component.literal("Delete"), button -> deleteSelected())
                .bounds(left + (BUTTON_WIDTH + BUTTON_GAP) * 3, rowY, BUTTON_WIDTH, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds((width - 200) / 2, height - 28, 200, 20).build());

        updateButtons();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 12, WHITE);

        if (waypoints == null) {
            graphics.centeredText(font, "Waypoints are kept per world. Join a world to manage them.", width / 2, height / 2 - 20, GREY);
        } else if (waypoints.isEmpty()) {
            graphics.centeredText(font, "No waypoints of your own yet. Press Add, or the Create Waypoint key in game.", width / 2, height / 2 - 20, GREY);
        }
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private Optional<Waypoint> selected() {
        WaypointEntry entry = list.getSelected();
        return entry == null ? Optional.empty() : Optional.of(entry.waypoint);
    }

    /** The selected waypoint if it is one of the world's own, which can be edited and deleted. */
    private Optional<Waypoint> selectedOwn() {
        WaypointEntry entry = list.getSelected();
        return entry == null || entry.builtIn ? Optional.empty() : Optional.of(entry.waypoint);
    }

    private void edit(@Nullable Waypoint waypoint) {
        minecraft.gui.setScreen(new WaypointEditScreen(this, waypoint));
    }

    private void toggleSelected() {
        WaypointEntry entry = list.getSelected();
        if (entry == null) return;

        entry.waypoint.visible = !entry.waypoint.visible;
        if (entry.builtIn) {
            WorldCentre.setVisible(entry.waypoint.visible);
        } else {
            WaypointStore.save();
        }
        updateButtons();
    }

    private void deleteSelected() {
        WaypointEntry entry = list.getSelected();
        if (entry == null || entry.builtIn) return;

        WaypointActions.remove(entry.waypoint.name);
        list.remove(entry);
        updateButtons();
    }

    private void updateButtons() {
        Optional<Waypoint> selected = selected();
        editButton.active = selectedOwn().isPresent();
        toggleButton.active = selected.isPresent();
        deleteButton.active = selectedOwn().isPresent();
        toggleButton.setMessage(Component.literal(selected.map(waypoint -> waypoint.visible).orElse(true) ? "Hide" : "Show"));
    }

    private final class WaypointList extends ObjectSelectionList<WaypointEntry> {
        private WaypointList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        private void add(WaypointEntry entry) {
            addEntry(entry);
        }

        private void remove(WaypointEntry entry) {
            removeEntry(entry);
            setSelected(null);
        }

        @Override
        public void setSelected(@Nullable WaypointEntry entry) {
            super.setSelected(entry);
            if (editButton != null) updateButtons();
        }

        @Override
        public int getRowWidth() {
            return 300;
        }
    }

    private final class WaypointEntry extends ObjectSelectionList.Entry<WaypointEntry> {
        private final Waypoint waypoint;
        /** The World Centre: it can be hidden, but not edited or deleted. */
        private final boolean builtIn;

        private WaypointEntry(Waypoint waypoint, boolean builtIn) {
            this.waypoint = waypoint;
            this.builtIn = builtIn;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int x = getContentX() + 2;
            int y = getContentY() + 1;
            graphics.text(font, WaypointActions.name(waypoint), x, y, WHITE);
            if (!waypoint.visible) {
                graphics.text(font, "(hidden)", x + font.width(waypoint.name) + 6, y, GREY);
            }
            String where = builtIn ? "every dimension" : waypoint.dimension;
            graphics.text(font, "%s  %s".formatted(waypoint.pos().toShortString(), where), x, y + 11, GREY);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (doubleClick && !builtIn) {
                edit(waypoint);
                return true;
            }
            return super.mouseClicked(event, doubleClick);
        }

        @Override
        public Component getNarration() {
            return Component.literal(waypoint.name);
        }
    }
}
