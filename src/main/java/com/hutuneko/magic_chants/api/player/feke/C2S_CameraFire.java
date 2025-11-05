package com.hutuneko.magic_chants.api.player.feke;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class C2S_CameraFire {
    public float yaw, pitch;
    public C2S_CameraFire(float yaw, float pitch) {
        this.yaw = yaw; this.pitch = pitch;
    }
    public static void encode(C2S_CameraFire msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
    }
    public static C2S_CameraFire decode(FriendlyByteBuf buf) {
        return new C2S_CameraFire(buf.readFloat(), buf.readFloat());
    }

    public static void handle(C2S_CameraFire msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();

            // 最寄りの代理エンティティを探す
            List<RemoteShooterEntity> list = level.getEntitiesOfClass(RemoteShooterEntity.class, player.getBoundingBox().inflate(128));
            RemoteShooterEntity nearest = list.stream()
                    .min(Comparator.comparingDouble(a -> a.distanceToSqr(player)))
                    .orElse(null);
            if (nearest == null) return;

            Vec3 dir = Vec3.directionFromRotation(msg.pitch, msg.yaw).scale(3.0);
            Arrow arrow = new Arrow(level, nearest.getX(), nearest.getY() + 1.5, nearest.getZ());
            arrow.setDeltaMovement(dir);
            arrow.setOwner(player); // 所有者をプレイヤーに（proxy経由でも良い）
            level.addFreshEntity(arrow);
        });
        ctx.get().setPacketHandled(true);
    }
}
