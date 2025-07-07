package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class EnchantsSquared_v1_18_R1 implements ISupportedPlugin {
    public String getName() {
        return "EnchantsSquared";
    }

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
