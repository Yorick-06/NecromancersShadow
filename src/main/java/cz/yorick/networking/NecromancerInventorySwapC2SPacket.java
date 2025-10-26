package cz.yorick.networking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.NecromancersShadow;
import cz.yorick.screen.NecromancerInventoryScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public record NecromancerInventorySwapC2SPacket(int from, int to) implements CustomPayload {
    private static final CustomPayload.Id<NecromancerInventorySwapC2SPacket> ID = new CustomPayload.Id<>(Identifier.of(NecromancersShadow.MOD_ID, "necromancer_inventory_swap"));
    private static final PacketCodec<RegistryByteBuf, NecromancerInventorySwapC2SPacket> CODEC = PacketCodecs.registryCodec(
            RecordCodecBuilder.create(instance -> instance.group(
                    Codecs.NON_NEGATIVE_INT.fieldOf("from").forGetter(NecromancerInventorySwapC2SPacket::from),
                    Codec.INT.fieldOf("to").forGetter(NecromancerInventorySwapC2SPacket::to)
            ).apply(instance, NecromancerInventorySwapC2SPacket::new))
    );

    @Override
    public CustomPayload.Id<NecromancerInventorySwapC2SPacket> getId() {
        return ID;
    }

    public static void init() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            if(context.player().currentScreenHandler instanceof NecromancerInventoryScreenHandler handler) {
                handler.onSwapPacket(payload.from(), payload.to());
            }
        });
    }
}
