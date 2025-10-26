package cz.yorick.networking;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.NecromancersShadow;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ImmutableShadowStorage;
import cz.yorick.data.MutableShadowStorage;
import cz.yorick.util.UiId;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public record SculkEmeraldInventorySwapC2SPacket(UiId from, UiId to) implements CustomPayload {
    private static final CustomPayload.Id<SculkEmeraldInventorySwapC2SPacket> ID = new CustomPayload.Id<>(Identifier.of(NecromancersShadow.MOD_ID, "sculk_emerald_inventory_swap"));
    private static final PacketCodec<RegistryByteBuf, SculkEmeraldInventorySwapC2SPacket> CODEC = PacketCodecs.registryCodec(
            RecordCodecBuilder.create(instance -> instance.group(
                    UiId.CODEC.fieldOf("from").forGetter(SculkEmeraldInventorySwapC2SPacket::from),
                    UiId.CODEC.fieldOf("to").forGetter(SculkEmeraldInventorySwapC2SPacket::to)
            ).apply(instance, SculkEmeraldInventorySwapC2SPacket::new))
    );

    @Override
    public CustomPayload.Id<SculkEmeraldInventorySwapC2SPacket> getId() {
        return ID;
    }

    public static void init() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            //PlayerScreenHandler is active when other handled screens are closed
            if(context.player().currentScreenHandler instanceof PlayerScreenHandler) {
                ImmutableShadowStorage itemStorage = context.player().getStackInHand(Hand.MAIN_HAND).get(NecromancersShadow.SHADOW_STORAGE_COMPONENT);
                if(itemStorage == null) {
                    return;
                }

                MutableShadowStorage mutableItemStorage = itemStorage.toMutable();
                //go through the shadow manager to handle messing with spawned shadows
                UiId.swap(payload.from(), payload.to(), DataAttachments.getShadowManager(context.player()), mutableItemStorage);

                //only update the item if one of the swap targets was the item
                if(payload.from().ui() == UiId.Ui.ITEM || payload.to().ui() == UiId.Ui.ITEM) {
                    context.player().getStackInHand(Hand.MAIN_HAND).set(NecromancersShadow.SHADOW_STORAGE_COMPONENT, mutableItemStorage.toImmutable());
                }
            }
        });
    }
}
