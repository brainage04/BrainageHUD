package io.github.brainage04.brainagehud.command.core.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Ported from GetEnchantInfo's {@code
 * io.github.brainage04.commands.core.argument.ClientHolderReferenceArgumentType}.
 */
public class ClientHolderReferenceArgumentType<T> implements ArgumentType<Holder.Reference<T>> {
    public static final Dynamic2CommandExceptionType NOT_FOUND_EXCEPTION =
            new Dynamic2CommandExceptionType(
                    (element, type) ->
                            Component.translatable("argument.resource.not_found", element, type));
    public static final Dynamic3CommandExceptionType INVALID_TYPE_EXCEPTION =
            new Dynamic3CommandExceptionType(
                    (element, type, expectedType) ->
                            Component.translatable(
                                    "argument.resource.invalid_type", element, type, expectedType));

    private final ResourceKey<? extends Registry<T>> registryRef;
    private final HolderLookup.RegistryLookup<T> registryLookup;

    public ClientHolderReferenceArgumentType(
            CommandBuildContext registryAccess, ResourceKey<? extends Registry<T>> registryRef) {
        this.registryRef = registryRef;
        this.registryLookup = registryAccess.lookupOrThrow(registryRef);
    }

    public static <T> ClientHolderReferenceArgumentType<T> registryEntry(
            CommandBuildContext registryAccess, ResourceKey<? extends Registry<T>> registryRef) {
        return new ClientHolderReferenceArgumentType<>(registryAccess, registryRef);
    }

    public static <S extends SharedSuggestionProvider, T> Holder.Reference<T> getHolder(
            CommandContext<S> context, String name, ResourceKey<Registry<T>> registryRef)
            throws CommandSyntaxException {
        @SuppressWarnings("unchecked")
        Holder.Reference<T> reference = context.getArgument(name, Holder.Reference.class);
        ResourceKey<T> registryKey = reference.key();
        if (registryKey.isFor(registryRef)) {
            return reference;
        } else {
            throw INVALID_TYPE_EXCEPTION.create(
                    registryKey.identifier(), registryKey.registry(), registryRef.identifier());
        }
    }

    public static <S extends SharedSuggestionProvider> Holder.Reference<Enchantment> getEnchantment(
            CommandContext<S> context, String name) throws CommandSyntaxException {
        return getHolder(context, name, Registries.ENCHANTMENT);
    }

    public static <S extends SharedSuggestionProvider> Holder.Reference<Item> getItem(
            CommandContext<S> context, String name) throws CommandSyntaxException {
        return getHolder(context, name, Registries.ITEM);
    }

    @Override
    public Holder.Reference<T> parse(StringReader stringReader) throws CommandSyntaxException {
        Identifier identifier = Identifier.read(stringReader);
        ResourceKey<T> registryKey = ResourceKey.create(this.registryRef, identifier);
        return this.registryLookup
                .get(registryKey)
                .orElseThrow(
                        () ->
                                NOT_FOUND_EXCEPTION.createWithContext(
                                        stringReader, identifier, this.registryRef.identifier()));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(
                this.registryLookup.listElementIds().map(ResourceKey::identifier), builder);
    }
}
