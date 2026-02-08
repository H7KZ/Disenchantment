package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Adapter for the EnchantsSquared plugin, targeting Minecraft 1.21 - 1.21.4 (v1_21_R1).
 *
 * <p>Reads custom enchantments stored in persistent data containers (PDC) by EnchantsSquared
 * and combines them with vanilla enchantments into the Disenchantment plugin's common
 * {@link IPluginEnchantment} format.</p>
 */
public class EnchantsSquared_v1_21_R1 implements ISupportedPlugin {
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "EnchantsSquared";
    }

    /**
     * {@inheritDoc}
     */
    public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        Map<CustomEnchant, Integer> squaredEnchantments = CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(item);
        List<IPluginEnchantment> enchantments = EnchantmentUtils.getItemEnchantments(item);

        enchantments.addAll(
                squaredEnchantments
                        .entrySet()
                        .stream()
                        .map(entry -> remapEnchantment(entry.getKey(), entry.getValue()))
                        .toList()
        );

        return enchantments;
    }

    /**
     * Wraps an EnchantsSquared {@link CustomEnchant} into an {@link IPluginEnchantment}
     * that delegates add/remove operations to the {@link CustomEnchantManager}.
     *
     * @param enchantment the EnchantsSquared custom enchantment
     * @param level       the enchantment level
     * @return a plugin enchantment adapter for the given custom enchantment
     */
    private static IPluginEnchantment remapEnchantment(CustomEnchant enchantment, int level) {
        return new IPluginEnchantment() {
            @Override
            public String getKey() {
                return enchantment.getType().toLowerCase();
            }

            @Override
            public int getLevel() {
                return level;
            }

            @Override
            public ItemStack addToBook(ItemStack book) {
                ItemStack item = book.clone();

                CustomEnchantManager manager = CustomEnchantManager.getInstance();

                manager.addEnchant(item, enchantment.getType(), level);

                return item;
            }

            @Override
            public ItemStack removeFromBook(ItemStack book) {
                ItemStack item = book.clone();

                CustomEnchantManager manager = CustomEnchantManager.getInstance();

                manager.removeEnchant(item, enchantment.getType());

                return item;
            }

            @Override
            public ItemStack addToItem(ItemStack item) {
                ItemStack result = item.clone();

                CustomEnchantManager manager = CustomEnchantManager.getInstance();

                manager.addEnchant(result, enchantment.getType(), level);

                return result;
            }

            @Override
            public ItemStack removeFromItem(ItemStack item) {
                ItemStack result = item.clone();

                CustomEnchantManager manager = CustomEnchantManager.getInstance();

                manager.removeEnchant(result, enchantment.getType());

                return result;
            }
        };
    }
}
