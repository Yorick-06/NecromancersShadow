package cz.yorick.mixin;

import cz.yorick.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectUtil.class)
public class StatusEffectUtilMixin {
    //an entity cannot apply a negative effect to its teammate - mixin in LivingEntity is not enough because of the elder
    //guardian (does not check if the effect was applied & sends the screen effect & sound anyway)
    @Inject(method = "lambda$addEffectToPlayersAround$0(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;DLnet/minecraft/core/Holder;Lnet/minecraft/world/effect/MobEffectInstance;ILnet/minecraft/server/level/ServerPlayer;)Z", at = @At("HEAD"), cancellable = true)
    private static void necromancers_shadow$targetValidationLambda(Entity source, Vec3 position, double range, Holder<MobEffect> effectType, MobEffectInstance effect, int duration, ServerPlayer target, CallbackInfoReturnable<Boolean> cir) {
        if(effectType.value().getCategory() == MobEffectCategory.HARMFUL && !Util.canHurt(source, target)) {
            cir.setReturnValue(false);
        }
    }
}
