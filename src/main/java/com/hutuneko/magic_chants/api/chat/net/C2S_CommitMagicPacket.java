package com.hutuneko.magic_chants.api.chat.net;

import com.hutuneko.magic_chants.api.chat.MagicChatServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record C2S_CommitMagicPacket(UUID itemUuid, InteractionHand hand,ItemStack itemStack) {
    public static void encode(C2S_CommitMagicPacket p, FriendlyByteBuf buf) {
        buf.writeUUID(p.itemUuid);
        buf.writeEnum(p.hand);
        buf.writeItem(p.itemStack);
    }
    public static C2S_CommitMagicPacket decode(FriendlyByteBuf buf) {
        UUID u = buf.readUUID();
        InteractionHand h = buf.readEnum(InteractionHand.class);
        ItemStack stack = buf.readItem();
        return new C2S_CommitMagicPacket(u, h,stack);
    }
    public static void handle(C2S_CommitMagicPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null) {
                MagicChatServer.handleCommit(sp);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

