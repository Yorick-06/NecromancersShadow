package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.util.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

//makes sure shadow entities do not drop loot
//since hostile entity overrides both shouldDropExperience and shouldDropLoot to true, the modification must be applied
//to HostileEntity too. While original.call() will normally always return true, checking for it ensures compatibility
//with other mods which might want to chain the checks
@Mixin(value = {LivingEntity.class, Monster.class})
public abstract class LivingEntityAndMonsterMixin extends Entity {
    protected LivingEntityAndMonsterMixin(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    @WrapMethod(method = "shouldDropExperience")
    private boolean necromancers_shadow$shouldDropExperience(Operation<Boolean> original) {
        return !Util.isShadow(this) && original.call();
    }

    @WrapMethod(method = "shouldDropLoot")
    private boolean necromancers_shadow$shouldDropLoot(ServerLevel world, Operation<Boolean> original) {
        return !Util.isShadow(this) && original.call(world);
    }
}
