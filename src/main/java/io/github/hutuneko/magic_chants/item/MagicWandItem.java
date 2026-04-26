package io.github.hutuneko.magic_chants.item;

import io.github.hutuneko.magic_chants.api.chat.MagicChatHook;
import io.github.hutuneko.magic_chants.api.chat.MagicChatServer;
import io.github.hutuneko.magic_chants.api.file.AliasRewriter;
import io.github.hutuneko.magic_chants.api.file.WorldJsonStorage;
import io.github.hutuneko.magic_chants.api.util.ChantItemUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class MagicWandItem extends Item {
    public MagicWandItem(Properties properties) {
        super(properties);
    }

    // --- クラフト時 ---
    @Override
    public void onCraftedBy(@NotNull ItemStack stack, @NotNull Player player) {
        Level level  = player.level();
        super.onCraftedBy(stack, player);
        if (!level.isClientSide()) {
            ChantItemUtil.ensureUuid(stack, (ServerLevel) level);
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    // --- Tick更新時（loot・command・pickupなど） ---
    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemStack, level, owner, slot);
        if (!level.isClientSide() && owner instanceof Player) {
            ChantItemUtil.ensureUuid(itemStack, level);
        }
    }

    // --- 右クリック動作 ---
    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        UUID uuid;

        // ⚙️ サーバー側：安全な置き換えでUUID付与＋同期
        if(!player.isShiftKeyDown()){
            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                uuid = ChantItemUtil.ensureUuidReplace(sp, hand);
                MagicChatServer.setCurrent(sp, uuid, hand, stack);
            }
            // ⚙️ クライアント側：チャット開く
            else if (level.isClientSide()) {
                uuid = ChantItemUtil.getUuid(stack);
                if (uuid != null) {
                    MagicChatHook.openMagicChatSession(uuid, hand, stack, player);
                }
            }
        }else {
            if (!level.isClientSide() && player instanceof ServerPlayer sp){
                uuid = ChantItemUtil.getUuid(stack);
                String jsonOut;
                Object raw = WorldJsonStorage.load((ServerLevel) level, "magics/" + uuid + ".json", Object.class);
                if (raw == null) {
                    jsonOut = "{\"magics\":[]}";
                } else {
                    jsonOut = AliasRewriter.toAliasLinesFromMagicsB(raw);
                }
                Component msg = Component.literal(jsonOut)
                        .withStyle(ChatFormatting.LIGHT_PURPLE);
                sp.sendSystemMessage(msg);
            }
        }

        return InteractionResult.SUCCESS;
    }
}