package com.hutuneko.magic_chants.api.chat.item.dictionary;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerLevel;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

public final class ItemAliasIO {
    private static final Gson GSON = new Gson();
    private static final Type LIST_STRING = new TypeToken<List<String>>(){}.getType();

    private ItemAliasIO(){}

    /** 保存ディレクトリ(world/data/magic_chants/aliases) */
    public static Path dir(ServerLevel level) {
        return level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("data").resolve("magic_chants").resolve("aliases");
    }

    /** 読み込み（無ければ空リスト） */
    public static List<String> read(ServerLevel level, UUID uuid) {
        try {
            Path p = dir(level).resolve(uuid.toString() + ".json");
            if (!Files.exists(p)) return List.of();
            String json = Files.readString(p);
            List<String> lines = GSON.fromJson(json, LIST_STRING);
            return (lines != null) ? lines : List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /** 書き込み（親フォルダ自動作成） */
    public static void write(ServerLevel level, UUID uuid, List<String> lines) {
        try {
            Path d = dir(level);
            Files.createDirectories(d);
            Path p = d.resolve(uuid.toString() + ".json");
            String json = GSON.toJson(lines, LIST_STRING);
            Files.writeString(p, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
