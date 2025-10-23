package com.hutuneko.magic_chants.api.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * ワールドセーブフォルダ内に JSON ファイルを保存／読み込みするユーティリティ。
 * 保存先例:  world/data/magic_chants/<category>/<uuid>.json
 */
public class WorldJsonStorage {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final String BASE_DIR = "data/magic_chants";

    // =====================================================
    // 保存
    // =====================================================
    /**
     * 任意のオブジェクトをワールドデータにJSONとして保存する
     *
     * @param level        サーバーレベル
     * @param relativePath "aliases/xxxx.json" のようなパス
     * @param data         保存したいオブジェクト
     */
    public static void save(ServerLevel level, String relativePath, Object data) {
        try {
            File file = getWorldFile(level, relativePath);
            file.getParentFile().mkdirs(); // ディレクトリ自動生成
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
            System.out.println("[WorldJsonStorage] Saved JSON → " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[WorldJsonStorage] Failed to save JSON: " + relativePath);
            e.printStackTrace();
        }
    }

    // =====================================================
    // 読み込み
    // =====================================================
    /**
     * ワールドデータからJSONを読み込む
     *
     * @param level        サーバーレベル
     * @param relativePath "aliases/xxxx.json" のようなパス
     * @param clazz        変換先の型（Map.class や CustomClass.class）
     */
    public static <T> T load(ServerLevel level, String relativePath, Class<T> clazz) {
        File file = getWorldFile(level, relativePath);
        if (!file.exists()) {
            System.out.println("[WorldJsonStorage] No file found for " + relativePath);
            return null;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            T obj = GSON.fromJson(reader, clazz);
            System.out.println("[WorldJsonStorage] Loaded JSON ← " + file.getAbsolutePath());
            return obj;
        } catch (Exception e) {
            System.err.println("[WorldJsonStorage] Failed to load JSON: " + relativePath);
            e.printStackTrace();
        }
        return null;
    }

    // =====================================================
    // 内部ユーティリティ
    // =====================================================
    private static File getWorldFile(ServerLevel level, String relativePath) {
        MinecraftServer server = level.getServer();
        Path worldDir = server.getWorldPath(LevelResource.ROOT);
        return worldDir.resolve(BASE_DIR).resolve(relativePath).toFile();
    }
    public static void savePlayerAliases(ServerLevel level, UUID uuid, Map<String, String> data) {
        WorldJsonStorage.save(level, "aliases/" + uuid + ".json", data);
    }

    public static Map<String, String> loadPlayerAliases(ServerLevel level, UUID uuid) {
        return WorldJsonStorage.load(level, "aliases/" + uuid + ".json", Map.class);
    }

}
