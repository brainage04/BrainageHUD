package io.github.brainage04.brainagehud.waypoint;

import com.google.common.net.HostAndPort;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.github.brainage04.brainagehud.BrainageHUD;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;

public final class WaypointStore {
    private static final String SERVER_PREFIX = "server/";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type WORLDS_TYPE =
            new TypeToken<LinkedHashMap<String, ArrayList<Waypoint>>>() {}.getType();
    private static final int[] COLOURS = {
        0xFF5555, 0x55FF55, 0x5555FF, 0xFFFF55, 0xFF55FF, 0x55FFFF,
        0xFFAA00, 0xAA00AA, 0x00AAAA, 0xFFFFFF, 0xAAAAAA, 0x00AA00
    };

    private static Map<String, List<Waypoint>> worlds;

    private WaypointStore() {}

    public static Optional<List<Waypoint>> current() {
        return currentWorldKey()
                .map(key -> load().computeIfAbsent(key, ignored -> new ArrayList<>()));
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
        if (serverData != null) return Optional.of(serverKey(serverData.ip));

        return Optional.empty();
    }

    public static void save() {
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(load(), writer);
            }
            Files.move(
                    temporary,
                    file,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            BrainageHUD.LOGGER.error("Could not save waypoints to {}", file, exception);
        }
    }

    public static Optional<Waypoint> find(List<Waypoint> waypoints, String name) {
        return waypoints.stream()
                .filter(waypoint -> waypoint.name.equalsIgnoreCase(name))
                .findFirst();
    }

    public static String nextQuickName(List<Waypoint> waypoints) {
        for (int number = 1; ; number++) {
            String name = "Waypoint " + number;
            if (find(waypoints, name).isEmpty()) return name;
        }
    }

    public static int nextColour(List<Waypoint> waypoints) {
        return COLOURS[waypoints.size() % COLOURS.length];
    }

    private static Map<String, List<Waypoint>> load() {
        if (worlds != null) return worlds;

        worlds = new LinkedHashMap<>();
        Path file = file();
        if (!Files.exists(file)) return worlds;

        try (Reader reader = Files.newBufferedReader(file)) {
            worlds.putAll(read(reader));
        } catch (IOException | JsonParseException exception) {
            // keep the unreadable file rather than overwriting it with an empty list on the next
            // save
            Path backup = file.resolveSibling(file.getFileName() + ".unreadable");
            BrainageHUD.LOGGER.error(
                    "Could not read waypoints from {}; moving it to {}", file, backup, exception);
            try {
                Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException moveException) {
                BrainageHUD.LOGGER.error("Could not move {} aside", file, moveException);
            }
        }

        return worlds;
    }

    static Map<String, List<Waypoint>> read(Reader reader) {
        Map<String, List<Waypoint>> loaded = GSON.fromJson(reader, WORLDS_TYPE);
        if (loaded == null) return new LinkedHashMap<>();
        loaded.values().removeIf(Objects::isNull);
        loaded.forEach(
                (world, waypoints) -> {
                    int before = waypoints.size();
                    waypoints.removeIf(WaypointStore::isIncomplete);
                    int dropped = before - waypoints.size();
                    if (dropped > 0) {
                        BrainageHUD.LOGGER.warn(
                                "Dropped {} waypoint(s) without a name or dimension in {}",
                                dropped,
                                world);
                    }
                });
        return mergeServerKeys(loaded);
    }

    /**
     * The key of a server's waypoints: the address in lower case, without a port that the game
     * treats as no port at all, so {@code host} and {@code host:25565} share one list.
     */
    static String serverKey(String address) {
        String lowerCase = address.toLowerCase(Locale.ROOT);
        try {
            HostAndPort hostAndPort = HostAndPort.fromString(lowerCase);
            String host = hostAndPort.getHost();
            String bare = HostAndPort.fromHost(host).toString();
            // ServerAddress gives an address without a port the default one, and the game resolves
            // both forms alike, SRV lookup included (26.3 ServerAddress.java:40,
            // ServerRedirectHandler.java:35).
            if (hostAndPort.hasPort()
                    && !host.isEmpty()
                    && ServerAddress.parseString(lowerCase)
                            .equals(ServerAddress.parseString(bare))) {
                return SERVER_PREFIX + bare;
            }
        } catch (IllegalArgumentException ignored) {
            // not an address the game can connect to: keep it as it is
        }
        return SERVER_PREFIX + lowerCase;
    }

    /**
     * Moves waypoints saved under an older form of a server's key into {@link #serverKey}'s list. A
     * moved waypoint whose name is taken there gets a number, as {@code find()} needs unique names.
     */
    private static Map<String, List<Waypoint>> mergeServerKeys(Map<String, List<Waypoint>> loaded) {
        Map<String, List<Waypoint>> merged = new LinkedHashMap<>();
        loaded.forEach(
                (world, waypoints) -> {
                    String key =
                            world.startsWith(SERVER_PREFIX)
                                    ? serverKey(world.substring(SERVER_PREFIX.length()))
                                    : world;
                    List<Waypoint> target = merged.get(key);
                    if (target == null) {
                        merged.put(key, waypoints);
                        return;
                    }
                    BrainageHUD.LOGGER.info("Moving the waypoints of {} to {}", world, key);
                    for (Waypoint waypoint : waypoints) {
                        waypoint.name = freeName(target, waypoint.name);
                        target.add(waypoint);
                    }
                });
        return merged;
    }

    private static String freeName(List<Waypoint> waypoints, String name) {
        if (find(waypoints, name).isEmpty()) return name;
        for (int number = 2; ; number++) {
            String numbered = "%s (%d)".formatted(name, number);
            if (find(waypoints, numbered).isEmpty()) return numbered;
        }
    }

    private static boolean isIncomplete(Waypoint waypoint) {
        // find() compares the name and the renderer the dimension, so an entry without them would
        // throw instead of being left out
        return waypoint == null || waypoint.name == null || waypoint.dimension == null;
    }

    private static Path file() {
        return Minecraft.getInstance()
                .gameDirectory
                .toPath()
                .resolve("config")
                .resolve(BrainageHUD.MOD_ID)
                .resolve("waypoints.json");
    }
}
