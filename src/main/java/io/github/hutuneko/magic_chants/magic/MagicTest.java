package io.magic_chants.magic;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;

public class MagicTest extends Magic {
    @Override
    public void subMagic(MagicContext ctx) {
        ctx.data().put(Keys.POWER,100.0f);
    }
}
