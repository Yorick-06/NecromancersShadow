package cz.yorick.util;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.ShadowData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ShadowDragonPhase extends AbstractPhase {
    public ShadowDragonPhase(EnderDragonEntity dragon) {
        super(dragon);
    }

    @Nullable
    @Override
    public Vec3d getPathTarget() {
        LivingEntity target = this.dragon.getTarget();
        if(target != null && target.isAlive()) {
            return target.getEntityPos();
        }

        ShadowData.Instance shadowInstance = Util.getShadowInstance(this.dragon);
        if(shadowInstance != null) {
            return shadowInstance.owner().getEntityPos().add(0, 5, 0);
        }

        return null;
    }

    @Override
    public PhaseType<? extends Phase> getType() {
        return NecromancersShadow.SHADOW_DRAGON_PHASE;
    }

    @Override
    public float getMaxYAcceleration() {
        return 10;
    }
}
