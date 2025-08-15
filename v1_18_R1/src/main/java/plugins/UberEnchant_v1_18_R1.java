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

public class UberEnchant_v1_18_R1 implements ISupportedPlugin {
    public String getName() {
        return "UberEnchant";
    }

    public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        HashMap<Enchantment, Integer> enchantments = new HashMap<>();

        enchantments.putAll(UberUtils.getAllMap(item));
        enchantments.putAll(UberUtils.getStoredMap(item));

        return enchantments
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
