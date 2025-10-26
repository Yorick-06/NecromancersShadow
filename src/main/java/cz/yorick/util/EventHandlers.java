package cz.yorick.util;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ShadowData;
import cz.yorick.data.MaxSoulEnergyGainConsumeEffect;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class EventHandlers {
    public static void init() {
        ServerLivingEntityEvents.AFTER_DEATH.register(EventHandlers::onEntityDeath);
        //clear spawned shadows after player disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> DataAttachments.getShadowManager(handler.player).despawnShadows());
        ServerLivingEntityEvents.MOB_CONVERSION.register(EventHandlers::onConversion);
        DefaultItemComponentEvents.MODIFY.register(context -> context.modify(item -> item == Items.ECHO_SHARD, EventHandlers::modifyEchoShardComponents));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.addAfter(Items.TOTEM_OF_UNDYING, NecromancersShadow.SCULK_TOTEM);
            entries.addAfter(NecromancersShadow.SCULK_TOTEM, NecromancersShadow.SCULK_EMERALD);
        });
    }

    private static void onEntityDeath(LivingEntity killed, DamageSource source) {
        //clear spawned shadows after player death
        if(killed instanceof ServerPlayerEntity player) {
            DataAttachments.getShadowManager(player).despawnShadows();
            return;
        }

        //spawn souls on entity death
        if(killed instanceof MobEntity mobEntity && source.getAttacker() instanceof ServerPlayerEntity player && Util.isHoldingTotem(player) && !Util.isShadow(killed)) {
            ServerWorld world = player.getEntityWorld();
            NecromancersShadow.SOUL_ENTITY_ENTITY_TYPE.spawn(world, soulEntity -> soulEntity.setShadow(ShadowData.fromEntity(mobEntity)), killed.getBlockPos(), SpawnReason.TRIGGERED, false, false);
        }
    }

    private static void onConversion(MobEntity previous, MobEntity converted, EntityConversionContext context) {
        ShadowData.Instance shadowInstance = Util.getShadowInstance(previous);
        //not a shadow, do not care
        if(shadowInstance == null) {
            return;
        }

        DataAttachments.getShadowManager(shadowInstance.owner()).convertShadow(previous, converted);
    }

    private static void modifyEchoShardComponents(ComponentMap.Builder map, Item item) {
        map.add(DataComponentTypes.CONSUMABLE, ConsumableComponent.builder().consumeEffect(new MaxSoulEnergyGainConsumeEffect(10)).build());
    }
}
