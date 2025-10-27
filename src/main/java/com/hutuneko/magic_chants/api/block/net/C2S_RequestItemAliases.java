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

public record C2S_RequestItemAliases(UUID itemUuid) {
    public static void encode(C2S_RequestItemAliases m, FriendlyByteBuf buf){ buf.writeUUID(m.itemUuid); }
    public static C2S_RequestItemAliases decode(FriendlyByteBuf buf){ return new C2S_RequestItemAliases(buf.readUUID()); }
    public static void handle(C2S_RequestItemAliases m, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        ServerPlayer sp = c.getSender();
        c.enqueueWork(() -> {
            if (sp == null) return;
            ServerLevel sl = sp.serverLevel();
            System.out.println("[C2S] req aliases uuid=" + m.itemUuid());

            // どこかで読み込んできた構造体を JSON 文字列に
            // 例) WorldJsonStorage が Map/JsonElement/Raw を返すケースに応じて分岐
            String jsonOut;
            Object raw = WorldJsonStorage.load(sl, "magics/" + m.itemUuid() + ".json", Object.class);
            if (raw == null) {
                jsonOut = "{\"magics\":[]}";
            } else {
                jsonOut = AliasRewriter.toAliasLinesFromMagics(raw);
            }

            System.out.println("[C2S] loaded json length=" + jsonOut.length());

            try {
                MagicNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> sp),
                        new S2C_SyncItemAliases(m.itemUuid(), jsonOut)
                );
                System.out.println("[C2S] sent S2C to " + sp.getGameProfile().getName());
            } catch (Throwable t) {
                System.err.println("[C2S] send failed: " + t);
                t.printStackTrace();
            }
        });
        c.setPacketHandled(true);
    }

}
