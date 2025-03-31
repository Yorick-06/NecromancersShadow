package cz.yorick.mixin.specific;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.ShadowData;
import cz.yorick.mixin.MobEntityMixin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonMixin extends MobEntityMixin {
    protected EnderDragonMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    @Final
    private PhaseManager phaseManager;

    @Override
    public void necromancers_shadow$setShadow(ShadowData.Instance shadowInstance) {
        super.necromancers_shadow$setShadow(shadowInstance);
        this.phaseManager.setPhase(NecromancersShadow.SHADOW_DRAGON_PHASE);
    }
}
