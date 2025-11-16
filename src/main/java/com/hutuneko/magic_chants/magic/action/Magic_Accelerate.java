package com.hutuneko.magic_chants.magic.action;

import com.hutuneko.magic_chants.api.magic.Keys;
import com.hutuneko.magic_chants.api.magic.Magic;
import com.hutuneko.magic_chants.api.magic.MagicContext;
import com.hutuneko.magic_chants.api.util.MagicChantsAPI;
import com.hutuneko.magic_chants.api.util.TickTaskManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class Magic_Accelerate extends Magic {
        @Override
        public void magic_content(MagicContext ctx) {
            var power = ctx.data().get(Keys.POWER).orElse(null);
            if (power == null)return;
            String s = ctx.data().get(Keys.TARGET).orElse(null);
            if (s == null) return;
            UUID uuid = UUID.fromString(s);
            ServerLevel level = ctx.level();
            Entity target = level.getEntity(uuid);
            if (target == null) return;
            Vec3 center = ctx.data().get(Keys.POS).orElse(null);
            if (center == null)return;
            TickTaskManager.addTask(Integer.MAX_VALUE,
                    () -> MagicChantsAPI.pullEntityTowards(target, center, 0.2),
                    () -> {
                        // ▼ 停止条件
                        if (target == null || !target.isAlive() || center == null) return true;

                        double dist = target.distanceToSqr(center.x, center.y, center.z);
                        return dist <= 1.5 * 1.5; // 条件を満たしたら終了
                    }
            );
        }
}
