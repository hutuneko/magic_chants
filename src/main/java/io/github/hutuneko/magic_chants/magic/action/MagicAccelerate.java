package io.magic_chants.magic.action;

import io.github.hutuneko.magic_chants.api.magic.Keys;
import io.github.hutuneko.magic_chants.api.magic.Magic;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import io.github.hutuneko.magic_chants.api.util.MagicChantsAPI;
import io.github.hutuneko.magic_chants.api.util.TickTaskManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MagicAccelerate extends Magic {
        @Override
        public void mainMagic(MagicContext ctx) {
            var power = ctx.data().get(Keys.POWER).orElse(null);
            if (power == null)return;
            UUID uuid = ctx.data().get(Keys.TARGET_UUID).orElse(null);
            if (uuid == null)return;
            ServerLevel level = ctx.level();
            Entity target = level.getEntity(uuid);
            if (target == null) return;
            Vec3 center = ctx.data().get(Keys.POS).orElse(null);
            if (center == null)return;
            TickTaskManager.addTask(Integer.MAX_VALUE,
                    () -> MagicChantsAPI.pullEntityTowards(target, center, 0.2),
                    () -> {
                        // ▼ 停止条件
                        if (!target.isAlive()) return true;

                        double dist = target.distanceToSqr(center.x, center.y, center.z);
                        return dist <= 1.5 * 1.5; // 条件を満たしたら終了
                    }
            );
        }
}
