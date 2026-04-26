package io.magic_chants.item;

import io.github.hutuneko.magic_chants.MagicChants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MagicItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MagicChants.MODID);
    public static final RegistryObject<Item> MAGIC_WAND = ITEMS.register("magic_wand",() ->
            new MagicWandItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TEST = ITEMS.register("test",() ->
            new Test(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MAGIC_SWORD = ITEMS.register("magic_sword",() ->
            new MagicSwordItem(Tiers.DIAMOND,2,1, new Item.Properties().stacksTo(1)));
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
