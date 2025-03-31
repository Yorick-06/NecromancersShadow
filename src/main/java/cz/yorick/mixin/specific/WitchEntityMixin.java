package cz.yorick.mixin.specific;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cz.yorick.mixin.MobEntityMixin;
import cz.yorick.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(WitchEntity.class)
public abstract class WitchEntityMixin extends MobEntityMixin {
    protected WitchEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapOperation(method = "shootAt", constant = @Constant(classValue = RaiderEntity.class))
    private boolean necromancers_shadow$isTargetFriendly(Object target, Operation<Boolean> original) {
        if(!Util.isShadow(this)) {
            return original.call(target);
        }

        return !Util.canHurt(this, (Entity)target);
    }
}
