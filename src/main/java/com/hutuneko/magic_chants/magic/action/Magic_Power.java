package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class Magic_Power extends Magic {
    private float power = 0.0F;
    @Override
    public void magic_content(MagicContext ctx) {
        var p = ctx.data().get(Keys.POWER).orElse(null);
        if (p == null)return;
        power = p;
        var level = ctx.level();
        if (level.isClientSide()) return;
        var entity = level.getEntity(ctx.data().get(Keys.TARGET_UUID).orElseGet(null));
        if (entity == null) return;
        if (entity instanceof LivingEntity living) {
            System.out.println(power);
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, (int) (power * 20), (int) (power - 1)));
        }

    }

}
