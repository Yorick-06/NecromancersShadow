package cz.yorick.networking;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.NecromancersShadow;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ImmutableShadowStorage;
import cz.yorick.data.MutableShadowStorage;
import cz.yorick.util.UiId;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.InventoryMenu;

public record SculkEmeraldInventorySwapC2SPacket(UiId from, UiId to) implements CustomPacketPayload {
    private static final CustomPacketPayload.Type<SculkEmeraldInventorySwapC2SPacket> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "sculk_emerald_inventory_swap"));
    private static final StreamCodec<RegistryFriendlyByteBuf, SculkEmeraldInventorySwapC2SPacket> CODEC = ByteBufCodecs.fromCodecWithRegistries(
            RecordCodecBuilder.create(instance -> instance.group(
                    UiId.CODEC.fieldOf("from").forGetter(SculkEmeraldInventorySwapC2SPacket::from),
                    UiId.CODEC.fieldOf("to").forGetter(SculkEmeraldInventorySwapC2SPacket::to)
            ).apply(instance, SculkEmeraldInventorySwapC2SPacket::new))
    );

    @Override
    public CustomPacketPayload.Type<SculkEmeraldInventorySwapC2SPacket> type() {
        return ID;
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            //PlayerScreenHandler is active when other handled screens are closed
            if(context.player().containerMenu instanceof InventoryMenu) {
                ImmutableShadowStorage itemStorage = context.player().getItemInHand(InteractionHand.MAIN_HAND).get(NecromancersShadow.SHADOW_STORAGE_COMPONENT);
                if(itemStorage == null) {
                    return;
                }

                MutableShadowStorage mutableItemStorage = itemStorage.toMutable();
                //go through the shadow manager to handle messing with spawned shadows
                UiId.swap(payload.from(), payload.to(), DataAttachments.getShadowManager(context.player()), mutableItemStorage);

                //only update the item if one of the swap targets was the item
                if(payload.from().ui() == UiId.Ui.ITEM || payload.to().ui() == UiId.Ui.ITEM) {
                    context.player().getItemInHand(InteractionHand.MAIN_HAND).set(NecromancersShadow.SHADOW_STORAGE_COMPONENT, mutableItemStorage.toImmutable());
                }
            }
        });
    }
}
