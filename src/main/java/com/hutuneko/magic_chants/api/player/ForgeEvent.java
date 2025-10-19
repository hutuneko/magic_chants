package com.hutuneko.magic_chants.api.player;

import com.hutuneko.magic_chants.Magic_chants;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import com.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPowerProvider;
import com.hutuneko.magic_chants.api.player.attribute.magic_power.net.S2C_SyncMagicPowerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Magic_chants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvent {
    private static final Map<UUID, Integer> tickMap = new HashMap<>();
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        // サーバー側かつ END フェーズでのみ処理
        if (e.phase != TickEvent.Phase.END || e.player.level().isClientSide) return;

        Player player = e.player;

        // カウント更新
        UUID uuid = player.getUUID();
        int ticks = tickMap.getOrDefault(uuid, 0) + 1;

        if (ticks >= 20) { // 1秒ごとに同期
            tickMap.put(uuid, 0);

            player.getCapability(MagicPowerProvider.MAGIC_POWER).ifPresent(pmp -> {
                // MP 回復処理
                double current = pmp.getMP();
                pmp.setMP(current + 1);

                // クライアントに同期
                MagicNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new S2C_SyncMagicPowerPacket(pmp.getMP(), pmp.getMaxMP())
                );
            });
        } else {
            tickMap.put(uuid, ticks);
        }
    }
    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(Magic_chants.MODID, "magic_power"),
                    new MagicPowerProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(MagicPowerProvider.MAGIC_POWER).ifPresent(oldStore -> event.getEntity().getCapability(MagicPowerProvider.MAGIC_POWER).ifPresent(newStore -> {
            newStore.setMP(oldStore.getMP());
            newStore.setMaxMP(oldStore.getMaxMP());
        }));
    }
}
