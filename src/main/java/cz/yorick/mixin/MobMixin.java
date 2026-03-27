package cz.yorick.mixin;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ShadowData;
import cz.yorick.imixin.IMobEntityMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements IMobEntityMixin {
	protected MobMixin(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}

	@Unique
	private ShadowData.Instance shadowInstance = null;
	@Unique
	private static final Supplier<BehaviorControl<Mob>> SIMPLE_TARGET_TASK_FACTORY = () -> StartAttacking.create((world, entity) -> Optional.ofNullable(entity.getTarget()));

	@Unique
	private static final Function<ServerPlayer, BehaviorControl<Mob>> SIMPLE_FORGET_TARGET_TASK_FACTORY = owner -> StopAttackingIfTargetInvalid.create((world, target) -> target != owner.getLastHurtMob(), (world, entity, target) -> {}, false);

	@Shadow
	@Final
	protected GoalSelector goalSelector;

	@Shadow
	@Final
	protected GoalSelector targetSelector;

	@Shadow
	public abstract LivingEntity getTarget();

	@Override
	public void necromancers_shadow$setShadow(ShadowData.Instance shadowInstance) {
		//set shadow instance & clear target selectors/modify brain
		this.shadowInstance = shadowInstance;
		this.targetSelector.removeAllGoals(goal -> true);
		//entity should no longer flee
		this.goalSelector.removeAllGoals(goal -> goal instanceof AvoidEntityGoal<?>);
		modifyBrain();
	}

	@Unique
	private void modifyBrain() {
		Map<Integer, Map<Activity, Set<BehaviorControl<? extends LivingEntity>>>> brainTasks = ((BrainAccessor)getBrain()).getAvailableBehaviorsByPriority();
		//if there are no activities, the entity probably uses the goal system
		if(brainTasks.isEmpty()) {
			return;
		}

		//copies the brain structure, replacing UpdateAttackTarget and ForgetAttackTarget tasks

		//the main map in Brain is created with Maps.newTreeMap()
		Map<Integer, Map<Activity, Set<BehaviorControl<? extends LivingEntity>>>> newBrainTasks = Maps.newTreeMap();
		for (Map.Entry<Integer, Map<Activity, Set<BehaviorControl<? extends LivingEntity>>>> mainEntry : brainTasks.entrySet()) {
			//inner maps are created with Maps.newHashMap();
			Map<Activity, Set<BehaviorControl<? extends LivingEntity>>> newActivities = Maps.newHashMap();
			for (Map.Entry<Activity, Set<BehaviorControl<? extends LivingEntity>>> activityEntry : mainEntry.getValue().entrySet()) {
				//sets are created with Sets.newLinkedHashSet();
				Set<BehaviorControl<? extends LivingEntity>> newTasks = Sets.newLinkedHashSet();
				for (BehaviorControl<? extends LivingEntity> task : activityEntry.getValue()) {
					//i really don't like this comparing by name but cannot find a better way since the tasks are lambdas
					//and not their own classes

					//replace the update attack target task to return the owners target
					if(task.debugString().contains(StartAttacking.class.getName())) {
						newTasks.add(SIMPLE_TARGET_TASK_FACTORY.get());
						continue;
					}

					//remove the forget target task
					if(task.debugString().contains(StopAttackingIfTargetInvalid.class.getName())) {
						newTasks.add(SIMPLE_FORGET_TARGET_TASK_FACTORY.apply(this.shadowInstance.owner()));
						continue;
					}

					newTasks.add(task);
				}

				newActivities.put(activityEntry.getKey(), newTasks);
			}

			newBrainTasks.put(mainEntry.getKey(), newActivities);
		}

		brainTasks.clear();
		brainTasks.putAll(newBrainTasks);
	}

	@Override
	public ShadowData.Instance necromancers_shadow$$getShadowInstance() {
		return this.shadowInstance;
	}

	//if the entity has an owner, return the owners target
	@Inject(method = {"getTarget", "getTargetFromBrain"}, at = @At("HEAD"), cancellable = true)
	private void necromancers_shadow$getOwnerTarget(CallbackInfoReturnable<LivingEntity> cir) {
		if(this.shadowInstance != null) {
			cir.setReturnValue(this.shadowInstance.getTarget());
			//if this entity is an ownable, check the owner
		} else if(DataAttachments.isMarkedAsShadow(this) && this instanceof TraceableEntity ownable && ownable.getOwner() instanceof Mob mobEntity) {
			cir.setReturnValue(mobEntity.getTarget());
		}
	}
}