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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jankominek.disenchantment.utils.AnvilCostUtils.countAnvilCost;

public class ShatterEvent {
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

        if (!Config.isPluginEnabled() || !Config.Shatterment.isEnabled() || Config.Shatterment.getDisabledWorlds().contains(p.getWorld()))
            return;

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        List<IPluginEnchantment> enchantments = new ArrayList<>();

        if (activatedPlugins.isEmpty()) {
            enchantments.addAll(EventUtils.Shatterment.getShattermentEnchantments(firstItem, secondItem, false));
        } else {
            for (ISupportedPlugin activatedPlugin : activatedPlugins) {
                enchantments.addAll(EventUtils.Shatterment.getShattermentEnchantments(firstItem, secondItem, false, activatedPlugin));
            }
        }

        if (enchantments.isEmpty()) return;

        if (!PermissionGroupType.SHATTER_EVENT.hasPermission(p)) return;

        List<IPluginEnchantment> randomEnchantmentShatter = new ArrayList<>();

        List<IPluginEnchantment> shuffleEnchantments = new ArrayList<>(enchantments);
        Collections.shuffle(shuffleEnchantments);

        int halfSize = Math.max(1, shuffleEnchantments.size() / 2);

        for (int i = 0; i < halfSize; i++) {
            IPluginEnchantment enchantment = shuffleEnchantments.get(i);
            randomEnchantmentShatter.add(enchantment);
        }

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        for (IPluginEnchantment enchantment : randomEnchantmentShatter) {
            book = enchantment.addToBook(book);
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
