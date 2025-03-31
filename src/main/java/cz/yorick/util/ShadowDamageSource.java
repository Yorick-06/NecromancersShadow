package cz.yorick.util;

import cz.yorick.NecromancersShadow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ShadowDamageSource extends DamageSource {
    public static final String GENERIC_DEATH = "death." + NecromancersShadow.MOD_ID + ".shadow.generic";
    public static final String KILLED_BY_PLAYER = "death." + NecromancersShadow.MOD_ID + ".shadow.killed_by";
    public static final String KILLED_BY_PLAYER_SHADOW = "death." + NecromancersShadow.MOD_ID + ".shadow.player_shadow";
    public ShadowDamageSource(RegistryEntry<DamageType> type, @Nullable Entity source, @Nullable Entity attacker) {
        super(type, source, attacker);
    }

    @Override
    public Text getDeathMessage(LivingEntity killed) {
        if(this.getAttacker() != null && this.getSource() != null) {
            return Text.translatable(KILLED_BY_PLAYER_SHADOW, killed.getDisplayName(), this.getAttacker().getDisplayName(), this.getSource().getDisplayName());
        }

        if(this.getAttacker() != null) {
            return Text.translatable(KILLED_BY_PLAYER, killed.getDisplayName(), this.getAttacker().getDisplayName());
        }

        return Text.translatable(GENERIC_DEATH, killed.getDisplayName());
    }
}
