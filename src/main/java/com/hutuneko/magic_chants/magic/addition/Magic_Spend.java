package com.hutuneko.magic_chants.magic.addition;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import com.hutuneko.magic_chants.api.magic.target.Target;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class Magic_Spend extends Magic {
    @Override
    public void magic_content(MagicContext ctx) {
        ServerLevel level = ctx.level();
        if (level == null)return;
        int spends = ctx.data().get(Keys.INT).orElse(-1);
        var spend = ctx.data().get(Keys.TARGET).orElse(null);
        if (spend == null)return;
        ServerPlayer sp = ctx.player();
        if (sp != null) {
            if (spend.equals(Target.HP)) {
                sp.hurt(level.damageSources().magic(), spends);
                ctx.data().put(Keys.POWER,ctx.data().get(Keys.POWER).orElse(0f) + spends);
            }
            if (spend.equals(Target.XP)) {
                sp.setExperiencePoints((int) (sp.experienceProgress - spends));
                ctx.data().put(Keys.POWER,ctx.data().get(Keys.POWER).orElse(0f) + spends);
            }
        }
    }
}
