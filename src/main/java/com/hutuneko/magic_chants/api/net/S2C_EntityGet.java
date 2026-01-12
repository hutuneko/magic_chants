package com.hutuneko.magic_chants.api.net;

import com.hutuneko.magic_chants.api.util.cliant.EntityNameLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record S2C_EntityGet(String s) {
    public static void encode(S2C_EntityGet m, FriendlyByteBuf buf) {
        buf.writeUtf(m.s());
    }

    public static S2C_EntityGet decode(FriendlyByteBuf buf) {
        return new S2C_EntityGet(buf.readUtf());
    }

    public static void handle(S2C_EntityGet m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            String registryName = EntityNameLookup.getRegistryName(m.s());
            MagicNetwork.CHANNEL.sendToServer(new C2S_EntityGet(Objects.requireNonNullElse(registryName, m.s())));
        });
        ctx.get().setPacketHandled(true);
    }
}