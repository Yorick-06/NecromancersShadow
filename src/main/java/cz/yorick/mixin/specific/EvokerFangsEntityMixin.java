package cz.yorick.mixin.specific;

import cz.yorick.data.NecromancyAttachments;
import cz.yorick.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EvokerFangsEntity.class)
public abstract class EvokerFangsEntityMixin extends Entity {
    public EvokerFangsEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    private LivingEntity owner;

    @Inject(method = "setOwner", at = @At("TAIL"))
    public void necromancers_shadow$setOwner(@Nullable LivingEntity owner, CallbackInfo info) {
        //when changing owner, check if the projectile should be a shadow
        NecromancyAttachments.markAsShadow(this, Util.isShadow(this.owner));
    }
}
