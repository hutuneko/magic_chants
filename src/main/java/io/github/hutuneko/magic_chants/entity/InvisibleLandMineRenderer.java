package io.github.hutuneko.magic_chants.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jspecify.annotations.NonNull;

public class InvisibleLandMineRenderer extends EntityRenderer<LandMineEntity,EntityRenderState> {

    public InvisibleLandMineRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NonNull EntityRenderState createRenderState() {
        return null;
    }
}