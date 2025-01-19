package plugins;

import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.VanillaPlugin;
import com.willfp.eco.core.items.builder.EnchantedBookBuilder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EcoEnchants_v1_21_R1 implements ISupportedPlugin {
    public String getName() {
        return "EcoEnchants";
    }

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book;

        EnchantedBookBuilder builder = new EnchantedBookBuilder();

        enchantments.forEach(builder::addStoredEnchantment);

        book = builder.build();

        return book;
    }

    public ItemStack removeEnchantmentsFromItem(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        return VanillaPlugin.removeEnchantments(firstItem, enchantments);
    }
}
