package com.hutuneko.magic_chants.api.player.feke;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SetCameraEntity {
    private final int entityId;

    public S2C_SetCameraEntity(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(S2C_SetCameraEntity msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static S2C_SetCameraEntity decode(FriendlyByteBuf buf) {
        return new S2C_SetCameraEntity(buf.readInt());
    }

    public static void handle(S2C_SetCameraEntity msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            var mc = Minecraft.getInstance();
            if (mc.level == null) return;
            var entity = mc.level.getEntity(msg.entityId);
            if (entity != null) {
                mc.setCameraEntity(entity);
            }
        }));
        ctx.get().setPacketHandled(true);
    }
}
