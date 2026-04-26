package io.magic_chants.magic.action;

import io.github.hutuneko.magic_chants.ModRegistry;
import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.world.effect.MobEffectInstance;

public class MagicInsRespawn extends Magic {
    @Override
    public void mainMagic(MagicContext ctx) {
        if (ctx.player() != null) {
            var p = ctx.data().get(Keys.POWER).orElse(null);
            int power = 0;
            if (p != null) {
                power = p.intValue();
            }
            ctx.player().addEffect(new MobEffectInstance(ModRegistry.INSRESPAWN.get(),power * 200));
        }
    }
}
