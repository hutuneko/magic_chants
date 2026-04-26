package io.magic_chants.magic.target;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;

public class MagicSelf extends Magic {
    @Override
    public void mainMagic(MagicContext ctx) {
        var p = ctx.player();
        if (p == null) return;
        ctx.data().put(Keys.TARGET_UUID, p.getUUID());
    }
}