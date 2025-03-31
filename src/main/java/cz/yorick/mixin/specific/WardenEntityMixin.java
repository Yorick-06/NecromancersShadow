package cz.yorick.mixin.specific;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.mixin.MobEntityMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

//warden does not use the normal UpdateAttackTargetTask, but relies on those two methods
@Mixin(WardenEntity.class)
public abstract class WardenEntityMixin extends MobEntityMixin {
    protected WardenEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapMethod(method = "isValidTarget")
    private boolean necromancers_shadow$isValidTarget(@Nullable Entity entity, Operation<Boolean> original) {
        if(necromancers_shadow$$getShadowInstance() != null) {
            return entity instanceof LivingEntity;
        }

        return original.call(entity);
    }

    @WrapMethod(method = "getPrimeSuspect")
    private Optional<LivingEntity> necromancers_shadow$getPrimeSuspect(Operation<Optional<LivingEntity>> original) {
        if(necromancers_shadow$$getShadowInstance() != null) {
            return Optional.ofNullable(getTarget());
        }

        return original.call();
    }
}
