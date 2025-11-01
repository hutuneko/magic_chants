package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class Magic_Kill extends Magic {
    @Override
    public void magic_content(MagicContext ctx) {
        ServerPlayer player = ctx.player();
        ServerLevel level = ctx.level();
        if (player == null || level.isClientSide()) return;
        var entity = level.getEntity(ctx.data().get(Keys.TARGET_UUID).orElse(null));
        if (entity == null) return;
        if (entity instanceof LivingEntity living) {
            living.kill();
        }
    }
}
