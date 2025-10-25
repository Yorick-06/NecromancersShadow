package cz.yorick.mixin;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ShadowData;
import cz.yorick.imixin.IMobEntityMixin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
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

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements IMobEntityMixin {
	protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Unique
	private ShadowData.Instance shadowInstance = null;
	@Unique
	private static final Supplier<Task<MobEntity>> SIMPLE_TARGET_TASK_FACTORY = () -> UpdateAttackTargetTask.create((world, entity) -> Optional.ofNullable(entity.getTarget()));

	@Unique
	private static final Function<ServerPlayerEntity, Task<MobEntity>> SIMPLE_FORGET_TARGET_TASK_FACTORY = owner -> ForgetAttackTargetTask.create((world, target) -> target != owner.getAttacking(), (world, entity, target) -> {}, false);

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
		this.targetSelector.clear(goal -> true);
		//entity should no longer flee
		this.goalSelector.clear(goal -> goal instanceof FleeEntityGoal<?>);
		modifyBrain();
	}

	@Unique
	private void modifyBrain() {
		Map<Integer, Map<Activity, Set<Task<? extends LivingEntity>>>> brainTasks = ((BrainAccessor)getBrain()).getTasks();
		//if there are no activities, the entity probably uses the goal system
		if(brainTasks.isEmpty()) {
			return;
		}

		//copies the brain structure, replacing UpdateAttackTarget and ForgetAttackTarget tasks

		//the main map in Brain is created with Maps.newTreeMap()
		Map<Integer, Map<Activity, Set<Task<? extends LivingEntity>>>> newBrainTasks = Maps.newTreeMap();
		for (Map.Entry<Integer, Map<Activity, Set<Task<? extends LivingEntity>>>> mainEntry : brainTasks.entrySet()) {
			//inner maps are created with Maps.newHashMap();
			Map<Activity, Set<Task<? extends LivingEntity>>> newActivities = Maps.newHashMap();
			for (Map.Entry<Activity, Set<Task<? extends LivingEntity>>> activityEntry : mainEntry.getValue().entrySet()) {
				//sets are created with Sets.newLinkedHashSet();
				Set<Task<? extends LivingEntity>> newTasks = Sets.newLinkedHashSet();
				for (Task<? extends LivingEntity> task : activityEntry.getValue()) {
					//i really don't like this comparing by name but cannot find a better way since the tasks are lambdas
					//and not their own classes

					//replace the update attack target task to return the owners target
					if(task.getName().contains(UpdateAttackTargetTask.class.getName())) {
						newTasks.add(SIMPLE_TARGET_TASK_FACTORY.get());
						continue;
					}

					//remove the forget target task
					if(task.getName().contains(ForgetAttackTargetTask.class.getName())) {
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
	@Inject(method = {"getTarget", "getTargetInBrain"}, at = @At("HEAD"), cancellable = true)
	private void necromancers_shadow$getOwnerTarget(CallbackInfoReturnable<LivingEntity> cir) {
		if(this.shadowInstance != null) {
			cir.setReturnValue(this.shadowInstance.getTarget());
			//if this entity is an ownable, check the owner
		} else if(DataAttachments.isMarkedAsShadow(this) && this instanceof Ownable ownable && ownable.getOwner() instanceof MobEntity mobEntity) {
			cir.setReturnValue(mobEntity.getTarget());
		}
	}
}