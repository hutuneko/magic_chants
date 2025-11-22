package com.hutuneko.magic_chants.api.player.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class RespawnHandler {

    public static void respawnNow(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        // 1. リスポーン位置の決定 (以前と同じロジック)
        GlobalPos globalPos = player.getLastDeathLocation().orElse(GlobalPos.of(level.dimension(),player.blockPosition()));
        BlockPos pos = globalPos.pos();
        player.setRespawnPosition(level.dimension(), pos, player.getYRot(), true, false);

        // 2. アイテム保持のためにフラグをセット
        player.getPersistentData().putBoolean("magic_chants:respawnf",true);

        // 3. 確実なリスポーン処理 (ServerPlayerList を使用)
        // respawn() は新しい ServerPlayer インスタンスを返します
        ServerPlayer newPlayer = player.server.getPlayerList().respawn(player, false);

        // 4. 新しいプレイヤーに各種効果を適用
        newPlayer.setHealth(newPlayer.getMaxHealth());
        newPlayer.getFoodData().setFoodLevel(20);
        newPlayer.invulnerableTime = 40;

        // 5. エフェクトの表示
        newPlayer.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                30, 0.2, 0.5, 0.2, 0.01);
    }
}