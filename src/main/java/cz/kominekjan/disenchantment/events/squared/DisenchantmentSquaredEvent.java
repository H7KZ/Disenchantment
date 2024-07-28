package cz.kominekjan.disenchantment.events.squared;

import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static cz.kominekjan.disenchantment.utils.AnvilCostUtil.countAnvilCost;

public class DisenchantmentSquaredEvent {
    public static void onDisenchantmentEvent(PrepareAnvilEvent e, Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> CustomEnchantManager.getInstance().addEnchant(book, en.getKey().getKey(), l));

        e.setResult(book);

        e.getInventory().setRepairCost(countAnvilCost(enchantments));
    }
}
