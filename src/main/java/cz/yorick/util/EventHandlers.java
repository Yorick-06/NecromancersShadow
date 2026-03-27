package cz.yorick.util;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ShadowData;
import cz.yorick.data.MaxSoulEnergyGainConsumeEffect;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;

public class EventHandlers {
    public static void init() {
        ServerLivingEntityEvents.AFTER_DEATH.register(EventHandlers::onEntityDeath);
        //clear spawned shadows after player disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> DataAttachments.getShadowManager(handler.player).despawnShadows());
        ServerLivingEntityEvents.MOB_CONVERSION.register(EventHandlers::onConversion);
        DefaultItemComponentEvents.MODIFY.register(context -> context.modify(item -> item == Items.ECHO_SHARD, EventHandlers::modifyEchoShardComponents));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(entries -> entries.insertAfter(Items.TOTEM_OF_UNDYING, NecromancersShadow.SCULK_TOTEM, NecromancersShadow.SCULK_EMERALD));
    }

    private static void onEntityDeath(LivingEntity killed, DamageSource source) {
        //clear spawned shadows after player death
        if(killed instanceof ServerPlayer player) {
            DataAttachments.getShadowManager(player).despawnShadows();
            return;
        }

        //spawn souls on entity death
        if(killed instanceof Mob mobEntity && source.getEntity() instanceof ServerPlayer player && Util.isHoldingTotem(player) && !Util.isShadow(killed)) {
            ServerLevel world = player.level();
            NecromancersShadow.SOUL_ENTITY_ENTITY_TYPE.spawn(world, soulEntity -> soulEntity.setShadow(ShadowData.fromEntity(mobEntity)), killed.blockPosition(), EntitySpawnReason.TRIGGERED, false, false);
        }
    }

    private static void onConversion(Mob previous, Mob converted, ConversionParams context) {
        ShadowData.Instance shadowInstance = Util.getShadowInstance(previous);
        //not a shadow, do not care
        if(shadowInstance == null) {
            return;
        }

        DataAttachments.getShadowManager(shadowInstance.owner()).convertShadow(previous, converted);
    }

    private static void modifyEchoShardComponents(DataComponentMap.Builder map, Item item) {
        map.set(DataComponents.CONSUMABLE, Consumable.builder().onConsume(new MaxSoulEnergyGainConsumeEffect(10)).build());
    }
}
