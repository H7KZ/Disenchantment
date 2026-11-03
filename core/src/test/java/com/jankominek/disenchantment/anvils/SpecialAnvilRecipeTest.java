package com.jankominek.disenchantment.anvils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the optional special anvil crafting recipe.
 */
class SpecialAnvilRecipeTest extends DisenchantmentTestBase {

    @Test
    void givenRecipeDisabled_whenRegistered_thenNoRecipe() {
        assertFalse(SpecialAnvilRecipe.register());

        assertNull(Bukkit.getRecipe(SpecialAnvilRecipe.key()));
    }

    @Test
    void givenRecipeEnabled_whenRegistered_thenShapedRecipeWithSpecialResult() {
        setConfig("special-anvils.recipe.enabled", true);

        assertTrue(SpecialAnvilRecipe.register());

        Recipe recipe = Bukkit.getRecipe(SpecialAnvilRecipe.key());
        assertInstanceOf(ShapedRecipe.class, recipe);
        assertTrue(SpecialAnvilItem.isSpecial(recipe.getResult()));
        assertEquals("disenchantment:special_anvil", SpecialAnvilRecipe.key().toString());
        assertArrayEquals(new String[]{"EEE", "EAE", "EEE"}, ((ShapedRecipe) recipe).getShape());
    }

    @Test
    void givenAnvilIngredient_whenBuilt_thenAcceptsEveryDamageState() {
        setConfig("special-anvils.recipe.enabled", true);

        ShapedRecipe recipe = SpecialAnvilRecipe.build();

        assertNotNull(recipe);
        var choice = (org.bukkit.inventory.RecipeChoice.MaterialChoice) recipe.getChoiceMap().get('A');
        assertTrue(choice.getChoices().containsAll(List.of(Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL)));
        var emerald = (org.bukkit.inventory.RecipeChoice.MaterialChoice) recipe.getChoiceMap().get('E');
        assertEquals(List.of(Material.EMERALD), emerald.getChoices());
    }

    @Test
    void givenRegisteredRecipe_whenReRegisteredDisabled_thenRemoved() {
        setConfig("special-anvils.recipe.enabled", true);
        SpecialAnvilRecipe.register();

        setConfig("special-anvils.recipe.enabled", false);
        SpecialAnvilRecipe.register();

        assertNull(Bukkit.getRecipe(SpecialAnvilRecipe.key()));
    }

    @Test
    void givenRegisteredTwice_whenReloaded_thenNoDuplicateFailure() {
        setConfig("special-anvils.recipe.enabled", true);

        assertTrue(SpecialAnvilRecipe.register());
        assertTrue(SpecialAnvilRecipe.register());
    }

    @Test
    void givenCustomShapeAndIngredients_whenBuilt_thenUsed() {
        setConfig("special-anvils.recipe.enabled", true);
        setConfig("special-anvils.recipe.shape", List.of("DD", "DA"));
        setConfig("special-anvils.recipe.ingredients.D", "minecraft:diamond");
        setConfig("special-anvils.recipe.ingredients.A", "anvil");

        ShapedRecipe recipe = SpecialAnvilRecipe.build();

        assertNotNull(recipe);
        assertArrayEquals(new String[]{"DD", "DA"}, recipe.getShape());
    }

    @Test
    void givenInvalidConfig_whenBuilt_thenNullAndNothingRegistered() {
        setConfig("special-anvils.recipe.enabled", true);

        setConfig("special-anvils.recipe.ingredients.E", "NOT_A_MATERIAL");
        assertNull(SpecialAnvilRecipe.build(), "unknown ingredient");

        setConfig("special-anvils.recipe.ingredients.E", "EMERALD");
        setConfig("special-anvils.recipe.shape", List.of("EEEE"));
        assertNull(SpecialAnvilRecipe.build(), "row too wide");

        setConfig("special-anvils.recipe.shape", List.of("EE", "E"));
        assertNull(SpecialAnvilRecipe.build(), "ragged rows");

        setConfig("special-anvils.recipe.shape", List.of());
        assertNull(SpecialAnvilRecipe.build(), "empty shape");

        setConfig("special-anvils.recipe.shape", List.of("EXE"));
        assertNull(SpecialAnvilRecipe.build(), "symbol without ingredient");

        assertFalse(SpecialAnvilRecipe.register());
        assertNull(Bukkit.getRecipe(SpecialAnvilRecipe.key()));
    }

    @Test
    void givenSpacesInShape_whenBuilt_thenEmptySlotsAllowed() {
        setConfig("special-anvils.recipe.enabled", true);
        setConfig("special-anvils.recipe.shape", List.of(" E ", "EAE"));

        assertNotNull(SpecialAnvilRecipe.build());
    }
}
