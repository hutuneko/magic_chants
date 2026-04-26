package io.magic_chants.magic.target;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import io.github.hutuneko.magic_chants.api.magic.SubWithMagic;

public class MagicSet extends Magic {
    @Override
    public void mainMagic(MagicContext ctx) {
    }

    @Override
    public void subMagic(MagicContext ctx) {
        String s = ctx.data().get(Keys.CHANT).orElse(null);
        if (s == null || s.isEmpty()) return;
        try {
            double d = Double.parseDouble(s);
            int i = (int) d;
            ctx.data().put(Keys.INT,i);
        } catch (NumberFormatException e) {
            ctx.data().put(Keys.STRING,s);
        }
    }
}
