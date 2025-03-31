package cz.yorick.mixin;

import cz.yorick.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    //entity cannot apply a negative status effect to its teammate (other shadow/owner)
    @Inject(method = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void necromancers_shadow$addStatusEffect(StatusEffectInstance effect, @Nullable Entity source, CallbackInfoReturnable<Boolean> cir) {
        if(effect.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL && !Util.canHurt(source, this)) {
            cir.setReturnValue(false);
        }
    }
}
