package com.hutuneko.magic_chants.api.block.gui;

import com.hutuneko.magic_chants.api.file.WorldJsonStorage;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
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
import java.util.Map;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

public class ChantTunerScreen extends AbstractContainerScreen<ChantTunerMenu> {
    private final static HashMap<String, Object> guistate = ChantTunerMenu.guistate;
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    MultiLineEditBox a;

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
        // リスナーを外す（メモリリーク防止）
        menu.removeSlotListener(slotListener);
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
//        this.a.setMaxLength(50);
//        this.a.setVisible(true);
        this.a.setFGColor(0xFFD700);

        // GUI に追加
        this.addRenderableWidget(this.a);

        // 必要に応じて guistate に登録
        guistate.put("text:a", a);
        menu.addSlotListener(slotListener);

        // 画面を開いた時点の内容を反映
        updateTextFromSlot();
    }
    private final ContainerListener slotListener = new ContainerListener() {
        @Override
        public void slotChanged(AbstractContainerMenu menu, int slotIndex, ItemStack stack) {
            // ★ 自分のBE用スロット（例：index 0）を監視
            if (slotIndex == 0) {
                updateText(stack);
            }
        }
        @Override public void dataChanged(AbstractContainerMenu menu, int dataIndex, int value) {}
    };

    private void updateTextFromSlot() {
        ItemStack stack = menu.getSlot(0).getItem(); // BE側の専用スロットindexに合わせる
        updateText(stack);
    }

    private void updateText(ItemStack stack) {
        if (stack.isEmpty()) {
            this.a.setValue("（スロットが空です）");
            return;
        }
        // 表示するアイテム情報を整形
        this.a.setValue(buildInfo(stack));
    }
    private String buildInfo(ItemStack s) {
        StringBuilder sb = new StringBuilder();
        CompoundTag tag = s.getTag();
        if (tag != null && !tag.isEmpty()) {
            if (tag.contains("magic_chants:item_uuid", Tag.TAG_STRING)) {
                WorldJsonStorage.loadPlayerAliases((ServerLevel) this.world, UUID.fromString(tag.getString("magic_chants:item_uuid")));
                Map<String, Object> data = WorldJsonStorage.load((ServerLevel)this.world, "chants/test.json", Map.class);
                if (data != null && data.containsKey("chant")) {
                    String chant = data.get("chant").toString();
                    for (String line : chant.split("\\r?\\n")) {
                        System.out.println("> " + line);
                    }
                }
            }
        }
        return sb.toString();
    }
}
