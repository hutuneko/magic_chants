package io.github.hutuneko.magic_chants.api.player.attribute.magic_power.net;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPower;
import io.github.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPowerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CSyncMagicPowerPacket(double mp, double maxMp) implements CustomPacketPayload {

    public static final Type<S2CSyncMagicPowerPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MagicChants.MODID, "sync_magic_power")
    );

    public static final StreamCodec<FriendlyByteBuf, S2CSyncMagicPowerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, S2CSyncMagicPowerPacket::mp,
            ByteBufCodecs.DOUBLE, S2CSyncMagicPowerPacket::maxMp,
            S2CSyncMagicPowerPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CSyncMagicPowerPacket msg,IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            MagicPower power = player.getData(MagicPowerProvider.MAGIC_POWER.get());
            power.setMP(msg.mp);
            power.setMaxMP(msg.maxMp);
        });
    }
}