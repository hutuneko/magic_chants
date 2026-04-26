package io.github.hutuneko.magic_chants.api.block.gui;

import io.github.hutuneko.magic_chants.api.block.net.C2SRequestItemAliases;
import io.github.hutuneko.magic_chants.api.block.net.C2SRewriteAndSaveAliases;
import io.github.hutuneko.magic_chants.api.util.MagicChantsAPI;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

public class ChantTunerScreen extends AbstractContainerScreen<ChantTunerMenu> implements ContainerListener {
    private final static HashMap<String, Object> guistate = ChantTunerMenu.guistate;
    private static final String KEY_UUID = "magic_chants:item_uuid";
    @Nullable
    private UUID viewingItemUuid = null;
    MultiLineEditBox a;
    private Button saveBtn;

    public ChantTunerScreen(ChantTunerMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
    }

    private static final Identifier texture = Identifier.fromNamespaceAndPath("magic_chants", "textures/screens/chant_tuner.png");


    @Override
    public void init() {
        super.init();

        this.a = MultiLineEditBox.builder().build(this.font, 150, 100,
                Component.literal("詠唱を入力"));
        this.a.setFGColor(0xFFD700);

        this.addRenderableWidget(this.a);
        guistate.put("text:a", a);
        this.menu.addSlotListener(this);
        this.menu.setClientSlot0Changed(this::updateText);
        saveBtn = Button.builder(Component.literal("Save"), b -> doSave())
                .pos(leftPos + 6, topPos + 6 + 110).size(60, 20).build();
        addRenderableWidget(saveBtn);
        updateTextFromSlot();
    }

    private void updateTextFromSlot() {
        ItemStack stack = menu.getSlot(0).getItem();
        updateText(stack);
    }

    public void updateText(ItemStack stack) {
        if (stack.isEmpty()) {
            this.a.setValue("");
            this.viewingItemUuid = null;
            return;
        }
        CompoundTag tag = MagicChantsAPI.getOrCreateTag(stack);
        String uuid = tag.getString(KEY_UUID).orElse("");
        this.viewingItemUuid = UUID.fromString(uuid);
        this.a.setValue("(loading aliases...)");
        ClientPacketDistributor.sendToServer(new C2SRequestItemAliases(uuid));
    }

    public void applyAliasesFromServerJson(String uuid, String json) {
        if (viewingItemUuid == null || !viewingItemUuid.equals(UUID.fromString(uuid))) {
            System.out.println("[GUI] uuid mismatch: viewing=" + viewingItemUuid + " recv=" + uuid);
            return;
        }
        try {
            this.a.setValue(json);
        } catch (Exception ex) {
            this.a.setValue("(invalid json)");
        }
    }

    private void doSave() {
        if (this.viewingItemUuid != null) {
            ClientPacketDistributor.sendToServer(
                    new C2SRewriteAndSaveAliases(this.viewingItemUuid.toString(), this.a.getValue())
            );
        }
    }

    @Override
    public void slotChanged(@NonNull AbstractContainerMenu menu, int slotIndex, @NonNull ItemStack stack) {
        if (slotIndex == 0) {
            updateText(stack);
        }
    }

    @Override
    public void dataChanged(@NonNull AbstractContainerMenu menu, int dataIndex, int value) {
    }
}