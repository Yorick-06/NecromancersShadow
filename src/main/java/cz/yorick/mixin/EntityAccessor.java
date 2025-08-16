package cz.yorick.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker("readCustomData")
    void invokeReadCustomData(ReadView view);
    @Invoker("writeCustomData")
    void invokeWriteCustomData(WriteView view);
}
