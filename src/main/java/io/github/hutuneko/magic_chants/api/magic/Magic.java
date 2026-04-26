package io.github.hutuneko.magic_chants.api.magic;

import net.minecraft.network.chat.Component;

public abstract class Magic implements BaseMagic{
    @Override
    public void mainMagic(MagicContext ctx) {
        if (ctx.player() != null) {
            ctx.player().sendSystemMessage(Component.literal(ctx.getChant()).append(Component.translatable("magic_chants.noeffect.main")));
        }
    }
    @Override
    public void subMagic(MagicContext ctx) {
        if (ctx.player() != null) {
            ctx.player().sendSystemMessage(Component.literal(ctx.getChant()).append(Component.translatable("magic_chants.noeffect.sub")));
        }
    }
}
