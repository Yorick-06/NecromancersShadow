package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.data.NecromancyAttachments;
import cz.yorick.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {
    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    private LazyEntityReference<Entity> owner;

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void necromancers_shadow$setOwner(@Nullable LazyEntityReference<Entity> owner, CallbackInfo info) {
        //when changing owner, check if the projectile should be a shadow
        NecromancyAttachments.markAsShadow(this, Util.isShadow(this.owner.resolve(getWorld(), Entity.class)));
    }

    //shadow projectile cannot hit a friendly shadow/owner
    @WrapMethod(method = "canHit")
    protected boolean necromancers_shadow$canHit(Entity entity, Operation<Boolean> original) {
        return original.call(entity) && Util.canHurt(this, entity);
    }
}
