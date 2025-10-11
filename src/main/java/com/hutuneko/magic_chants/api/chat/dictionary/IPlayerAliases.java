package com.hutuneko.magic_chants.api.chat.dictionary;

import net.minecraft.nbt.CompoundTag;

import java.util.List;

public interface IPlayerAliases {
    List<AliasRule> getRules();
    void setRules(List<AliasRule> rules);
    void addRule(AliasRule rule);
    void clear();
    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag tag);

    record AliasRule(
            String type,        // "literal" | "regex"
            String from,        // 置換元
            String to,          // 置換先
            int priority        // 小さいほど先に適用
    ) {}
}
