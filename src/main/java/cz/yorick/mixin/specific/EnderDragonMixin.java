package cz.yorick.mixin.specific;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.ShadowData;
import cz.yorick.mixin.MobMixin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin extends MobMixin {
    protected EnderDragonMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow
    @Final
    private EnderDragonPhaseManager phaseManager;

    @Override
    public void necromancers_shadow$setShadow(ShadowData.Instance shadowInstance) {
        super.necromancers_shadow$setShadow(shadowInstance);
        this.phaseManager.setPhase(NecromancersShadow.SHADOW_DRAGON_PHASE);
    }
}
