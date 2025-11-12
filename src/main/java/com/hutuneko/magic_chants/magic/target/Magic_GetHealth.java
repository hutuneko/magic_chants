package com.hutuneko.magic_chants.magic.target;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import com.hutuneko.magic_chants.api.magic.target.Target;

public class Magic_GetHealth extends Magic {
    @Override
    public void magic_content(MagicContext ctx) {
        ctx.data().put(Keys.TARGET, Target.HP);
    }
}
