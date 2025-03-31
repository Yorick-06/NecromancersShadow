package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.NecromancersShadow;
import cz.yorick.util.ShadowDamageSource;
import cz.yorick.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSources.class)
public class DamageSourcesMixin {
    //source -> the entity which inflicted the damage
    //attacker -> the entity which is responsible for the source
    @WrapMethod(method = "Lnet/minecraft/entity/damage/DamageSources;create(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/entity/Entity;)Lnet/minecraft/entity/damage/DamageSource;")
    private DamageSource necromancers_shadow$create(RegistryKey<DamageType> key, @Nullable Entity attacker, Operation<DamageSource> original) {
        //check if the entity which dealt damage is a shadow
        ServerPlayerEntity shadowOwner = Util.getShadowOwner(attacker);
        if(shadowOwner != null) {
            return shadowDamage(attacker, shadowOwner);
        }

        //always convert shadow damage types to the custom damage source
        if(key.equals(this.shadow)) {
            return shadowDamage(null, attacker);
        }

        return original.call(key, attacker);
    }

    @WrapMethod(method = "Lnet/minecraft/entity/damage/DamageSources;create(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;)Lnet/minecraft/entity/damage/DamageSource;")
    private DamageSource necromancers_shadow$create(RegistryKey<DamageType> key, @Nullable Entity source, @Nullable Entity attacker, Operation<DamageSource> original) {
        //check if the entity which is responsible for the source is a shadow and swap the responsible entity for the player
        ServerPlayerEntity shadowOwner = Util.getShadowOwner(attacker);
        if(shadowOwner != null) {
            return shadowDamage(attacker, shadowOwner);
        }

        //always convert shadow damage types to the custom damage source
        if(key.equals(this.shadow)) {
            return shadowDamage(null, attacker);
        }

        return original.call(key, source, attacker);
    }

    @Shadow
    @Final
    private Registry<DamageType> registry;
    @Unique
    private final RegistryKey<DamageType> shadow = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(NecromancersShadow.MOD_ID, "shadow"));
    @Unique
    private DamageSource shadowDamage(Entity shadow, Entity necromancer) {
        return new ShadowDamageSource(this.registry.getOrThrow(this.shadow), shadow, necromancer);
    }
}
