package cz.yorick;

import cz.yorick.data.NecromancerData;
import cz.yorick.data.ShadowData;
import cz.yorick.networking.ShadowInventoryInteractC2SPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.OptionalInt;

public class ShadowInventoryScreenHandler extends ScreenHandler {
    private final List<ShadowData> shadowData;
    private final SimpleInventory shadowInventory = new SimpleInventory(6);
    public ShadowInventoryScreenHandler(int syncId, PlayerInventory playerInventory, List<ShadowData> data) {
        super(NecromancersShadow.NECROMANCER_INVENTORY_SCREEN_HANDLER_TYPE, syncId);
        this.shadowData = data;
        int j = 18 + 6 * 18 + 13;
        this.addPlayerSlots(playerInventory, 8, j);
        this.addSoulSlots(8, 18);
    }

    private void addSoulSlots(int startX, int startY) {
        for (int y = 0; y < 2; y++) {
            for(int x = 0; x < 3; x++) {
                int index = x + 3 * y;
                if(index < this.shadowData.size()) {
                    ItemStack stack = new ItemStack(NecromancersShadow.SOUL_ITEM);
                    stack.set(NecromancersShadow.SOUL_DATA_COMPONENT, this.shadowData.get(index));
                    this.shadowInventory.setStack(index, stack);
                }

                this.addSlot(new Slot(this.shadowInventory, index, startX + 18 + (x * 3 * 18), startY + 36 + (3 * 18 * y)));
            }
        }
    }

    public Slot getShadowInventorySlot(int index) {
        OptionalInt slotIndex = this.getSlotIndex(this.shadowInventory, index);
        return slotIndex.isPresent() ? getSlot(slotIndex.getAsInt()) : null;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public List<ShadowData> getShadowData() {
        return this.shadowData;
    }

    public void onInventoryAction(ShadowInventoryInteractC2SPacket packet, ServerPlayerEntity player) {
        System.out.println("received inventory action: " + packet);
        int shadowIndex = packet.id();
        List<ShadowData> shadows = NecromancerData.getShadows(player);
        if(shadowIndex < 0 || shadowIndex >= shadows.size()) {
            NecromancersShadow.LOGGER.warn("Received an invalid shadow inventory index from player " + player.getName().getString() + " (index: " + shadowIndex + ", size: " + shadows.size() + ")");
            return;
        }

        switch (packet.action()) {
            case TOGGLE_SUMMON -> NecromancerData.toggleShadow(player, shadows.get(shadowIndex));
            case CONVERT -> System.out.println("convert");
        }
    }
}
