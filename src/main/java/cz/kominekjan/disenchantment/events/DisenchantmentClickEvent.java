package cz.kominekjan.disenchantment.events;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

import static cz.kominekjan.disenchantment.Disenchantment.config;
import static cz.kominekjan.disenchantment.Disenchantment.enabled;
import static cz.kominekjan.disenchantment.events.DisenchantmentEvent.isValid;

public class DisenchantmentClickEvent implements Listener {
    @EventHandler
    public void onDisenchantmentClickEvent(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();

        if (!enabled || config.getStringList("disabled-worlds").contains(p.getWorld().getName())) return;

        if (e.getInventory().getType() != InventoryType.ANVIL) return;

        AnvilInventory ai = (AnvilInventory) e.getInventory();

        if (e.getSlot() != 2) return;

        if (ai.getItem(2) == null) return;

        ItemStack result = ai.getItem(2);

        if (result.getType() != Material.ENCHANTED_BOOK) return;

        if (!isValid(ai.getItem(0), ai.getItem(1))) return;

        if (ai.getRepairCost() > p.getLevel() && p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

        int exp = p.getLevel() - ai.getRepairCost();

        ItemStack firstItem = ai.getItem(0);
        ItemStack secondItem = ai.getItem(1);

        ItemStack item = firstItem.clone();

        Map<Enchantment, Integer> enchantmentsCopy = new LinkedHashMap<>(firstItem.getEnchantments());

        for (Map<?, ?> entry : config.getMapList("disabled-enchantments")) {
            String enchantment = (String) entry.get("enchantment");
            if (enchantment == null) continue;

            for (Map.Entry<Enchantment, Integer> en : firstItem.getEnchantments().entrySet()) {
                if (en.getKey().getName().equalsIgnoreCase(enchantment)) continue;

                enchantmentsCopy.remove(en.getKey());
            }
        }

        item.getEnchantments().forEach((en, l) -> item.removeEnchantment(en));
        item.addUnsafeEnchantments(enchantmentsCopy);
        ai.setItem(0, item);

        if (secondItem.getAmount() > 1) {
            secondItem.setAmount(secondItem.getAmount() - 1);
        } else {
            ai.setItem(1, null);
        }

        p.setItemOnCursor(result);

        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE)
            p.setLevel(exp);
    }
}
