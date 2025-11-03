package com.hutuneko.magic_chants.magic.addition;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;

import java.util.Objects;

public class Magic_TargetPos extends Magic {
    @Override
    public void magic_content(MagicContext ctx) {
        var level = ctx.level();
        if (level.isClientSide()) return;
        var uuid = ctx.data().get(Keys.TARGET_UUID)
                .orElse(null);
        if (uuid == null) return;
        ctx.data().put(Keys.POS, Objects.requireNonNull(level.getEntity(uuid)).position());
        System.out.println("["+level.getEntity(uuid)+"],["+ Objects.requireNonNull(level.getEntity(uuid)).position()+"]");
    }
}
