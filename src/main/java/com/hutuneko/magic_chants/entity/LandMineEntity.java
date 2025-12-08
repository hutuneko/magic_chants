package com.hutuneko.magic_chants.entity;

import com.hutuneko.magic_chants.api.magic.MagicCast;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LandMineEntity extends Entity {

    private List<MagicCast.Step> steps;
    private List<Boolean> sub;
    private String chantRaw;
    private ServerPlayer sp;
    private LivingEntity entity;
    public LandMineEntity(EntityType<? extends LandMineEntity> type, Level level) {
        super(type, level);
        this.noCulling = true; // 遠くても描画が途切れないようにする（任意）
    }

    // 必須メソッド: エンティティの状態を初期化するためのメソッド
    @Override
    protected void defineSynchedData() {
    }

    // 必須メソッド: エンティティのデータ（NBT）を読み込む
    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.@NotNull CompoundTag tag) {
    }

    // 必須メソッド: エンティティのデータ（NBT）を書き込む
    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.@NotNull CompoundTag tag) {
    }

    // 重要なメソッド: 毎ティック（毎フレーム）呼ばれる処理
    // LandMineEntity.java (抜粋 - サーバー側の処理を想定)
    @Override
    public void tick() {
        super.tick();

        // サーバー側でのみ処理を実行 (重要)
        if (!this.level().isClientSide) {
            // 現在のエンティティの位置とサイズに基づいてAABB（軸平行境界ボックス）を取得
            AABB boundingBox = this.getBoundingBox().inflate(0.1); // 判定をわずかに広げる

            // 接触したエンティティのリストを取得
            // この例では、プレイヤーのみを対象としています。
            List<LivingEntity> nearbyPlayers = this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    boundingBox,
                    LivingEntity::isAlive // 生きているエンティティのみ
            );

            if (!nearbyPlayers.isEmpty()) {
                // プレイヤーが触れた場合の処理
                LivingEntity touchedPlayer = nearbyPlayers.get(0);
                this.onEntityTouched(touchedPlayer);
            }
        }
    }

    // プレイヤーが触れたときの処理
    protected void onEntityTouched(LivingEntity entity) {
        this.entity = entity;
        this.remove(Entity.RemovalReason.DISCARDED);
        MagicCast.startChain((ServerLevel) entity.level(), sp, steps, null, 200, chantRaw,sub,null);
    }
    @Override
    public boolean isInvisible() {
        return true;
    }
    public void setSteps(List<MagicCast.Step> steps){this.steps = steps;}
    public void setSub(List<Boolean> sub){this.sub = sub;}
    public void setChantRaw(String chantRaw){this.chantRaw = chantRaw;}
    public void setSp(ServerPlayer sp){this.sp = sp;}


}