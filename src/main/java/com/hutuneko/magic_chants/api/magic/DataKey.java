package com.hutuneko.magic_chants.api.magic;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public record DataKey<T>(ResourceLocation id, Codec<T> codec) {
    public static <T> DataKey<T> of(String ns, String path, Codec<T> codec) {
        return new DataKey<>(new ResourceLocation(ns, path), codec);
    }
}