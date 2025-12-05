package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.ModRegistry;
import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.world.effect.MobEffectInstance;

public class Magic_InsRespawn extends Magic {
    @Override
    public void magic_content(MagicContext ctx) {
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
