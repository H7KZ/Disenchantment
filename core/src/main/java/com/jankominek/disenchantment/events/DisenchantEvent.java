package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.AnvilEventType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.EventUtils;
import com.jankominek.disenchantment.utils.SchedulerUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.jankominek.disenchantment.utils.AnvilCostUtils.countAnvilCost;

public class DisenchantEvent {
    public static void onEvent(Event event) {
        try {
            handler(event);
        } catch (Exception e) {
            DiagnosticUtils.throwReport(e);
        }
    }

    private static void handler(Event event) {
        if (!(event instanceof PrepareAnvilEvent e)) return;

        if (!(e.getView().getPlayer() instanceof Player p)) return;

        if (!Config.isPluginEnabled() || !Config.Disenchantment.isEnabled() || Config.Disenchantment.getDisabledWorlds().contains(p.getWorld()))
            return;

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        List<IPluginEnchantment> pluginEnchantments = new ArrayList<>();

        if (activatedPlugins.isEmpty()) {
            pluginEnchantments.addAll(EventUtils.Disenchantment.getDisenchantedEnchantments(firstItem, secondItem, true));
        } else {
            for (ISupportedPlugin activatedPlugin : activatedPlugins) {
                pluginEnchantments.addAll(EventUtils.Disenchantment.getDisenchantedEnchantments(firstItem, secondItem, true, activatedPlugin));
            }
        }

        if (pluginEnchantments.isEmpty()) return;

        if (!PermissionGroupType.DISENCHANT_EVENT.hasPermission(p)) return;

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        for (IPluginEnchantment pluginEnchantment : pluginEnchantments) {
            book = pluginEnchantment.addToBook(book);
        }

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        e.setResult(book);

        AnvilCostUtils.setAnvilRepairCost(e.getInventory(), e.getView(), countAnvilCost(pluginEnchantments, AnvilEventType.DISENCHANTMENT));

        SchedulerUtils.runForEntity(Disenchantment.plugin, p, () -> {
            AnvilCostUtils.setAnvilRepairCost(e.getInventory(), e.getView(), countAnvilCost(pluginEnchantments, AnvilEventType.DISENCHANTMENT));
            p.updateInventory();
        });
    }
}
