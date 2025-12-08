package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.ModRegistry;
import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicCast;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import com.hutuneko.magic_chants.entity.LandMineEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;

public class Magic_LandMine extends Magic {
    @Override
    public void magic_content(MagicContext ctx) {
        ServerPlayer player = ctx.player();
        Level level = ctx.level();
        if (player == null || level.isClientSide()) return;
        List<MagicCast.Step> rest = ctx.peekRest();
        if (rest.isEmpty()) return;
        List<Boolean> sub = MagicCast.SUBLIST.get(ctx.data().get(Keys.PLAYER_UUID).orElse(null));
        String chantRaw = ctx.data().get(Keys.CHANT_RAW).orElse("");
        LandMineEntity entity = new LandMineEntity(ModRegistry.LAND_MINE.get(), player.level());
        entity.setChantRaw(chantRaw);
        entity.setSub(sub);
        entity.setSteps(rest);
        entity.setSp(player);
        level.addFreshEntity(entity);
        ctx.requestCancel();
    }
}
