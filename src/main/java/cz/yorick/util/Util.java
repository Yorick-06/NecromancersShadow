package cz.yorick.util;

import cz.yorick.data.ShadowData;
import cz.yorick.imixin.IMobEntityMixin;
import cz.yorick.item.SculkTotemItem;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

public class Util {
    public static boolean canHurt(Entity attacker, Entity target) {
        ServerPlayer attackerLeader = getShadowTeamLeader(attacker);
        ServerPlayer targetLeader = getShadowTeamLeader(target);
        if(attackerLeader == null || targetLeader == null) {
            return true;
        }

        return !attackerLeader.equals(targetLeader);
    }

    public static ServerPlayer getShadowTeamLeader(Entity entity) {
        //a player is its own leader
        if(entity instanceof ServerPlayer player) {
            return player;
        }

        return getShadowOwner(entity);
    }

    public static ServerPlayer getShadowOwner(Entity shadow) {
        //if it is an owned mob entity
        ShadowData.Instance shadowInstance = getShadowInstance(shadow);
        if(shadowInstance != null) {
            return shadowInstance.owner();
        }

        //if the target is ownable and has an owner (any projectile, vex) return the owners team
        if(shadow instanceof TraceableEntity ownable && ownable.getOwner() != null) {
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

    public static int getAttribute(EntityType<?> entityType, Holder<Attribute> attribute) {
        AttributeSupplier defaultContainer =  DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>)entityType);
        if(defaultContainer != null && defaultContainer.hasAttribute(attribute)) {
            return (int)Math.round(defaultContainer.getValue(attribute));
        }

        return 0;
    }

    public static boolean isHoldingTotem(ServerPlayer player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SculkTotemItem || player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof SculkTotemItem;
    }
}
