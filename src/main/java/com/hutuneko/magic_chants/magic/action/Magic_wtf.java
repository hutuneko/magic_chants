package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;

public class Magic_wtf extends Magic {
    @Override
    public void magic_content(MagicContext ctx) {
        ServerPlayer player = ctx.player();
        ServerLevel level = ctx.level();
        if (player == null || level.isClientSide()) return;
        Creeper creeper = EntityType.CREEPER.create(level);
        if (creeper != null) {
            creeper.moveTo(player.position());
            creeper.ignite();
            level.addFreshEntity(creeper);
        }
    }
}
