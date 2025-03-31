package cz.yorick.item;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.NecromancerData;
import cz.yorick.imixin.IServerPlayerEntityMixin;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class SculkTotemItem extends Item {
    public static final String HELP_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".sculk_totem_help";
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
}
