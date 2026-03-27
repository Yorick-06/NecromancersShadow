package cz.yorick.mixin.specific;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.mixin.MobMixin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;

//warden does not use the normal UpdateAttackTargetTask, but relies on those two methods
@Mixin(Warden.class)
public abstract class WardenMixin extends MobMixin {
    protected WardenMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @WrapMethod(method = "canTargetEntity")
    private boolean necromancers_shadow$canTargetEntity(@Nullable Entity entity, Operation<Boolean> original) {
        if(necromancers_shadow$$getShadowInstance() != null) {
            return entity instanceof LivingEntity;
        }

        return original.call(entity);
    }

    @WrapMethod(method = "getEntityAngryAt")
    private Optional<LivingEntity> necromancers_shadow$getEntityAngryAt(Operation<Optional<LivingEntity>> original) {
        if(necromancers_shadow$$getShadowInstance() != null) {
            return Optional.ofNullable(getTarget());
        }

        return original.call();
    }
}
