package com.hutuneko.magic_chants.api.player.feke;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class DimensionSyncHelper {
    private static EntityType<? extends Entity> a;
    public static void syncOtherEntities(ServerLevel origin, ServerLevel target) {
        List<? extends Entity> entities = target.getEntities(a, e -> !(e instanceof ServerPlayer));
        for (Entity e : entities) {
            // 別ディメンションに存在しているものを元のディメンションにも転送
            Entity copy = e.getType().create(origin);
            if (copy == null) continue;
            copy.moveTo(e.position());
            copy.setDeltaMovement(e.getDeltaMovement());
            copy.setYRot(e.getYRot());
            copy.setXRot(e.getXRot());
            origin.addFreshEntity(copy);
        }
    }
}
