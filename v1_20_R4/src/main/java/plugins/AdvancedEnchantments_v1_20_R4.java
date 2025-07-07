package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import net.advancedplugins.ae.api.AEAPI;
import net.advancedplugins.ae.enchanthandler.enchantments.AdvancedEnchantment;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AdvancedEnchantments_v1_20_R4 implements ISupportedPlugin {
    public String getName() {
        return "AdvancedEnchantments";
    }

    public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        Map<String, Integer> advancedEnchantments = AEAPI.getEnchantmentsOnItem(item);

        List<IPluginEnchantment> enchantments = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : advancedEnchantments.entrySet()) {
            String key = entry.getKey();
            int level = entry.getValue();

            if (AEAPI.isAnEnchantment(key)) {
                AdvancedEnchantment enchantment = AEAPI.getEnchantmentInstance(key);

                enchantments.add(remapEnchantment(enchantment, key, level));
            } else {
                Enchantment vanillaEnchantment = Registry.ENCHANTMENT.get(Objects.requireNonNull(NamespacedKey.fromString(key)));

                if (vanillaEnchantment == null) continue;

                enchantments.add(EnchantmentUtils.remapEnchantment(vanillaEnchantment, level));
            }
        }

        return enchantments;
    }

    private static IPluginEnchantment remapEnchantment(AdvancedEnchantment enchantment, String key, int level) {
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

                item = AEAPI.applyEnchant(key, level, item);

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
}
