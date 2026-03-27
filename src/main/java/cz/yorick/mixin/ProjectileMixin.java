package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.data.DataAttachments;
import cz.yorick.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity {
    public ProjectileMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    private EntityReference<Entity> owner;

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void necromancers_shadow$setOwner(@Nullable EntityReference<Entity> owner, CallbackInfo info) {
        //when changing owner, check if the projectile should be a shadow
        DataAttachments.markAsShadow(this, Util.isShadow(EntityReference.getEntity(this.owner, this.level())));
    }

    //shadow projectile cannot hit a friendly shadow/owner
    @WrapMethod(method = "canHitEntity")
    protected boolean necromancers_shadow$canHitEntity(Entity entity, Operation<Boolean> original) {
        return original.call(entity) && Util.canHurt(this, entity);
    }
}
