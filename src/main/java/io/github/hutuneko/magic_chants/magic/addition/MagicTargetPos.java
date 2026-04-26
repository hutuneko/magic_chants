package io.github.hutuneko.magic_chants.magic.addition;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;

import java.util.Objects;

public class MagicTargetPos extends Magic {
    @Override
    public void mainMagic(MagicContext ctx) {
        var level = ctx.level();
        if (level.isClientSide()) return;
        var uuid = ctx.data().get(Keys.TARGET_UUID)
                .orElse(null);
        if (uuid == null) return;
        ctx.data().put(Keys.POS, Objects.requireNonNull(level.getEntity(uuid)).position());
        System.out.println("["+level.getEntity(uuid)+"],["+ Objects.requireNonNull(level.getEntity(uuid)).position()+"]");
    }
}
