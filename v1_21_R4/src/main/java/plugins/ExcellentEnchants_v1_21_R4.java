package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.excellentenchants.util.EnchantUtils;

import java.util.List;
import java.util.stream.Collectors;

public class ExcellentEnchants_v1_21_R4 implements ISupportedPlugin {
    public String getName() {
        return "ExcellentEnchants";
    }

    public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        return EnchantUtils.getEnchantments(item)
                .entrySet()
                .stream()
                .map(entry -> remapEnchantment(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

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

                EnchantUtils.add(item, enchantment, level, true);

                return item;
            }

            @Override
            public ItemStack removeFromBook(ItemStack book) {
                ItemStack item = book.clone();

                EnchantUtils.remove(item, enchantment);

                return item;
            }

            @Override
            public ItemStack addToItem(ItemStack item) {
                ItemStack result = item.clone();

                EnchantUtils.add(result, enchantment, level, true);

                return result;
            }

            @Override
            public ItemStack removeFromItem(ItemStack item) {
                ItemStack result = item.clone();

                EnchantUtils.remove(result, enchantment);

                return result;
            }
        };
    }
}
