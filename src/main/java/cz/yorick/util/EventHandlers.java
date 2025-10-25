package cz.yorick.util;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ShadowData;
import cz.yorick.data.MaxSoulEnergyGainConsumeEffect;
import cz.yorick.mixin.EntityAccessor;
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
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.WriteView;

public class EventHandlers {
    public static void init() {
        ServerLivingEntityEvents.AFTER_DEATH.register(EventHandlers::onEntityDeath);
        //clear spawned shadows after player disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> DataAttachments.getShadowManager(handler.player).despawnShadows());
        ServerLivingEntityEvents.MOB_CONVERSION.register(EventHandlers::onConversion);
        //UseEntityCallback.EVENT.register(EventHandlers::onEntityInteract);

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
            WriteView nbt = NbtWriteView.create(NecromancersShadow.ERROR_REPORTER, mobEntity.getRegistryManager());
            ((EntityAccessor)mobEntity).invokeWriteCustomData(nbt);
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

    /*
    //fired before checking entity.interact() which prevents entities with interactions from being detected
    private static ActionResult onEntityInteract(PlayerEntity player, World world, Hand hand, Entity entity, HitResult hitResult) {
        ItemStack heldStack = player.getStackInHand(hand);
        if(heldStack.getItem() instanceof SculkEmeraldItem sculkEmeraldItem) {
            //if a dragon part got interacted, replace it with the dragon
            Entity actualEntity = entity instanceof EnderDragonPart dragonPart ? dragonPart.owner : entity;

            //if server, try to run the method
            if(player instanceof ServerPlayerEntity serverPlayer) {
                ShadowData.Instance shadowInstance = Util.getShadowInstance(actualEntity);
                if(shadowInstance != null && shadowInstance.owner() == serverPlayer) {
                    //fabrics event handler is injected before the main handler method runs so a hand swing needs to be caused manually
                    ActionResult itemResult = sculkEmeraldItem.onOwnedShadowUse(serverPlayer, heldStack, shadowInstance.shadow());
                    if(itemResult instanceof ActionResult.Success success && success.swingSource() == ActionResult.SwingSource.SERVER) {
                        serverPlayer.swingHand(hand, true);
                    }
                    return itemResult;
                }

            } else if(DataAttachments.isMarkedAsShadow(actualEntity)) {
                //cancels further processing, does not swing hand, sends packet to the server
                return ActionResult.CONSUME;
            }
        }

        //falls back to further processing
        return ActionResult.PASS;
    }*/

    private static void modifyEchoShardComponents(ComponentMap.Builder map, Item item) {
        map.add(DataComponentTypes.CONSUMABLE, ConsumableComponent.builder().consumeEffect(new MaxSoulEnergyGainConsumeEffect(10)).build());
    }
}
