package plugins;

import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginCustomEnchantment;
import com.jankominek.disenchantment.plugins.VanillaPlugin;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EnchantsSquared_v1_21_R1 implements ISupportedPlugin {
    public String getName() {
        return "EnchantsSquared";
    }

    public Map<Enchantment, Integer> getItemEnchantments(ItemStack item) {
        Map<CustomEnchant, Integer> enchantments = CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(item);

        return enchantments.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                entry -> new SupportedPluginCustomEnchantment(NamespacedKey.fromString(entry.getKey().getType())),
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                HashMap::new
                        )
                );
    }

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        CustomEnchantManager manager = CustomEnchantManager.getInstance();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            CustomEnchant customEnchant = CustomEnchantManager.getInstance().getEnchantmentFromType(enchantment.getKey().getKey());

            if (customEnchant == null) {
                book = VanillaPlugin.addEnchantmentToBook(book, enchantment, level);
                break;
            } else {
                manager.addEnchant(book, customEnchant.getType(), level);
            }
        }

        return book;
    }

    public ItemStack removeEnchantmentsFromItem(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            CustomEnchant customEnchant = CustomEnchantManager.getInstance().getEnchantmentFromType(enchantment.getKey().getKey());

            if (customEnchant == null) {
                item = VanillaPlugin.removeEnchantment(item, enchantment);
                break;
            } else {
                CustomEnchantManager.getInstance().removeEnchant(item, customEnchant.getType());
            }
        }

        return item;
    }
}
