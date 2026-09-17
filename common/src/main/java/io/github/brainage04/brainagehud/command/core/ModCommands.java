package io.github.brainage04.brainagehud.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.github.brainage04.brainagehud.command.BlacklistedEnchantsCommand;
import io.github.brainage04.brainagehud.command.GetEnchantInfoCommand;
import io.github.brainage04.brainagehud.command.GetEnchantsCommand;
import io.github.brainage04.brainagehud.command.core.argument.ClientHolderReferenceArgumentType;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Registers BrainageHUD's loader-agnostic client commands.
 *
 * <p>The enchantment commands are ported from GetEnchantInfo, which registered them through its own
 * platform abstraction. BrainageHUD registers them directly from each loader entrypoint instead;
 * the command sources of both loaders implement {@link SharedSuggestionProvider}, so no casts are
 * required.
 */
public final class ModCommands {
    private ModCommands() {}

    public static <S extends SharedSuggestionProvider> void registerClientCommands(
            CommandDispatcher<S> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
                LiteralArgumentBuilder.<S>literal("getenchantinfo")
                        .then(
                                RequiredArgumentBuilder.<S, Holder.Reference<Enchantment>>argument(
                                                "enchantmentId",
                                                ClientHolderReferenceArgumentType.registryEntry(
                                                        registryAccess, Registries.ENCHANTMENT))
                                        .executes(
                                                context ->
                                                        GetEnchantInfoCommand.execute(
                                                                context.getSource(),
                                                                ClientHolderReferenceArgumentType
                                                                        .getEnchantment(
                                                                                context,
                                                                                "enchantmentId"))))
                        .then(
                                RequiredArgumentBuilder.<S, String>argument(
                                                "enchantmentName", StringArgumentType.string())
                                        .executes(
                                                context ->
                                                        GetEnchantInfoCommand.execute(
                                                                context.getSource(),
                                                                StringArgumentType.getString(
                                                                        context,
                                                                        "enchantmentName")))));

        dispatcher.register(
                LiteralArgumentBuilder.<S>literal("getenchants")
                        .executes(context -> GetEnchantsCommand.execute(context.getSource()))
                        .then(
                                RequiredArgumentBuilder.<S, Holder.Reference<Item>>argument(
                                                "item",
                                                ClientHolderReferenceArgumentType.registryEntry(
                                                        registryAccess, Registries.ITEM))
                                        .executes(
                                                context ->
                                                        GetEnchantsCommand.execute(
                                                                context.getSource(),
                                                                ClientHolderReferenceArgumentType
                                                                        .getItem(
                                                                                context,
                                                                                "item")))));

        dispatcher.register(
                LiteralArgumentBuilder.<S>literal("blacklistedenchants")
                        .then(
                                LiteralArgumentBuilder.<S>literal("add")
                                        .then(
                                                RequiredArgumentBuilder
                                                        .<S, Holder.Reference<Enchantment>>argument(
                                                                "enchantmentId",
                                                                ClientHolderReferenceArgumentType
                                                                        .registryEntry(
                                                                                registryAccess,
                                                                                Registries
                                                                                        .ENCHANTMENT))
                                                        .executes(
                                                                context ->
                                                                        BlacklistedEnchantsCommand
                                                                                .executeAdd(
                                                                                        context
                                                                                                .getSource(),
                                                                                        ClientHolderReferenceArgumentType
                                                                                                .getEnchantment(
                                                                                                        context,
                                                                                                        "enchantmentId")))))
                        .then(
                                LiteralArgumentBuilder.<S>literal("remove")
                                        .then(
                                                RequiredArgumentBuilder
                                                        .<S, Holder.Reference<Enchantment>>argument(
                                                                "enchantmentId",
                                                                ClientHolderReferenceArgumentType
                                                                        .registryEntry(
                                                                                registryAccess,
                                                                                Registries
                                                                                        .ENCHANTMENT))
                                                        .executes(
                                                                context ->
                                                                        BlacklistedEnchantsCommand
                                                                                .executeRemove(
                                                                                        context
                                                                                                .getSource(),
                                                                                        ClientHolderReferenceArgumentType
                                                                                                .getEnchantment(
                                                                                                        context,
                                                                                                        "enchantmentId")))))
                        .then(
                                LiteralArgumentBuilder.<S>literal("query")
                                        .executes(
                                                context ->
                                                        BlacklistedEnchantsCommand.executeQuery(
                                                                context.getSource()))));
    }
}
