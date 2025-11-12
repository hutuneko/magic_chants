package com.hutuneko.magic_chants.magic.target;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraft.nbt.CompoundTag;

public class Magic_Self extends Magic {
    public Magic_Self() {}
    public Magic_Self(CompoundTag args) {
    }

    @Override
    public void magic_content(MagicContext ctx) {
        var p = ctx.player();
        if (p == null) return;
        ctx.data().put(Keys.TARGET_UUID, p.getUUID());
    }
}