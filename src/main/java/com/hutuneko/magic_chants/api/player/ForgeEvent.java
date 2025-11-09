package com.hutuneko.magic_chants.api.player;

import com.google.common.collect.Multimap;
import com.hutuneko.magic_chants.Magic_chants;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import com.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPowerProvider;
import com.hutuneko.magic_chants.api.player.attribute.magic_power.net.S2C_SyncMagicPowerPacket;
import com.hutuneko.magic_chants.api.util.LookControlUtil;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Magic_chants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvent {
    private static final Map<UUID, Integer> tickMap = new HashMap<>();
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        // サーバー側かつ END フェーズでのみ処理
        if (e.phase != TickEvent.Phase.END || e.player.level().isClientSide) return;

        Player player = e.player;

        // カウント更新
        UUID uuid = player.getUUID();
        int ticks = tickMap.getOrDefault(uuid, 0) + 1;

        if (ticks >= 20) { // 1秒ごとに同期
            tickMap.put(uuid, 0);

            player.getCapability(MagicPowerProvider.MAGIC_POWER).ifPresent(pmp -> {
                // MP 回復処理
                double current = pmp.getMP();
                pmp.setMP(current + 1);

                // クライアントに同期
                MagicNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new S2C_SyncMagicPowerPacket(pmp.getMP(), pmp.getMaxMP())
                );
            });
        } else {
            tickMap.put(uuid, ticks);
        }
    }
    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(Magic_chants.MODID, "magic_power"),
                    new MagicPowerProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(MagicPowerProvider.MAGIC_POWER).ifPresent(oldStore -> event.getEntity().getCapability(MagicPowerProvider.MAGIC_POWER).ifPresent(newStore -> {
            newStore.setMP(oldStore.getMP());
            newStore.setMaxMP(oldStore.getMaxMP());
        }));
    }
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide()) return;
        // プレイヤー以外 && spiritf のとき
        if (dead.getType() != EntityType.PLAYER && dead.getPersistentData().getBoolean("magic_chants:spiritf")) {
            ServerLevel level = (ServerLevel) dead.level();
            UUID uid = dead.getPersistentData().getUUID("magic_chants:spiritu");
            Entity e = level.getEntity(uid);
            if (e instanceof ServerPlayer sp && sp.getPersistentData().getBoolean("magic_chants:spiritf")) {
                spiritification(level, sp);
            }
        }
    }


    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (e.player.level().isClientSide) return;

        ServerPlayer sp = (ServerPlayer) e.player;
        if (!sp.getPersistentData().getBoolean("magic_chants:spiritf")) return;

        // まだ重力切ってなければ切る（維持）
        if (!sp.isNoGravity()) sp.setNoGravity(true);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level().isClientSide()) return;
        if (!e.getPersistentData().getBoolean("magic_chants:spiritf")) return;

        // 必要なら noAI 維持
        if (e instanceof Mob mob && !mob.isNoAi()) mob.setNoAi(true);

        // 必要なら、リンク元プレイヤーに時々だけカメラ再送（連発は控える）
        // ※スパム防止に 1秒毎などで
        // if ((e.level().getGameTime() & 19) == 0) { ...ClientboundSetCameraPacket(e) ... }
    }



    // 例: プレイヤーから最も近いMob（生物）を探す
    public static @Nullable LivingEntity getNearestLivingEntitySafe(Level level, LivingEntity center, double range) {
        if (level == null || center == null) return null;
        if (!Double.isFinite(range) || range <= 0) return null;

        // 過剰広範囲を禁止（必要なら値を上げてOK）
        range = Mth.clamp(range, 1.0, 10000);

        // 基準座標はそのまま（変なオフセットは付けない）
        Vec3 p = center.position();
        if (!Double.isFinite(p.x) || !Double.isFinite(p.y) || !Double.isFinite(p.z)) {
            p = Vec3.atCenterOf(center.blockPosition());
        }

        // 反転しないAABBを使う
        AABB box = AABB.ofSize(p, range * 2, range * 2, range * 2);

        return level.getNearestEntity(
                level.getEntitiesOfClass(LivingEntity.class, box, e -> e != center),
                TargetingConditions.forCombat(),
                center,
                p.x, p.y, p.z
        );
    }

    public static void spiritification(Level level,ServerPlayer sp){
        LivingEntity livingEntity = getNearestLivingEntitySafe(level,sp,1000);
        if (livingEntity == null)return;
        livingEntity.getPersistentData().putBoolean("magic_chants:spiritf",true);
        livingEntity.getPersistentData().putUUID("magic_chants:spiritu",sp.getUUID());
        if (livingEntity instanceof Mob mob) {
            mob.setNoAi(true);   // AI停止
        }
        sp.getPersistentData().putUUID("magic_chants:spiritu",livingEntity.getUUID());
        sp.getPersistentData().putBoolean("magic_chants:spiritf",true);
        sp.teleportTo(livingEntity.getX(), livingEntity.getY() + 2.0, livingEntity.getZ());
        sp.setNoGravity(true);
        sp.noPhysics = true;
        sp.setInvulnerable(true);
        sp.setInvisible(true); // 任意：見えなくする
        sp.connection.send(new ClientboundSetCameraPacket(livingEntity));
    }
    public static void spiritFormRelease(Level level, ServerPlayer sp) {
        ServerLevel sl = (ServerLevel) level;
        UUID uid = sp.getPersistentData().getUUID("magic_chants:spiritu");
        Entity entity = uid != null ? sl.getEntity(uid) : null;

        if (entity != null) {
            entity.getPersistentData().remove("magic_chants:spiritf");
            entity.getPersistentData().remove("magic_chants:spiritu");
            if (entity instanceof Mob mob) {
                mob.setNoAi(false);   // AI停止
            }
        }
        sp.getPersistentData().remove("magic_chants:spiritu");
        sp.getPersistentData().remove("magic_chants:spiritf");
        sp.setNoGravity(false);
        sp.connection.send(new ClientboundSetCameraPacket(sp));
    }
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (!sp.getPersistentData().getBoolean("magic_chants:spiritf")) return;

        // 元の攻撃をキャンセル（invalid entity切断や二重ダメージを防ぐ）
        e.setCanceled(true);

        Entity rawVictim = e.getTarget();
        if (!(rawVictim instanceof LivingEntity victim)) return;
        ServerLevel level = (ServerLevel) sp.level();

        // 霊体リンク先（カメラ対象）を取得
        UUID hostId = sp.getPersistentData().getUUID("magic_chants:spiritu");
        if (hostId == null) return;
        Entity maybeHost = level.getEntity(hostId);
        if (!(maybeHost instanceof LivingEntity host)) return;
        if (host.isRemoved() || host.isDeadOrDying()) return;
        if (host.level() != victim.level()) return; // 別ディメンション保護

        // 追跡範囲外/遠すぎると不自然になるので距離制限（必要なら調整）
        if (host.distanceToSqr(victim) > (64 * 64)) return;

        // プレイヤーの手持ちをコピー
        ItemStack playerWeapon = sp.getMainHandItem();
        ItemStack prevHostMain = host.getMainHandItem().copy();
        ItemStack temp = playerWeapon.copy();

        try {
            // 一時的にhostへ装備（エンチャ/属性をhost側で参照可能にする）
            host.setItemInHand(InteractionHand.MAIN_HAND, temp);

            // 見た目スイング（trueで周囲へアニメ配信）
            host.swing(InteractionHand.MAIN_HAND, true);

            // --- ダメージ計算（シンプル版） ---
            // 基礎攻撃力 = hostのATTACK_DAMAGE（アイテム分は下で別途加算）
            double base = host.getAttributeValue(Attributes.ATTACK_DAMAGE);

            // アイテムの攻撃力修飾（MAIN_HANDのAttributeModifiersから抽出）
            double itemAtk = 0.0;
            Multimap<Attribute, AttributeModifier> mods =
                    temp.getAttributeModifiers(EquipmentSlot.MAINHAND);
            Collection<AttributeModifier> atkMods = mods.get(Attributes.ATTACK_DAMAGE);
            for (AttributeModifier m : atkMods) {
                itemAtk += m.getAmount(); // 1.20.1はOPERATIONによって扱い分けが必要なら分岐
            }

            // エンチャ補正
            float enchBonus = EnchantmentHelper.getDamageBonus(temp, victim.getMobType());
            int kb = EnchantmentHelper.getKnockbackBonus(host);           // ノックバック
            int fire = EnchantmentHelper.getFireAspect(host);             // 火属性

            float damage = (float) (base + itemAtk) + enchBonus;
            if (damage <= 0f) damage = 1.0f; // 最低保証

            // ダメージ適用（攻撃者=host）
            DamageSource src = level.damageSources().mobAttack(host);
            boolean hurt = victim.hurt(src, damage);
            if (!hurt) return;

            // ノックバック（向きはhost→victim）
            if (kb > 0) {
                double dx = Mth.sin(host.getYRot() * ((float)Math.PI / 180F));
                double dz = -Mth.cos(host.getYRot() * ((float)Math.PI / 180F));
                victim.knockback(0.5F * kb, dx, dz);
                host.setDeltaMovement(host.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }

            // 火属性
            if (fire > 0) {
                victim.setSecondsOnFire(4 * fire);
            }

            // クリティカル・スイープ等をやりたければここに追加（プレイヤー限定の処理は適宜再現）

            // 耐久減少など（必要なら）
            temp.hurtAndBreak(1, host, h -> h.broadcastBreakEvent(InteractionHand.MAIN_HAND));

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
