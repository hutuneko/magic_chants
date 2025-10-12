package com.hutuneko.magic_chants.magic.addition;

import com.hutuneko.magic_chants.api.magic.DataKey;
import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;

public class Magic_Target extends Magic {
    public Magic_Target() {}
    public Magic_Target(CompoundTag args) {}

    @Override
    public void magic_content(MagicContext ctx) {
        var level = ctx.level();
        if (level.isClientSide()) return;
        var pos = ctx.data().get(Keys.POS)
                .orElse(null);
        if (pos == null) return;
        var p = ctx.player();
        if (p == null) return;
        LivingEntity entity = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(10))
                .stream()
                .filter(e -> !(e instanceof Player))
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(pos)))
                .orElse(null);

        if (entity == null)return;
        ctx.data().put(Keys.TARGET_UUID,entity.getUUID());
        System.out.println("["+entity.getUUID()+"]");
    }
}
