package com.hutuneko.magic_chants.api.magic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

// 2) レジストリ本体（id → Class）
public final class MagicClassRegistry {
    private static final Map<ResourceLocation, Class<? extends BaseMagic>> TABLE = new HashMap<>();

    public static void registerSpell(ResourceLocation id, Class<? extends BaseMagic> cls) {
        if (TABLE.putIfAbsent(id, cls) != null)
            throw new IllegalStateException("Duplicate spell id: " + id);
    }

    // 生成ヘルパ（CompoundTagコンストラクタ優先→0引数）
    private static BaseMagic newInstance(Class<? extends BaseMagic> cls, CompoundTag args) throws ReflectiveOperationException {

        try {
            var ctor = cls.getDeclaredConstructor(CompoundTag.class);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (NoSuchMethodException miss) {
            var ctor0 = cls.getDeclaredConstructor();
            ctor0.setAccessible(true);
            return ctor0.newInstance();
        }
    }

//    // 呼び出し：IDに一致するクラスの非staticメソッド magic_content を実行
//    public static boolean call(ResourceLocation id, ServerLevel level, @Nullable ServerPlayer player,
//                               CompoundTag args, DataBag bag) {          // ← DataBag を引数で受ける
//        Class<? extends BaseMagic> cls = TABLE.get(id);
//        if (cls == null) return false;
//        try {
//            BaseMagic inst = newInstance(cls, args);
//            MagicContext ctx = new MagicContext(level, player, bag);     // ← ここで組む
//            inst.magic_content(ctx);                                     // ← ctx を渡す
//            return true;
//        } catch (ReflectiveOperationException e) {
//            return false;
//        }
//    }
    public static boolean call(ResourceLocation id, MagicContext ctx, CompoundTag args) {
        Class<? extends BaseMagic> cls = TABLE.get(id);
        if (cls == null) {
            System.out.println("[MagicRegistry] NOT FOUND: " + id);
            return false;
        }
        try {
            BaseMagic inst = newInstance(cls, args);
            System.out.println("[MagicRegistry] RUN: " + id);
            inst.magic_content(ctx);
            return true;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
    }
}


