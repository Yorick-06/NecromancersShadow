package cz.yorick.item;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.ImmutableShadowStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class SculkEmeraldItem extends Item {
    public static final String HELP_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".sculk_emerald_help";
    public SculkEmeraldItem(Settings settings) {
        super(settings.maxCount(1).rarity(Rarity.UNCOMMON).component(NecromancersShadow.SHADOW_STORAGE_COMPONENT, ImmutableShadowStorage.empty()).fireproof());
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        //on the client
        if(hand == Hand.MAIN_HAND && !(user instanceof ServerPlayerEntity)) {
            NecromancersShadow.SCULK_EMERALD_INVENTORY_OPENER.run();
            return ActionResult.SUCCESS;
        }

        return super.use(world, user, hand);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        ImmutableShadowStorage shadowStorage = stack.get(NecromancersShadow.SHADOW_STORAGE_COMPONENT);
        return (shadowStorage != null && !shadowStorage.isEmpty()) || super.hasGlint(stack);
    }
}
