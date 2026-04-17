package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.AnvilEventType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.*;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

        // Use getViewers() instead of getView().getPlayer() to avoid IncompatibleClassChangeError
        // in environments without Paper's classloader shim (InventoryView: class→interface in 1.21).
        if (e.getInventory().getViewers().isEmpty() || !(e.getInventory().getViewers().get(0) instanceof Player p))
            return;

        if (!Config.isPluginEnabled() || !Config.Shatterment.isEnabled() || Config.Shatterment.getDisabledWorlds().contains(p.getWorld()))
            return;

        DiagnosticUtils.debug("SHATTER", "PrepareAnvil: player=" + p.getName() + ", world=" + p.getWorld().getName());

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        DiagnosticUtils.debug("SHATTER", "PrepareAnvil: slot0=" + (firstItem != null ? firstItem.getType() : "null")
                + ", slot1=" + (secondItem != null ? secondItem.getType() : "null"));

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        List<IPluginEnchantment> enchantments = new ArrayList<>();

        if (activatedPlugins.isEmpty()) {
            enchantments.addAll(EventUtils.Shatterment.getShattermentEnchantments(firstItem, secondItem, false));
        } else {
            for (ISupportedPlugin activatedPlugin : activatedPlugins) {
                enchantments.addAll(EventUtils.Shatterment.getShattermentEnchantments(firstItem, secondItem, false, activatedPlugin, p.getWorld()));
            }
        }

        if (enchantments.isEmpty()) {
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: no eligible enchantments → exit");
            return;
        }

        if (!PermissionGroupType.SHATTER_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: permission denied → exit");
            return;
        }

        List<IPluginEnchantment> pluginEnchantments = new ArrayList<>();

        List<IPluginEnchantment> shuffleEnchantments = new ArrayList<>(enchantments);
        Collections.shuffle(shuffleEnchantments);

        int halfSize = Math.max(1, shuffleEnchantments.size() / 2);

        for (int i = 0; i < halfSize; i++) {
            IPluginEnchantment enchantment = shuffleEnchantments.get(i);
            pluginEnchantments.add(enchantment);
        }

        if (DiagnosticUtils.isDebugEnabled()) {
            String allNames = enchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            String splitNames = pluginEnchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: source enchantments=[" + allNames + "] (" + enchantments.size() + " total)");
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: splitting → selected=[" + splitNames + "] (halfSize=" + halfSize + ")");
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

        int anvilCost = countAnvilCost(enchantments, AnvilEventType.SHATTERMENT);
        DiagnosticUtils.debug("SHATTER", "PrepareAnvil: anvil cost=" + anvilCost);
        AnvilCostUtils.setAnvilRepairCost(e.getInventory(), e.getView(), anvilCost);

        DiagnosticUtils.debug("SHATTER", "PrepareAnvil: economy display — enabled=" + Config.Shatterment.Economy.isEnabled()
                + ", available=" + EconomyUtils.isAvailable()
                + ", show-cost=" + Config.Shatterment.Economy.isShowCostEnabled()
                + ", gameMode=" + p.getGameMode());

        if (Config.Shatterment.Economy.isEnabled()
                && EconomyUtils.isAvailable()
                && Config.Shatterment.Economy.isShowCostEnabled()
                && p.getGameMode() != GameMode.CREATIVE) {
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: showing economy action bar → " + EconomyUtils.format(Config.Shatterment.Economy.getCost()));
            p.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(
                    I18n.Messages.economyCost(EconomyUtils.format(Config.Shatterment.Economy.getCost()))
            ));
        }

        SchedulerUtils.runForEntity(Disenchantment.plugin, p, () -> {
            AnvilCostUtils.setAnvilRepairCost(e.getInventory(), e.getView(), countAnvilCost(enchantments, AnvilEventType.SHATTERMENT));
            p.updateInventory();
        });
    }
}
