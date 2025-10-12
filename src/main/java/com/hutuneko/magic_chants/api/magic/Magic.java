package com.hutuneko.magic_chants.api.magic;

import net.minecraft.nbt.CompoundTag;

public abstract class Magic implements BaseMagic{
    public Magic() {} // 0引数でもOK
    public Magic(CompoundTag args) {}
}
