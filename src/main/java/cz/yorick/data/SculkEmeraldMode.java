package cz.yorick.data;

import com.mojang.serialization.Codec;
import cz.yorick.NecromancersShadow;
import cz.yorick.item.SculkEmeraldItem;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public record SculkEmeraldMode(boolean input) implements TooltipAppender {
    public static final String MODE_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".mode";
    public static final String INPUT_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".input";
    public static final String OUTPUT_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".output";
    public static final Codec<SculkEmeraldMode> CODEC = Codec.BOOL.xmap(SculkEmeraldMode::new, SculkEmeraldMode::input);

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(Text.translatable(MODE_TRANSLATION_KEY).append(Text.translatable(this.input ? INPUT_TRANSLATION_KEY : OUTPUT_TRANSLATION_KEY)));
    }

    public SculkEmeraldMode invert() {
        return new SculkEmeraldMode(!this.input);
    }

    public boolean onUse(ServerPlayerEntity player, ItemStack stack) {
        List<ShadowData> storedShadows = SculkEmeraldItem.getMutableShadowData(stack);
        if(this.input) {
            //inputting but players has no shadows
            if(NecromancerData.storedAmount(player) == 0) {
                return false;
            }

            if(player.isSneaking()) {
                storedShadows.addAll(NecromancerData.releaseShadows(player));
            } else {
                storedShadows.add(NecromancerData.releaseFirstShadow(player));
            }

        } else {
            //outputting & no shadows stored in item
            if(storedShadows.isEmpty()) {
                return false;
            }

            if(player.isSneaking()) {
                NecromancerData.addShadows(player, storedShadows);
                storedShadows.clear();
            } else {
                NecromancerData.addShadow(player, storedShadows.remove(0));
            }
        }

        stack.set(NecromancersShadow.SHADOW_DATA_COMPONENT, storedShadows);
        return true;
    }
}
