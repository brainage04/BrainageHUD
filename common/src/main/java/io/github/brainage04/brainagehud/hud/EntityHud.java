package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.EntityHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

/** How many entities the client has loaded, grouped by kind of mob. */
public class EntityHud implements BasicCoreHudElement<EntityHudConfig> {
    enum Group {
        CREATURES("Creatures"),
        WATER_CREATURES("Water Creatures"),
        AMBIENT("Ambient"),
        MONSTERS("Monsters"),
        OTHERS("Others");

        final String label;

        Group(String label) {
            this.label = label;
        }

        static Group of(MobCategory category) {
            return switch (category) {
                case CREATURE -> CREATURES;
                case WATER_CREATURE, UNDERGROUND_WATER_CREATURE, WATER_AMBIENT, AXOLOTLS -> WATER_CREATURES;
                case AMBIENT -> AMBIENT;
                case MONSTER -> MONSTERS;
                case MISC -> OTHERS;
            };
        }
    }

    @Override
    public TextList getLines() {
        TextList lines = new TextList();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return lines;

        Map<Group, Integer> counts = new EnumMap<>(Group.class);
        int total = 0;
        for (Entity entity : level.entitiesForRendering()) {
            counts.merge(Group.of(entity.getType().getCategory()), 1, Integer::sum);
            total++;
        }

        EntityHudConfig config = getElementConfig();
        lines.add("Entities: %d".formatted(total));
        addGroup(lines, counts, Group.CREATURES, config.showCreatures);
        addGroup(lines, counts, Group.WATER_CREATURES, config.showWaterCreatures);
        addGroup(lines, counts, Group.AMBIENT, config.showAmbient);
        addGroup(lines, counts, Group.MONSTERS, config.showMonsters);
        addGroup(lines, counts, Group.OTHERS, config.showOthers);
        return lines;
    }

    private static void addGroup(TextList lines, Map<Group, Integer> counts, Group group, boolean shown) {
        if (shown) lines.add("  %s: %d".formatted(group.label, counts.getOrDefault(group, 0)));
    }

    @Override
    public EntityHudConfig getElementConfig() {
        return getConfig().entityHudConfig;
    }
}
