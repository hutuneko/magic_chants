package com.hutuneko.magic_chants.api.player.feke;

import com.hutuneko.magic_chants.api.net.MagicNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class C2S_StartRemoteView {
    public C2S_StartRemoteView() {}
    public static final Map<UUID, RemoteShooterEntity> PROXIES = new ConcurrentHashMap<>();
    public static void encode(C2S_StartRemoteView msg, FriendlyByteBuf buf) {}
    public static C2S_StartRemoteView decode(FriendlyByteBuf buf) { return new C2S_StartRemoteView(); }

    public static void handle(C2S_StartRemoteView msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            MinecraftServer server = player.server;

            // 元ディメンションと別ディメンションを取得
            ServerLevel origin = player.serverLevel();
            ResourceKey<Level> targetKey = Level.NETHER; // 例：ネザーへ転送（自由に変更可能）
            ServerLevel target = server.getLevel(targetKey);

            if (target == null) return;

            // プレイヤー転送
            player.changeDimension(target);

            // 元ディメンションに代理生成
            RemoteShooterEntity proxy = new RemoteShooterEntity(origin, player.getUUID());
            PROXIES.put(player.getUUID(), proxy);
            proxy.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            origin.addFreshEntity(proxy);

            // 他エンティティの同期転送
            DimensionSyncHelper.syncOtherEntities(origin, target);
            // after origin.addFreshEntity(proxy);
            MagicNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new S2C_SetCameraEntity(proxy.getId()));

        });
        ctx.get().setPacketHandled(true);
    }
}
