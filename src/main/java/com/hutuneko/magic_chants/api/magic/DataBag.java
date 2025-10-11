package com.hutuneko.magic_chants.api.magic;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class DataBag {
    private final Map<ResourceLocation, Entry<?>> map = new HashMap<>();

    private record Entry<T>(DataKey<T> key, T value) {}

    public <T> void put(DataKey<T> key, T value) {
        map.put(key.id(), new Entry<>(key, value));
    }

    public <T> Optional<T> get(DataKey<T> key) {
        var e = map.get(key.id());
        if (e == null) return Optional.empty();
        try {
            @SuppressWarnings("unchecked")
            T v = (T) ((Entry<?>) e).value;
            return Optional.ofNullable(v);
        } catch (ClassCastException ex) {
            return Optional.empty();
        }
    }


    public <T> T require(DataKey<T> key) {
        return get(key).orElseThrow(() -> new IllegalStateException("Missing: " + key.id()));
    }

    private static <T> Tag encodeToNbt(Codec<T> codec, T value) {
        return codec.encodeStart(NbtOps.INSTANCE, value)
                .result()
                .orElse(null);
    }

    public ListTag saveToNbt() {
        ListTag out = new ListTag();
        for (Entry<?> en : map.values()) {
            putOne(out, en);
        }
        return out;
    }

    private static <T> void putOne(ListTag out, Entry<T> en) {
        Tag data = encodeToNbt(en.key.codec(), en.value);  // T で型がそろう
        if (data == null) return;
        CompoundTag row = new CompoundTag();
        row.putString("id", en.key.id().toString());
        row.put("data", data);
        out.add(row);
    }


    // NBT読込：外から提供される "キー定義表" が必要（id→DataKey）
    public static DataBag loadFromNbt(ListTag list, Function<ResourceLocation, DataKey<?>> keyResolver) {
        DataBag bag = new DataBag();
        for (Tag t : list) {
            if (!(t instanceof CompoundTag ct)) continue;
            ResourceLocation id = new ResourceLocation(ct.getString("id"));
            DataKey<?> key = keyResolver.apply(id);
            if (key == null) continue; // 未知キーはスキップ
            Tag data = ct.get("data");
            if (data == null) continue;
            Object value = key.codec().parse(NbtOps.INSTANCE, data).result().orElse(null);
            if (value != null) {
                // 型擦り合わせOKなのでput
                @SuppressWarnings("unchecked") DataKey<Object> k = (DataKey<Object>) key;
                bag.put(k, value);
            }
        }
        return bag;
    }
}