package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.NecromancersShadow;
import cz.yorick.util.ShadowDamageSource;
import cz.yorick.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSources.class)
public class DamageSourcesMixin {
    //source -> the entity which inflicted the damage
    //attacker -> the entity which is responsible for the source
    @WrapMethod(method = "source(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/damagesource/DamageSource;")
    private DamageSource necromancers_shadow$source(ResourceKey<DamageType> key, @Nullable Entity attacker, Operation<DamageSource> original) {
        //check if the entity which dealt damage is a shadow
        ServerPlayer shadowOwner = Util.getShadowOwner(attacker);
        if(shadowOwner != null) {
            return shadowDamage(attacker, shadowOwner);
        }

        //always convert shadow damage types to the custom damage source
        if(key.equals(this.shadow)) {
            return shadowDamage(null, attacker);
        }

        return original.call(key, attacker);
    }

    @WrapMethod(method = "source(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/damagesource/DamageSource;")
    private DamageSource necromancers_shadow$source(ResourceKey<DamageType> key, @Nullable Entity source, @Nullable Entity attacker, Operation<DamageSource> original) {
        //check if the entity which is responsible for the source is a shadow and swap the responsible entity for the player
        ServerPlayer shadowOwner = Util.getShadowOwner(attacker);
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
    private Registry<DamageType> damageTypes;
    @Unique
    private final ResourceKey<DamageType> shadow = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "shadow"));
    @Unique
    private DamageSource shadowDamage(Entity shadow, Entity necromancer) {
        return new ShadowDamageSource(this.damageTypes.getOrThrow(this.shadow), shadow, necromancer);
    }
}
