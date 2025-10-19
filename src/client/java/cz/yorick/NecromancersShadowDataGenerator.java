package cz.yorick;

import cz.yorick.command.SoulEnergyCommand;
import cz.yorick.data.NecromancerData;
import cz.yorick.data.SculkEmeraldMode;
import cz.yorick.data.ShadowData;
import cz.yorick.item.SculkEmeraldItem;
import cz.yorick.item.SculkTotemItem;
import cz.yorick.util.ShadowDamageSource;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.client.data.*;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NecromancersShadowDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(this::genLang);
		pack.addProvider(this::genModels);
		pack.addProvider(this::genRecipes);
	}

	private FabricLanguageProvider genLang(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryFuture) {
		return new FabricLanguageProvider(output, registryFuture) {
			@Override
			public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
				translationBuilder.add(NecromancersShadow.SCULK_TOTEM, "Sculk Totem");
				translationBuilder.add(NecromancersShadow.SCULK_EMERALD, "Sculk Emerald");
				translationBuilder.add(ShadowData.STORED_SHADOWS_TRANSLATION_KEY, "§7Stored shadows:");
				translationBuilder.add(NecromancerData.SOUL_ENERGY_TRANSLATION_KEY, "§7Soul energy: ");
                translationBuilder.add(SculkTotemItem.NECROMANCER_INVENTORY_TRANSLATION_KEY, "Necromancer Inventory");
                translationBuilder.add(SculkEmeraldItem.INVENTORY_TRANSLATION_KEY, "Sculk Emerald Inventory");

				translationBuilder.add(SculkEmeraldMode.MODE_TRANSLATION_KEY, "§7Mode: ");
				translationBuilder.add(SculkEmeraldMode.INPUT_TRANSLATION_KEY, "§aINPUT");
				translationBuilder.add(SculkEmeraldMode.OUTPUT_TRANSLATION_KEY, "§aOUTPUT");
				translationBuilder.add(NecromancersShadow.HELP_TRANSLATION_KEY, "§7Press shift to show more info");
				translationBuilder.add(SoulEnergyCommand.MODIFIED_TRANSLATION_KEY, "Modified the data of %d players");
				translationBuilder.add(SoulEnergyCommand.GET_ENERGY_TRANSLATION_KEY, "%d has %d soul energy");
				translationBuilder.add(SoulEnergyCommand.GET_MAX_ENERGY_TRANSLATION_KEY, "%d has %d max soul energy");
				translationBuilder.add(NecromancersShadow.SOUL_ENTITY_ENTITY_TYPE, "Soul");
				translationBuilder.add(ShadowDamageSource.GENERIC_DEATH, "%d was killed by a shadow");
				translationBuilder.add(ShadowDamageSource.KILLED_BY_PLAYER, "%1d was killed by %d's shadow");
				translationBuilder.add(ShadowDamageSource.KILLED_BY_PLAYER_SHADOW, "%d was killed by %d's shadow %d");

				NecromancersShadowClient.generateMultiline(translationBuilder::add, SculkEmeraldItem.HELP_TRANSLATION_KEY, List.of(
                        "",
						"§7Right clicking on a soul will insert it into this item instead of your inventory",
						"§7Left click to swap modes",
						"§7Mode: §aINPUT",
						"  §7Right click to transfer the first shadow from your inventory to this item",
						"  §7Shift right click to transfer all shadows from your inventory to this item",
						"  §7Right clicking on a spawned shadow will despawn it and insert it into this items inventory",
						"§7Mode: §aOUTPUT",
						"  §7Right click to transfer the first shadow from this item to your inventory",
						"  §7Shift right click to transfer all shadows from this item to your inventory",
                        "§7Placing this item into a crafting grid will output an empty sculk emerald",
                        "§cPERNAMENTLY DELETING §7all shadows inside"
				));

                NecromancersShadowClient.generateMultiline(translationBuilder::add, SculkTotemItem.HELP_TRANSLATION_KEY, List.of(
                        "",
                        "§7Holding this item in your hand will cause the entities you kill to drop their soul,",
                        "§7it will also convert absorbed xp orbs into soul energy (instead of going to the xp bar/mending)",
                        "§7Right click to spawn/despawn shadows present in your shadow inventory",
                        "§7Hitting an entity with this totem makes all spawned shadows target it",
                        "§7Shift right clicking stops the shadows from attacking"
                ));
			}
		};
	}

	private FabricModelProvider genModels(FabricDataOutput output) {
		return new FabricModelProvider(output) {
			@Override
			public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
			}

			@Override
			public void generateItemModels(ItemModelGenerator itemModelGenerator) {
				itemModelGenerator.register(NecromancersShadow.SCULK_TOTEM, Models.GENERATED);
				itemModelGenerator.register(NecromancersShadow.SCULK_EMERALD, Models.GENERATED);
                itemModelGenerator.register(NecromancersShadow.SOUL_ITEM, Models.GENERATED);
			}
		};
	}

	private FabricRecipeProvider genRecipes(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryFuture) {
		return new FabricRecipeProvider(output, registryFuture) {
			@Override
			protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
				return new RecipeGenerator(registryLookup, exporter) {
					@Override
					public void generate() {
						ShapelessRecipeJsonBuilder.create(registryLookup.getOrThrow(RegistryKeys.ITEM), RecipeCategory.COMBAT, NecromancersShadow.SCULK_TOTEM)
								.input(Items.TOTEM_OF_UNDYING)
								.input(Items.ECHO_SHARD)
								.criterion(hasItem(Items.TOTEM_OF_UNDYING), conditionsFromItem(Items.TOTEM_OF_UNDYING))
								.offerTo(this.exporter);

						ShapelessRecipeJsonBuilder.create(registryLookup.getOrThrow(RegistryKeys.ITEM), RecipeCategory.MISC, NecromancersShadow.SCULK_EMERALD)
								.input(Items.EMERALD)
								.input(Items.ECHO_SHARD)
								.criterion(hasItem(Items.ECHO_SHARD), conditionsFromItem(Items.ECHO_SHARD))
								.offerTo(this.exporter, "sculk_emerald");
					}
				};
			}

			@Override
			public String getName() {
				return NecromancersShadow.MOD_ID + " recipe generator";
			}
		};
	}
}
