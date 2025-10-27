package com.hutuneko.magic_chants.api.block.net;

import com.hutuneko.magic_chants.api.file.AliasRewriter;
import com.hutuneko.magic_chants.api.file.WorldJsonStorage;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

// C2S_RewriteAndSaveAliases.java
public record C2S_RewriteAndSaveAliases(UUID itemUuid, String rulesText) {
    public static void encode(C2S_RewriteAndSaveAliases m, FriendlyByteBuf buf){
        buf.writeUUID(m.itemUuid());
        buf.writeUtf(m.rulesText(), 32767);
    }
    public static C2S_RewriteAndSaveAliases decode(FriendlyByteBuf buf){
        return new C2S_RewriteAndSaveAliases(buf.readUUID(), buf.readUtf(32767));
    }
    public static void handle(C2S_RewriteAndSaveAliases m, Supplier<NetworkEvent.Context> ctx){
        var c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp == null) return;
            ServerLevel sl = sp.serverLevel();

            // 1) サーバでロード
            Object raw = WorldJsonStorage.load(sl, "magics/" + m.itemUuid() + ".json", Object.class);
            String js  = (raw == null) ? "{\"magics\":[]}" : new com.google.gson.Gson().toJson(raw);

            // 2) ルール適用
            String out = AliasRewriter.rewriteChants(js, m.rulesText());

            // 3) 検証 & 保存
            com.google.gson.JsonParser.parseString(out); // throws if invalid
            String pretty = new com.google.gson.GsonBuilder().setPrettyPrinting().create()
                    .toJson(com.google.gson.JsonParser.parseString(out));
            WorldJsonStorage.save(sl, "magics/" + m.itemUuid() + ".json", pretty);
            Object o = WorldJsonStorage.load(sl, "magics/" + m.itemUuid() + ".json", Object.class);
            String j  = AliasRewriter.toAliasLinesFromMagics(o);
            // 4) （任意）最新状態をS2Cで返して画面更新
            MagicNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> sp),
                    new S2C_SyncItemAliases(m.itemUuid(), j)
            );
        });
        c.setPacketHandled(true);
    }
}
