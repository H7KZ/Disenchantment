package cz.kominekjan.disenchantment.events.advanced;

import net.advancedplugins.ae.api.AEAPI;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static cz.kominekjan.disenchantment.events.DisenchantmentEvent.countAnvilCost;

public class DisenchantmentAdvancedEvent {
    public static void onDisenchantmentEvent(PrepareAnvilEvent e, Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> {
            AEAPI.applyEnchant(en.getKey().toString(), l, book, true);
        });

        e.setResult(book);

        e.getInventory().setRepairCost(countAnvilCost(enchantments));
    }
}
