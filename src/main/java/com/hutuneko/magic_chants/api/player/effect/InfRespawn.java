package com.hutuneko.magic_chants.api.player.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class InfRespawn extends MobEffect {
    public InfRespawn(MobEffectCategory category, int color) {
        super(category, color);
    }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 例: 60 tick (3秒) ごとにダメージを与える処理
        if (entity.level().isClientSide) {
            return;
        }

        if (this.isDurationEffectTick(60, amplifier)) {

        }
    }

    // エフェクトを tick ごとに適用するかどうか (true にすると applyEffectTick が実行される)
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Duration が 1 以上なら常に tick 処理を行う
        return duration >= 1;
    }
}
