package cz.kominekjan.disenchantment.plugins.impl.squared;

import cz.kominekjan.disenchantment.plugins.IPlugin;
import cz.kominekjan.disenchantment.plugins.impl.VanillaPlugin;
import cz.kominekjan.disenchantment.utils.DisenchantUtils;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SquaredPlugin implements IPlugin {
    public static final String name = "EnchantsSquared";

    private final HashMap<CustomEnchant, Enchantment> squaredEnchantmentMap;
    public SquaredPlugin(){
        squaredEnchantmentMap = new HashMap<>();

        for (CustomEnchant enchant : CustomEnchantManager.getInstance().getAllEnchants().values()) {
            Enchantment wrapped = new SquarredWrappedEnchantment(enchant);
            squaredEnchantmentMap.put(enchant, wrapped);
        }

    }

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = VanillaPlugin.createEnchantedBook(enchantments);

        enchantments.forEach((bukkitEnchantment, level) -> {

            if(!(bukkitEnchantment instanceof SquarredWrappedEnchantment squaredEnchantment)) return;

            // Remove the dummy enchantment object
            DisenchantUtils.removeStoredEnchantment(book, bukkitEnchantment);

            // And add the enchantment squared enchantment
            CustomEnchantManager.getInstance().addEnchant(book, squaredEnchantment.getEnchantment().getType(), level);
        });

        return book;
    }

    public ItemStack removeEnchantments(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack item = VanillaPlugin.removeEnchantments(firstItem, enchantments);

        enchantments.forEach((bukkitEnchantment, level) -> {

            if(!(bukkitEnchantment instanceof SquarredWrappedEnchantment squaredEnchantment)) return;

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
