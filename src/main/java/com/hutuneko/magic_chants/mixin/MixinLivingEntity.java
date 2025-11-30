package com.hutuneko.magic_chants.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    // dropAllDeathLootメソッドの開始時にフックし、処理をキャンセルします
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    private void magic_chants_preventDrops(DamageSource pDamageSource, CallbackInfo ci) {
        // Playerエンティティであり、かつサーバー側でのみ処理します
        if ((Object)this instanceof ServerPlayer player) {

            // カスタムフラグが立っているかチェック
            if (player.getPersistentData().getBoolean("magic_chants:respawnf")) {

                // ドロップ処理全体をキャンセルし、アイテムが飛ぶのを防ぐ
                ci.cancel();
            }
        }
    }
}