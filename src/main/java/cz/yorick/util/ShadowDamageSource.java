package cz.yorick.util;

import cz.yorick.NecromancersShadow;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class ShadowDamageSource extends DamageSource {
    public static final String GENERIC_DEATH = "death." + NecromancersShadow.MOD_ID + ".shadow.generic";
    public static final String KILLED_BY_PLAYER = "death." + NecromancersShadow.MOD_ID + ".shadow.killed_by";
    public static final String KILLED_BY_PLAYER_SHADOW = "death." + NecromancersShadow.MOD_ID + ".shadow.player_shadow";
    public ShadowDamageSource(Holder<DamageType> type, @Nullable Entity source, @Nullable Entity attacker) {
        super(type, source, attacker);
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity killed) {
        if(this.getEntity() != null && this.getDirectEntity() != null) {
            return Component.translatable(KILLED_BY_PLAYER_SHADOW, killed.getDisplayName(), this.getEntity().getDisplayName(), this.getDirectEntity().getDisplayName());
        }

        if(this.getEntity() != null) {
            return Component.translatable(KILLED_BY_PLAYER, killed.getDisplayName(), this.getEntity().getDisplayName());
        }

        return Component.translatable(GENERIC_DEATH, killed.getDisplayName());
    }
}
