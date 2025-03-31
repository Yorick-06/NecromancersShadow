package cz.yorick.util;

import cz.yorick.data.ShadowData;
import cz.yorick.imixin.IMobEntityMixin;
import cz.yorick.item.SculkTotemItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public class Util {
    public static boolean canHurt(Entity attacker, Entity target) {
        ServerPlayerEntity attackerLeader = getShadowTeamLeader(attacker);
        ServerPlayerEntity targetLeader = getShadowTeamLeader(target);
        if(attackerLeader == null || targetLeader == null) {
            return true;
        }

        return !attackerLeader.equals(targetLeader);
    }

    public static ServerPlayerEntity getShadowTeamLeader(Entity entity) {
        //a player is its own leader
        if(entity instanceof ServerPlayerEntity player) {
            return player;
        }

        return getShadowOwner(entity);
    }

    public static ServerPlayerEntity getShadowOwner(Entity shadow) {
        //if it is an owned mob entity
        ShadowData.Instance shadowInstance = getShadowInstance(shadow);
        if(shadowInstance != null) {
            return shadowInstance.owner();
        }

        //if the target is ownable and has an owner (any projectile, vex) return the owners team
        if(shadow instanceof Ownable ownable && ownable.getOwner() != null) {
            return getShadowTeamLeader(ownable.getOwner());
        }

        return null;
    }

    public static boolean isShadow(Entity entity) {
        return getShadowInstance(entity) != null;
    }

    public static ShadowData.Instance getShadowInstance(Entity entity) {
        return entity instanceof IMobEntityMixin mixin ? mixin.necromancers_shadow$$getShadowInstance() : null;
    }

    public static int getAttribute(EntityType<?> entityType, RegistryEntry<EntityAttribute> attribute) {
        DefaultAttributeContainer defaultContainer =  DefaultAttributeRegistry.get((EntityType<? extends LivingEntity>)entityType);
        if(defaultContainer != null && defaultContainer.has(attribute)) {
            return (int)Math.round(defaultContainer.getValue(attribute));
        }

        return 0;
    }

    public static boolean isHoldingTotem(ServerPlayerEntity player) {
        return player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof SculkTotemItem || player.getStackInHand(Hand.OFF_HAND).getItem() instanceof SculkTotemItem;
    }
}
