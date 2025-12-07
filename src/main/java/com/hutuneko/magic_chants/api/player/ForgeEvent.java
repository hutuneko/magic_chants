package com.hutuneko.magic_chants.api.player;

import com.google.common.collect.Multimap;
import com.hutuneko.magic_chants.Magic_chants;
import com.hutuneko.magic_chants.ModRegistry;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import com.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPowerProvider;
import com.hutuneko.magic_chants.api.player.net.S2C_Rot;
import com.hutuneko.magic_chants.api.util.LookControlUtil;
import com.hutuneko.magic_chants.api.util.MagicChantsAPI;
import com.hutuneko.magic_chants.api.util.TickTaskManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = Magic_chants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvent {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            TickTaskManager.onTick();
        }
    }
    private static final Map<UUID, Integer> tickMap = new HashMap<>();
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        // サーバー側かつ END フェーズでのみ処理
        if (e.phase != TickEvent.Phase.END || e.player.level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) e.player;

        // カウント更新
        UUID uuid = player.getUUID();
        int ticks = tickMap.getOrDefault(uuid, 0) + 1;

        if (ticks >= 20) { // 1秒ごとに同期
            tickMap.put(uuid, 0);

            player.getCapability(MagicPowerProvider.MAGIC_POWER).ifPresent(pmp -> {
                // MP 回復処理
                double current = pmp.getMP();
                pmp.setMP(current + 1);
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

    public static final Map<UUID,Float> YROT = new HashMap<>();
    public static final Map<UUID,Float> XROT = new HashMap<>();
    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (e.player.level().isClientSide) return;

        ServerPlayer sp = (ServerPlayer) e.player;
        if (!sp.getPersistentData().getBoolean("magic_chants:spiritf")) return;
        YROT.put(sp.getUUID(),sp.getYRot());
        XROT.put(sp.getUUID(),sp.getXRot());
        MagicNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),new S2C_Rot(sp.getUUID(),sp.getYRot(),sp.getXRot()));
        if (!sp.isNoGravity()) sp.setNoGravity(true);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level().isClientSide()) return;
        if (!e.getPersistentData().getBoolean("magic_chants:spiritf")) return;
        UUID uid = e.getPersistentData().getUUID("magic_chants:spiritu");
        ServerLevel level = e.level().getServer().getLevel(e.level().dimension());
       Entity entity = level.getEntity(uid);

        if (entity != null){
            Float yaw = YROT.get(entity.getUUID());
            Float pitch = XROT.get(entity.getUUID());

            if (yaw == null || pitch == null) {
                // 値がまだ登録されていないならスキップ
                return;
            }

            LookControlUtil.setAllRotations(e, yaw, pitch);

        }
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
    private static final Map<UUID, List<WrappedGoal>> removedGoals = new HashMap<>();
    public static void spiritification(Level level,ServerPlayer sp){
        LivingEntity livingEntity = getNearestLivingEntitySafe(level,sp,1000);
        if (livingEntity == null)return;
        livingEntity.getPersistentData().putBoolean("magic_chants:spiritf",true);
        livingEntity.getPersistentData().putUUID("magic_chants:spiritu",sp.getUUID());
        if (livingEntity instanceof Mob mob) {
            List<WrappedGoal> saved = new ArrayList<>();

            mob.goalSelector.getAvailableGoals().removeIf(g -> {
                if (g.getGoal() instanceof LookAtPlayerGoal || g.getGoal() instanceof RandomLookAroundGoal) {
                    saved.add(g); // 保存
                    return true;  // 削除
                }
                return false;
            });

            removedGoals.put(mob.getUUID(), saved); // 後で再登録用に保存
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
                List<WrappedGoal> saved = removedGoals.remove(mob.getUUID());
                if (saved != null) {
                    for (WrappedGoal g : saved) {
                        mob.goalSelector.addGoal(g.getPriority(), g.getGoal());
                    }
                }
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

            temp.hurtAndBreak(1, host, h -> h.broadcastBreakEvent(InteractionHand.MAIN_HAND));

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!player.getPersistentData().getBoolean("magic_chants:respawnf"))return;
            // 経験値オーブの生成をキャンセル
            event.setCanceled(true);
        }
    }
    public static final HashMap<UUID, CompoundTag> SAVED_INVENTORIES = new HashMap<>();
    public static final HashMap<UUID, GlobalPos> GLOBAL_POS_HASH_MAP = new HashMap<>();
    @SubscribeEvent
    public static void onPlayerCloneR(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getOriginal().getPersistentData().getBoolean("magic_chants:respawnf")) {
            Player newPlayer = event.getEntity();
            UUID playerId = newPlayer.getUUID();
            if (SAVED_INVENTORIES.containsKey(playerId)) {

                CompoundTag rootTag = SAVED_INVENTORIES.get(playerId);

                ListTag inventoryList = rootTag.getList("Items", Tag.TAG_COMPOUND);

                newPlayer.getInventory().load(inventoryList);

                SAVED_INVENTORIES.remove(playerId);
            }
            event.getEntity().experienceLevel = event.getOriginal().experienceLevel;
            event.getEntity().experienceProgress = event.getOriginal().experienceProgress;
            event.getEntity().totalExperience = event.getOriginal().totalExperience;
            if (GLOBAL_POS_HASH_MAP.containsKey(playerId)) {
                GlobalPos pos = GLOBAL_POS_HASH_MAP.get(playerId);
                BlockPos bpos = pos.pos();
                MinecraftServer server = newPlayer.getServer();
                if (server != null) {
                    ServerLevel serverLevel = server.getLevel(pos.dimension());
                    if (serverLevel != null) {
                        Entity entity = newPlayer.changeDimension(serverLevel);
                        if (entity instanceof ServerPlayer player){
                            player.teleportTo(bpos.getX(),bpos.getY(),bpos.getZ());
                        }
                    }
                }
                GLOBAL_POS_HASH_MAP.remove(playerId);
            }
            event.getOriginal().getPersistentData().remove("magic_chants:respawnf");
        }
    }
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player newPlayer = event.getEntity();
        UUID playerId = newPlayer.getUUID();

        // サーバー側でのみ実行
        if (newPlayer.level().isClientSide) return;

        if (SAVED_INVENTORIES.containsKey(playerId)&& event.isEndConquered()&&newPlayer instanceof ServerPlayer serverPlayer) {
            CompoundTag rootTag = SAVED_INVENTORIES.remove(playerId);
            ListTag inventoryList = rootTag.getList("Items", Tag.TAG_COMPOUND);
            newPlayer.getInventory().load(inventoryList);
        }

    }
    private static final Map<UUID,Integer> integerMap = new HashMap<>();
    @SubscribeEvent
    public static void onLivingDeaths(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player/*&&player.hasEffect(ModRegistry.INFRESPAWN.get())*/) {
                player.getPersistentData().putBoolean("magic_chants:respawnf", true);

                ListTag inventoryList = new ListTag();
                player.getInventory().save(inventoryList);

                CompoundTag rootTag = new CompoundTag();
                rootTag.put("Items", inventoryList);
                CompoundTag effectTag = new CompoundTag();
                SAVED_INVENTORIES.put(player.getUUID(), rootTag);

        }
    }
    @SubscribeEvent
    public static void onPlayerTickCreative(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() || event.phase != TickEvent.Phase.START) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        // 💡 監視フラグ/保存データがある場合のみ実行
        if (!player.getPersistentData().contains("magic_chants:saved_inventory")) return;

        CompoundTag savedRootTag = player.getPersistentData().getCompound("magic_chants:saved_inventory");

        // --- 現在のインベントリ状態をNBTとして取得 ---
        ListTag currentInventoryList = new ListTag();
        player.getInventory().save(currentInventoryList);

        CompoundTag currentRootTag = new CompoundTag();
        currentRootTag.put(Magic_chants.MODID+"Items", currentInventoryList);

        // --- 変更があったかどうかをチェック ---
        // NBTのtoString()はパフォーマンスが良いわけではありませんが、
        // 毎回深いNBT比較を行うよりはシンプルで、多くの変更を検知できます。
        if (!savedRootTag.equals(currentRootTag)) {
            // 変更があった場合のみ、保存されたNBTを強制的にロード（ロールバック）
            ListTag inventoryListToLoad = savedRootTag.getList(Magic_chants.MODID+"Items", Tag.TAG_COMPOUND);
            player.getInventory().load(inventoryListToLoad);

            // 変更があった場合のみ、クライアントに同期
            player.containerMenu.sendAllDataToRemote();
        }
    }
    @SubscribeEvent
    public static void checkEffectRemoved(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() || event.phase != TickEvent.Phase.START) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        // 1. 以前付いていたという記録があるかチェック
        if (player.getPersistentData().getBoolean("magic_chants:has_corruption")) {

            // 2. 現在エフェクトが付いているかチェック
            if (!player.hasEffect(ModRegistry.DISCREATIVE.get())) {
                player.getPersistentData().remove("magic_chants:saved_inventory");
                // 3. 監視フラグを削除
                player.getPersistentData().remove("magic_chants:has_corruption");
                player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            }
        }
    }

    // MobEffectEvent.Added の中で、フラグを立てるロジックを修正
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player){
            if (event.getEffectInstance().getEffect() == ModRegistry.DISCREATIVE.get()) {
                MagicChantsAPI.setOwnerTagToAllItems(player);
                event.getEntity().getPersistentData().putBoolean("magic_chants:has_corruption", true);
                ListTag inventoryList = new ListTag();
                player.getInventory().save(inventoryList);
                CompoundTag rootTag = new CompoundTag();
                rootTag.put(Magic_chants.MODID+"Items", inventoryList);
                player.getPersistentData().put("magic_chants:saved_inventory", rootTag);
                player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
            }
        }
    }
    @SubscribeEvent
    public static void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getStack();

        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("magic_chants", Tag.TAG_COMPOUND)) {

                CompoundTag customTag = tag.getCompound("magic_chants");

                if (customTag.contains("magic_chants:creative", Tag.TAG_STRING)) {
                    UUID uuid = customTag.getUUID("magic_chants:creativeuuid");

                    // 💡 UUIDを比較
                    if (!(player.getUUID() == uuid)) {
                        // 所有者ではない場合、アイテム回収をキャンセル
                        event.setCanceled(true);
                        event.getOriginalEntity().clearFire();
                    }
                }
            }
        }
    }
}
