package io.github.brainage04.brainagehud.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.github.brainage04.brainagehud.BrainageHUD;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;

/**
 * The user's waypoints, kept per world in {@code config/brainagehud/waypoints.json}. A world is a
 * singleplayer save (by folder name) or a server (by address). Used only on the render thread.
 */
public final class WaypointStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int[] COLOURS = {
        0xFF5555, 0x55FF55, 0x5555FF, 0xFFFF55, 0xFF55FF, 0x55FFFF,
        0xFFAA00, 0xAA00AA, 0x00AAAA, 0xFFFFFF, 0xAAAAAA, 0x00AA00
    };

    private static Map<String, List<Waypoint>> worlds;

    private WaypointStore() {}

    /** The current world's waypoints, or an empty optional outside a world. The list is live. */
    public static Optional<List<Waypoint>> current() {
        return currentWorldKey().map(key -> load().computeIfAbsent(key, ignored -> new ArrayList<>()));
    }

    public static Optional<String> currentWorldKey() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return Optional.empty();

        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server != null) {
            Path root = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            return Optional.of("singleplayer/" + root.getFileName());
        }

        ServerData serverData = minecraft.getCurrentServer();
        if (serverData != null) return Optional.of("server/" + serverData.ip.toLowerCase(Locale.ROOT));

        return Optional.empty();
    }

    /** Writes every world's waypoints to disk. */
    public static void save() {
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(load(), writer);
            }
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            BrainageHUD.LOGGER.error("Could not save waypoints to {}", file, exception);
        }
    }

    /** The waypoint called {@code name}, ignoring case. */
    public static Optional<Waypoint> find(List<Waypoint> waypoints, String name) {
        return waypoints.stream().filter(waypoint -> waypoint.name.equalsIgnoreCase(name)).findFirst();
    }

    /** The first of "Waypoint 1", "Waypoint 2", ... that no waypoint in the list is called. */
    public static String nextQuickName(List<Waypoint> waypoints) {
        for (int number = 1; ; number++) {
            String name = "Waypoint " + number;
            if (find(waypoints, name).isEmpty()) return name;
        }
    }

    /** Cycles through a palette so that consecutive waypoints are told apart. */
    public static int nextColour(List<Waypoint> waypoints) {
        return COLOURS[waypoints.size() % COLOURS.length];
    }

    private static Map<String, List<Waypoint>> load() {
        if (worlds != null) return worlds;

        worlds = new LinkedHashMap<>();
        Path file = file();
        if (!Files.exists(file)) return worlds;

        try (Reader reader = Files.newBufferedReader(file)) {
            Map<String, List<Waypoint>> loaded =
                    GSON.fromJson(reader, new TypeToken<LinkedHashMap<String, ArrayList<Waypoint>>>() {}.getType());
            if (loaded != null) worlds.putAll(loaded);
        } catch (IOException | JsonParseException exception) {
            // keep the unreadable file rather than overwriting it with an empty list on the next save
            Path backup = file.resolveSibling(file.getFileName() + ".unreadable");
            BrainageHUD.LOGGER.error("Could not read waypoints from {}; moving it to {}", file, backup, exception);
            try {
                Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException moveException) {
                BrainageHUD.LOGGER.error("Could not move {} aside", file, moveException);
            }
        }

        return worlds;
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(BrainageHUD.MOD_ID).resolve("waypoints.json");
    }
}
