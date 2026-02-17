package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.Plugin;
import org.oddlama.vane.core.Core;
import org.oddlama.vane.core.enchantments.EnchantmentManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter for the Vane enchantments plugin, targeting Minecraft 1.21.8+ (v1_21_R5).
 *
 * <p>Vane enchantments are registered as standard Bukkit enchantments through the Paper registry,
 * but require special handling via Vane's {@link EnchantmentManager} to maintain proper lore
 * formatting.</p>
 */
public class Vane_v1_21_R5 implements ISupportedPlugin {
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "vane";
    }

    /**
     * {@inheritDoc}
     */
    public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        Map<Enchantment, Integer> enchantments;

        // Get enchantments based on item type
        if (item.getType() == Material.ENCHANTED_BOOK && item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            enchantments = meta.getStoredEnchants();
        } else {
            enchantments = item.getEnchantments();
        }

        // Filter for Vane enchantments only (namespace starts with "vane")
        return enchantments
                .entrySet()
                .stream()
                .filter(entry -> isVaneEnchantment(entry.getKey()))
                .map(entry -> remapEnchantment(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Checks if the given enchantment is a Vane enchantment by inspecting its namespace.
     *
     * @param enchantment the enchantment to check
     * @return true if the enchantment namespace starts with "vane", false otherwise
     */
    private static boolean isVaneEnchantment(Enchantment enchantment) {
        return enchantment.getKey().getNamespace().startsWith("vane");
    }

    /**
     * Gets the Vane EnchantmentManager instance for updating item lore.
     *
     * @return the EnchantmentManager, or null if Vane Core is not loaded
     */
    private static EnchantmentManager getEnchantmentManager() {
        Plugin vaneCore = Bukkit.getPluginManager().getPlugin("vane-core");

        if (vaneCore instanceof Core core) {
            return core.enchantment_manager;
        }

        return null;
    }

    /**
     * Wraps a Bukkit {@link Enchantment} into an {@link IPluginEnchantment} that delegates
     * to standard Bukkit methods with Vane's {@link EnchantmentManager} for lore updates.
     *
     * @param enchantment the enchantment to wrap
     * @param level       the enchantment level
     * @return a plugin enchantment adapter for the given enchantment
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
                ItemStack result = book.clone();

                // Convert book to enchanted book if necessary
                if (result.getType() == Material.BOOK) {
                    result.setType(Material.ENCHANTED_BOOK);
                }

                // Add stored enchantment to book
                if (result.getItemMeta() instanceof EnchantmentStorageMeta meta) {
                    meta.addStoredEnchant(enchantment, level, false);
                    result.setItemMeta(meta);
                }

                // Update lore using Vane's enchantment manager
                EnchantmentManager manager = getEnchantmentManager();
                if (manager != null) {
                    manager.update_enchanted_item(result);
                }

                return result;
            }

            @Override
            public ItemStack removeFromBook(ItemStack book) {
                ItemStack result = book.clone();

                // Remove stored enchantment from book
                if (result.getItemMeta() instanceof EnchantmentStorageMeta meta) {
                    meta.removeStoredEnchant(enchantment);
                    result.setItemMeta(meta);
                }

                // Update lore using Vane's enchantment manager
                EnchantmentManager manager = getEnchantmentManager();
                if (manager != null) {
                    manager.update_enchanted_item(result);
                }

                return result;
            }

            @Override
            public ItemStack addToItem(ItemStack item) {
                ItemStack result = item.clone();

                // Add enchantment to item
                result.addEnchantment(enchantment, level);

                // Update lore using Vane's enchantment manager
                EnchantmentManager manager = getEnchantmentManager();
                if (manager != null) {
                    manager.update_enchanted_item(result);
                }

                return result;
            }

            @Override
            public ItemStack removeFromItem(ItemStack item) {
                ItemStack result = item.clone();

                // Remove enchantment from item
                result.removeEnchantment(enchantment);

                // Update lore using Vane's enchantment manager
                EnchantmentManager manager = getEnchantmentManager();
                if (manager != null) {
                    manager.update_enchanted_item(result);
                }

                return result;
            }
        };
    }
}
