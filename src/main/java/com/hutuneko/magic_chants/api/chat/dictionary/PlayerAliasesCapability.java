// PlayerAliasesCapability.java
package com.hutuneko.magic_chants.api.chat.dictionary;

import com.hutuneko.magic_chants.Magic_chants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber // ← 受信するため付けておく（既にどこかで登録してるなら不要）
public class PlayerAliasesCapability {
    public static final ResourceLocation KEY = new ResourceLocation(Magic_chants.MODID, "player_aliases");

    // ★ 追加：これが “CAP”
    public static final Capability<IPlayerAliases> CAP = CapabilityManager.get(new CapabilityToken<>(){});

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> e){
        if (e.getObject() instanceof Player){
            // ICapabilitySerializable を実装しておくと後で serialize/deserialize が呼ばれる
            e.addCapability(KEY, new ICapabilitySerializable<CompoundTag>() {
                final PlayerAliases inst = new PlayerAliases();
                final LazyOptional<IPlayerAliases> opt = LazyOptional.of(() -> inst);

                @Override public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side){
                    return cap == CAP ? opt.cast() : LazyOptional.empty();
                }
                @Override public CompoundTag serializeNBT(){ return inst.serializeNBT(); }
                @Override public void deserializeNBT(CompoundTag nbt){ inst.deserializeNBT(nbt); }
            });
        }
    }
}
