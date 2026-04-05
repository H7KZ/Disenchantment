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
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.jankominek.disenchantment.utils.AnvilCostUtils.countAnvilCost;

/**
 * Handles the {@link PrepareAnvilEvent} for disenchantment operations.
 * When a player places an enchanted item in an anvil with a book, this handler
 * prepares an enchanted book result containing the extracted enchantments and
 * sets the appropriate anvil repair cost.
 */
public class DisenchantEvent {
    /**
     * Entry point for the disenchant prepare-anvil event.
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

        if (!Config.isPluginEnabled() || !Config.Disenchantment.isEnabled() || Config.Disenchantment.getDisabledWorlds().contains(p.getWorld()))
            return;

        DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: player=" + p.getName() + ", world=" + p.getWorld().getName());

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: slot0=" + (firstItem != null ? firstItem.getType() : "null")
                + ", slot1=" + (secondItem != null ? secondItem.getType() : "null"));

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        List<IPluginEnchantment> pluginEnchantments = new ArrayList<>();

        if (activatedPlugins.isEmpty()) {
            pluginEnchantments.addAll(EventUtils.Disenchantment.getDisenchantedEnchantments(firstItem, secondItem, true));
        } else {
            for (ISupportedPlugin activatedPlugin : activatedPlugins) {
                pluginEnchantments.addAll(EventUtils.Disenchantment.getDisenchantedEnchantments(firstItem, secondItem, true, activatedPlugin, p.getWorld()));
            }
        }

        if (pluginEnchantments.isEmpty()) {
            DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: no eligible enchantments → exit");
            return;
        }

        if (DiagnosticUtils.isDebugEnabled()) {
            String names = pluginEnchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: enchantments=[" + names + "] (" + pluginEnchantments.size() + " total)");
        }

        if (!PermissionGroupType.DISENCHANT_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: permission denied → exit");
            return;
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

        int anvilCost = countAnvilCost(pluginEnchantments, AnvilEventType.DISENCHANTMENT);
        DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: anvil cost=" + anvilCost);
        AnvilCostUtils.setAnvilRepairCost(e.getInventory(), e.getView(), anvilCost);

        DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: economy display — enabled=" + Config.Disenchantment.Economy.isEnabled()
                + ", available=" + EconomyUtils.isAvailable()
                + ", show-cost=" + Config.Disenchantment.Economy.isShowCostEnabled()
                + ", gameMode=" + p.getGameMode());

        if (Config.Disenchantment.Economy.isEnabled()
                && EconomyUtils.isAvailable()
                && Config.Disenchantment.Economy.isShowCostEnabled()
                && p.getGameMode() != GameMode.CREATIVE) {
            DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: showing economy action bar → " + EconomyUtils.format(Config.Disenchantment.Economy.getCost()));
            p.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(I18n.Messages.economyCost(EconomyUtils.format(Config.Disenchantment.Economy.getCost())))
            );
        }

        SchedulerUtils.runForEntity(Disenchantment.plugin, p, () -> {
            AnvilCostUtils.setAnvilRepairCost(e.getInventory(), e.getView(), countAnvilCost(pluginEnchantments, AnvilEventType.DISENCHANTMENT));
            p.updateInventory();
        });
    }
}
