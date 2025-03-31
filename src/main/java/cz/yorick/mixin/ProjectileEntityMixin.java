package cz.yorick.mixin;

import cz.yorick.data.NecromancyAttachments;
import cz.yorick.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
    private Entity owner;

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void necromancers_shadow$setOwner(@Nullable Entity owner, CallbackInfo info) {
        //when changing owner, check if the projectile should be a shadow
        NecromancyAttachments.markAsShadow(this, Util.isShadow(this.owner));
    }
}
