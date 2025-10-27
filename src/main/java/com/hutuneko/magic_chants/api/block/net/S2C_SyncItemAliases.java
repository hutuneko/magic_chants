package com.hutuneko.magic_chants.api.block.net;

import com.hutuneko.magic_chants.api.block.gui.ChantTunerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public record S2C_SyncItemAliases(UUID itemUuid, String json) {

    public static void encode(S2C_SyncItemAliases m, FriendlyByteBuf buf){
        buf.writeUUID(m.itemUuid);
        buf.writeUtf(m.json, 32767); // ← JSON をそのまま入れる
    }

    public static S2C_SyncItemAliases decode(FriendlyByteBuf buf){
        UUID id = buf.readUUID();
        String json = buf.readUtf(32767);
        return new S2C_SyncItemAliases(id, json);
    }

    public static void handle(S2C_SyncItemAliases m, Supplier<NetworkEvent.Context> ctx){
        var c = ctx.get();
        c.enqueueWork(() -> {
            System.out.println("[S2C] recv json length=" + (m.json()==null?0:m.json().length()));
            var mc = Minecraft.getInstance();
            if (mc.screen instanceof ChantTunerScreen scr) {
                scr.applyAliasesFromServerJson(m.itemUuid(), m.json()); // ← 画面に反映
                System.out.println("[S2C] applied to screen");
            } else {
                System.out.println("[S2C] screen not ChantTunerScreen: " + mc.screen);
            }
        });
        c.setPacketHandled(true);
    }
}
