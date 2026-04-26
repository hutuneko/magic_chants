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

public record S2C_EntityGet(String registryName) implements CustomPacketPayload {

    public static final Type<S2C_EntityGet> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MagicChants.MODID, "s2c_entity_get")
    );

    public static final StreamCodec<FriendlyByteBuf, S2C_EntityGet> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, S2C_EntityGet::registryName,
            S2C_EntityGet::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            String resolved = EntityNameLookup.getRegistryName(registryName);
            ClientPacketDistributor.sendToServer(new C2S_EntityGet(
                    Objects.requireNonNullElse(resolved, registryName)
            ));
        });
    }
}