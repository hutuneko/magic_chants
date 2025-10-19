package com.hutuneko.magic_chants.api.player.attribute.magic_power;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class MagicPowerProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<IMagicPower> MAGIC_POWER = CapabilityManager.get(new CapabilityToken<>(){});

    private final IMagicPower instance = new MagicPower();
    private final LazyOptional<IMagicPower> optional = LazyOptional.of(() -> instance);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == MAGIC_POWER ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("mp", instance.getMP());
        tag.putDouble("maxMP", instance.getMaxMP());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.setMP(nbt.getDouble("mp"));
        instance.setMaxMP(nbt.getDouble("maxMP"));
    }
}

