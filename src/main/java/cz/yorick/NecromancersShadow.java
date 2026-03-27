package cz.yorick;

import com.mojang.serialization.Codec;
import cz.yorick.command.SoulEnergyCommand;
import cz.yorick.data.*;
import cz.yorick.entity.SoulEntity;
import cz.yorick.item.SculkEmeraldItem;
import cz.yorick.item.SculkTotemItem;
import cz.yorick.mixin.EnderDragonPhaseInvoker;
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
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
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
    public static final ProblemReporter ERROR_REPORTER = new ProblemReporter.ScopedCollector(LOGGER);
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");
    public static final ResourceKey<EntityType<?>> SOUL_ENTITY_REGISTRY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "soul"));
    public static final EntityType<SoulEntity> SOUL_ENTITY_ENTITY_TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE, SOUL_ENTITY_REGISTRY_KEY,
            EntityType.Builder
                    .of(SoulEntity::new, MobCategory.MISC)
                    .sized(0.75F, 0.75F)
                    .noLootTable()
                    .fireImmune()
                    .build(SOUL_ENTITY_REGISTRY_KEY)
    );
	public static final ResourceKey<MenuType<?>> NECROMANCER_INVENTORY_REGISTRY_KEY = ResourceKey.create(Registries.MENU, Identifier.fromNamespaceAndPath(MOD_ID, "player_shadow_inventory"));
	public static final MenuType<NecromancerInventoryScreenHandler> PLAYER_SHADOW_INVENTORY_SCREEN_HANDLER_TYPE = Registry.register(
			BuiltInRegistries.MENU,
			NECROMANCER_INVENTORY_REGISTRY_KEY,
			new ExtendedScreenHandlerType<>(NecromancerInventoryScreenHandler::new, ByteBufCodecs.fromCodecWithRegistries(ImmutableShadowStorage.CODEC))
	);

    //sync only partially (for text display), rest is synced on ui open
	public static final DataComponentType<ImmutableShadowStorage> SHADOW_STORAGE_COMPONENT = registerComponent("shadow_storage", ImmutableShadowStorage.CODEC, ByteBufCodecs.fromCodecWithRegistries(ImmutableShadowStorage.SYNC_CODEC));
	public static final ConsumeEffect.Type<MaxSoulEnergyGainConsumeEffect> SOUL_ENERGY_GAIN_EFFECT = Registry.register(BuiltInRegistries.CONSUME_EFFECT_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "soul_energy_gain"), MaxSoulEnergyGainConsumeEffect.TYPE);
	public static final SculkTotemItem SCULK_TOTEM = registerItem("sculk_totem", SculkTotemItem::new);
	public static final Item SCULK_EMERALD = registerItem("sculk_emerald", SculkEmeraldItem::new);
	public static final EnderDragonPhase<ShadowDragonPhase> SHADOW_DRAGON_PHASE = EnderDragonPhaseInvoker.invokeCreate(ShadowDragonPhase.class, "Shadow");
	//since methods checking for tooltip and item bar step do not provide a player (tooltip can be done with a mixin, but
	//not the others), this provides Optional.empty() on the server and MinecraftClient.getInstance().player on the client
    public static Supplier<Optional<Player>> LOCAL_PLAYER = Optional::empty;
    public static TooltipProvider SCULK_TOTEM_TOOLTIP_APPENDER = (context, textConsumer, type, components) -> {};
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

	private static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
	}

    //fairly large component, from my understanding caching should keep it from decoding every time the component is queried
	private static<T> DataComponentType<T> registerComponent(String name, Codec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> syncCodec) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name), DataComponentType.<T>builder().persistent(codec).networkSynchronized(syncCodec).cacheEncoding().build());
	}
}