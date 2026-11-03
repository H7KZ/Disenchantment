package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.EconomyUtils;
import com.jankominek.disenchantment.utils.EraseUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the {@link PrepareAnvilEvent} for erase operations.
 * When a player places an enchanted item in an anvil together with a catalyst, this handler prepares a
 * copy of the item without its erasable enchantments as the result and sets the anvil repair cost.
 */
public class EraseEvent {
    /**
     * Entry point for the erase prepare-anvil event.
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

        if (!Config.isPluginEnabled() || !EraseUtils.isActive()) return;

        // Erase shares worlds, materials and enchantment states with disenchantment.
        if (AnvilEventGuards.isWorldBlocked(p, Config.Disenchantment.isWorldRestricted(p.getWorld())))
            return;

        DiagnosticUtils.debug("ERASE", () -> "PrepareAnvil: player=" + p.getName() + ", world=" + p.getWorld().getName());

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        DiagnosticUtils.debug("ERASE", () -> "PrepareAnvil: slot0=" + (firstItem != null ? firstItem.getType() : "null")
                + ", slot1=" + (secondItem != null ? secondItem.getType() : "null"));

        List<IPluginEnchantment> pluginEnchantments = AnvilOperations.collect(AnvilOperations.Kind.ERASE, p, firstItem, secondItem, true);

        if (pluginEnchantments.isEmpty()) {
            DiagnosticUtils.debug("ERASE", "PrepareAnvil: no erasable enchantments → exit");
            return;
        }

        // Special anvils gate erase exactly like disenchant/shatter.
        if (!AnvilEventGuards.isAnvilAllowed(e.getInventory(), p)) {
            DiagnosticUtils.debug("ERASE", "PrepareAnvil: not a special anvil → exit");
            AnvilEventGuards.sendSpecialAnvilHint(p);
            return;
        }

        if (DiagnosticUtils.isDebugEnabled()) {
            String names = pluginEnchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("ERASE", "PrepareAnvil: enchantments=[" + names + "] (" + pluginEnchantments.size() + " total)");
        }

        // Only a genuine plugin operation is blocked by maintenance mode — vanilla anvil use
        // (repair/rename/combine) must keep working. No message here: PrepareAnvilEvent fires on
        // every keystroke, the click handler informs the player.
        if (AnvilEventGuards.isMaintenanceActiveFor(p)) {
            DiagnosticUtils.debug("ERASE", "PrepareAnvil: maintenance active → result cleared");
            e.setResult(null);
            return;
        }

        if (AnvilEventGuards.isOnCooldownQuiet(p)) {
            DiagnosticUtils.debug("ERASE", "PrepareAnvil: player on cooldown → result cleared (no message, the click path informs)");
            e.setResult(null);
            return;
        }

        if (!PermissionGroupType.ERASE_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("ERASE", "PrepareAnvil: permission denied → exit");
            return;
        }

        ItemStack result = EraseUtils.buildResult(firstItem, pluginEnchantments);
        if (Config.Erase.Anvil.Repair.isResetEnabled()) AnvilCostUtils.setItemRepairCost(result, 0);

        // Tagged so the click handler never mistakes a vanilla repair result (a catalyst can also be a repair
        // material) for this one; the tag never reaches the player, the click rebuilds the item from the source.
        e.setResult(EraseUtils.markPrepared(result));

        DiagnosticUtils.debug("ERASE", () -> "PrepareAnvil: anvil cost=" + AnvilCostUtils.countAnvilCost(pluginEnchantments, AnvilEventType.ERASE, p));
        AnvilEventGuards.applyAnvilCostAndSchedule(e, p, pluginEnchantments, AnvilEventType.ERASE);

        DiagnosticUtils.debug("ERASE", () -> "PrepareAnvil: economy display — enabled=" + Config.Erase.Economy.isEnabled()
                + ", available=" + EconomyUtils.isAvailable()
                + ", show-cost=" + Config.Erase.Economy.isShowCostEnabled()
                + ", gameMode=" + p.getGameMode());

        double economyCost = AnvilOperations.economyCost(AnvilOperations.Kind.ERASE, pluginEnchantments);

        AnvilEventGuards.showEconomyActionBarCost(p, AnvilOperations.Kind.ERASE.economy(), economyCost);
    }
}
