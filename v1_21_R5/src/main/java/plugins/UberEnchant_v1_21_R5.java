package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import me.sciguymjm.uberenchant.api.utils.UberUtils;
import me.sciguymjm.uberenchant.utils.enchanting.EnchantmentUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter for the UberEnchant plugin, targeting Minecraft 1.21.8 - 1.21.9 (v1_21_R5).
 *
 * <p>Reads enchantments from both the regular and stored enchantment maps provided by
 * the UberEnchant API and maps them into the Disenchantment plugin's common
 * {@link IPluginEnchantment} format.</p>
 */
public class UberEnchant_v1_21_R5 implements ISupportedPlugin {
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "UberEnchant";
    }

    /**
     * {@inheritDoc}
     */
    public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        HashMap<Enchantment, Integer> enchantments = new HashMap<>();

        enchantments.putAll(UberUtils.getAllMap(item));
        enchantments.putAll(UberUtils.getAllStoredMap(item));

        return enchantments
                .entrySet()
                .stream()
                .map(entry -> remapEnchantment(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Wraps a Bukkit {@link Enchantment} into an {@link IPluginEnchantment} that delegates
     * add/remove operations to UberEnchant's {@code EnchantmentUtils}.
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

                EnchantmentUtils.setStoredEnchantment(enchantment, item, level);

                return item;
            }

            @Override
            public ItemStack removeFromBook(ItemStack book) {
                ItemStack item = book.clone();

                EnchantmentUtils.removeEnchantment(enchantment, item);

                return item;
            }

            @Override
            public ItemStack addToItem(ItemStack item) {
                ItemStack result = item.clone();

                EnchantmentUtils.setEnchantment(enchantment, result, level);

                return result;
            }

            @Override
            public ItemStack removeFromItem(ItemStack item) {
                ItemStack result = item.clone();

                EnchantmentUtils.removeEnchantment(enchantment, result);

                return result;
            }
        };
    }
}
