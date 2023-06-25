package cz.kominekjan.disenchantment.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import static cz.kominekjan.disenchantment.events.DisenchantmentEvent.isValid;

public class DisenchantmentClickEvent implements Listener {
    @EventHandler
    public void onDisenchantmentClickEvent(InventoryClickEvent e) {
        if (e.getInventory().getType() != InventoryType.ANVIL) return;

        AnvilInventory ai = (AnvilInventory) e.getInventory();

        if (e.getSlot() != 2) return;

        if (ai.getResult() == null) return;

        ItemStack result = ai.getResult();

        if (result.getType() != Material.ENCHANTED_BOOK) return;

        if (ai.getFirstItem() == null) return;
        if (ai.getSecondItem() == null) return;

        ItemStack firstItem = ai.getFirstItem();
        ItemStack secondItem = ai.getSecondItem();

        if (!isValid(firstItem, secondItem)) return;

        if (!(e.getWhoClicked() instanceof Player p)) return;

        ItemStack item = firstItem.clone();

        item.getEnchantments().forEach((en, l) -> item.removeEnchantment(en));

        ai.setFirstItem(item);

        if (secondItem.getAmount() > 1) {
            secondItem.setAmount(secondItem.getAmount() - 1);
        } else {
            ai.setSecondItem(null);
        }

        p.setItemOnCursor(result);
    }
}
