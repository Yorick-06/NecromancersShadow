package cz.yorick;

import cz.yorick.screen.ShadowInventoryScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NecromancersShadowClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        EntityRendererFactories.register(NecromancersShadow.SOUL_ENTITY_ENTITY_TYPE, SoulEntityRenderer::new);
		HandledScreens.register(NecromancersShadow.NECROMANCER_INVENTORY_SCREEN_HANDLER_TYPE, ShadowInventoryScreen::new);
		NecromancersShadow.LOCAL_PLAYER = () -> Optional.ofNullable(MinecraftClient.getInstance().player);
		NecromancersShadow.HAS_SHIFT_DOWN = () -> InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), InputUtil.GLFW_KEY_LEFT_SHIFT);
		NecromancersShadow.MULTILINE_TOOLTIP_DECODER = NecromancersShadowClient::decodeMultiline;
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