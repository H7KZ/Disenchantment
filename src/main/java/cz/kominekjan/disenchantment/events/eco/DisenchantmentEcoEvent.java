package cz.kominekjan.disenchantment.events.eco;

import com.willfp.eco.core.items.builder.EnchantedBookBuilder;
import com.willfp.ecoenchants.enchants.EcoEnchants;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static cz.kominekjan.disenchantment.events.DisenchantmentEvent.countAnvilCost;

public class DisenchantmentEcoEvent {
    public static void onDisenchantmentEvent(PrepareAnvilEvent e, Map<Enchantment, Integer> enchantments) {
        EnchantedBookBuilder builder = new EnchantedBookBuilder();

        enchantments.forEach((en, l) -> {
            if (EcoEnchants.getByID(en.getName().toLowerCase()) != null) {
                builder.addStoredEnchantment(EcoEnchants.getByID(en.getName().toLowerCase()), l);
            } else {
                builder.addStoredEnchantment(en, l);
            }
        });

        ItemStack book = builder.build();

        e.setResult(book);

        e.getInventory().setRepairCost(countAnvilCost(enchantments));
    }
}
