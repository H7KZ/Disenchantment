package cz.kominekjan.disenchantment.events.eco;

import com.willfp.eco.core.items.builder.ItemStackBuilder;
import com.willfp.ecoenchants.enchants.EcoEnchants;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cz.kominekjan.disenchantment.Disenchantment.config;

public class DisenchantmentEcoClickEvent {
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

            ItemMeta meta = item.getItemMeta();

            item.getEnchantments().forEach((en, l) -> {
                if (meta instanceof EnchantmentStorageMeta) {
                    ((EnchantmentStorageMeta) meta).removeStoredEnchant(en);
                }

                meta.removeEnchant(en);
            });

            item.setItemMeta(meta);

            ItemStackBuilder builder = new ItemStackBuilder(item);

            enchantmentsCopy.forEach((en, l) -> {
                if (EcoEnchants.getByID(en.getName().toLowerCase()) != null) {
                    builder.addEnchantment(EcoEnchants.getByID(en.getName().toLowerCase()), l);
                } else {
                    builder.addEnchantment(en, l);
                }
            });

            item = builder.build();
        } else {
            ItemMeta meta = item.getItemMeta();

            item.getEnchantments().forEach((en, l) -> {
                if (meta instanceof EnchantmentStorageMeta) {
                    ((EnchantmentStorageMeta) meta).removeStoredEnchant(en);
                }

                meta.removeEnchant(en);
            });

            item.setItemMeta(meta);
        }

        anvilInventory.setItem(0, item);

        if (secondItem.getAmount() > 1) {
            secondItem.setAmount(secondItem.getAmount() - 1);
        } else {
            anvilInventory.setItem(1, null);
        }

        p.setItemOnCursor(result);

        AtomicReference<Boolean> enableAnvilSound = new AtomicReference<>(config.getBoolean("enable-anvil-sound"));

        if (enableAnvilSound.get()) {
            AtomicReference<Double> anvilVolume = new AtomicReference<>(config.getDouble("anvil-volume"));
            AtomicReference<Double> anvilPitch = new AtomicReference<>(config.getDouble("anvil-pitch"));

            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, anvilVolume.get().floatValue(), anvilPitch.get().floatValue());
        }

        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE)
            p.setLevel(exp);
    }
}
