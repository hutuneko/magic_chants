package io.github.hutuneko.magic_chants.api.player.attribute;

import io.github.hutuneko.magic_chants.MagicChants;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@EventBusSubscriber(modid = MagicChants.MODID)
public class AttributeEvents {
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, MagicAttributes.CHANT_POWER);
    }
}
