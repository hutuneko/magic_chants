package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import com.hutuneko.magic_chants.api.net.S2C_EntityGet;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraftforge.network.PacketDistributor;

public class Magic_Summon extends Magic {

    @Override
    public void magic_content(MagicContext ctx) {
        String s = ctx.data().get(Keys.STRING).orElse(null);
        ServerPlayer player = ctx.player();
        if (s == null || player == null) return;

        MagicNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2C_EntityGet(s));

    }

    public static void spawnEntity(ServerPlayer player, String registryName) {
        var entityTypeOpt = EntityType.byString(registryName);

        if (entityTypeOpt.isPresent()) {
            EntityType<?> type = entityTypeOpt.get();
            Entity entity = type.create(player.level());
            if (entity != null) {
                entity.moveTo(player.position());
                entity.getPersistentData().putUUID("psi_ex:summon_entity", player.getUUID());
                player.level().addFreshEntity(entity);
                return;
            }
        }
        Pig p = new Pig(EntityType.PIG, player.level());
        p.moveTo(player.position()); // 豚も座標移動が必要
        p.getPersistentData().putUUID("psi_ex:summon_entity", player.getUUID());
        p.setCustomName(Component.literal(registryName));
        player.level().addFreshEntity(p);
    }
}