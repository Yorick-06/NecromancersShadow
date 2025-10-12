package cz.yorick.mixin.specific;

import cz.yorick.data.NecromancyAttachments;
import cz.yorick.data.ShadowData;
import cz.yorick.mixin.MobEntityMixin;
import cz.yorick.util.Util;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VexEntity.class)
public abstract class VexEntityMixin extends MobEntityMixin {
    protected VexEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    //does not actually represent if the entity is alive, but if it loses life ticks
    @Shadow
    private boolean alive;

    @Override
    public void necromancers_shadow$setShadow(ShadowData.Instance shadowInstance) {
        super.necromancers_shadow$setShadow(shadowInstance);
        this.alive = false;
    }

    @Shadow
    private LazyEntityReference<MobEntity> owner;

    @Inject(method = "setOwner", at = @At("TAIL"))
    public void setOwner(MobEntity owner, CallbackInfo info) {
        NecromancyAttachments.markAsShadow(this, Util.isShadow(LazyEntityReference.resolve(this.owner, this.getEntityWorld(), MobEntity.class)));
    }
}
