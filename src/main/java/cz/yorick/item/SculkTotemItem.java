package cz.yorick.item;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ImmutableShadowStorage;
import cz.yorick.data.ServerShadowManager;
import cz.yorick.screen.NecromancerInventoryScreenHandler;
import cz.yorick.imixin.IServerPlayerEntityMixin;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

//TODO add a way to ride the entities?
public class SculkTotemItem extends Item implements ExtendedMenuProvider<ImmutableShadowStorage> {
    public static final String HELP_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".help";
    public static final String HELP_CONTENT_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".sculk_totem_help";
    public static final String NECROMANCER_INVENTORY_TRANSLATION_KEY = "title." + NecromancersShadow.MOD_ID + ".necromancer_inventory";
    public SculkTotemItem(Properties settings) {
        super(settings.stacksTo(1).rarity(Rarity.RARE).fireResistant());
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if(user instanceof ServerPlayer player) {
            if(user.isShiftKeyDown()) {
                ((IServerPlayerEntityMixin)user).necromancers_shadow$setTarget(null);
                return InteractionResult.SUCCESS_SERVER;
            } else if(ServerShadowManager.toggleShadows(player)) {
                player.getCooldowns().addCooldown(user.getItemInHand(hand), 60);
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        return super.use(world, user, hand);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(attacker instanceof ServerPlayer serverPlayer) {
            ((IServerPlayerEntityMixin)serverPlayer).necromancers_shadow$setTarget(target);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        NecromancersShadow.SCULK_TOTEM_TOOLTIP_APPENDER.addToTooltip(context, textConsumer, type, stack);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        //opening the menu here using player.openHandledScreen() causes two issues
        //- this method is only ran on the client when in creative, causing the menu to only work in survival
        //- opening a menu from this method causes the inventory to desync for some reason
        if(clickType == ClickAction.SECONDARY && !(player instanceof ServerPlayer)) {
            //creative menu has weird indexing, server also ignores checks when in creative
            if(player.isCreative()) {
                NecromancersShadow.NECROMANCER_INVENTORY_OPENER.accept(0);
                return true;
            }

            player.containerMenu.findSlot(slot.container, slot.getContainerSlot()).ifPresentOrElse(
                    id -> NecromancersShadow.NECROMANCER_INVENTORY_OPENER.accept(id),
                    () -> NecromancersShadow.LOGGER.warn("OnClicked somehow called with an invalid slot, should never happen")
            );

            return true;
        }

        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return DataAttachments.getSoulEnergyItemBarStep();
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x24e0ec;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(NECROMANCER_INVENTORY_TRANSLATION_KEY);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        if(player instanceof ServerPlayer serverPlayer) {
            return new NecromancerInventoryScreenHandler(syncId, playerInventory, DataAttachments.getShadowManager(serverPlayer));
        }

        throw new UnsupportedOperationException("SculkTotemItem.createMenu() called with a PlayerEntity instead of a ServerPlayerEntity, should never happen!");
    }

    @Override
    public ImmutableShadowStorage getScreenOpeningData(ServerPlayer player) {
        return DataAttachments.getShadowStorage(player);
    }
}
