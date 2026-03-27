package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.data.DataAttachments;
import cz.yorick.util.Util;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public class EntityMixin {
    @WrapMethod(method = "isInvulnerableToBase")
    private boolean necromancers_shadow$isInvulnerableToBase(DamageSource damageSource, Operation<Boolean> original) {
        return original.call(damageSource) || !Util.canHurt(damageSource.getEntity(), (Entity)(Object)this);
    }

    @WrapMethod(method = "shouldBeSaved")
    private boolean necromancers_shadow$shouldBeSaved(Operation<Boolean> original) {
        return original.call() && !DataAttachments.isMarkedAsShadow((Entity)(Object)this);
    }
}
