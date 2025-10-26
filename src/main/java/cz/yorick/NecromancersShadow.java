package cz.yorick;

import com.mojang.serialization.Codec;
import cz.yorick.command.SoulEnergyCommand;
import cz.yorick.data.*;
import cz.yorick.entity.SoulEntity;
import cz.yorick.item.SculkEmeraldItem;
import cz.yorick.item.SculkTotemItem;
import cz.yorick.networking.NecromancerInventorySwapC2SPacket;
import cz.yorick.networking.RequestNecromancerInventoryC2SPacket;
import cz.yorick.networking.SculkEmeraldInventorySwapC2SPacket;
import cz.yorick.screen.NecromancerInventoryScreenHandler;
import cz.yorick.util.EventHandlers;
import cz.yorick.util.ShadowDragonPhase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NecromancersShadow implements ModInitializer {
	public static final String MOD_ID = "necromancers-shadow";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ErrorReporter ERROR_REPORTER = new ErrorReporter.Logging(LOGGER);
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");
    public static final RegistryKey<EntityType<?>> SOUL_ENTITY_REGISTRY_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "soul"));
    public static final EntityType<SoulEntity> SOUL_ENTITY_ENTITY_TYPE = Registry.register(Registries.ENTITY_TYPE, SOUL_ENTITY_REGISTRY_KEY,
            EntityType.Builder
                    .create(SoulEntity::new, SpawnGroup.MISC)
                    .dimensions(0.75F, 0.75F)
                    .dropsNothing()
                    .makeFireImmune()
                    .build(SOUL_ENTITY_REGISTRY_KEY)
    );
	public static final RegistryKey<ScreenHandlerType<?>> NECROMANCER_INVENTORY_REGISTRY_KEY = RegistryKey.of(RegistryKeys.SCREEN_HANDLER, Identifier.of(MOD_ID, "player_shadow_inventory"));
	public static final ScreenHandlerType<NecromancerInventoryScreenHandler> PLAYER_SHADOW_INVENTORY_SCREEN_HANDLER_TYPE = Registry.register(
			Registries.SCREEN_HANDLER,
			NECROMANCER_INVENTORY_REGISTRY_KEY,
			new ExtendedScreenHandlerType<>(NecromancerInventoryScreenHandler::new, PacketCodecs.registryCodec(ImmutableShadowStorage.CODEC))
	);

    //sync only partially (for text display), rest is synced on ui open
	public static final ComponentType<ImmutableShadowStorage> SHADOW_STORAGE_COMPONENT = registerComponent("shadow_storage", ImmutableShadowStorage.CODEC, PacketCodecs.registryCodec(ImmutableShadowStorage.SYNC_CODEC));
	public static final ConsumeEffect.Type<MaxSoulEnergyGainConsumeEffect> SOUL_ENERGY_GAIN_EFFECT = Registry.register(Registries.CONSUME_EFFECT_TYPE, Identifier.of(MOD_ID, "soul_energy_gain"), MaxSoulEnergyGainConsumeEffect.TYPE);
	public static final SculkTotemItem SCULK_TOTEM = registerItem("sculk_totem", SculkTotemItem::new);
	public static final Item SCULK_EMERALD = registerItem("sculk_emerald", SculkEmeraldItem::new);
	public static final PhaseType<ShadowDragonPhase> SHADOW_DRAGON_PHASE = PhaseType.register(ShadowDragonPhase.class, "Shadow");
	//since methods checking for tooltip and item bar step do not provide a player (tooltip can be done with a mixin, but
	//not the others), this provides Optional.empty() on the server and MinecraftClient.getInstance().player on the client
    public static Supplier<Optional<PlayerEntity>> LOCAL_PLAYER = Optional::empty;
    public static TooltipAppender SCULK_TOTEM_TOOLTIP_APPENDER = (context, textConsumer, type, components) -> {};
    public static Consumer<Integer> NECROMANCER_INVENTORY_OPENER = slot -> {};
    public static Runnable SCULK_EMERALD_INVENTORY_OPENER = () -> {};
	@Override
	public void onInitialize() {
        SoulEntity.registerTrackedData();
        DataAttachments.init();
		EventHandlers.init();
        NecromancerInventorySwapC2SPacket.init();
        SculkEmeraldInventorySwapC2SPacket.init();
        RequestNecromancerInventoryC2SPacket.init();
        ComponentTooltipAppenderRegistry.addFirst(SHADOW_STORAGE_COMPONENT);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SoulEnergyCommand.init(dispatcher));
	}

	private static <T extends Item> T registerItem(String name, Function<Item.Settings, T> factory) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
		return Registry.register(Registries.ITEM, key, factory.apply(new Item.Settings().registryKey(key)));
	}

    //fairly large component, from my understanding caching should keep it from decoding every time the component is queried
	private static<T> ComponentType<T> registerComponent(String name, Codec<T> codec, PacketCodec<RegistryByteBuf, T> syncCodec) {
		return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, name), ComponentType.<T>builder().codec(codec).packetCodec(syncCodec).cache().build());
	}
}