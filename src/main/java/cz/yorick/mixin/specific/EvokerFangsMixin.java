package cz.yorick.mixin.specific;

import cz.yorick.data.DataAttachments;
import cz.yorick.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EvokerFangs.class)
public abstract class EvokerFangsMixin extends Entity {
    public EvokerFangsMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    private EntityReference<LivingEntity> owner;

    @Inject(method = "setOwner", at = @At("TAIL"))
    public void necromancers_shadow$setOwner(@Nullable LivingEntity owner, CallbackInfo info) {
        //when changing owner, check if the fang should be a shadow
        DataAttachments.markAsShadow(this, Util.isShadow(EntityReference.getLivingEntity(this.owner, this.level())));
    }
}
