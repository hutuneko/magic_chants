package com.hutuneko.magic_chants.magic.target;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import com.hutuneko.magic_chants.api.magic.SubWithMagic;

public class Magic_Set extends SubWithMagic {
    @Override
    public void magic_content(MagicContext ctx) {
    }

    @Override
    public void sub_magic(MagicContext ctx) {
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
