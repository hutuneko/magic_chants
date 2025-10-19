package com.hutuneko.magic_chants.api.player.attribute.magic_power;

public class MagicPower implements IMagicPower {
    private double mp = 100;
    private double maxMP = 100;

    @Override public double getMP() { return mp; }
    @Override public void setMP(double value) { this.mp = Math.max(0, Math.min(value, maxMP)); }

    @Override public double getMaxMP() { return maxMP; }
    @Override public void setMaxMP(double value) { this.maxMP = value; }
}
