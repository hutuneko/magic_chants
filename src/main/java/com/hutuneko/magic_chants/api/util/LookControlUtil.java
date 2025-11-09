package com.hutuneko.magic_chants.api.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class LookControlUtil {
    /** yaw: -180..180, pitch: -90..90 */
    public static void applyLook(LivingEntity entity, float yaw, float pitch) {
        pitch = Mth.clamp(pitch, -90f, 90f);

        // Mob ならAIの LookControl を使うと自然に首だけ向く
        if (entity instanceof Mob mob) {
            // 第2引数: 最大ヨー変化/1tick、第3引数: 最大ピッチ変化/1tick（要調整）
            mob.getLookControl().setLookAt(
                    mob.getX() + Mth.cos(-yaw * Mth.DEG_TO_RAD) * 1.0,
                    mob.getEyeY() + Mth.sin(-pitch * Mth.DEG_TO_RAD) * 0.0,
                    mob.getZ() + Mth.sin(-yaw * Mth.DEG_TO_RAD) * 1.0,
                    30.0F, 30.0F
            );
            // すぐに反映したい/AIに上書きされたくない場合は直接セットも併用
            setAllRotations(mob, yaw, pitch);
        } else {
            // 汎用 LivingEntity（防具立て等を含む）— 直接回す
            setAllRotations(entity, yaw, pitch);
        }
    }

    /** 直接回す（頭・体・視線を揃える） */
    private static void setAllRotations(LivingEntity le, float yaw, float pitch) {
        le.setYRot(yaw);
        le.setXRot(pitch);

        // 旧値も合わせて補間破綻を防ぐ
        le.yRotO = yaw;
        le.xRotO = pitch;

        // 体と頭の向きも同期（Mob系はこれが無いと体が別方向を向く）
        le.setYBodyRot(yaw);
        le.setYHeadRot(yaw);
    }
    public static void forceCameraView(LivingEntity le, float yaw, float pitch) {
        yaw   = Mth.wrapDegrees(yaw);
        pitch = Mth.clamp(pitch, -90f, 90f);

        // 本体回転（カメラが参照しやすい）
        le.setYRot(yaw);  le.yRotO = yaw;
        le.setXRot(pitch); le.xRotO = pitch;

        // 頭＆体も合わせる（同期ズレ防止）
        le.setYHeadRot(yaw);
        le.yHeadRotO = yaw;
        if (le instanceof Mob mob) {
            mob.setYBodyRot(yaw);
        }
    }

}

