package cz.yorick.mixin.specific;

import cz.yorick.data.DataAttachments;
import cz.yorick.data.ShadowData;
import cz.yorick.mixin.MobMixin;
import cz.yorick.util.Util;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Vex.class)
public abstract class VexMixin extends MobMixin {
    protected VexMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    //does not actually represent if the entity is alive, but if it loses life ticks
    @Shadow
    private boolean hasLimitedLife;

    @Override
    public void necromancers_shadow$setShadow(ShadowData.Instance shadowInstance) {
        super.necromancers_shadow$setShadow(shadowInstance);
        this.hasLimitedLife = false;
    }

    @Shadow
    private EntityReference<Mob> owner;

    @Inject(method = "setOwner", at = @At("TAIL"))
    public void setOwner(Mob owner, CallbackInfo info) {
        DataAttachments.markAsShadow(this, Util.isShadow(EntityReference.get(this.owner, this.level(), Mob.class)));
    }
}
