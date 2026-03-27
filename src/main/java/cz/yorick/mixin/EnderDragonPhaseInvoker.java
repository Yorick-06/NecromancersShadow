package cz.yorick.mixin;

import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EnderDragonPhase.class)
public interface EnderDragonPhaseInvoker {
    @Invoker
    static <T extends DragonPhaseInstance> EnderDragonPhase<T> invokeCreate(Class<T> class_, String string) {
        throw new UnsupportedOperationException("Implemented via mixin!");
    }
}
