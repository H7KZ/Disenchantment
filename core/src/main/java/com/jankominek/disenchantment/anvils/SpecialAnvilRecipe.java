package com.jankominek.disenchantment.anvils;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.ConfigValues;
import com.jankominek.disenchantment.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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
     * Re-registers the recipe from a thread that may not own the server's recipe manager (the {@code reload}
     * command). On Folia the recipe manager belongs to the global region, so the work is handed to the global
     * scheduler; elsewhere it runs immediately.
     */
    public static void reregister() {
        reregister(SchedulerUtils.isFolia(), task -> SchedulerUtils.runGlobal(plugin, task));
    }

    // Injectable for tests: Folia cannot be started in the unit tests.
    static void reregister(boolean folia, Consumer<Runnable> globalRunner) {
        if (folia) {
            globalRunner.accept(SpecialAnvilRecipe::register);
        } else {
            register();
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
     * Checks the configured recipe shape and ingredients. Used by {@link #build()} (an invalid recipe is not
     * registered) and by the config validator, which reports each message once.
     *
     * @return what is wrong with the recipe, empty when it can be built
     */
    public static List<String> validate() {
        List<String> problems = new ArrayList<>();

        List<String> shape = Config.SpecialAnvils.getRecipeShape();
        if (shape.isEmpty() || shape.size() > 3) {
            problems.add("recipe.shape needs 1 to 3 rows; the recipe is not registered");
            return problems;
        }

        int width = shape.get(0).length();
        for (String row : shape) {
            if (row.isEmpty() || row.length() > 3 || row.length() != width) {
                problems.add("every recipe.shape row needs the same length of 1 to 3 characters; the recipe is not registered");
                return problems;
            }
        }

        Map<Character, String> ingredients = Config.SpecialAnvils.getRecipeIngredients();
        Set<Character> reported = new LinkedHashSet<>();
        for (String row : shape) {
            for (char symbol : row.toCharArray()) {
                if (symbol == ' ' || !reported.add(symbol)) continue;

                String name = ingredients.get(symbol);
                if (name == null || choiceFor(name) == null) {
                    problems.add("recipe.ingredients has no valid material for '" + symbol + "'" + (name == null ? "" : " (\"" + name + "\")") + "; the recipe is not registered");
                }
            }
        }

        return problems;
    }

    /**
     * Builds the recipe from the config.
     *
     * @return the recipe, or {@code null} when the configuration is invalid (see {@link #validate()})
     */
    static ShapedRecipe build() {
        if (!validate().isEmpty()) return null;

        List<String> shape = Config.SpecialAnvils.getRecipeShape();
        Map<Character, String> ingredients = Config.SpecialAnvils.getRecipeIngredients();
        ShapedRecipe recipe = new ShapedRecipe(key(), SpecialAnvilItem.create(1));
        recipe.shape(shape.toArray(new String[0]));

        for (String row : shape) {
            for (char symbol : row.toCharArray()) {
                if (symbol == ' ') continue;

                recipe.setIngredient(symbol, choiceFor(ingredients.get(symbol)));
            }
        }

        return recipe;
    }

    private static RecipeChoice choiceFor(String name) {
        if (name.trim().equalsIgnoreCase("ANVIL")) {
            return new RecipeChoice.MaterialChoice(Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL);
        }

        Material material = ConfigValues.matchMaterial(name);
        if (material == null || !material.isItem() || material.isAir()) return null;

        return new RecipeChoice.MaterialChoice(material);
    }
}
