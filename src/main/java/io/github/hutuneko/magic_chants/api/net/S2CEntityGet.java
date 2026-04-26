package io.github.hutuneko.magic_chants.api.net;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.api.util.cliant.EntityNameLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;

public record S2CEntityGet(String registryName) implements CustomPacketPayload {

    public static final Type<S2CEntityGet> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MagicChants.MODID, "s2c_entity_get")
    );

    public static final StreamCodec<FriendlyByteBuf, S2CEntityGet> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, S2CEntityGet::registryName,
            S2CEntityGet::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CEntityGet msg,IPayloadContext context) {
        context.enqueueWork(() -> {
            String resolved = EntityNameLookup.getRegistryName(msg.registryName);
            ClientPacketDistributor.sendToServer(new C2SEntityGet(
                    Objects.requireNonNullElse(resolved, msg.registryName)
            ));
        });
    }
}