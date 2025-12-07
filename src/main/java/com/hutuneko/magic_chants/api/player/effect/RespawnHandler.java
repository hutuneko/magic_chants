package com.hutuneko.magic_chants.api.player.effect;

import com.hutuneko.magic_chants.api.player.ForgeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class RespawnHandler {
    public static final Map<UUID, GlobalPos> ORIGINAL_SPAWN_LOCATIONS = new HashMap<>();
    public static void respawnNow(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        GlobalPos globalPos = player.getLastDeathLocation().orElse(GlobalPos.of(level.dimension(),player.blockPosition()));
        BlockPos pos = globalPos.pos();
        ForgeEvent.GLOBAL_POS_HASH_MAP.put(player.getUUID(),globalPos);
        player.getPersistentData().putBoolean("magic_chants:respawnf",true);
        player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                30, 0.2, 0.5, 0.2, 0.01);
    }
}