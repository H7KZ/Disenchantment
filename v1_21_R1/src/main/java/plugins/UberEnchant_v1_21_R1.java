package plugins;

import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginCustomEnchantment;
import me.sciguymjm.uberenchant.api.utils.UberUtils;
import me.sciguymjm.uberenchant.utils.enchanting.EnchantmentUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UberEnchant_v1_21_R1 implements ISupportedPlugin {
    public String getName() {
        return "UberEnchant";
    }

    public Map<Enchantment, Integer> getItemEnchantments(ItemStack item) {
        return UberUtils.getAllMap(item);
    }

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> EnchantmentUtils.setStoredEnchantment(en, book, l));

        return book;
    }

    public ItemStack removeEnchantmentsFromItem(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            EnchantmentUtils.removeEnchantment(enchantment, item);
        }

        return item;
    }
}
