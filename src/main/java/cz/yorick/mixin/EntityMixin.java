package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.data.NecromancyAttachments;
import cz.yorick.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public class EntityMixin {
    @WrapMethod(method = "isAlwaysInvulnerableTo")
    private boolean necromancers_shadow$isAlwaysInvulnerableTo(DamageSource damageSource, Operation<Boolean> original) {
        return original.call(damageSource) || !Util.canHurt(damageSource.getAttacker(), (Entity)(Object)this);
    }

    @WrapMethod(method = "shouldSave")
    private boolean necromancers_shadow$shouldSave(Operation<Boolean> original) {
        return original.call() && !NecromancyAttachments.isMarkedAsShadow((Entity)(Object)this);
    }
}
