package cz.kominekjan.disenchantment.events.normal;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Map;

import static cz.kominekjan.disenchantment.utils.AnvilCostUtils.countAnvilCost;

public class DisenchantmentNormalEvent {

    public static void onDisenchantmentEvent(PrepareAnvilEvent e, Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        EnchantmentStorageMeta esm = (EnchantmentStorageMeta) book.getItemMeta();

        enchantments.forEach((en, l) -> {
            assert esm != null;
            esm.addStoredEnchant(en, l, true);
        });

        book.setItemMeta(esm);

        e.setResult(book);

        e.getInventory().setRepairCost(countAnvilCost(enchantments));
    }
}
