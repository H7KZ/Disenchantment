package com.jankominek.disenchantment.anvils;

import com.jankominek.disenchantment.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;
import java.util.Map;

import static com.jankominek.disenchantment.Disenchantment.logger;
import static com.jankominek.disenchantment.Disenchantment.plugin;

/**
 * Registers the optional shaped crafting recipe for the special anvil item
 * ({@code special-anvils.recipe}). The recipe key is {@code disenchantment:special_anvil}; the
 * recipe is removed again on disable and before every re-registration on reload.
 *
 * <p>The ingredient name {@code ANVIL} stands for any of the three anvil damage states.</p>
 */
public class SpecialAnvilRecipe {
    private static final String KEY_NAME = "special_anvil";

    private SpecialAnvilRecipe() {
    }

    /**
     * The recipe's namespaced key.
     *
     * @return {@code disenchantment:special_anvil}
     */
    public static NamespacedKey key() {
        return new NamespacedKey(plugin, KEY_NAME);
    }

    /**
     * Removes the recipe if it is registered, then registers it again when
     * {@code special-anvils.recipe.enabled} is on and the configured recipe is valid. An invalid recipe
     * is skipped with a warning; it never prevents the plugin from starting.
     *
     * @return {@code true} if the recipe is registered afterwards
     */
    public static boolean register() {
        unregister();

        if (!Config.SpecialAnvils.isRecipeEnabled()) return false;

        try {
            ShapedRecipe recipe = build();
            if (recipe == null) return false;

            return Bukkit.addRecipe(recipe);
        } catch (Exception e) {
            logger.warning("Config: the special anvil recipe could not be registered: " + e.getMessage());
            return false;
        }
    }

    /**
     * Removes the recipe from the server. A no-op when it is not registered.
     */
    public static void unregister() {
        try {
            Bukkit.removeRecipe(key());
        } catch (Exception e) {
            logger.warning("Could not remove the special anvil recipe: " + e.getMessage());
        }
    }

    /**
     * Builds the recipe from the config.
     *
     * @return the recipe, or {@code null} (after logging why) when the configuration is invalid
     */
    static ShapedRecipe build() {
        List<String> shape = Config.SpecialAnvils.getRecipeShape();
        if (shape.isEmpty() || shape.size() > 3) {
            logger.warning("Config: special-anvils.recipe.shape needs 1 to 3 rows — recipe not registered");
            return null;
        }

        int width = shape.get(0).length();
        for (String row : shape) {
            if (row.isEmpty() || row.length() > 3 || row.length() != width) {
                logger.warning("Config: every special-anvils.recipe.shape row needs the same length of 1 to 3 characters — recipe not registered");
                return null;
            }
        }

        Map<Character, String> ingredients = Config.SpecialAnvils.getRecipeIngredients();
        ShapedRecipe recipe = new ShapedRecipe(key(), SpecialAnvilItem.create(1));
        recipe.shape(shape.toArray(new String[0]));

        for (String row : shape) {
            for (char symbol : row.toCharArray()) {
                if (symbol == ' ') continue;

                String name = ingredients.get(symbol);
                RecipeChoice choice = name == null ? null : choiceFor(name);
                if (choice == null) {
                    logger.warning("Config: special-anvils.recipe.ingredients has no valid material for '" + symbol + "' — recipe not registered");
                    return null;
                }

                recipe.setIngredient(symbol, choice);
            }
        }

        return recipe;
    }

    private static RecipeChoice choiceFor(String name) {
        if (name.trim().equalsIgnoreCase("ANVIL")) {
            return new RecipeChoice.MaterialChoice(Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL);
        }

        Material material = Material.matchMaterial(name.trim());
        if (material == null || !material.isItem() || material.isAir()) return null;

        return new RecipeChoice.MaterialChoice(material);
    }
}
