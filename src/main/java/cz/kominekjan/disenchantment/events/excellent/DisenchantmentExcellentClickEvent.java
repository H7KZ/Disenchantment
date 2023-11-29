package cz.kominekjan.disenchantment.events.excellent;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static cz.kominekjan.disenchantment.Disenchantment.config;

public class DisenchantmentExcellentClickEvent {
    public static void onDisenchantmentClickEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        ItemStack result = anvilInventory.getItem(2);

        int exp = p.getLevel() - anvilInventory.getRepairCost();

        ItemStack firstItem = anvilInventory.getItem(0);
        ItemStack secondItem = anvilInventory.getItem(1);

        ItemStack item = firstItem.clone();

        Map<Enchantment, Integer> enchantmentsCopy = new LinkedHashMap<>(firstItem.getEnchantments());

        if (!config.getMapList("disabled-enchantments").isEmpty()) {
            for (Map<?, ?> entry : config.getMapList("disabled-enchantments")) {
                String enchantment = (String) entry.get("enchantment");
                if (enchantment == null) continue;

                for (Map.Entry<Enchantment, Integer> en : firstItem.getEnchantments().entrySet()) {
                    if (en.getKey().getName().equalsIgnoreCase(enchantment)) continue;

                    enchantmentsCopy.remove(en.getKey());
                }
            }

            item.getEnchantments().forEach((en, l) -> EnchantUtils.remove(item, en));
            enchantmentsCopy.forEach((en, l) -> EnchantUtils.add(item, en, l, true));
        } else {
            item.getEnchantments().forEach((en, l) -> EnchantUtils.remove(item, en));
        }

        EnchantUtils.updateDisplay(item);

        anvilInventory.setItem(0, item);

        if (secondItem.getAmount() > 1) {
            secondItem.setAmount(secondItem.getAmount() - 1);
        } else {
            anvilInventory.setItem(1, null);
        }

        p.setItemOnCursor(result);

        p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1, 1);

        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE)
            p.setLevel(exp);
    }
}
