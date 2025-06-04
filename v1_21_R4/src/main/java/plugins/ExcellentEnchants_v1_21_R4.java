package plugins;

import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.excellentenchants.util.EnchantUtils;

import java.util.Map;

public class ExcellentEnchants_v1_21_R4 implements ISupportedPlugin {
    public String getName() {
        return "ExcellentEnchants";
    }

    public Map<Enchantment, Integer> getItemEnchantments(ItemStack item) {
        return EnchantUtils.getEnchantments(item);
    }

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> EnchantUtils.add(book, en, l, true));

        return book;
    }

    public ItemStack removeEnchantmentsFromItem(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            EnchantUtils.remove(item, enchantment);
        }

        return item;
    }
}
