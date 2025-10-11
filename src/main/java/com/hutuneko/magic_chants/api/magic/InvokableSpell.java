package com.hutuneko.magic_chants.api.magic;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public interface InvokableSpell {
    void invoke(ServerLevel level, @Nullable ServerPlayer player);
}
