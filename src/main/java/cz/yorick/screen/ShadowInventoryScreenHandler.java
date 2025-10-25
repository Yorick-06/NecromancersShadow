package cz.yorick.screen;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.*;
import cz.yorick.networking.ShadowInventoryInteractC2SPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShadowInventoryScreenHandler extends ScreenHandler {
    private final ShadowManager shadowManager;
    private final PlayerEntity player;
    public ShadowInventoryScreenHandler(int syncId, PlayerInventory playerInventory, ShadowStorage shadowStorage) {
        this(syncId, playerInventory, new ClientShadowManager(shadowStorage));
    }

    public ShadowInventoryScreenHandler(int syncId, PlayerInventory playerInventory, ShadowManager shadowManager) {
        super(NecromancersShadow.NECROMANCER_INVENTORY_SCREEN_HANDLER_TYPE, syncId);
        this.shadowManager = shadowManager;
        this.player = playerInventory.player;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public ShadowManager getShadowManager() {
        return this.shadowManager;
    }

    public void onInventoryAction(ShadowInventoryInteractC2SPacket packet, ServerPlayerEntity player) {
        /*
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
        }*/
    }

    public void onSwapPacket(int from, int to) {
       this.shadowManager.swapShadows(from, to);
    }
}
