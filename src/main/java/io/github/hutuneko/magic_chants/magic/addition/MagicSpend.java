package io.github.hutuneko.magic_chants.magic.addition;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class MagicSpend extends Magic {
    @Override
    public void mainMagic(MagicContext ctx) {
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
                sp.setExperienceLevels(sp.experienceLevel - spends);
                ctx.data().put(Keys.POWER,ctx.data().get(Keys.POWER).orElse(0f) + spends);
            }
        }
    }

    public enum Target {
        HP,
        XP
    }
}
