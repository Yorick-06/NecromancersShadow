package cz.yorick.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(RenderSetup.class)
public interface RenderSetupAccessor {
    @Accessor
    RenderPipeline getPipeline();
    @Accessor("textures")
    Map<String, Object/*RenderSetup.TextureBinding*/> getTextureBindings();
    @Accessor
    TextureTransform getTextureTransform();
    @Accessor
    OutputTarget getOutputTarget();
    @Accessor
    RenderSetup.OutlineProperty getOutlineProperty();
    @Accessor
    boolean getUseLightmap();
    @Accessor
    boolean getUseOverlay();
    @Accessor
    boolean getAffectsCrumbling();
    @Accessor
    boolean getSortOnUpload();
    @Accessor
    int getBufferSize();
    @Accessor
    LayeringTransform getLayeringTransform();

    @Invoker("<init>")
    static RenderSetup invokeConstructor(RenderPipeline pipeline, Map<String, Object/*RenderSetup.TextureBinding*/> textures, boolean useLightmap, boolean useOverlay, LayeringTransform layeringTransform, OutputTarget outputTarget, TextureTransform textureTransform, RenderSetup.OutlineProperty outlineMode, boolean hasCrumbling, boolean translucent, int expectedBufferSize) {
        throw new UnsupportedOperationException("Implemented via mixin!");
    }
}
