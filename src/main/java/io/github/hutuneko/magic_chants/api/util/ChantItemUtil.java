package io.github.hutuneko.magic_chants.api.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.hutuneko.magic_chants.api.file.WorldJsonStorage;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.UUID;

public final class ChantItemUtil {
    private ChantItemUtil(){}

    public static final String KEY_UUID  = "magic_chants:item_uuid";

    /** アイテムにUUIDを付与（既にあればそれを返す） */
    public static UUID ensureUuid(ItemStack stack, ServerLevel level) {
        var tag = MagicChantsAPI.getOrCreateTag(stack);
        if (!tag.contains(KEY_UUID)) {
            UUID uuid = UUID.randomUUID();
            tag.putString(KEY_UUID, uuid.toString());

            // サーバーの ResourceManager からテンプレートを読む
            JsonElement tmpl = WorldJsonStorage.loadDataJson(
                    level.getServer(),
                    "magic_chants",
                    "magics/magic.json"
            );

            // なければ空オブジェクトでも配列でもOKなようにデフォルトを決める
            if (tmpl == null) tmpl = new JsonObject();

            // JSON を“そのまま”保存（オブジェクトでも配列でも可）
            WorldJsonStorage.save(level, "magics/" + uuid + ".json", tmpl);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return UUID.fromString(tag.getString(KEY_UUID).orElse(""));
    }


    public static UUID ensureUuidReplace(ServerPlayer sp, InteractionHand hand) {
        ItemStack old = sp.getItemInHand(hand);
        if (old.isEmpty()) return null;

        ItemStack stack = old.copy();
        CompoundTag tag = MagicChantsAPI.getOrCreateTag(stack);
        if (!tag.contains(KEY_UUID)) {
            UUID uuid = UUID.randomUUID();
            tag.putString(KEY_UUID, uuid.toString());

            // サーバーの ResourceManager からテンプレートを読む
            JsonElement tmpl = WorldJsonStorage.loadDataJson(
                    sp.level().getServer(),
                    "magic_chants",
                    "magics/magic.json"
            );

            // なければ空オブジェクトでも配列でもOKなようにデフォルトを決める
            if (tmpl == null) tmpl = new JsonObject();

            // JSON を“そのまま”保存（オブジェクトでも配列でも可）
            WorldJsonStorage.save(sp.level(), "magics/" + uuid + ".json", tmpl);
        }
        UUID id = UUID.fromString(tag.getString(KEY_UUID).orElse(""));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        sp.setItemInHand(hand, stack);
        sp.getInventory().setChanged();
        sp.containerMenu.broadcastChanges(); // クライアントへ確実に同期
        return id;
    }

    /**
     * UUID を読む（無ければ empty）
     */
    public static UUID getUuid(ItemStack stack){
        var tag = MagicChantsAPI.getTag(stack);
        if (tag == null || !tag.contains(KEY_UUID)) return null;
        return UUID.fromString(tag.getString(KEY_UUID).orElse(""));
    }
}
