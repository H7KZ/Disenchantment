package cz.kominekjan.disenchantment.events.excellent;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.Map;

import static cz.kominekjan.disenchantment.utils.AnvilCostUtils.countAnvilCost;

public class DisenchantmentExcellentEvent {
    public static void onDisenchantmentEvent(PrepareAnvilEvent e, Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> {
            EnchantUtils.add(book, en, l, true);
            EnchantUtils.updateDisplay(book);
        });

        e.setResult(book);

        e.getInventory().setRepairCost(countAnvilCost(enchantments));
    }
}
