package com.hutuneko.magic_chants.api.chat.item;

import com.hutuneko.magic_chants.api.file.WorldJsonStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public final class ChantItemUtil {
    private ChantItemUtil(){}

    public static final String KEY_UUID  = "magic_chants:item_uuid";

    /** アイテムにUUIDを付与（既にあればそれを返す） */
    public static UUID ensureUuid(ItemStack stack, ServerLevel level){
        var tag = stack.getOrCreateTag();
        if (!tag.hasUUID(KEY_UUID)) {
            UUID uuid = UUID.randomUUID();
            tag.putUUID(KEY_UUID,uuid);
            WorldJsonStorage.save(level, "items/" + uuid + ".json", new HashMap<>());
        }
        return tag.getUUID(KEY_UUID);
    }
    public static UUID ensureUuidReplace(ServerPlayer sp, InteractionHand hand) {
        ItemStack old = sp.getItemInHand(hand);
        if (old.isEmpty()) return null;

        ItemStack stack = old.copy();
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.hasUUID(KEY_UUID)) tag.putUUID(KEY_UUID, UUID.randomUUID());
        UUID id = tag.getUUID(KEY_UUID);

        sp.setItemInHand(hand, stack);
        sp.getInventory().setChanged();
        sp.containerMenu.broadcastChanges(); // クライアントへ確実に同期
        return id;
    }

    /**
     * UUID を読む（無ければ empty）
     */
    public static UUID getUuid(ItemStack stack){
        var tag = stack.getTag();
        if (tag == null || !tag.hasUUID(KEY_UUID)) return null;
        return tag.getUUID(KEY_UUID);
    }
}
