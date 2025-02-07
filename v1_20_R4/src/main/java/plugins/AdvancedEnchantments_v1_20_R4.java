package plugins;

import com.jankominek.disenchantment.plugins.ISupportedPlugin;
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

        enchantments.forEach((en, l) -> {
            AEAPI.applyEnchant(en.getKey().getKey(), l, book, true);
        });
        return book;
    }

    public ItemStack removeEnchantmentsFromItem(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            AEAPI.removeEnchantment(item, enchantment.getKey().getKey());
        }

        return item;
    }
}
