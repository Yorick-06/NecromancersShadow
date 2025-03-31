package cz.yorick.imixin;

import net.minecraft.entity.LivingEntity;

public interface IServerPlayerEntityMixin {
    LivingEntity necromancers_shadow$getTarget();
    void necromancers_shadow$setTarget(LivingEntity target);
}
