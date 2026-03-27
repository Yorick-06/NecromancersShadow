package cz.yorick;

import cz.yorick.command.SoulEnergyCommand;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ImmutableShadowStorage;
import cz.yorick.item.SculkEmeraldItem;
import cz.yorick.item.SculkTotemItem;
import cz.yorick.screen.SculkEmeraldScreen;
import cz.yorick.screen.widget.ShadowPreviewWidget;
import cz.yorick.screen.widget.ToggleSummonWidget;
import cz.yorick.util.ShadowDamageSource;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.client.data.*;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
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

	private FabricLanguageProvider genLang(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryFuture) {
		return new FabricLanguageProvider(output, registryFuture) {
			@Override
			public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {

                //commands
				translationBuilder.add(SoulEnergyCommand.MODIFIED_TRANSLATION_KEY, "Modified the data of %d players");
				translationBuilder.add(SoulEnergyCommand.GET_ENERGY_TRANSLATION_KEY, "%d has %d soul energy");
				translationBuilder.add(SoulEnergyCommand.GET_MAX_ENERGY_TRANSLATION_KEY, "%d has %d max soul energy");
                //death messages
				translationBuilder.add(ShadowDamageSource.GENERIC_DEATH, "%d was killed by a shadow");
				translationBuilder.add(ShadowDamageSource.KILLED_BY_PLAYER, "%1d was killed by %d's shadow");
				translationBuilder.add(ShadowDamageSource.KILLED_BY_PLAYER_SHADOW, "%d was killed by %d's shadow %d");
                //entity
                translationBuilder.add(NecromancersShadow.SOUL_ENTITY_ENTITY_TYPE, "Soul");

                //items
                translationBuilder.add(NecromancersShadow.SCULK_EMERALD, "Sculk Emerald");
                translationBuilder.add(SculkEmeraldScreen.TITLE_TRANSLATION_KEY, "Sculk Emerald Inventory");
				NecromancersShadowClient.generateMultiline(translationBuilder::add, SculkEmeraldItem.HELP_TRANSLATION_KEY, List.of(
                        "",
						"§7Right clicking on a soul will insert it into this item instead of your inventory",
						"§7Right click to open the ui"
				));

                //sculk totem
                translationBuilder.add(NecromancersShadow.SCULK_TOTEM, "Sculk Totem");
                translationBuilder.add(SculkTotemItem.HELP_TRANSLATION_KEY, "§7Press shift to show more info");
                NecromancersShadowClient.generateMultiline(translationBuilder::add, SculkTotemItem.HELP_CONTENT_TRANSLATION_KEY, List.of(
                        "",
                        "§7Holding this item in your hand will cause the entities you kill to drop their soul,",
                        "§7it will also convert absorbed xp orbs into soul energy (instead of going to the xp bar/mending)",
                        "§7Right click to spawn/despawn shadows present in your shadow inventory",
                        "§7Hitting an entity with this totem makes all spawned shadows target it",
                        "§7Shift right clicking stops the shadows from attacking",
                        "§7Right clicking on the item inside your inventory opens your necromancer inventory"
                ));

                translationBuilder.add(SculkTotemItem.NECROMANCER_INVENTORY_TRANSLATION_KEY, "Necromancer Inventory");
                translationBuilder.add(ToggleSummonWidget.TOGGLE_SUMMON_TRANSLATION_KEY, "Click to summon/unsummon this shadow");


                //preview
                NecromancersShadowClient.generateMultiline(translationBuilder::add, ShadowPreviewWidget.HELP_TRANSLATION_KEY, List.of(
                        "Trash can ->",
                        "Clicking on souls:",
                        "Left -> pick up",
                        "Right -> preview",
                        "Preview:",
                        "Left -> rotate",
                        "Wheel -> zoom"
                ));

                translationBuilder.add(ShadowPreviewWidget.TYPE_TRANSLATION_KEY, "Type: ");
                translationBuilder.add(ShadowPreviewWidget.SUMMON_COST_TRANSLATION_KEY, "Summon cost: ");
                translationBuilder.add(ShadowPreviewWidget.HEALTH_TRANSLATION_KEY, "Health: ");
                translationBuilder.add(ShadowPreviewWidget.DAMAGE_TRANSLATION_KEY, "Damage: ");

                //data
                translationBuilder.add(DataAttachments.SOUL_ENERGY_TRANSLATION_KEY, "§7Soul energy: ");
                translationBuilder.add(ImmutableShadowStorage.STORED_SHADOWS_TRANSLATION_KEY, "Stored shadows:");

			}
		};
	}

	private FabricModelProvider genModels(FabricDataOutput output) {
		return new FabricModelProvider(output) {
			@Override
			public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
			}

			@Override
			public void generateItemModels(ItemModelGenerators itemModelGenerator) {
				itemModelGenerator.generateFlatItem(NecromancersShadow.SCULK_TOTEM, ModelTemplates.FLAT_ITEM);
				itemModelGenerator.generateFlatItem(NecromancersShadow.SCULK_EMERALD, ModelTemplates.FLAT_ITEM);
			}
		};
	}

	private FabricRecipeProvider genRecipes(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryFuture) {
		return new FabricRecipeProvider(output, registryFuture) {
			@Override
			protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
				return new RecipeProvider(registryLookup, exporter) {
					@Override
					public void buildRecipes() {
						ShapelessRecipeBuilder.shapeless(registryLookup.lookupOrThrow(Registries.ITEM), RecipeCategory.COMBAT, NecromancersShadow.SCULK_TOTEM)
                                .requires(Items.TOTEM_OF_UNDYING)
								.requires(Items.ECHO_SHARD)
                                .unlockedBy(getHasName(Items.TOTEM_OF_UNDYING), has(Items.TOTEM_OF_UNDYING))
                                .save(this.output);

						ShapelessRecipeBuilder.shapeless(registryLookup.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, NecromancersShadow.SCULK_EMERALD)
								.requires(Items.EMERALD)
								.requires(Items.ECHO_SHARD)
								.unlockedBy(getHasName(Items.ECHO_SHARD), has(Items.ECHO_SHARD))
								.save(this.output);
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
