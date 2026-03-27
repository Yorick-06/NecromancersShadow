package cz.yorick.networking;

import cz.yorick.NecromancersShadow;
import cz.yorick.item.SculkTotemItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.Slot;

public record RequestNecromancerInventoryC2SPacket(int clickedSlot) implements CustomPacketPayload {
    private static final CustomPacketPayload.Type<RequestNecromancerInventoryC2SPacket> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "necromancer_inventory_request"));
    private static final StreamCodec<RegistryFriendlyByteBuf, RequestNecromancerInventoryC2SPacket> CODEC = ByteBufCodecs.fromCodecWithRegistries(ExtraCodecs.NON_NEGATIVE_INT.xmap(RequestNecromancerInventoryC2SPacket::new, RequestNecromancerInventoryC2SPacket::clickedSlot));

    @Override
    public CustomPacketPayload.Type<RequestNecromancerInventoryC2SPacket> type() {
        return ID;
    }

    public static void init() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            ServerPlayer player = context.player();
            SculkTotemItem totemItem = getTotem(context.player(), payload.clickedSlot());
            if(totemItem != null) {
                player.openMenu(totemItem);
            }
        });
    }

    private static SculkTotemItem getTotem(ServerPlayer player, int slot) {
        //do not check in creative mode, the ScreenHandler situation is weird
        if(player.isCreative()) {
            return NecromancersShadow.SCULK_TOTEM;
        }

        Slot clickedSlot = player.containerMenu.getSlot(slot);
        if (clickedSlot == null) {
            return null;
        }

        return clickedSlot.getItem().getItem() instanceof SculkTotemItem totemItem ? totemItem : null;
    }
}
