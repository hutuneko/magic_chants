package com.hutuneko.magic_chants.api.chat.item;

import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public final class ChantItemUtil {
    private ChantItemUtil(){}

    private static final String KEY_UUID  = "chant_uuid";

    /** アイテムにUUIDを付与（既にあればそれを返す） */
    public static UUID ensureUuid(ItemStack stack){
        var tag = stack.getOrCreateTag();
        if (!tag.hasUUID(KEY_UUID)) tag.putUUID(KEY_UUID, java.util.UUID.randomUUID());
        return tag.getUUID(KEY_UUID);
    }

    /**
     * UUID を読む（無ければ empty）
     */
    public static Optional<Object> getUuid(ItemStack stack){
        var tag = stack.getTag();
        if (tag == null || !tag.hasUUID(KEY_UUID)) return Optional.empty();
        return Optional.of(tag.getUUID(KEY_UUID));
    }
}
