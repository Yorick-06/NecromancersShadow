package cz.yorick.mixin.specific;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cz.yorick.mixin.MobMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin extends MobMixin {
    protected WitherBossMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    //stops the wither from randomly shooting nearby entities
    @WrapOperation(method = "customServerAiStep",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/server/level/ServerLevel;getNearbyEntities(Ljava/lang/Class;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
            )
    )
    private List<LivingEntity> necromancers_shadow$modifyHeadTargets(ServerLevel instance,Class<LivingEntity> clazz, TargetingConditions targetPredicate, LivingEntity entity, AABB box, Operation<List<LivingEntity>> original) {
        if(necromancers_shadow$$getShadowInstance() != null) {
            LivingEntity ownerTarget = necromancers_shadow$$getShadowInstance().getTarget();
            return ownerTarget != null ? List.of(ownerTarget) : List.of();
        }

        return original.call(instance, clazz, targetPredicate, entity, box);
    }

    //stops the wither from randomly shooting charged skulls, if there is no target the wither won't shoot at all,
    //if there is a target the skulls will get aimed at the target
    @WrapOperation(method = "customServerAiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;performRangedAttack(IDDDZ)V"
            )
    )
    private void necromancers_shadow$shootSkullAt(WitherBoss instance, int headIndex, double targetX, double targetY, double targetZ, boolean charged, Operation<Void> original) {
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
