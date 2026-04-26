package io.github.hutuneko.magic_chants.api.util.cliant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class EntityNameLookup {
    // Key: 表示名 (例: "ゾンビ"), Value: レジストリ名 (例: "minecraft:zombie")
    private static final Map<String, String> NAME_TO_ID = new HashMap<>();

    /**
     * キャッシュの初期化
     * クライアント側で言語が確定したタイミング（ログイン時やリソースリロード時）に呼ぶ
     */
    public static void initCache() {
        NAME_TO_ID.clear();

        BuiltInRegistries.ENTITY_TYPE.entrySet().forEach(entry -> {
            EntityType<?> type = entry.getValue();
            String registryName = entry.getKey().identifier().toString();

            // 現在の言語設定での表示名を取得
            String displayName = Component.translatable(type.getDescriptionId()).getString();

            // 小文字で登録（検索時のゆらぎ防止）
            NAME_TO_ID.put(displayName.toLowerCase(), registryName);
        });
    }

    /**
     * 表示名からレジストリ名（String）を返す
     * 見つからない場合は null
     */
    public static String getRegistryName(String displayName) {
        if (NAME_TO_ID.isEmpty()) initCache();
        return NAME_TO_ID.get(displayName.toLowerCase());
    }
}