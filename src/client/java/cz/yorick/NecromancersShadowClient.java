package cz.yorick;

import cz.yorick.data.DataAttachments;
import cz.yorick.data.ImmutableShadowStorage;
import cz.yorick.item.SculkTotemItem;
import cz.yorick.networking.RequestNecromancerInventoryC2SPacket;
import cz.yorick.screen.NecromancerInventoryScreen;
import cz.yorick.screen.SculkEmeraldScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NecromancersShadowClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        EntityRendererFactories.register(NecromancersShadow.SOUL_ENTITY_ENTITY_TYPE, SoulEntityRenderer::new);
		HandledScreens.register(NecromancersShadow.PLAYER_SHADOW_INVENTORY_SCREEN_HANDLER_TYPE, NecromancerInventoryScreen::new);

        NecromancersShadow.LOCAL_PLAYER = () -> Optional.ofNullable(MinecraftClient.getInstance().player);
        NecromancersShadow.SCULK_TOTEM_TOOLTIP_APPENDER = (context, textConsumer, type, components) -> {
            DataAttachments.appendEnergyTooltip(context, textConsumer, type, components);
            if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), InputUtil.GLFW_KEY_LEFT_SHIFT)) {
                decodeMultiline(SculkTotemItem.HELP_CONTENT_TRANSLATION_KEY, textConsumer);
            } else {
                textConsumer.accept(Text.translatable(SculkTotemItem.HELP_TRANSLATION_KEY));
            }
        };

        NecromancersShadow.NECROMANCER_INVENTORY_OPENER = slot -> ClientPlayNetworking.send(new RequestNecromancerInventoryC2SPacket(slot));
        NecromancersShadow.SCULK_EMERALD_INVENTORY_OPENER = () -> {
            //can only be opened using main hand
            ImmutableShadowStorage heldItemStorage = MinecraftClient.getInstance().player.getStackInHand(Hand.MAIN_HAND).get(NecromancersShadow.SHADOW_STORAGE_COMPONENT);
            if(heldItemStorage == null) {
                return;
            }
            MinecraftClient.getInstance().setScreen(new SculkEmeraldScreen(heldItemStorage.toMutable()));
        };
	}

	public static void generateMultiline(BiConsumer<String, String> consumer, String translationKey, List<String> translations) {
		for (int i = 0; i < translations.size(); i++) {
			consumer.accept(translationKey + "_" + i, translations.get(i));
		}
	}

	public static void decodeMultiline(String translationKey, Consumer<Text> applier) {
		for (int i = 0; I18n.hasTranslation(translationKey + "_" + i); i++) {
			applier.accept(Text.translatable(translationKey + "_" + i));
		}
	}
}