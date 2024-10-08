package cz.kominekjan.disenchantment.plugins.impl.squared;

import cz.kominekjan.disenchantment.plugins.IPlugin;
import cz.kominekjan.disenchantment.plugins.impl.VanillaPlugin;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SquaredPlugin implements IPlugin {
    public static final String name = "EnchantsSquared";

    private HashMap<CustomEnchant, Enchantment> squaredEnchantmentMap;

    public void activate() {
        squaredEnchantmentMap = new HashMap<>();

        for (CustomEnchant enchant : CustomEnchantManager.getInstance().getAllEnchants().values()) {
            Enchantment wrapped = new SquarredWrappedEnchantment(enchant);
            squaredEnchantmentMap.put(enchant, wrapped);
        }
    }

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        Map<SquarredWrappedEnchantment, Integer> esEnchants = new HashMap<>();

        // Fetch enchantment squared enchants
        enchantments.forEach((bukkitEnchantment, level) -> {
            if (!(bukkitEnchantment instanceof SquarredWrappedEnchantment squaredEnchantment)) return;

            esEnchants.put(squaredEnchantment, level);
        });

        // Remove es enchantment from bukkit enchantment map
        for (SquarredWrappedEnchantment esEnchantment : esEnchants.keySet()) {
            enchantments.remove(esEnchantment);
        }

        // Create book with only bukkit enchantments
        ItemStack book = VanillaPlugin.createEnchantedBook(enchantments);

        // Add enchantment squared enchantments to the book
        CustomEnchantManager manager = CustomEnchantManager.getInstance();
        esEnchants.forEach((enchantment, level) -> manager.addEnchant(book, enchantment.getEnchantment().getType(), level));

        return book;
    }

    public ItemStack removeEnchantments(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack item = VanillaPlugin.removeEnchantments(firstItem, enchantments);

        enchantments.forEach((bukkitEnchantment, level) -> {
            if (!(bukkitEnchantment instanceof SquarredWrappedEnchantment squaredEnchantment)) return;

            // And add the enchantment squared enchantment
            CustomEnchantManager.getInstance().removeEnchant(item, squaredEnchantment.getEnchantment().getType());
        });

        return item;
    }

    @Override
    public void fetchComplementaryEnchantment(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        // Add wrapper of squared enchantments
        CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(firstItem).forEach(
                (enchant, level) -> enchantments.put(squaredEnchantmentMap.get(enchant), level)
        );
    }

    @Override
    public List<Enchantment> everyComplementaryEnchantments() {
        return List.copyOf(squaredEnchantmentMap.values());
    }
}
