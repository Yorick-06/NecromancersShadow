package cz.yorick.imixin;

import net.minecraft.world.entity.LivingEntity;

public interface IServerPlayerEntityMixin {
    LivingEntity necromancers_shadow$getTarget();
    void necromancers_shadow$setTarget(LivingEntity target);
}
