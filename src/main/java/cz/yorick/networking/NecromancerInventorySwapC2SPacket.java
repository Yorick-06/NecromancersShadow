package cz.yorick.networking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.NecromancersShadow;
import cz.yorick.screen.NecromancerInventoryScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public record NecromancerInventorySwapC2SPacket(int from, int to) implements CustomPacketPayload {
    private static final CustomPacketPayload.Type<NecromancerInventorySwapC2SPacket> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "necromancer_inventory_swap"));
    private static final StreamCodec<RegistryFriendlyByteBuf, NecromancerInventorySwapC2SPacket> CODEC = ByteBufCodecs.fromCodecWithRegistries(
            RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("from").forGetter(NecromancerInventorySwapC2SPacket::from),
                    Codec.INT.fieldOf("to").forGetter(NecromancerInventorySwapC2SPacket::to)
            ).apply(instance, NecromancerInventorySwapC2SPacket::new))
    );

    @Override
    public CustomPacketPayload.Type<NecromancerInventorySwapC2SPacket> type() {
        return ID;
    }

    public static void init() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            if(context.player().containerMenu instanceof NecromancerInventoryScreenHandler handler) {
                handler.onSwapPacket(payload.from(), payload.to());
            }
        });
    }
}
