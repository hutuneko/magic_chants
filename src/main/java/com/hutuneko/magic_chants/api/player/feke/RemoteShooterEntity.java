package com.hutuneko.magic_chants.api.player.feke;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RemoteShooterEntity extends Mob {
    private UUID ownerUUID = null;

    public RemoteShooterEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    public RemoteShooterEntity(Level level, UUID ownerUUID) {
        this(EntityType.ALLAY, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.ownerUUID = ownerUUID;
    }

    @Override
    protected void registerGoals() {}
    @Override
    public boolean isInvisible() { return true; } // 見えない
    @Override
    protected void defineSynchedData() {}
    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.@NotNull CompoundTag tag) {}
    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.@NotNull CompoundTag tag) {}

    public UUID getOwnerUUID() { return ownerUUID; }
    @Override
    public void tick() {
        super.tick();
        this.noPhysics = true;
        this.setDeltaMovement(0,0,0); // 動かない
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return EntityDimensions.fixed(0.5f, 0.5f);
    }
}

