package cz.yorick.item;

import cz.yorick.NecromancersShadow;
import cz.yorick.ShadowInventoryScreenHandler;
import cz.yorick.data.NecromancerData;
import cz.yorick.data.ShadowData;
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

import java.util.List;
import java.util.function.Consumer;

//TODO add a way to ride the entities?
public class SculkTotemItem extends Item implements ExtendedScreenHandlerFactory<List<ShadowData>> {
    public static final String HELP_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".sculk_totem_help";
    public static final String NECROMANCER_INVENTORY_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".necromancer_inventory";
    public SculkTotemItem(Settings settings) {
        super(settings.maxCount(1).rarity(Rarity.RARE).fireproof());
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if(user instanceof ServerPlayerEntity player) {
            if(user.isSneaking()) {
                ((IServerPlayerEntityMixin)user).necromancers_shadow$setTarget(null);
                return ActionResult.SUCCESS_SERVER;
            } else if(NecromancerData.toggleShadows(player)) {
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
        NecromancerData.appendTotemTooltip(stack, context, textConsumer, type);
        if(NecromancersShadow.HAS_SHIFT_DOWN.get()) {
            NecromancersShadow.MULTILINE_TOOLTIP_DECODER.accept(HELP_TRANSLATION_KEY, textConsumer);
        } else {
            textConsumer.accept(Text.translatable(NecromancersShadow.HELP_TRANSLATION_KEY));
        }
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if(clickType == ClickType.RIGHT) {
            player.openHandledScreen(this);
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
        return NecromancerData.getItemBarStep();
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
        return new ShadowInventoryScreenHandler(syncId, playerInventory, NecromancerData.getShadows(player));
    }

    @Override
    public List<ShadowData> getScreenOpeningData(ServerPlayerEntity player) {
        return NecromancerData.getShadows(player);
    }
}
