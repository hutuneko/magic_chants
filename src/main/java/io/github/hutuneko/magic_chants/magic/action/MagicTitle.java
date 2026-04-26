package io.magic_chants.magic.action;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;

public class MagicTitle extends Magic {
    @Override
    public void mainMagic(MagicContext ctx) {
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
        }
    }
}
