package cz.yorick.item;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.ImmutableShadowStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class SculkEmeraldItem extends Item {
    public static final String HELP_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".sculk_emerald_help";
    public SculkEmeraldItem(Properties settings) {
        super(settings.stacksTo(1).rarity(Rarity.UNCOMMON).component(NecromancersShadow.SHADOW_STORAGE_COMPONENT, ImmutableShadowStorage.empty()).fireResistant());
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        //on the client
        if(hand == InteractionHand.MAIN_HAND && !(user instanceof ServerPlayer)) {
            NecromancersShadow.SCULK_EMERALD_INVENTORY_OPENER.run();
            return InteractionResult.SUCCESS;
        }

        return super.use(world, user, hand);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        ImmutableShadowStorage shadowStorage = stack.get(NecromancersShadow.SHADOW_STORAGE_COMPONENT);
        return (shadowStorage != null && !shadowStorage.isEmpty()) || super.isFoil(stack);
    }
}
