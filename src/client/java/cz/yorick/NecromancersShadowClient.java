package cz.yorick;

import cz.yorick.networking.SwapSculkEmeraldModesC2SPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.MinecraftClient;
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
		//EntityRendererRegistry.register(NecromancersShadow.SOUL_ENTITY_ENTITY_TYPE, SoulEntityRenderer::new);
		NecromancersShadow.LOCAL_PLAYER = () -> Optional.ofNullable(MinecraftClient.getInstance().player);
		ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
			if(clickCount != 0 && player.getStackInHand(Hand.MAIN_HAND).get(NecromancersShadow.SCULK_EMERALD_MODE_COMPONENT) != null) {
				ClientPlayNetworking.send(new SwapSculkEmeraldModesC2SPacket());
				return true;
			}

			return false;
		});

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