package cz.yorick.mixin.client;

import cz.yorick.ShadowRenderHelper;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    public void extractRenderState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo info) {
        ShadowRenderHelper.updateRenderState(livingEntity, livingEntityRenderState);
    }
}
