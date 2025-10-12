package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

//makes sure shadow entities do not drop loot
//since hostile entity overrides both shouldDropExperience and shouldDropLoot to true, the modification must be applied
//to HostileEntity too. While original.call() will normally always return true, checking for it ensures compatibility
//with other mods which might want to chain the checks
@Mixin(value = {LivingEntity.class, HostileEntity.class})
public abstract class LivingAndHostileEntityMixin extends Entity {
    protected LivingAndHostileEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapMethod(method = "shouldDropExperience")
    private boolean necromancers_shadow$shouldDropExperience(Operation<Boolean> original) {
        return !Util.isShadow(this) && original.call();
    }

    @WrapMethod(method = "shouldDropLoot")
    private boolean necromancers_shadow$shouldDropLoot(ServerWorld world, Operation<Boolean> original) {
        return !Util.isShadow(this) && original.call(world);
    }
}
