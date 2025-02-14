package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.plugins.VanillaPlugin;
import com.jankominek.disenchantment.types.AnvilEventType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.jankominek.disenchantment.utils.AnvilCostUtils.countAnvilCost;

public class ShatterEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisenchantmentEvent(PrepareAnvilEvent e) {
        if (!(e.getView().getPlayer() instanceof Player p)) return;

        if (!Config.isPluginEnabled() || !Config.Shatterment.isEnabled() || Config.Shatterment.getDisabledWorlds().contains(p.getWorld()))
            return;

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        Map<Enchantment, Integer> enchantments = EventUtils.Shatterment.getDisenchantedEnchantments(firstItem, secondItem, true);

        if (enchantments.isEmpty()) return;

        if (!PermissionGroupType.SHATTER_EVENT.hasPermission(p)) return;

        HashMap<Enchantment, Integer> randomEnchantmentShatter = new HashMap<>();

        List<Enchantment> keys = new ArrayList<>(enchantments.keySet());
        Collections.shuffle(keys);

        int halfSize = Math.max(1, keys.size() / 2);

        for (int i = 0; i < halfSize; i++) {
            Enchantment key = keys.get(i);
            randomEnchantmentShatter.put(key, enchantments.get(key));
        }

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        if (activatedPlugins.isEmpty()) {
            book = VanillaPlugin.createEnchantedBook(randomEnchantmentShatter);
        } else {
            for (ISupportedPlugin plugin : activatedPlugins) {
                // won't 2 plugin activated on the same time would only use the last book ?
                book = plugin.createEnchantedBook(randomEnchantmentShatter);
            }
        }

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        e.setResult(book);

        AnvilCostUtils.setAnvilRepairCost(e.getInventory(), e.getView(), countAnvilCost(enchantments, AnvilEventType.SHATTERMENT));

        Bukkit.getScheduler().runTask(Disenchantment.plugin, () -> {
            AnvilCostUtils.setAnvilRepairCost(e.getInventory(), e.getView(), countAnvilCost(enchantments, AnvilEventType.SHATTERMENT));

            p.updateInventory();
        });
    }
}
