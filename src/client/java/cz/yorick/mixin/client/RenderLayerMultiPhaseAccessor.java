package cz.yorick.mixin.client;

import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderLayer.MultiPhase.class)
public interface RenderLayerMultiPhaseAccessor {
    @Accessor
    RenderLayer.MultiPhaseParameters getPhases();
}
