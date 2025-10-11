package com.hutuneko.magic_chants.api.block.gui;

import com.hutuneko.magic_chants.api.block.net.C2S_ApplyAliasesFromTuner;
import com.hutuneko.magic_chants.api.block.net.C2S_SaveAliasesFromTunerToItem;
import com.hutuneko.magic_chants.api.net.MagicNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ChantTunerScreen extends AbstractContainerScreen<ChantTunerMenu> {
    private EditBox area;

    public ChantTunerScreen(ChantTunerMenu m, Inventory inv, Component title) {
        super(m, inv, title);
        this.imageWidth = 248;
        this.imageHeight = 186; // 少し高さUP（ボタン3つ分）
    }

    @Override
    protected void init() {
        super.init();

        // テキストエリア（1行=1ルール: type|priority|from|to）
        area = new EditBox(this.font, leftPos + 8, topPos + 20, 232, 110, Component.empty());
        StringBuilder sb = new StringBuilder();
        for (var r : this.menu.snapshot) {
            sb.append(r.type()).append('|')
                    .append(r.priority()).append('|')
                    .append(r.from()).append('|')
                    .append(r.to()).append('\n');
        }
        area.setValue(sb.toString());
        addRenderableWidget(area);

        // 自分に適用
        addRenderableWidget(
                Button.builder(Component.literal("自分に適用"), b ->
                        MagicNetwork.CHANNEL.sendToServer(
                                new C2S_ApplyAliasesFromTuner(this.menu.pos, area.getValue(), false)
                        )
                ).bounds(leftPos + 8, topPos + 140, 110, 20).build()
        );

        // ブロックに保存
        addRenderableWidget(
                Button.builder(Component.literal("ブロックに保存"), b ->
                        MagicNetwork.CHANNEL.sendToServer(
                                new C2S_ApplyAliasesFromTuner(this.menu.pos, area.getValue(), true)
                        )
                ).bounds(leftPos + 130, topPos + 140, 110, 20).build()
        );

        // ★ アイテムに保存（BEスロット0のアイテムへ）
        addRenderableWidget(
                Button.builder(Component.literal("アイテムに保存"), b ->
                        MagicNetwork.CHANNEL.sendToServer(
                                new C2S_SaveAliasesFromTunerToItem(this.menu.pos, area.getValue())
                        )
                ).bounds(leftPos + 8, topPos + 164, 232, 20).build()
        );
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // テクスチャが無ければ無地でOK。用意していれば g.blit(...) で貼る
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, Component.literal("Chant Tuner"),
                leftPos + this.imageWidth / 2, topPos + 6, 0xFFFFFF);
        this.renderTooltip(g, mouseX, mouseY);
    }
}
