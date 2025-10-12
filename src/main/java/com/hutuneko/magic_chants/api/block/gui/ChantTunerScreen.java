package com.hutuneko.magic_chants.api.block.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

public class ChantTunerScreen extends AbstractContainerScreen<ChantTunerMenu> {
    private final static HashMap<String, Object> guistate = ChantTunerMenu.guistate;
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    EditBox a;

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

    private static final ResourceLocation texture = new ResourceLocation("a:textures/screens/chant_tuner.png");

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        a.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        guiGraphics.blit(new ResourceLocation("magic_chants:textures/screens/chant_tuner.png"), this.leftPos, this.topPos + 41, 0, 0, 390, 240, 390, 240);

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
    }
}
