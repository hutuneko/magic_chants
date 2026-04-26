package io.github.hutuneko.magic_chants.magic.target;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import io.github.hutuneko.magic_chants.magic.addition.MagicSpend;

public class MagicGetExperience extends Magic {
    @Override
    public void mainMagic(MagicContext ctx) {
        ctx.data().put(Keys.TARGET, MagicSpend.Target.XP);
    }
}
