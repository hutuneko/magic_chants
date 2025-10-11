package com.hutuneko.magic_chants.api.magic;

import net.minecraft.nbt.CompoundTag;

@FunctionalInterface
public interface MagicFactory {
    BaseMagic create(CompoundTag args); // argsで初期化できる
}
