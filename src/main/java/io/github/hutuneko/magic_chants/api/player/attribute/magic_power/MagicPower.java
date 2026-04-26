package io.github.hutuneko.magic_chants.api.player.attribute.magic_power;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class MagicPower implements IMagicPower, ValueIOSerializable {
    private double mp = 100;
    private double maxMP = 100;

    public MagicPower() {}

    @Override public double getMP() { return mp; }
    @Override public void setMP(double value) { this.mp = Math.max(0, Math.min(value, maxMP)); }

    @Override public double getMaxMP() { return maxMP; }
    @Override public void setMaxMP(double value) { this.maxMP = value; }

    @Override
    public void serialize(ValueOutput output) {
        output.putDouble("mp", this.mp);
        output.putDouble("maxMP", this.maxMP);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.mp = input.getDoubleOr("mp", 100);
        this.maxMP = input.getDoubleOr("maxMP", 100);
    }
}