package io.github.hutuneko.magic_chants;

import io.github.hutuneko.magic_chants.api.player.effect.DisguiseCreative;
import io.github.hutuneko.magic_chants.api.player.effect.InsRespawn;
import io.github.hutuneko.magic_chants.block.ChantTunerBE;
import io.github.hutuneko.magic_chants.api.block.gui.ChantTunerMenu;
import io.github.hutuneko.magic_chants.block.ChantTunerBlock;
import io.github.hutuneko.magic_chants.entity.LandMineEntity;
import io.github.hutuneko.magic_chants.item.MagicSwordItem;
import io.github.hutuneko.magic_chants.item.MagicWandItem;
import io.github.hutuneko.magic_chants.item.Test;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRegistry {
    public static final String MODID = MagicChants.MODID;

    // DeferredRegister
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BEs = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    // ブロック
    public static final DeferredHolder<Block,ChantTunerBlock> CHANT_TUNER = BLOCKS.registerBlock("chant_tuner",
                    ChantTunerBlock::new,
                    BlockBehaviour.Properties::of);

    public static final DeferredHolder<Item, MagicWandItem> MAGIC_WAND = ITEMS.registerItem("magic_wand",
            MagicWandItem::new,()-> new Item.Properties().stacksTo(1));
    public static final DeferredHolder<Item, Test> TEST = ITEMS.registerItem("test",Test::new,() -> new Item.Properties().stacksTo(1));
    public static final DeferredHolder<Item, MagicSwordItem> MAGIC_SWORD = ITEMS.registerItem("magic_sword",
            MagicSwordItem::new,()-> new Item.Properties().stacksTo(1));
    // ブロックアイテム
    public static final DeferredHolder<Item,BlockItem> CHANT_TUNER_ITEM = ITEMS
            .registerSimpleBlockItem("chant_tuner", CHANT_TUNER, Item.Properties::new);

    // ブロックエンティティ
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ChantTunerBE>> CHANT_TUNER_BE = BEs.register(
            "chant_tuner", () -> new BlockEntityType<>(ChantTunerBE::new, CHANT_TUNER.get()));

    // メニュー（コンテナ）
    public static final DeferredHolder<MenuType<?>,MenuType<ChantTunerMenu>> CHANT_TUNER_MENU =
            MENUS.register("chant_tuner", () -> IMenuTypeExtension.create(ChantTunerMenu::fromNetwork));

    public static final DeferredHolder<MobEffect,InsRespawn> INSRESPAWN = MOB_EFFECTS.register("instant_respawn",
            () -> new InsRespawn(MobEffectCategory.HARMFUL, 0xCA8BF7));
    public static final DeferredHolder<MobEffect,DisguiseCreative> DISCREATIVE = MOB_EFFECTS.register("disguise_creative",
            () -> new DisguiseCreative(MobEffectCategory.HARMFUL, 0xCA8BF7));
    public static final DeferredHolder<EntityType<?>,EntityType<LandMineEntity>> LAND_MINE =
            ENTITY_TYPES.register("land_mine",
                    () -> EntityType.Builder.of(
                                    // エンティティファクトリ（コンストラクタ参照）
                                    LandMineEntity::new,
                                    MobCategory.MISC)
                            // エンティティの当たり判定のサイズを定義 (幅, 高さ)
                            .sized(0.5f, 0.1f)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,MagicChants.rl("land_mine")))
            );
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BEs.register(bus);
        MENUS.register(bus);
        MOB_EFFECTS.register(bus);
        ENTITY_TYPES.register(bus);
    }
}
