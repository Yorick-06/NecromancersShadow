package cz.yorick.screen;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class NecromancerInventoryScreenHandler extends ScreenHandler {
    public NecromancerInventoryScreenHandler(int syncId, PlayerInventory playerInventory, ImmutableShadowStorage shadowStorage) {
        this(syncId, playerInventory, shadowStorage.toMutable());
    }

    private final MutableShadowAccess playerShadows;
    public NecromancerInventoryScreenHandler(int syncId, PlayerInventory playerInventory, MutableShadowAccess playerShadows) {
        super(NecromancersShadow.PLAYER_SHADOW_INVENTORY_SCREEN_HANDLER_TYPE, syncId);
        this.playerShadows = playerShadows;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public MutableShadowAccess getPlayerShadows() {
        return this.playerShadows;
    }

    public void onSwapPacket(int from, int to) {
        //from must always be a valid slot
        if(from < 0) {
            return;
        }

        //to 0 or above -> normal swap
        if(to >= 0) {
            this.getPlayerShadows().swapShadows(from, to);
            return;
        }

        //-1 means toggle summon
        if(to == -1) {
            if(this.playerShadows instanceof ServerShadowManager serverShadowManager) {
                serverShadowManager.toggleShadow(from);
            }
            return;
        }

        //other negatives mean delete
        this.playerShadows.setShadow(from, null);
    }
}
