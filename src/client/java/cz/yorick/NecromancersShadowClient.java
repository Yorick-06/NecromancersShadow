package cz.yorick;

import com.mojang.blaze3d.platform.InputConstants;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ImmutableShadowStorage;
import cz.yorick.item.SculkTotemItem;
import cz.yorick.networking.RequestNecromancerInventoryC2SPacket;
import cz.yorick.screen.NecromancerInventoryScreen;
import cz.yorick.screen.SculkEmeraldScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NecromancersShadowClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        EntityRenderers.register(NecromancersShadow.SOUL_ENTITY_ENTITY_TYPE, SoulEntityRenderer::new);
		MenuScreens.register(NecromancersShadow.PLAYER_SHADOW_INVENTORY_SCREEN_HANDLER_TYPE, NecromancerInventoryScreen::new);

        NecromancersShadow.LOCAL_PLAYER = () -> Optional.ofNullable(Minecraft.getInstance().player);
        NecromancersShadow.SCULK_TOTEM_TOOLTIP_APPENDER = (context, textConsumer, type, components) -> {
            DataAttachments.appendEnergyTooltip(context, textConsumer, type, components);
            if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT)) {
                decodeMultiline(SculkTotemItem.HELP_CONTENT_TRANSLATION_KEY, textConsumer);
            } else {
                textConsumer.accept(Component.translatable(SculkTotemItem.HELP_TRANSLATION_KEY));
            }
        };

        NecromancersShadow.NECROMANCER_INVENTORY_OPENER = slot -> ClientPlayNetworking.send(new RequestNecromancerInventoryC2SPacket(slot));
        NecromancersShadow.SCULK_EMERALD_INVENTORY_OPENER = () -> {
            //can only be opened using main hand
            ImmutableShadowStorage heldItemStorage = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND.MAIN_HAND).get(NecromancersShadow.SHADOW_STORAGE_COMPONENT);
            if(heldItemStorage == null) {
                return;
            }
            Minecraft.getInstance().setScreen(new SculkEmeraldScreen(heldItemStorage.toMutable()));
        };
	}

	public static void generateMultiline(BiConsumer<String, String> consumer, String translationKey, List<String> translations) {
		for (int i = 0; i < translations.size(); i++) {
			consumer.accept(translationKey + "_" + i, translations.get(i));
		}
	}

	public static void decodeMultiline(String translationKey, Consumer<Component> applier) {
		for (int i = 0; I18n.exists(translationKey + "_" + i); i++) {
			applier.accept(Component.translatable(translationKey + "_" + i));
		}
	}
}