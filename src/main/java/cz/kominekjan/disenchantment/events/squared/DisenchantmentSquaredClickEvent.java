package cz.kominekjan.disenchantment.events.squared;

import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static cz.kominekjan.disenchantment.config.Config.*;
import static cz.kominekjan.disenchantment.libs.nbteditor.NBT.setNBTRepairCost;

public class DisenchantmentSquaredClickEvent {
    public static void onDisenchantmentClickEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        int exp = p.getLevel() - anvilInventory.getRepairCost();

        ItemStack firstItem = anvilInventory.getItem(0);
        ItemStack secondItem = anvilInventory.getItem(1);
        ItemStack result = anvilInventory.getItem(2);

        assert firstItem != null;
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : firstItem.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();

            if (getDisabledEnchantments().containsKey(enchantment))
                continue;

            CustomEnchantManager.getInstance().removeEnchant(item, enchantment.getKey().getKey());
        }

        if (getEnableRepairReset()) item = setNBTRepairCost(item, 0);

        anvilInventory.setItem(0, item);

        assert secondItem != null;
        if (secondItem.getAmount() > 1) {
            secondItem.setAmount(secondItem.getAmount() - 1);
        } else {
            anvilInventory.setItem(1, null);
        }

        p.setItemOnCursor(result);

        if (getEnableAnvilSound())
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, Float.parseFloat(getAnvilSoundVolume().toString()), Float.parseFloat(getAnvilSoundPitch().toString()));

        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) p.setLevel(exp);
    }
}
