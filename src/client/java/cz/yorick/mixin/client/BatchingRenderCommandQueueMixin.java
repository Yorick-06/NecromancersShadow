package cz.yorick.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.ShadowRenderHelper;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BatchingRenderCommandQueue.class)
public class BatchingRenderCommandQueueMixin {

    @WrapMethod(method = "submitModel")
    public <S> void submitModel(Model<? super S> model, S state, MatrixStack matrices, RenderLayer renderLayer, int light, int overlay, int tintedColor, @Nullable Sprite sprite, int outlineColor, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay, Operation<Void> original) {
        //make the layer transparent and modify the tint (lower alpha) if the entity is a shadow
        if(ShadowRenderHelper.shouldModifyTint(state)) {
            renderLayer = ShadowRenderHelper.ensureTransparentLayer(renderLayer);
            tintedColor = ShadowRenderHelper.modifyTint(tintedColor);
        }

        original.call(model, state, matrices, renderLayer, light, overlay, tintedColor, sprite, outlineColor, crumblingOverlay);
    }
}
