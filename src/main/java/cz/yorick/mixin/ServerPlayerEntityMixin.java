package cz.yorick.mixin;

import cz.yorick.imixin.IServerPlayerEntityMixin;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements IServerPlayerEntityMixin {
    @Unique
    private LivingEntity target = null;

    @Override
    public LivingEntity necromancers_shadow$getTarget() {
        return this.target;
    }

    @Override
    public void necromancers_shadow$setTarget(LivingEntity target) {
        this.target = target;
    }
}
