package cz.yorick.mixin.specific;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cz.yorick.mixin.MobEntityMixin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(WitherEntity.class)
public abstract class WitherEntityMixin extends MobEntityMixin {
    protected WitherEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    //stops the wither from randomly shooting nearby entities
    @WrapOperation(method = "mobTick",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/server/world/ServerWorld;getTargets(Ljava/lang/Class;Lnet/minecraft/entity/ai/TargetPredicate;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/math/Box;)Ljava/util/List;"
            )
    )
    private List<LivingEntity> necromancers_shadow$modifyHeadTargets(ServerWorld instance,Class<LivingEntity> clazz, TargetPredicate targetPredicate, LivingEntity entity, Box box, Operation<List<LivingEntity>> original) {
        if(necromancers_shadow$$getShadowInstance() != null) {
            LivingEntity ownerTarget = necromancers_shadow$$getShadowInstance().getTarget();
            return ownerTarget != null ? List.of(ownerTarget) : List.of();
        }

        return original.call(instance, clazz, targetPredicate, entity, box);
    }

    //stops the wither from randomly shooting charged skulls, if there is no target the wither won't shoot at all,
    //if there is a target the skulls will get aimed at the target
    @WrapOperation(method = "mobTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/boss/WitherEntity;shootSkullAt(IDDDZ)V"
            )
    )
    private void necromancers_shadow$shootSkullAt(WitherEntity instance, int headIndex, double targetX, double targetY, double targetZ, boolean charged, Operation<Void> original) {
        if(necromancers_shadow$$getShadowInstance() != null) {
            LivingEntity target = necromancers_shadow$$getShadowInstance().getTarget();
            if(target == null) {
                return;
            }

            original.call(instance, headIndex, target.getX(), target.getY(), target.getZ(), charged);
            return;
        }

        original.call(instance, headIndex, targetX, targetY, targetZ, charged);
    }
}
