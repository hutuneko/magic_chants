package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import com.hutuneko.magic_chants.api.util.MagicChantsAPI;
import com.hutuneko.magic_chants.api.util.TickTaskManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class Magic_Suction extends Magic {
    @Override
    public void magic_content(MagicContext ctx) {
        var power = ctx.data().get(Keys.POWER).orElse(null);
        if (power == null)return;
        String s = ctx.data().get(Keys.TARGET).orElse(null);
        if (s == null) return;
        UUID uuid = UUID.fromString(s);
        ServerLevel level = ctx.level();
        Entity target = level.getEntity(uuid);
        if (target == null) return;
        ServerPlayer sp = ctx.player();
        if (sp == null)return;
        Vec3 center = sp.position();
        if (center == null)return;
        TickTaskManager.addTask(5,
                () -> MagicChantsAPI.pullEntityTowards(target, center, 0.2),
                () -> target == null || !target.isAlive() || center == null
        );
        if (target instanceof LivingEntity living) {
            System.out.println(power);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (power * 20), (int) (power - 1)));
        }
    }
}
