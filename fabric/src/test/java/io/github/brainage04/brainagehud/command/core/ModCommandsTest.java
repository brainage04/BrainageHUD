package io.github.brainage04.brainagehud.command.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import io.github.brainage04.brainagehud.TestEnchantments;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Parses commands against the registered tree without executing them. */
class ModCommandsTest {
    private static CommandDispatcher<SharedSuggestionProvider> dispatcher;

    @BeforeAll
    static void registerCommands() {
        HolderLookup.Provider registries =
                HolderLookup.Provider.create(Stream.of(TestEnchantments.registry(), BuiltInRegistries.ITEM));
        dispatcher = new CommandDispatcher<>();
        ModCommands.registerClientCommands(
                dispatcher, CommandBuildContext.simple(registries, FeatureFlags.VANILLA_SET));
    }

    @Test
    void getEnchantInfoReadsAnIdAsAnEnchantmentId() {
        assertEquals(
                List.of("getenchantinfo", "enchantmentId"),
                parsedNodes("getenchantinfo brainagehud_test:sharpness"));
    }

    @Test
    void getEnchantInfoFallsBackToANameThatIsNotAnId() {
        assertEquals(List.of("getenchantinfo", "enchantmentName"), parsedNodes("getenchantinfo Sharpness"));
        assertEquals(List.of("getenchantinfo", "enchantmentName"), parsedNodes("getenchantinfo \"Fire Aspect\""));
    }

    @Test
    void fullbrightAcceptsOnlyTheSupportedRange() {
        assertTrue(isExecutable("fullbright -1"));
        assertTrue(isExecutable("fullbright 0.5"));
        assertFalse(isExecutable("fullbright 1.5"));
    }

    @Test
    void blacklistRejectsUnknownEnchantments() {
        assertTrue(isExecutable("blacklistedenchants add brainagehud_test:smite"));
        assertFalse(isExecutable("blacklistedenchants add brainagehud_test:protection"));
    }

    private static List<String> parsedNodes(String command) {
        ParseResults<SharedSuggestionProvider> parse = dispatcher.parse(command, null);
        assertTrue(parse.getExceptions().isEmpty() && !parse.getReader().canRead(), () -> command + " did not parse: " + parse.getExceptions());

        CommandContextBuilder<SharedSuggestionProvider> context = parse.getContext();
        return context.getNodes().stream().map(ParsedCommandNode::getNode).map(node -> node.getName()).toList();
    }

    private static boolean isExecutable(String command) {
        ParseResults<SharedSuggestionProvider> parse = dispatcher.parse(command, null);
        return !parse.getReader().canRead() && parse.getContext().getCommand() != null;
    }
}
