package io.github.hutuneko.magic_chants.api.player;

import io.github.hutuneko.magic_chants.MagicChants;
import io.github.hutuneko.magic_chants.ModRegistry;
import io.github.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPower;
import io.github.hutuneko.magic_chants.api.player.attribute.magic_power.MagicPowerProvider;
import io.github.hutuneko.magic_chants.api.util.MagicChantsAPI;
import io.github.hutuneko.magic_chants.api.util.TickTaskManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = MagicChants.MODID)
public class ForgeEvent {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        TickTaskManager.onTick();
    }

    private static final Map<UUID, Integer> tickMap = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        if (e.getEntity().level().isClientSide()) return;
        ServerPlayer player = (ServerPlayer) e.getEntity();
        UUID uuid = player.getUUID();
        int ticks = tickMap.getOrDefault(uuid, 0) + 1;
        if (ticks >= 20) { // 1秒ごとに同期
            tickMap.put(uuid, 0);
            // Capability → Data Attachment
            MagicPower power = player.getData(MagicPowerProvider.MAGIC_POWER.get());
            // 必要に応じてここでMP同期などの処理を行う
        } else {
            tickMap.put(uuid, ticks);
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!player.getPersistentData().getBooleanOr("magic_chants:respawnf", false)) return;
            event.setCanceled(true);
        }
    }

    public static final HashMap<UUID, CompoundTag> SAVED_INVENTORIES = new HashMap<>();
    public static final HashMap<UUID, GlobalPos> GLOBAL_POS_HASH_MAP = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerCloneR(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getOriginal().getPersistentData().getBooleanOr("magic_chants:respawnf", false)) {
            Player newPlayer = event.getEntity();
            UUID playerId = newPlayer.getUUID();
            HolderLookup.Provider provider = newPlayer.level().registryAccess();

            if (SAVED_INVENTORIES.containsKey(playerId)) {
                CompoundTag rootTag = SAVED_INVENTORIES.get(playerId);
                ListTag inventoryList = rootTag.getListOrEmpty("Items");
                loadInventoryFromListTag(newPlayer.getInventory(), inventoryList, provider);
                SAVED_INVENTORIES.remove(playerId);
            }

            newPlayer.experienceLevel = event.getOriginal().experienceLevel;
            newPlayer.experienceProgress = event.getOriginal().experienceProgress;
            newPlayer.totalExperience = event.getOriginal().totalExperience;

            if (GLOBAL_POS_HASH_MAP.containsKey(playerId)) {
                GlobalPos pos = GLOBAL_POS_HASH_MAP.get(playerId);
                BlockPos bpos = pos.pos();
                MinecraftServer server = newPlayer.level().getServer();
                if (server != null) {
                    ServerLevel serverLevel = server.getLevel(pos.dimension());
                    if (serverLevel != null) {
                        newPlayer.teleportTo(bpos.getX(), bpos.getY(), bpos.getZ());
                    }
                }
                GLOBAL_POS_HASH_MAP.remove(playerId);
            }
            event.getOriginal().getPersistentData().remove("magic_chants:respawnf");
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player newPlayer = event.getEntity();
        UUID playerId = newPlayer.getUUID();
        if (newPlayer.level().isClientSide()) return;

        if (SAVED_INVENTORIES.containsKey(playerId) && event.isEndConquered() && newPlayer instanceof ServerPlayer) {
            CompoundTag rootTag = SAVED_INVENTORIES.remove(playerId);
            ListTag inventoryList = rootTag.getListOrEmpty("Items");
            loadInventoryFromListTag(newPlayer.getInventory(), inventoryList, newPlayer.level().registryAccess());
        }
    }

    private static final Map<UUID, Integer> integerMap = new HashMap<>();

    @SubscribeEvent
    public static void onLivingDeaths(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().putBoolean("magic_chants:respawnf", true);
            HolderLookup.Provider provider = player.level().registryAccess();

            ListTag inventoryList = saveInventoryToListTag(player.getInventory(), provider);

            CompoundTag rootTag = new CompoundTag();
            rootTag.put("Items", inventoryList);
            SAVED_INVENTORIES.put(player.getUUID(), rootTag);
        }
    }

    @SubscribeEvent
    public static void onPlayerTickCreative(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.getPersistentData().contains("magic_chants:saved_inventory")) return;

        CompoundTag savedRootTag = player.getPersistentData().getCompoundOrEmpty("magic_chants:saved_inventory");
        HolderLookup.Provider provider = player.level().registryAccess();

        ListTag currentInventoryList = saveInventoryToListTag(player.getInventory(), provider);
        CompoundTag currentRootTag = new CompoundTag();
        currentRootTag.put(MagicChants.MODID + "Items", currentInventoryList);

        if (!savedRootTag.equals(currentRootTag)) {
            ListTag inventoryListToLoad = savedRootTag.getListOrEmpty(MagicChants.MODID + "Items");
            loadInventoryFromListTag(player.getInventory(), inventoryListToLoad, provider);
            player.containerMenu.sendAllDataToRemote();
        }
    }

    @SubscribeEvent
    public static void checkEffectRemoved(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.getPersistentData().getBooleanOr("magic_chants:has_corruption", false)) {
            if (!player.hasEffect(ModRegistry.DISCREATIVE)) {
                player.getPersistentData().remove("magic_chants:saved_inventory");
                player.getPersistentData().remove("magic_chants:has_corruption");
                player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            if (event.getEffectInstance().getEffect() == ModRegistry.DISCREATIVE) {
                MagicChantsAPI.setOwnerTagToAllItems(player);
                player.getPersistentData().putBoolean("magic_chants:has_corruption", true);

                HolderLookup.Provider provider = player.level().registryAccess();
                ListTag inventoryList = saveInventoryToListTag(player.getInventory(), provider);
                CompoundTag rootTag = new CompoundTag();
                rootTag.put(MagicChants.MODID + "Items", inventoryList);
                player.getPersistentData().put("magic_chants:saved_inventory", rootTag);

                player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
            }
        }
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItemEntity().getItem();

        if (!player.level().isClientSide()) {
            CompoundTag tag = MagicChantsAPI.getOrCreateTag(stack);
            if (tag != null && tag.contains("magic_chants")) {
                CompoundTag customTag = tag.getCompound("magic_chants").orElse(new CompoundTag());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                if (customTag.contains("magic_chants:creative")) {
                    UUID uuid = UUID.fromString(customTag.getStringOr("magic_chants:creativeuuid",""));
                    if (!player.getUUID().equals(uuid)) {
                        event.getItemEntity().clearFire();
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------
    // Helper methods: bridge between new Value I/O API and legacy ListTag
    // -----------------------------------------------------------------

    private static ListTag saveInventoryToListTag(Inventory inventory, HolderLookup.Provider provider) {
        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING,
                provider
        );
        ValueOutput.TypedOutputList<ItemStackWithSlot> list = output.list("Items", ItemStackWithSlot.CODEC);
        inventory.save(list);
        return output.buildResult().getListOrEmpty("Items");
    }

    private static void loadInventoryFromListTag(Inventory inventory, ListTag listTag, HolderLookup.Provider provider) {
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("Items", listTag);

        ProblemReporter.Collector reporter =
                new ProblemReporter.Collector(
                        new ProblemReporter.RootFieldPathElement("inventory")
                );
        if (!(TagValueInput.create(reporter, provider, wrapper) instanceof TagValueInput input)) return;
        ValueInput.TypedInputList<ItemStackWithSlot> list = input.listOrEmpty("Items", ItemStackWithSlot.CODEC);
        inventory.load(list);
    }
}