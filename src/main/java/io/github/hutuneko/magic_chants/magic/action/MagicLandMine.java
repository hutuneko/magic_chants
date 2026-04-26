package io.magic_chants.magic.action;

import io.github.hutuneko.magic_chants.ModRegistry;
import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicCast;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import io.github.hutuneko.magic_chants.entity.LandMineEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;

public class MagicLandMine extends Magic {
    @Override
    public void mainMagic(MagicContext ctx) {
        ServerPlayer player = ctx.player();
        Level level = ctx.level();
        if (player == null || level.isClientSide()) return;
        List<MagicCast.Step> rest = ctx.peekRest();
        if (rest.isEmpty()) return;
        String chantRaw = ctx.data().get(Keys.CHANT_RAW).orElse("");
        LandMineEntity entity = new LandMineEntity(ModRegistry.LAND_MINE.get(), player.level());
        entity.setChantRaw(chantRaw);
        entity.setSteps(rest);
        entity.setSp(player);
        level.addFreshEntity(entity);
        ctx.requestCancel();
    }
}
