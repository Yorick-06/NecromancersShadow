package cz.yorick;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import cz.yorick.data.DataAttachments;
import cz.yorick.mixin.client.RenderLayerMultiPhaseAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;

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
        return ColorHelper.withAlpha(ColorHelper.getAlphaFloat(original)/2F, original);
    }

    public static RenderLayer ensureTransparentLayer(RenderLayer original) {
        if(original.isTranslucent()) {
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
    private static final Function<RenderLayer, RenderLayer> TRANSPARENT_CONVERTER = Util.memoize(originalLayer -> {
        if(originalLayer instanceof RenderLayerMultiPhaseAccessor multiPhase) {
            return RenderLayer.of(originalLayer.getName() + "_translucent_generated", originalLayer.getExpectedBufferSize(), originalLayer.hasCrumbling(), true, new TransparentRenderPipeline(multiPhase.getPipeline()), multiPhase.getPhases());
        }

        NecromancersShadow.LOGGER.error("Found non-multiphase layer, it will not get rendered as transparent!");
        return originalLayer;
    });

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
