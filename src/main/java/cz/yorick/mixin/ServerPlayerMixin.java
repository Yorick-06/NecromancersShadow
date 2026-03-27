package cz.yorick.mixin;

import cz.yorick.imixin.IServerPlayerEntityMixin;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements IServerPlayerEntityMixin {
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
