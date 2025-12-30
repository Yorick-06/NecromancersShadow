package cz.yorick.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.render.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(RenderSetup.class)
public interface RenderSetupAccessor {
    @Accessor
    RenderPipeline getPipeline();
    @Accessor
    Map<String, Object/*RenderSetup.TextureSpec*/> getTextures();
    @Accessor
    TextureTransform getTextureTransform();
    @Accessor
    OutputTarget getOutputTarget();
    @Accessor
    RenderSetup.OutlineMode getOutlineMode();
    @Accessor
    boolean getUseLightmap();
    @Accessor
    boolean getUseOverlay();
    @Accessor
    boolean getHasCrumbling();
    @Accessor
    boolean getTranslucent();
    @Accessor
    int getExpectedBufferSize();
    @Accessor
    LayeringTransform getLayeringTransform();

    @Invoker("<init>")
    static RenderSetup invokeConstructor(RenderPipeline pipeline, Map<String, Object/*RenderSetup.TextureSpec*/> textures, boolean useLightmap, boolean useOverlay, LayeringTransform layeringTransform, OutputTarget outputTarget, TextureTransform textureTransform, RenderSetup.OutlineMode outlineMode, boolean hasCrumbling, boolean translucent, int expectedBufferSize) {
        throw new UnsupportedOperationException("Implemented via mixin!");
    }
}
