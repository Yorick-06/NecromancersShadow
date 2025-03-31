package cz.yorick.mixin;

import cz.yorick.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectUtil.class)
public class StatusEffectUtilMixin {
    //an entity cannot apply a negative effect to its teammate - mixin in LivingEntity is not enough because of the elder
    //guardian (does not check if the effect was applied & sends the screen effect & sound anyway)
    @Inject(method = "Lnet/minecraft/entity/effect/StatusEffectUtil;method_42145(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;DLnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/effect/StatusEffectInstance;ILnet/minecraft/server/network/ServerPlayerEntity;)Z", at = @At("HEAD"), cancellable = true)
    private static void necromancers_shadow$targetValidationLambda(Entity source, Vec3d position, double range, RegistryEntry<StatusEffect> effectType, StatusEffectInstance effect, int duration, ServerPlayerEntity target, CallbackInfoReturnable<Boolean> cir) {
        if(effectType.value().getCategory() == StatusEffectCategory.HARMFUL && !Util.canHurt(source, target)) {
            cir.setReturnValue(false);
        }
    }
}
