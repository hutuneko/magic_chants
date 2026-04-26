package io.magic_chants.api.magic;

import io.github.hutuneko.magic_chants.api.event.MagicCastEvent;
import io.github.hutuneko.magic_chants.api.player.attribute.magic_power.MPAPI;
import io.github.hutuneko.magic_chants.api.util.MagicChantsAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

// 2) レジストリ本体（id → Class）
public final class MagicClassRegistry {
    private static final Map<ResourceLocation, Class<? extends BaseMagic>> TABLE = new HashMap<>();

    public static void registerMagic(ResourceLocation id, Class<? extends BaseMagic> cls) {
        if (TABLE.putIfAbsent(id, cls) != null)
            throw new IllegalStateException("Duplicate magic id: " + id);
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

    private static void post(MagicContext context){
        MagicCastEvent event = new MagicCastEvent(context,context.current());
        MagicChantsAPI.post(event);
    }

    public static boolean call(ResourceLocation id, MagicContext ctx, CompoundTag args, float scorer, boolean sub) {
        Class<? extends BaseMagic> cls;
        cls = TABLE.get(id);
        if (cls == null) {
            return false;
        }
        if (sub) {
            try {
                BaseMagic inst = newInstance(cls, args);
                post(ctx);
                inst.subMagic(ctx);
                return true;
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return false;
            }
        }else {
            try {
                BaseMagic inst = newInstance(cls, args);
                if (!(MPAPI.calculateMpCost(scorer, ctx))) return false;
                post(ctx);
                inst.mainMagic(ctx);
                return true;
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}


