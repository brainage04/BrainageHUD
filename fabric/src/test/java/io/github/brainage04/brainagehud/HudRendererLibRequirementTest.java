package io.github.brainage04.brainagehud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.junit.jupiter.api.Test;

/**
 * BrainageHUD renders its wrapped HUD lines through HudRendererLib 1.1.1's per-element maximum width, so both loaders
 * have to turn away an older library instead of starting and crashing on the first frame. Fabric Loader and NeoForge
 * read the requirement from the metadata this test checks: the Fabric metadata Fabric Loader itself ships, and the
 * NeoForge template whose version range is filled in from the one pin in gradle.properties.
 */
class HudRendererLibRequirementTest {
    private static final String MOD_ID = "brainagehud";
    private static final String LIBRARY_ID = "hudrendererlib";
    private static final Version FIRST_WRAPPING_LIBRARY = version("1.1.1");
    private static final Version LIBRARY_WITHOUT_WRAPPING_API = version("1.1.0");
    private static final Version LATER_LIBRARY = version("1.2.0");
    private static final Pattern NEOFORGE_LIBRARY_DEPENDENCY = Pattern.compile(
            "\\[\\[dependencies\\.brainagehud]]\\s+modId=\""
                    + LIBRARY_ID
                    + "\".*?versionRange=\"([^\"]+)\"",
            Pattern.DOTALL);

    @Test
    void fabricLoaderRefusesTheLibraryWithoutTheWrappingApi() throws IOException {
        JsonObject depends = fabricMetadata().getAsJsonObject("depends");
        String requirement = depends.get(LIBRARY_ID).getAsString();
        VersionPredicate accepted = predicate(requirement);

        assertFalse(
                accepted.test(LIBRARY_WITHOUT_WRAPPING_API),
                "Fabric Loader would accept " + LIBRARY_WITHOUT_WRAPPING_API.getFriendlyString() + " for " + requirement);
        assertTrue(accepted.test(FIRST_WRAPPING_LIBRARY), requirement + " refuses 1.1.1");
        assertTrue(accepted.test(LATER_LIBRARY), requirement + " refuses a later library");
    }

    @Test
    void neoForgeRequiresTheSameLibraryVersion() throws IOException {
        Path root = projectRoot();
        String template = Files.readString(
                root.resolve("neoforge/src/main/resources/META-INF/neoforge.mods.toml"), StandardCharsets.UTF_8);
        Matcher dependency = NEOFORGE_LIBRARY_DEPENDENCY.matcher(template);
        assertTrue(dependency.find(), "neoforge.mods.toml has no dependency block for " + LIBRARY_ID);

        Properties pins = new Properties();
        try (Reader reader = Files.newBufferedReader(root.resolve("gradle.properties"), StandardCharsets.UTF_8)) {
            pins.load(reader);
        }
        String libraryVersion = pins.getProperty("hudrendererlib_version");

        assertEquals(
                "[${hudrendererlib_version},)",
                dependency.group(1),
                "the NeoForge range must come from the one hudrendererlib_version pin");
        assertEquals("[1.1.1,)", dependency.group(1).replace("${hudrendererlib_version}", libraryVersion));
    }

    private static JsonObject fabricMetadata() throws IOException {
        // The GameTest source set ships its own fabric.mod.json under the same classpath path, so pick the mod's own.
        Enumeration<URL> candidates =
                HudRendererLibRequirementTest.class.getClassLoader().getResources("fabric.mod.json");
        while (candidates.hasMoreElements()) {
            URL candidate = candidates.nextElement();
            try (InputStream stream = candidate.openStream()) {
                JsonObject metadata = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                        .getAsJsonObject();
                if (MOD_ID.equals(metadata.get("id").getAsString())) {
                    return metadata;
                }
            }
        }
        throw new AssertionError("No fabric.mod.json for " + MOD_ID + " on the test classpath");
    }

    private static Version version(String version) {
        try {
            return Version.parse(version);
        } catch (VersionParsingException exception) {
            throw new AssertionError(version, exception);
        }
    }

    private static VersionPredicate predicate(String requirement) {
        try {
            return VersionPredicate.parse(requirement);
        } catch (VersionParsingException exception) {
            throw new AssertionError(requirement, exception);
        }
    }

    /** The directory holding gradle.properties and settings.gradle, wherever the test task runs from. */
    private static Path projectRoot() {
        for (Path candidate = Path.of("").toAbsolutePath(); candidate != null; candidate = candidate.getParent()) {
            if (Files.isRegularFile(candidate.resolve("settings.gradle"))
                    && Files.isRegularFile(candidate.resolve("gradle.properties"))) {
                return candidate;
            }
        }
        throw new AssertionError("Cannot find the Gradle root above " + Path.of("").toAbsolutePath());
    }
}
