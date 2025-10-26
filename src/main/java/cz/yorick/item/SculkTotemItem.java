package cz.yorick.item;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ImmutableShadowStorage;
import cz.yorick.data.ServerShadowManager;
import cz.yorick.screen.NecromancerInventoryScreenHandler;
import cz.yorick.imixin.IServerPlayerEntityMixin;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

//TODO add a way to ride the entities?
public class SculkTotemItem extends Item implements ExtendedScreenHandlerFactory<ImmutableShadowStorage> {
    public static final String HELP_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".help";
    public static final String HELP_CONTENT_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".sculk_totem_help";
    public static final String NECROMANCER_INVENTORY_TRANSLATION_KEY = "title." + NecromancersShadow.MOD_ID + ".necromancer_inventory";
    public SculkTotemItem(Settings settings) {
        super(settings.maxCount(1).rarity(Rarity.RARE).fireproof());
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if(user instanceof ServerPlayerEntity player) {
            if(user.isSneaking()) {
                ((IServerPlayerEntityMixin)user).necromancers_shadow$setTarget(null);
                return ActionResult.SUCCESS_SERVER;
            } else if(ServerShadowManager.toggleShadows(player)) {
                player.getItemCooldownManager().set(user.getStackInHand(hand), 60);
                return ActionResult.SUCCESS_SERVER;
            }
        }

        return super.use(world, user, hand);
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(attacker instanceof ServerPlayerEntity serverPlayer) {
            ((IServerPlayerEntityMixin)serverPlayer).necromancers_shadow$setTarget(target);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        NecromancersShadow.SCULK_TOTEM_TOOLTIP_APPENDER.appendTooltip(context, textConsumer, type, stack);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        //opening the menu here using player.openHandledScreen() causes two issues
        //- this method is only ran on the client when in creative, causing the menu to only work in survival
        //- opening a menu from this method causes the inventory to desync for some reason
        if(clickType == ClickType.RIGHT && !(player instanceof ServerPlayerEntity)) {
            //creative menu has weird indexing, server also ignores checks when in creative
            if(player.isCreative()) {
                NecromancersShadow.NECROMANCER_INVENTORY_OPENER.accept(0);
                return true;
            }

            player.currentScreenHandler.getSlotIndex(slot.inventory, slot.getIndex()).ifPresentOrElse(
                    id -> NecromancersShadow.NECROMANCER_INVENTORY_OPENER.accept(id),
                    () -> NecromancersShadow.LOGGER.warn("OnClicked somehow called with an invalid slot, should never happen")
            );

            return true;
        }

        return false;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return DataAttachments.getSoulEnergyItemBarStep();
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0x24e0ec;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(NECROMANCER_INVENTORY_TRANSLATION_KEY);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if(player instanceof ServerPlayerEntity serverPlayer) {
            return new NecromancerInventoryScreenHandler(syncId, playerInventory, DataAttachments.getShadowManager(serverPlayer));
        }

        throw new UnsupportedOperationException("SculkTotemItem.createMenu() called with a PlayerEntity instead of a ServerPlayerEntity, should never happen!");
    }

    @Override
    public ImmutableShadowStorage getScreenOpeningData(ServerPlayerEntity player) {
        return DataAttachments.getShadowStorage(player);
    }
}
