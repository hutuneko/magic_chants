package io.github.hutuneko.magic_chants.api.player.attribute.magic_power.net;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPowerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2C_SyncMagicPowerPacket(double mp, double maxMp) implements CustomPacketPayload {

    public static final Type<S2C_SyncMagicPowerPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MagicChants.MODID, "sync_magic_power")
    );

    public static final StreamCodec<FriendlyByteBuf, S2C_SyncMagicPowerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, S2C_SyncMagicPowerPacket::mp,
            ByteBufCodecs.DOUBLE, S2C_SyncMagicPowerPacket::maxMp,
            S2C_SyncMagicPowerPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

//            player.getCapability(MagicPowerProvider.MAGIC_POWER).ifPresent(cap -> {
//                cap.setMP(mp);
//                cap.setMaxMP(maxMp);
//            });
        });
    }
}