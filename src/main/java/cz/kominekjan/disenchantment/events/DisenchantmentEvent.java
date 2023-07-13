package cz.kominekjan.disenchantment.events;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static cz.kominekjan.disenchantment.Disenchantment.config;
import static cz.kominekjan.disenchantment.Disenchantment.enabled;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class DisenchantmentEvent implements Listener {
    public static boolean keepEnchantments(ItemStack item) {
        AtomicBoolean keeps = new AtomicBoolean(true);
        for (Map<?, ?> entry : config.getMapList("disabled-enchantments")) {
            String enchantment = (String) entry.get("enchantment");
            if (enchantment == null) continue;

            if (item.getEnchantments().keySet().stream().map(Enchantment::getName).anyMatch(m -> m.equalsIgnoreCase(enchantment))) {
                Boolean keep = (Boolean) entry.get("keep");
                if (keep == null) continue;

                if (!keep) {
                    keeps.set(false);
                }
            }
        }

        return keeps.get();
    }

    public static boolean isValid(ItemStack firstItem, ItemStack secondItem) {
        if (firstItem == null) return false;
        if (secondItem == null) return false;

        if (secondItem.getType() != Material.BOOK) return false;

        if (secondItem.getEnchantments().size() > 0) return false;

        if (config.getStringList("disabled-items").stream().map(String::toLowerCase).anyMatch(m -> m.equalsIgnoreCase(firstItem.getType().name())))
            return false;

        if (!keepEnchantments(firstItem)) return false;

        return firstItem.getEnchantments().size() != 0;
    }

    @EventHandler
    public void onDisenchantmentEvent(PrepareAnvilEvent e) {
        if (!(e.getView().getPlayer() instanceof Player)) return;

        Player p = (Player) e.getView().getPlayer();

        if (!enabled || config.getStringList("disabled-worlds").contains(p.getWorld().getName())) return;

        if (!isValid(e.getInventory().getItem(0), e.getInventory().getItem(1))) return;

        ItemStack firstItem = e.getInventory().getItem(0);

        Map<Enchantment, Integer> enchantmentsCopy = new LinkedHashMap<>(firstItem.getEnchantments());

        for (Map<?, ?> entry : config.getMapList("disabled-enchantments")) {
            String enchantment = (String) entry.get("enchantment");
            if (enchantment == null) continue;

            for (Map.Entry<Enchantment, Integer> en : firstItem.getEnchantments().entrySet()) {
                if (!en.getKey().getName().equalsIgnoreCase(enchantment)) continue;

                Boolean keep = (Boolean) entry.get("keep");
                if (keep == null) continue;
                Integer level = (Integer) entry.get("level");
                if (level == null) continue;

                if (keep && en.getValue() >= level) {
                    enchantmentsCopy.remove(en.getKey());
                }
            }
        }

        Map<Enchantment, Integer> enchantments = ImmutableMap.copyOf(enchantmentsCopy);

        if (enchantments.size() == 0) return;

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
