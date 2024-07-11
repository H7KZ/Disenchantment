package cz.kominekjan.disenchantment.events;

import com.google.common.collect.ImmutableMap;
import cz.kominekjan.disenchantment.events.advanced.DisenchantmentAdvancedEvent;
import cz.kominekjan.disenchantment.events.eco.DisenchantmentEcoEvent;
import cz.kominekjan.disenchantment.events.excellent.DisenchantmentExcellentEvent;
import cz.kominekjan.disenchantment.events.normal.DisenchantmentNormalEvent;
import cz.kominekjan.disenchantment.nbteditor.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

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

            if (item.getEnchantments().keySet().stream().map(Enchantment::getKey).anyMatch(m -> m.toString().equalsIgnoreCase(enchantment))) {
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

        if (!secondItem.getEnchantments().isEmpty()) return false;

        if (config.getStringList("disabled-items").stream().map(String::toLowerCase).anyMatch(m -> m.equalsIgnoreCase(firstItem.getType().name())))
            return false;

        if (!keepEnchantments(firstItem)) return false;

        return !firstItem.getEnchantments().isEmpty();
    }

    public static int countAnvilCost(Map<Enchantment, Integer> enchantments) {
        AtomicReference<Boolean> enableRepairCost = new AtomicReference<>(config.getBoolean("enable-repair-cost"));

        if (!enableRepairCost.get()) return 0;

        AtomicReference<Integer> enchantmentCost = new AtomicReference<>(config.getInt("base"));
        AtomicReference<Double> baseMultiplier = new AtomicReference<>(1 - config.getDouble("multiply"));

        List<Map.Entry<Enchantment, Integer>> enchantmentList = new ArrayList<>(enchantments.entrySet());

        enchantmentList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        enchantmentList.forEach((entry) -> {
            baseMultiplier.updateAndGet(v -> v + config.getDouble("multiply"));
            enchantmentCost.updateAndGet(v -> (int) (v + entry.getValue() * baseMultiplier.get()));
        });

        return enchantmentCost.get();
    }

    public static ItemStack setNBTRepairCost(ItemStack item, int repairCost) {
        return NBTEditor.set(item, repairCost, "RepairCost");
    }

    @EventHandler
    public void onDisenchantmentEvent(PrepareAnvilEvent e) {
        if (!(e.getView().getPlayer() instanceof Player p)) return;

        if (!p.hasPermission("disenchantment.anvil")) return;

        if (!enabled || config.getStringList("disabled-worlds").contains(p.getWorld().getName())) return;

        if (!isValid(e.getInventory().getItem(0), e.getInventory().getItem(1))) return;

        ItemStack firstItem = e.getInventory().getItem(0);

        assert firstItem != null;
        Map<Enchantment, Integer> enchantmentsCopy = new LinkedHashMap<>(firstItem.getEnchantments());

        for (Map<?, ?> entry : config.getMapList("disabled-enchantments")) {
            String enchantment = (String) entry.get("enchantment");
            if (enchantment == null) continue;

            for (Map.Entry<Enchantment, Integer> en : firstItem.getEnchantments().entrySet()) {
                if (!en.getKey().getKey().toString().equalsIgnoreCase(enchantment)) continue;

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

        if (enchantments.isEmpty()) return;

        if (Bukkit.getServer().getPluginManager().getPlugin("ExcellentEnchants") != null) {
            DisenchantmentExcellentEvent.onDisenchantmentEvent(e, enchantments);
        } else if (Bukkit.getServer().getPluginManager().getPlugin("EcoEnchants") != null && Bukkit.getServer().getPluginManager().getPlugin("eco") != null) {
            DisenchantmentEcoEvent.onDisenchantmentEvent(e, enchantments);
        } else if (Bukkit.getServer().getPluginManager().getPlugin("AdvancedEnchantments") != null) {
            DisenchantmentAdvancedEvent.onDisenchantmentEvent(e, enchantments);
        } else {
            DisenchantmentNormalEvent.onDisenchantmentEvent(e, enchantments);
        }
    }
}
