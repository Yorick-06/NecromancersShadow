package cz.yorick.mixin.specific;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cz.yorick.mixin.MobMixin;
import cz.yorick.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(Witch.class)
public abstract class WitchMixin extends MobMixin {
    protected WitchMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @WrapOperation(method = "performRangedAttack", constant = @Constant(classValue = Raider.class))
    private boolean necromancers_shadow$isTargetFriendly(Object target, Operation<Boolean> original) {
        if(!Util.isShadow(this)) {
            return original.call(target);
        }

        return !Util.canHurt(this, (Entity)target);
    }
}
