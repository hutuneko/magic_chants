package io.github.hutuneko.magic_chants.api.magic;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

public record DataKey<T>(Identifier id, Codec<T> codec) {
    public static <T> DataKey<T> of(String ns, String path, Codec<T> codec) {
        return new DataKey<>(Identifier.fromNamespaceAndPath(ns, path), codec);
    }
}