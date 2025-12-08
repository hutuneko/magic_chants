// ModRegistry.java
package com.hutuneko.magic_chants;

import com.hutuneko.magic_chants.api.player.effect.DisguiseCreative;
import com.hutuneko.magic_chants.api.player.effect.InsRespawn;
import com.hutuneko.magic_chants.block.ChantTunerBE;
import com.hutuneko.magic_chants.api.block.gui.ChantTunerMenu;
import com.hutuneko.magic_chants.block.ChantTunerBlock;
import com.hutuneko.magic_chants.entity.LandMineEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ModRegistry {
    public static final String MODID = Magic_chants.MODID;

    // DeferredRegister
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BEs = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    // ブロック
    public static final RegistryObject<Block> CHANT_TUNER = BLOCKS.register("chant_tuner", () ->
            new ChantTunerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(2.0F, 3.0F)
                    .noOcclusion()                       // 透過モデルなら
                    .pushReaction(PushReaction.NORMAL)));

    // ブロックアイテム
    public static final RegistryObject<Item> CHANT_TUNER_ITEM = ITEMS.register("chant_tuner", () ->
            new BlockItem(CHANT_TUNER.get(), new Item.Properties()));

    // ブロックエンティティ
    public static final RegistryObject<BlockEntityType<ChantTunerBE>> CHANT_TUNER_BE = BEs.register(
            "chant_tuner", () -> BlockEntityType.Builder.of(ChantTunerBE::new, CHANT_TUNER.get()).build(null));

    // メニュー（コンテナ）
    public static final RegistryObject<MenuType<ChantTunerMenu>> CHANT_TUNER_MENU =
            MENUS.register("chant_tuner", () -> IForgeMenuType.create(ChantTunerMenu::fromNetwork));

    public static final RegistryObject<MobEffect> INSRESPAWN = MOB_EFFECTS.register("instant_respawn",
            () -> new InsRespawn(MobEffectCategory.HARMFUL, 0xCA8BF7));
    public static final RegistryObject<MobEffect> DISCREATIVE = MOB_EFFECTS.register("disguise_creative",
            () -> new DisguiseCreative(MobEffectCategory.HARMFUL, 0xCA8BF7));
    public static final RegistryObject<EntityType<LandMineEntity>> LAND_MINE =
            ENTITY_TYPES.register("land_mine",
                    () -> EntityType.Builder.of(
                                    // エンティティファクトリ（コンストラクタ参照）
                                    LandMineEntity::new,
                                    MobCategory.MISC)
                            // エンティティの当たり判定のサイズを定義 (幅, 高さ)
                            .sized(0.5f, 0.1f)
                            .build("land_mine") // 内部名（MODID:land_mine となる）
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
