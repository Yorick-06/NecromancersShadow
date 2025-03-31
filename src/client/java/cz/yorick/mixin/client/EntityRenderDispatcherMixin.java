package cz.yorick.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.data.NecromancyAttachments;
import cz.yorick.render.ShadowVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @WrapMethod(method = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V")
    private <E extends Entity, S extends EntityRenderState> void necromancers_shadow$render(E entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityRenderer<? super E, S> renderer, Operation<Void> original) {
        //if the rendered entity is a shadow, wrap the vertex consumer
        original.call(entity, x, y, z, tickDelta, matrices, NecromancyAttachments.isMarkedAsShadow(entity) ? new ShadowVertexConsumerProvider(vertexConsumers) : vertexConsumers, light, renderer);
    }
}
