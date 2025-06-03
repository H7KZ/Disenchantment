package plugins;

import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import me.sciguymjm.uberenchant.api.utils.UberUtils;
import me.sciguymjm.uberenchant.utils.enchanting.EnchantmentUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class UberEnchant_v1_18_R1 implements ISupportedPlugin {
    public String getName() {
        return "UberEnchant";
    }

    public Map<Enchantment, Integer> getItemEnchantments(ItemStack item) {
        HashMap<Enchantment, Integer> enchantments = new HashMap<>();

        enchantments.putAll(UberUtils.getAllMap(item));
        enchantments.putAll(UberUtils.getStoredMap(item));

        return enchantments;
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
