package cz.yorick.networking;

import cz.yorick.NecromancersShadow;
import cz.yorick.item.SculkTotemItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public record RequestNecromancerInventoryC2SPacket(int clickedSlot) implements CustomPayload {
    private static final CustomPayload.Id<RequestNecromancerInventoryC2SPacket> ID = new CustomPayload.Id<>(Identifier.of(NecromancersShadow.MOD_ID, "necromancer_inventory_request"));
    private static final PacketCodec<RegistryByteBuf, RequestNecromancerInventoryC2SPacket> CODEC = PacketCodecs.registryCodec(Codecs.NON_NEGATIVE_INT.xmap(RequestNecromancerInventoryC2SPacket::new, RequestNecromancerInventoryC2SPacket::clickedSlot));

    @Override
    public CustomPayload.Id<RequestNecromancerInventoryC2SPacket> getId() {
        return ID;
    }

    public static void init() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            SculkTotemItem totemItem = getTotem(context.player(), payload.clickedSlot());
            if(totemItem != null) {
                player.openHandledScreen(totemItem);
            }
        });
    }

    private static SculkTotemItem getTotem(ServerPlayerEntity player, int slot) {
        //do not check in creative mode, the ScreenHandler situation is weird
        if(player.isCreative()) {
            return NecromancersShadow.SCULK_TOTEM;
        }

        Slot clickedSlot = player.currentScreenHandler.getSlot(slot);
        if (clickedSlot == null) {
            return null;
        }

        return clickedSlot.getStack().getItem() instanceof SculkTotemItem totemItem ? totemItem : null;
    }
}
