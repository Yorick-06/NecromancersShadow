package cz.yorick.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import cz.yorick.ShadowRenderHelper;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin {

    @WrapMethod(method = "submitModel")
    public <S> void submitModel(Model<? super S> model, S state, PoseStack matrices, RenderType renderLayer, int light, int overlay, int tintedColor, @Nullable TextureAtlasSprite sprite, int outlineColor, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, Operation<Void> original) {
        //make the layer transparent and modify the tint (lower alpha) if the entity is a shadow
        if(ShadowRenderHelper.shouldModifyTint(state)) {
            renderLayer = ShadowRenderHelper.ensureTransparentLayer(renderLayer);
            tintedColor = ShadowRenderHelper.modifyTint(tintedColor);
        }

        original.call(model, state, matrices, renderLayer, light, overlay, tintedColor, sprite, outlineColor, crumblingOverlay);
    }
}
