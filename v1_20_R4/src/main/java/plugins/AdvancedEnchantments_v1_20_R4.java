package plugins;

import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.VanillaPlugin;
import net.advancedplugins.ae.api.AEAPI;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AdvancedEnchantments_v1_20_R4 implements ISupportedPlugin {
    public String getName() {
        return "AdvancedEnchantments";
    }

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            String key = enchantment.getKey().getKey();
            int level = entry.getValue();

            if (AEAPI.isAnEnchantment(key)) {
                book = AEAPI.applyEnchant(key, level, book);
            } else {
                book = VanillaPlugin.addEnchantmentToBook(book, enchantment, level);
            }
        }
        return book;
    }

    public ItemStack removeEnchantmentsFromItem(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack book = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            String key = enchantment.getKey().getKey();

            if (AEAPI.isAnEnchantment(key)) {
                book = AEAPI.removeEnchantment(book, key);
            } else {
                book = VanillaPlugin.removeEnchantment(book, enchantment);
            }
        }

        return book;
    }
}
