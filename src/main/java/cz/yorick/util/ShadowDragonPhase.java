package cz.yorick.util;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.ShadowData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ShadowDragonPhase extends AbstractDragonPhaseInstance {
    public ShadowDragonPhase(EnderDragon dragon) {
        super(dragon);
    }

    @Nullable
    @Override
    public Vec3 getFlyTargetLocation() {
        LivingEntity target = this.dragon.getTarget();
        if(target != null && target.isAlive()) {
            return target.position();
        }

        ShadowData.Instance shadowInstance = Util.getShadowInstance(this.dragon);
        if(shadowInstance != null) {
            return shadowInstance.owner().position().add(0, 5, 0);
        }

        return null;
    }

    @Override
    public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() {
        return NecromancersShadow.SHADOW_DRAGON_PHASE;
    }

    @Override
    public float getFlySpeed() {
        return 10;
    }
}
