package cz.yorick.networking;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.SculkEmeraldMode;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public record SwapSculkEmeraldModesC2SPacket() implements CustomPayload {
    private static final Id<SwapSculkEmeraldModesC2SPacket> ID = new Id<>(Identifier.of(NecromancersShadow.MOD_ID, "swap_sculk_emerald_modes"));
    private static final PacketCodec<RegistryByteBuf, SwapSculkEmeraldModesC2SPacket> CODEC = PacketCodec.of((data, buf) -> {}, buf -> new SwapSculkEmeraldModesC2SPacket());

    @Override
    public Id<SwapSculkEmeraldModesC2SPacket> getId() {
        return ID;
    }

    public static void init() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            ItemStack handStack = context.player().getStackInHand(Hand.MAIN_HAND);
            SculkEmeraldMode mode = handStack.get(NecromancersShadow.SCULK_EMERALD_MODE_COMPONENT);
            if(mode != null) {
               handStack.set(NecromancersShadow.SCULK_EMERALD_MODE_COMPONENT, mode.invert());
            }
        });
    }
}
