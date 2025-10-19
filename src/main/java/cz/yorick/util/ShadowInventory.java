package cz.yorick.util;

import cz.yorick.data.NecromancerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

public class ShadowInventory implements Inventory {
    private final ServerPlayerEntity player;
    private final DefaultedList<ItemStack> stacks;
    public ShadowInventory(ServerPlayerEntity player, int size) {
        this.player = player;
        this.stacks = DefaultedList.ofSize(size);
    }


    @Override
    public int size() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return this.stacks.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.stacks, slot, amount);
        if(this.stacks.get(slot).isEmpty()) {
            onStackRemoved(slot);
        }
        return itemStack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removed = this.stacks.remove(slot);
        onStackRemoved(slot);
        return removed;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
        if(!stack.isEmpty()) {
            onStackAdded(slot);
        }
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return player == this.player;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }

    private void onStackRemoved(int slot) {
        //NecromancerData.releaseShadow(this.player, );
    }

    private void onStackAdded(int slot) {

    }
}
