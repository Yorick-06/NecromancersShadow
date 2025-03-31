package cz.yorick.recipe;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;

import java.util.Optional;

public class SmithingUpgradeRecipe implements SmithingRecipe {
    @Override
    public ItemStack craft(SmithingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return null;
    }

    @Override
    public RecipeSerializer<? extends SmithingRecipe> getSerializer() {
        return null;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return null;
    }

    @Override
    public Optional<Ingredient> template() {
        return Optional.empty();
    }

    @Override
    public Ingredient base() {
        return null;
    }

    @Override
    public Optional<Ingredient> addition() {
        return Optional.empty();
    }
}
