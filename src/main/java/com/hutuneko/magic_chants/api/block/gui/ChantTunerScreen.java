package com.hutuneko.magic_chants.api.block.gui;

import com.hutuneko.magic_chants.api.block.net.C2S_RequestItemAliases;
import com.hutuneko.magic_chants.api.block.net.C2S_RewriteAndSaveAliases;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ChantTunerScreen extends AbstractContainerScreen<ChantTunerMenu> implements ContainerListener{
    private final static HashMap<String, Object> guistate = ChantTunerMenu.guistate;
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    private static final String KEY_UUID = "magic_chants:item_uuid";
    @Nullable
    private UUID viewingItemUuid = null;
    MultiLineEditBox a;
    private Button saveBtn;

    public ChantTunerScreen(ChantTunerMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = 390;
        this.imageHeight = 240;
    }

    private static final ResourceLocation texture = new ResourceLocation("magic_chants:textures/screens/chant_tuner.png");

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        a.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        guiGraphics.blit(new ResourceLocation("magic_chants:textures/screens/chant_tuner.png"), this.leftPos, this.topPos, 0, 0, 390, 240, 390, 240);

        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        if (a.isFocused())
            return a.keyPressed(key, b, c);
        return super.keyPressed(key, b, c);
        }

    @Override
    public void containerTick() {
        super.containerTick();
        a.tick();
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        String aValue = a.getValue();
        super.resize(minecraft, width, height);
        a.setValue(aValue);
    }

    @Override
        protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void init() {
        super.init();

        // EditBox を初期化
        this.a = new MultiLineEditBox(this.font, this.leftPos + 10, this.topPos + 10, 150, 100, Component.literal("詠唱を入力"),Component.literal("詠唱を入力"));
        this.a.setFGColor(0xFFD700);

        // GUI に追加
        this.addRenderableWidget(this.a);

        // 必要に応じて guistate に登録
        guistate.put("text:a", a);
        this.menu.addSlotListener(this);
        this.menu.setClientSlot0Changed(this::updateText);
        saveBtn = Button.builder(Component.literal("Save"), b -> doSave()).pos(leftPos+6, topPos+6+110).size(60,20).build();
        addRenderableWidget(saveBtn);
        updateTextFromSlot();
    }
    private void updateTextFromSlot() {
        ItemStack stack = menu.getSlot(0).getItem(); // BE側の専用スロットindexに合わせる
        updateText(stack);
    }

    public void updateText(ItemStack stack) {
        if (stack.isEmpty()) {
            this.a.setValue("");
            this.viewingItemUuid = null;
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.hasUUID(KEY_UUID)) {
            UUID uuid = tag.getUUID(KEY_UUID);
            this.viewingItemUuid = uuid;

            // いったんプレースホルダ表示
            this.a.setValue("(loading aliases...)");

            // ★ サーバーへ問い合わせ
            MagicNetwork.CHANNEL.sendToServer(new C2S_RequestItemAliases(uuid));

        } else {
            this.a.setValue(stack.getHoverName().getString());
            this.viewingItemUuid = null;
        }
    }

    public void applyAliasesFromServerJson(UUID uuid, String json) {
        if (viewingItemUuid == null || !viewingItemUuid.equals(uuid)) {
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
        MagicNetwork.CHANNEL.sendToServer(
                new C2S_RewriteAndSaveAliases(this.viewingItemUuid, this.a.getValue())
        );
    }

    @Override
    public void slotChanged(AbstractContainerMenu menu, int slotIndex, ItemStack stack) {
        if (slotIndex == 0) {
            updateText(stack);
        }
    }
    @Override public void dataChanged(AbstractContainerMenu menu, int dataIndex, int value) {}
}
