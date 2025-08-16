package cz.yorick.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import cz.yorick.NecromancersShadow;
import cz.yorick.mixin.client.RenderLayerMultiPhaseAccessor;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Util;

import java.util.Optional;
import java.util.function.Function;

public record ShadowVertexConsumerProvider(VertexConsumerProvider delegate) implements VertexConsumerProvider {
    //supplies a shadow vertex consumer (makes the color transparent) and replaces non-transparent layers
    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        if(!layer.isTranslucent()) {
            return new ShadowVertexConsumer(this.delegate.getBuffer(TRANSPARENT_CONVERTER.apply(layer)));
        }
        return new ShadowVertexConsumer(this.delegate.getBuffer(layer));
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
