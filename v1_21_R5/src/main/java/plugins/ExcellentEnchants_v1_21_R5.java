package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.excellentenchants.EnchantsUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter for the ExcellentEnchants plugin, targeting Minecraft 1.21.8 - 1.21.+ (v1_21_R5).
 *
 * <p>Uses the ExcellentEnchants {@code EnchantsUtils} API to read, add, and remove
 * enchantments, mapping them into the Disenchantment plugin's common
 * {@link IPluginEnchantment} format.</p>
 */
public class ExcellentEnchants_v1_21_R5 implements ISupportedPlugin {
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "ExcellentEnchants";
    }

    /**
     * {@inheritDoc}
     */
    public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        return EnchantsUtils.getEnchantments(item)
                .entrySet()
                .stream()
                .map(entry -> remapEnchantment(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Wraps a Bukkit {@link Enchantment} (including ExcellentEnchants custom ones) into
     * an {@link IPluginEnchantment} that delegates to the ExcellentEnchants API.
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
                ItemStack item = book.clone();

                EnchantsUtils.add(item, enchantment, level, true);

                return item;
            }

            @Override
            public ItemStack removeFromBook(ItemStack book) {
                ItemStack item = book.clone();

                EnchantsUtils.remove(item, enchantment);

                return item;
            }

            @Override
            public ItemStack addToItem(ItemStack item) {
                ItemStack result = item.clone();

                EnchantsUtils.add(result, enchantment, level, true);

                return result;
            }

            @Override
            public ItemStack removeFromItem(ItemStack item) {
                ItemStack result = item.clone();

                EnchantsUtils.remove(result, enchantment);

                return result;
            }
        };
    }
}
