package cz.yorick;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import cz.yorick.data.DataAttachments;
import cz.yorick.mixin.client.RenderTypeAccessor;
import cz.yorick.mixin.client.RenderSetupAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import java.util.Optional;
import java.util.function.Function;

public class ShadowRenderHelper {
    public static RenderStateDataKey<Boolean> IS_SHADOW_RENDER_STATE_DATA = RenderStateDataKey.create(() -> "isShadow");

    public static <S> boolean shouldModifyTint(S renderState) {
        if (renderState instanceof FabricRenderState fabricRenderState) {
            Boolean isShadow = fabricRenderState.getData(IS_SHADOW_RENDER_STATE_DATA);
            return isShadow != null && isShadow;
        }

        return false;
    }

    public static int modifyTint(int original) {
        return ARGB.color(ARGB.alphaFloat(original)/2F, original);
    }

    public static RenderType ensureTransparentLayer(RenderType original) {
        if(original.sortOnUpload()) {
            return original;
        }

        return TRANSPARENT_CONVERTER.apply(original);
    }

    public static void updateRenderState(LivingEntity entity, LivingEntityRenderState state) {
        if(DataAttachments.isMarkedAsShadow(entity)) {
            state.setData(IS_SHADOW_RENDER_STATE_DATA, true);
        }
    }

    //cached factory (the same as minecraft uses in RenderLayer) which converts non-transparent to transparent layers
    private static final Function<RenderType, RenderType> TRANSPARENT_CONVERTER = Util.memoize(originalLayer -> {
        RenderTypeAccessor accessor = (RenderTypeAccessor)originalLayer;
        return RenderType.create(accessor.getName() + "_translucent_generated", copyAsTranslucent(accessor.getState()));
    });

    private static RenderSetup copyAsTranslucent(RenderSetup original) {
        RenderSetupAccessor accessor = (RenderSetupAccessor)(Object)original;
        return RenderSetupAccessor.invokeConstructor(
                new TransparentRenderPipeline(accessor.getPipeline()),
                accessor.getTextureBindings(),
                accessor.getUseLightmap(),
                accessor.getUseOverlay(),
                accessor.getLayeringTransform(),
                accessor.getOutputTarget(),
                accessor.getTextureTransform(),
                accessor.getOutlineProperty(),
                accessor.getAffectsCrumbling(),
                //always translucent
                true,
                accessor.getBufferSize()
        );
    }

    //a simple delegated pipeline which makes the BlendFunction transparent
    private static class TransparentRenderPipeline extends RenderPipeline {

        protected TransparentRenderPipeline(RenderPipeline delegate) {
            super(
                    delegate.getLocation(),
                    delegate.getVertexShader(),
                    delegate.getFragmentShader(),
                    delegate.getShaderDefines(),
                    delegate.getSamplers(),
                    delegate.getUniforms(),
                    Optional.of(BlendFunction.TRANSLUCENT),
                    delegate.getDepthTestFunction(),
                    delegate.getPolygonMode(),
                    delegate.isCull(),
                    delegate.isWriteColor(),
                    delegate.isWriteAlpha(),
                    delegate.isWriteDepth(),
                    delegate.getColorLogic(),
                    delegate.getVertexFormat(),
                    delegate.getVertexFormatMode(),
                    delegate.getDepthBiasScaleFactor(),
                    delegate.getDepthBiasConstant(),
                    delegate.getSortKey()
            );
        }
    }
}
