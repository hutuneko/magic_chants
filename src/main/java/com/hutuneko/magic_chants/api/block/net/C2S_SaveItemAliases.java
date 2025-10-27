package com.hutuneko.magic_chants.api.block.net;

import com.hutuneko.magic_chants.api.file.AliasRewriter;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import com.hutuneko.magic_chants.api.file.WorldJsonStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

// C2S_SaveItemAliases.java
public record C2S_SaveItemAliases(UUID itemUuid, String json) {
    public static void encode(C2S_SaveItemAliases m, FriendlyByteBuf buf){
        buf.writeUUID(m.itemUuid);
        buf.writeUtf(m.json, 32767);
    }
    public static C2S_SaveItemAliases decode(FriendlyByteBuf buf){
        return new C2S_SaveItemAliases(buf.readUUID(), buf.readUtf(32767));
    }
    public static void handle(C2S_SaveItemAliases m, Supplier<NetworkEvent.Context> ctx){
        var c = ctx.get();
        ServerPlayer sp = c.getSender();
        c.enqueueWork(() -> {
            if (sp == null) return;
            try {
                // validate
                var je = com.google.gson.JsonParser.parseString(m.json());
                String json = (String) WorldJsonStorage.load(sp.serverLevel(), "magics/" + m.itemUuid() + ".json",Object.class);
                String pretty = AliasRewriter.rewriteChants(json, String.valueOf(je));
                WorldJsonStorage.save(sp.serverLevel(), "magics/" + m.itemUuid() + ".json", pretty);
                System.out.println("[C2S_Save] saved " + m.itemUuid());
                // 保存後に再送して画面を更新（任意）
                MagicNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                        new S2C_SyncItemAliases(m.itemUuid(), pretty));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        c.setPacketHandled(true);
    }
}

