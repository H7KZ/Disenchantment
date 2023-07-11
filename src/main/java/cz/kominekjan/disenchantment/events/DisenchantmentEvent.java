package cz.kominekjan.disenchantment.events;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cz.kominekjan.disenchantment.Disenchantment.config;
import static cz.kominekjan.disenchantment.Disenchantment.enabled;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class DisenchantmentEvent implements Listener {
    public static boolean isValid(ItemStack firstItem, ItemStack secondItem) {
        if (firstItem == null) return false;
        if (secondItem == null) return false;

        if (secondItem.getType() != Material.BOOK) return false;

        if (secondItem.getEnchantments().size() > 0) return false;

        return firstItem.getEnchantments().size() != 0;
    }

    @EventHandler
    public void onDisenchantmentEvent(PrepareAnvilEvent e) {
        if (!enabled) return;

        if (!isValid(e.getInventory().getItem(0), e.getInventory().getItem(1))) return;

        ItemStack firstItem = e.getInventory().getItem(0);

        Map<Enchantment, Integer> enchantments = firstItem.getEnchantments();

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta esm = (EnchantmentStorageMeta) book.getItemMeta();
        enchantments.forEach((en, l) -> esm.addStoredEnchant(en, l, true));
        book.setItemMeta(esm);

        e.setResult(book);

        AtomicReference<Integer> enchantmentCost = new AtomicReference<>(config.getInt("base"));
        AtomicReference<Double> baseMultiplier = new AtomicReference<>(1 - config.getDouble("multiply"));

        List<Map.Entry<Enchantment, Integer>> enchList = new ArrayList<>(enchantments.entrySet());

        enchList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        enchList.forEach((entry) -> {
            baseMultiplier.updateAndGet(v -> v + config.getDouble("multiply"));
            enchantmentCost.updateAndGet(v -> (int) (v + entry.getValue() * baseMultiplier.get()));
        });

        e.getInventory().setRepairCost(enchantmentCost.get());
    }
}
