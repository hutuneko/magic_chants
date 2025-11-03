package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;

public class Magic_Title extends Magic {
    @Override
    public void magic_content(MagicContext ctx) {
        var level = ctx.level();
        if (level.isClientSide()) return;
        var id = ctx.data().get(Keys.TARGET_UUID).orElse(null);
        if (id == null) return;
        var entity = level.getEntity(id);
        if (entity instanceof ServerPlayer serverPlayer){
            String text = ctx.data().get(Keys.STRING).orElse(null);
            System.out.println(text);
            if (text == null)return;
            serverPlayer.connection.send(new ClientboundSetTitleTextPacket(Component.literal(text)));
            serverPlayer.sendSystemMessage(Component.literal(text));
        }
    }
}
