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

import java.util.List;
import java.util.Map;

import static com.jankominek.disenchantment.utils.AnvilCostUtils.countAnvilCost;

public class DisenchantEvent implements Listener {
    private static void handleEvent(PrepareAnvilEvent e) {
        if (!(e.getView().getPlayer() instanceof Player p)) return;

        if (!Config.isPluginEnabled() || !Config.Disenchantment.isEnabled() || Config.Disenchantment.getDisabledWorlds().contains(p.getWorld()))
            return;

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        Map<Enchantment, Integer> enchantments = EventUtils.Disenchantment.getDisenchantedEnchantments(firstItem, secondItem, true);

        if (enchantments.isEmpty()) return;

        if (!PermissionGroupType.DISENCHANT_EVENT.hasPermission(p)) return;

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        if (activatedPlugins.isEmpty()) {
            book = VanillaPlugin.createEnchantedBook(enchantments);
        } else {
            for (ISupportedPlugin plugin : activatedPlugins) {
                // won't 2 plugin activated on the same time would only use the last book ?
                book = plugin.createEnchantedBook(enchantments);
            }
        }

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        e.setResult(book);

        AnvilCostUtils.setAnvilRepairCost(e.getInventory(), e.getView(), countAnvilCost(enchantments, AnvilEventType.DISENCHANTMENT));

        Bukkit.getScheduler().runTask(Disenchantment.plugin, () -> {
            AnvilCostUtils.setAnvilRepairCost(e.getInventory(), e.getView(), countAnvilCost(enchantments, AnvilEventType.DISENCHANTMENT));

            p.updateInventory();
        });
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDisenchantmentEventLowest(PrepareAnvilEvent e) {
        if (Config.getDisenchantEventPriority() == EventPriority.LOWEST) handleEvent(e);
    }
    @EventHandler(priority = EventPriority.LOW)
    public void onDisenchantmentEventLow(PrepareAnvilEvent e) {
        if (Config.getDisenchantEventPriority() == EventPriority.LOW) handleEvent(e);
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDisenchantmentEventNormal(PrepareAnvilEvent e) {
        if (Config.getDisenchantEventPriority() == EventPriority.NORMAL) handleEvent(e);
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onDisenchantmentEventHigh(PrepareAnvilEvent e) {
        if (Config.getDisenchantEventPriority() == EventPriority.HIGH) handleEvent(e);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisenchantmentEventHighest(PrepareAnvilEvent e) {
        if (Config.getDisenchantEventPriority() == EventPriority.HIGHEST) handleEvent(e);
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisenchantmentEventMonitor(PrepareAnvilEvent e) {
        if (Config.getDisenchantEventPriority() == EventPriority.MONITOR) handleEvent(e);
    }
}
