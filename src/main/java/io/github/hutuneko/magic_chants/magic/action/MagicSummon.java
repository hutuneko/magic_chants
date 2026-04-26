package io.github.hutuneko.magic_chants.magic.action;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import io.github.hutuneko.magic_chants.api.net.S2CEntityGet;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.pig.Pig;
import net.neoforged.neoforge.network.PacketDistributor;

public class MagicSummon extends Magic {

    @Override
    public void mainMagic(MagicContext ctx) {
        String s = ctx.data().get(Keys.STRING).orElse(null);
        ServerPlayer player = ctx.player();
        if (s == null || player == null) return;

        PacketDistributor.sendToPlayer(player, new S2CEntityGet(s));

    }

    public static void spawnEntity(ServerPlayer player, String registryName) {
        var entityTypeOpt = EntityType.byString(registryName);

        if (entityTypeOpt.isPresent()) {
            EntityType<?> type = entityTypeOpt.get();
            Entity entity = type.create(player.level(),EntitySpawnReason.EVENT);
            if (entity != null) {
                entity.move(MoverType.SELF,player.position());
                entity.getPersistentData().putString("psi_ex:summon_entity", player.getUUID().toString());
                player.level().addFreshEntity(entity);
                return;
            }
        }
        Pig p = new Pig(EntityType.PIG, player.level());
        p.move(MoverType.SELF,player.position()); // 豚も座標移動が必要
        p.getPersistentData().putString("psi_ex:summon_entity", player.getUUID().toString());
        p.setCustomName(Component.literal(registryName));
        player.level().addFreshEntity(p);
    }
}