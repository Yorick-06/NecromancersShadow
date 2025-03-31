package cz.yorick.item;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.NecromancerData;
import cz.yorick.data.SculkEmeraldMode;
import cz.yorick.data.ShadowData;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SculkEmeraldItem extends Item {
    public static final String HELP_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".sculk_emerald_help";
    public SculkEmeraldItem(Settings settings) {
        super(settings.maxCount(1).rarity(Rarity.UNCOMMON).component(NecromancersShadow.SHADOW_DATA_COMPONENT, List.of()).component(NecromancersShadow.SCULK_EMERALD_MODE_COMPONENT, new SculkEmeraldMode(true)).fireproof());
    }

    public ActionResult onOwnedShadowUse(ServerPlayerEntity user, ItemStack stack, ShadowData shadowData) {
        SculkEmeraldMode mode = stack.get(NecromancersShadow.SCULK_EMERALD_MODE_COMPONENT);
        if(mode != null && !mode.input()) {
            return ActionResult.PASS;
        }

        List<ShadowData> storedShadows = getMutableShadowData(stack);
        NecromancerData.releaseShadow(user, shadowData);
        storedShadows.add(shadowData);
        stack.set(NecromancersShadow.SHADOW_DATA_COMPONENT, storedShadows);
        return ActionResult.SUCCESS_SERVER;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if(user instanceof ServerPlayerEntity serverPlayer) {

            ItemStack stack = user.getStackInHand(hand);
            SculkEmeraldMode mode = stack.get(NecromancersShadow.SCULK_EMERALD_MODE_COMPONENT);
            if(mode != null && mode.onUse(serverPlayer, stack)) {
                return ActionResult.SUCCESS_SERVER;
            }
        }
        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        SculkEmeraldMode mode = stack.get(NecromancersShadow.SCULK_EMERALD_MODE_COMPONENT);
        if(mode != null) {
            mode.appendTooltip(context, textConsumer, type, stack);
        }

        List<ShadowData> storedShadows = stack.get(NecromancersShadow.SHADOW_DATA_COMPONENT);
        if(storedShadows != null) {
            ShadowData.appendShadowsTooltip(stack, context, textConsumer, type, storedShadows);
        }

        if(NecromancersShadow.HAS_SHIFT_DOWN.get()) {
            NecromancersShadow.MULTILINE_TOOLTIP_DECODER.accept(HELP_TRANSLATION_KEY, textConsumer);
        } else {
            textConsumer.accept(Text.translatable(NecromancersShadow.HELP_TRANSLATION_KEY));
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        List<ShadowData> storedShadows = stack.get(NecromancersShadow.SHADOW_DATA_COMPONENT);
        return storedShadows != null && !storedShadows.isEmpty();
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHit(stack, target, attacker);
    }

    public static List<ShadowData> getMutableShadowData(ItemStack stack) {
        List<ShadowData> originalData = stack.get(NecromancersShadow.SHADOW_DATA_COMPONENT);
        return originalData != null ? new ArrayList<>(originalData) : new ArrayList<>();
    }
}
