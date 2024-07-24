package cz.kominekjan.disenchantment.events.eco;

import com.willfp.eco.core.items.builder.EnchantedBookBuilder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static cz.kominekjan.disenchantment.utils.AnvilCostUtil.countAnvilCost;

public class DisenchantmentEcoEvent {
    public static void onDisenchantmentEvent(PrepareAnvilEvent e, Map<Enchantment, Integer> enchantments) {
        EnchantedBookBuilder builder = new EnchantedBookBuilder();

        enchantments.forEach(builder::addStoredEnchantment);

        ItemStack book = builder.build();

        e.setResult(book);

        e.getInventory().setRepairCost(countAnvilCost(enchantments));
    }
}
