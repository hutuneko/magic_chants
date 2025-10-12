package com.hutuneko.magic_chants.api.player.attribute;

import com.hutuneko.magic_chants.Magic_chants;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Magic_chants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AttributeEvents {
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, MagicAttributes.MAGIC_POWER.get());
        event.add(EntityType.PLAYER, MagicAttributes.CHANT_POWER.get());
    }
}
