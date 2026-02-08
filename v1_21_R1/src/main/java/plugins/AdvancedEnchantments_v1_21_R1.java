package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import net.advancedplugins.ae.api.AEAPI;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter for the AdvancedEnchantments plugin, targeting Minecraft 1.21 - 1.21.4 (v1_21_R1).
 *
 * <p>Bridges AdvancedEnchantments custom enchantments and vanilla enchantments into
 * the Disenchantment plugin's common {@link IPluginEnchantment} representation. Handles
 * the special case where AE stores book enchantments differently from item enchantments.</p>
 */
public class AdvancedEnchantments_v1_21_R1 implements ISupportedPlugin {
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "AdvancedEnchantments";
    }

    /**
     * {@inheritDoc}
     */
    public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        List<IPluginEnchantment> enchantments = getItemVanillaEnchantments(item);

        Map<String, Integer> advancedEnchantments = AEAPI.getEnchantmentsOnItem(item);

        for (Map.Entry<String, Integer> entry : advancedEnchantments.entrySet()) {
            String key = entry.getKey();
            int level = entry.getValue();

            if (enchantments.stream().anyMatch(enchantment -> enchantment.getKey().equals(key))) continue;

            enchantments.add(remapEnchantment(key, level));
        }

        // In AE we can expect only one single enchantment on the book
        // For some reason AE .getEnchantmentsOnItem(item) does not return book enchantments
        if (item.getType() == Material.ENCHANTED_BOOK && enchantments.isEmpty()) {
            String bookKey = AEAPI.getBookEnchantment(item);
            int bookLevel = AEAPI.getBookEnchantmentLevel(item);

            enchantments.add(remapEnchantment(bookKey, bookLevel));
        }

        return enchantments;
    }

    /**
     * Extracts vanilla enchantments from the given item, handling both regular items
     * and enchanted books (via {@link EnchantmentStorageMeta}).
     *
     * @param item the item to inspect
     * @return a mutable list of vanilla enchantments as {@link IPluginEnchantment} instances
     */
    private static List<IPluginEnchantment> getItemVanillaEnchantments(ItemStack item) {
        List<IPluginEnchantment> enchantments;

        if (item.hasItemMeta() && item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            enchantments = meta.getStoredEnchants()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() != null && entry.getValue() > 0)
                    .map(entry -> remapEnchantment(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        } else {
            enchantments = item.getEnchantments()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() != null && entry.getValue() > 0)
                    .map(entry -> remapEnchantment(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

        return enchantments;
    }


    /**
     * Wraps an AdvancedEnchantments custom enchantment (identified by string key) into
     * an {@link IPluginEnchantment} that delegates add/remove operations to the AE API.
     *
     * @param key   the AE enchantment key
     * @param level the enchantment level
     * @return a plugin enchantment adapter for the given AE enchantment
     */
    private static IPluginEnchantment remapEnchantment(String key, int level) {
        return new IPluginEnchantment() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public int getLevel() {
                return level;
            }

            @Override
            public ItemStack addToBook(ItemStack book) {
                ItemStack item = book.clone();

                if (!item.getEnchantments().isEmpty()) return item;
                if (AEAPI.getBookEnchantment(item) != null) return item;

                item = AEAPI.createEnchantmentBook(key, level, 100, 0, null);

                return item;
            }

            @Override
            public ItemStack removeFromBook(ItemStack book) {
                ItemStack item = book.clone();

                item = AEAPI.removeEnchantment(item, key);

                return item;
            }

            @Override
            public ItemStack addToItem(ItemStack item) {
                ItemStack result = item.clone();

                result = AEAPI.applyEnchant(key, level, result);

                return result;
            }

            @Override
            public ItemStack removeFromItem(ItemStack item) {
                ItemStack result = item.clone();

                result = AEAPI.removeEnchantment(result, key);

                return result;
            }
        };
    }

    /**
     * Wraps a vanilla {@link Enchantment} into an {@link IPluginEnchantment} that uses
     * standard Bukkit utilities for add/remove operations, while coexisting with AE books.
     *
     * @param enchantment the vanilla enchantment
     * @param level       the enchantment level
     * @return a plugin enchantment adapter for the given vanilla enchantment
     */
    private static IPluginEnchantment remapEnchantment(Enchantment enchantment, int level) {
        return new IPluginEnchantment() {
            @Override
            public String getKey() {
                return enchantment.getKey().getKey().toLowerCase();
            }

            @Override
            public int getLevel() {
                return level;
            }

            @Override
            public ItemStack addToBook(ItemStack book) {
                ItemStack item = book.clone();

                if (AEAPI.getBookEnchantment(item) != null) return item;

                Enchantment vanillaEnchantment = Registry.ENCHANTMENT.get(enchantment.getKey());

                if (vanillaEnchantment == null) return item;

                EnchantmentUtils.addStoredEnchantment(item, vanillaEnchantment, this.getLevel());

                return item;
            }

            @Override
            public ItemStack removeFromBook(ItemStack book) {
                ItemStack item = book.clone();
                EnchantmentUtils.removeStoredEnchantment(item, enchantment);
                return item;
            }

            @Override
            public ItemStack addToItem(ItemStack item) {
                ItemStack result = item.clone();
                EnchantmentUtils.addStoredEnchantment(result, enchantment, this.getLevel());
                return result;
            }

            @Override
            public ItemStack removeFromItem(ItemStack item) {
                ItemStack result = item.clone();
                EnchantmentUtils.removeStoredEnchantment(result, enchantment);
                return result;
            }
        };
    }
}
