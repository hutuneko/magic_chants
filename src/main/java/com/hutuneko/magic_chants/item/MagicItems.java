package com.hutuneko.magic_chants.item;

import com.hutuneko.magic_chants.Magic_chants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MagicItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Magic_chants.MODID);
    public static final RegistryObject<Item> TESTITEM = ITEMS.register("test",() ->
            new TestItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MAGIC_SWORD = ITEMS.register("magic_sword",() ->
            new MagicSwordItem(Tiers.DIAMOND,2,1, new Item.Properties().stacksTo(1)));
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
