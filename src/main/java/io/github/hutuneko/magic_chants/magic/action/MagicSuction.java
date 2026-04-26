package io.github.hutuneko.magic_chants.magic.action;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import io.github.hutuneko.magic_chants.api.util.MagicChantsAPI;
import io.github.hutuneko.magic_chants.api.util.TickTaskManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MagicSuction extends Magic {
    @Override
    public void mainMagic(MagicContext ctx) {
        var power = ctx.data().get(Keys.POWER).orElse(null);
        if (power == null)return;
        UUID uuid = ctx.data().get(Keys.TARGET_UUID).orElse(null);
        if (uuid == null)return;
        ServerLevel level = ctx.level();
        Entity target = level.getEntity(uuid);
        if (target == null) return;
        ServerPlayer sp = ctx.player();
        if (sp == null)return;
        Vec3 center = sp.position();
        TickTaskManager.addTask(5,
                () -> MagicChantsAPI.pullEntityTowards(target, center, 0.2),
                () -> !target.isAlive()
        );
        if (target instanceof LivingEntity living) {
            System.out.println(power);
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, (int) (power * 20), (int) (power - 1)));
        }
    }
}
