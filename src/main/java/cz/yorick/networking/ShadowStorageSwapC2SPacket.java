package cz.yorick.networking;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.NecromancersShadow;
import cz.yorick.data.ShadowStorage;
import cz.yorick.screen.ShadowInventoryScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public record ShadowStorageSwapC2SPacket(int from, int to) implements CustomPayload {
    private static final CustomPayload.Id<ShadowStorageSwapC2SPacket> ID = new CustomPayload.Id<>(Identifier.of(NecromancersShadow.MOD_ID, "shadow_storage_swap"));
    private static final PacketCodec<RegistryByteBuf, ShadowStorageSwapC2SPacket> CODEC = PacketCodecs.registryCodec(
            RecordCodecBuilder.create(instance -> instance.group(
                    Codecs.NON_NEGATIVE_INT.fieldOf("from").forGetter(ShadowStorageSwapC2SPacket::from),
                    Codecs.NON_NEGATIVE_INT.fieldOf("to").forGetter(ShadowStorageSwapC2SPacket::to)
            ).apply(instance, ShadowStorageSwapC2SPacket::new))
    );

    @Override
    public CustomPayload.Id<ShadowStorageSwapC2SPacket> getId() {
        return ID;
    }

    public static void init() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            if(context.player().currentScreenHandler instanceof ShadowInventoryScreenHandler handler) {
                handler.onSwapPacket(payload.from(), payload.to());
            }
        });
    }
}
