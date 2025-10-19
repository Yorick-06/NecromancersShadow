package cz.yorick.networking;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.ShadowInventoryScreenHandler;
import cz.yorick.NecromancersShadow;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public record ShadowInventoryInteractC2SPacket(int id, Action action) implements CustomPayload {
    private static final Id<ShadowInventoryInteractC2SPacket> ID = new Id<>(Identifier.of(NecromancersShadow.MOD_ID, "shadow_inventory_interact"));
    private static final PacketCodec<RegistryByteBuf, ShadowInventoryInteractC2SPacket> CODEC = PacketCodecs.registryCodec(
            RecordCodecBuilder.create(instance -> instance.group(
                    Codecs.NON_NEGATIVE_INT.fieldOf("id").forGetter(ShadowInventoryInteractC2SPacket::id),
                    Codecs.NON_NEGATIVE_INT.xmap(index -> Action.values()[index], Action::ordinal).fieldOf("action").forGetter(ShadowInventoryInteractC2SPacket::action)
            ).apply(instance, ShadowInventoryInteractC2SPacket::new))
    );

    @Override
    public Id<ShadowInventoryInteractC2SPacket> getId() {
        return ID;
    }

    public static void init() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            if(context.player().currentScreenHandler instanceof ShadowInventoryScreenHandler handler) {
                handler.onInventoryAction(payload, context.player());
            }
        });
    }

    public enum Action {
        TOGGLE_SUMMON,
        CONVERT
    }
}
