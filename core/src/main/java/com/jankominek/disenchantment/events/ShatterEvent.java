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
import java.util.Collections;
import java.util.List;

import static com.jankominek.disenchantment.utils.AnvilCostUtils.countAnvilCost;

/**
 * Handles the {@link PrepareAnvilEvent} for shatterment (book-splitting) operations.
 * When a player places an enchanted book in the first anvil slot with a regular book,
 * this handler prepares a result book containing a random subset (half) of the
 * source book's enchantments and sets the appropriate anvil cost.
 */
public class ShatterEvent {
    /**
     * Entry point for the shatter prepare-anvil event.
     * Delegates to the internal handler and reports any exceptions via diagnostics.
     *
     * @param event the Bukkit event to process
     */
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

        List<IPluginEnchantment> pluginEnchantments = new ArrayList<>();

        List<IPluginEnchantment> shuffleEnchantments = new ArrayList<>(enchantments);
        Collections.shuffle(shuffleEnchantments);

        int halfSize = Math.max(1, shuffleEnchantments.size() / 2);

        for (int i = 0; i < halfSize; i++) {
            IPluginEnchantment enchantment = shuffleEnchantments.get(i);
            pluginEnchantments.add(enchantment);
        }

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        for (IPluginEnchantment pluginEnchantment : pluginEnchantments) {
            book = pluginEnchantment.addToBook(book);
        }

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        e.setResult(book);

        AnvilCostUtils.setAnvilRepairCost(e.getInventory(), e.getView(), countAnvilCost(enchantments, AnvilEventType.SHATTERMENT));

        SchedulerUtils.runForEntity(Disenchantment.plugin, p, () -> {
            AnvilCostUtils.setAnvilRepairCost(e.getInventory(), e.getView(), countAnvilCost(enchantments, AnvilEventType.SHATTERMENT));
            p.updateInventory();
        });
    }
}
