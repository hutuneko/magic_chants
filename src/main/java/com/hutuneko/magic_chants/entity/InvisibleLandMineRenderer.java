package com.hutuneko.magic_chants.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class InvisibleLandMineRenderer extends EntityRenderer<LandMineEntity> {

    public InvisibleLandMineRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // 描画処理のオーバーライド
    @Override
    public void render(LandMineEntity entity, float yaw, float partialTicks, @NotNull PoseStack poseStack, net.minecraft.client.renderer.@NotNull MultiBufferSource buffer, int packedLight) {
        entity.isInvisible();// 透明な場合は何もしない
    }

    // テクスチャの場所を返すメソッド（レンダリングしないので、基本的にはnullやダミーでOK）
    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull LandMineEntity entity) {
        return null;
    }
}