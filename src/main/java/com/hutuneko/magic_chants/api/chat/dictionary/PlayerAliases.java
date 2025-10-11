package com.hutuneko.magic_chants.api.chat.dictionary;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerAliases implements IPlayerAliases {
    private final List<AliasRule> rules = new ArrayList<>();

    @Override public List<AliasRule> getRules() { return rules; }
    @Override public void setRules(List<AliasRule> rs){ rules.clear(); rules.addAll(rs); }
    @Override public void addRule(AliasRule r){ rules.add(r); rules.sort(Comparator.comparingInt(AliasRule::priority)); }
    @Override public void clear(){ rules.clear(); }

    @Override public CompoundTag serializeNBT() {
        ListTag list = new ListTag();
        for (var r : rules) {
            CompoundTag t = new CompoundTag();
            t.putString("type", r.type());
            t.putString("from", r.from());
            t.putString("to", r.to());
            t.putInt("priority", r.priority());
            list.add(t);
        }
        CompoundTag root = new CompoundTag();
        root.put("rules", list);
        return root;
    }

    @Override public void deserializeNBT(CompoundTag tag) {
        rules.clear();
        ListTag list = tag.getList("rules", Tag.TAG_COMPOUND);
        for (Tag tt : list) {
            CompoundTag t = (CompoundTag) tt;
            rules.add(new AliasRule(
                    t.getString("type"),
                    t.getString("from"),
                    t.getString("to"),
                    t.getInt("priority")
            ));
        }
        rules.sort(Comparator.comparingInt(AliasRule::priority));
    }
}
