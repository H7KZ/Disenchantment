package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.guis.GUIBorderComponent;
import com.jankominek.disenchantment.guis.GUIComponent;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.InventoryBuilder;
import com.jankominek.disenchantment.utils.PrecisionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * GUI for configuring shatterment anvil repair cost settings.
 * Allows toggling repair cost, repair cost reset, and adjusting base cost and multiplier values.
 */
public class ShattermentRepairGUI implements InventoryHolder {
    private final GUIItem[] items = ArrayUtils.addAll(
            GUIBorderComponent.border9x3(new Integer[]{0}),
            new GUIItem(
                    0,
                    GUIComponent.back(),
                    event -> {
                        event.setCancelled(true);

                        event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
                    }
            ),
            GUIBorderComponent.border(10),
            new GUIItem(
                    11,
                    Config.Shatterment.Anvil.Repair.isCostEnabled() ? GUIComponent.Repair.Shatterment.Cost.enabled() : GUIComponent.Repair.Shatterment.Cost.disabled(),
                    event -> {
                        event.setCancelled(true);

                        boolean repairCostEnabled = !Config.Shatterment.Anvil.Repair.isCostEnabled();

                        Config.Shatterment.Anvil.Repair.setCostEnabled(repairCostEnabled);

                        event.setCurrentItem(repairCostEnabled ? GUIComponent.Repair.Shatterment.Cost.enabled() : GUIComponent.Repair.Shatterment.Cost.disabled());
                    }
            ),
            GUIBorderComponent.border(12),
            new GUIItem(
                    13,
                    Config.Shatterment.Anvil.Repair.isResetEnabled() ? GUIComponent.Repair.Shatterment.Reset.enabled() : GUIComponent.Repair.Shatterment.Reset.disabled(),
                    event -> {
                        event.setCancelled(true);

                        boolean repairResetEnabled = !Config.Shatterment.Anvil.Repair.isResetEnabled();

                        Config.Shatterment.Anvil.Repair.setResetEnabled(repairResetEnabled);

                        event.setCurrentItem(repairResetEnabled ? GUIComponent.Repair.Shatterment.Reset.enabled() : GUIComponent.Repair.Shatterment.Reset.disabled());
                    }
            ),
            new GUIItem(
                    14,
                    GUIComponent.Repair.Shatterment.base(
                            (int) Math.round(Config.Shatterment.Anvil.Repair.getBaseCost()),
                            Math.min((int) Math.round(Config.Shatterment.Anvil.Repair.getBaseCost()), 64)
                    ),
                    event -> {
                        event.setCancelled(true);

                        double cost = Config.Shatterment.Anvil.Repair.getBaseCost();

                        switch (event.getClick()) {
                            case LEFT: {
                                cost = PrecisionUtils.round(cost + 1, 1);
                                break;
                            }
                            case RIGHT: {
                                cost = PrecisionUtils.round(cost - 1, 1);
                                break;
                            }
                            default:
                                return;
                        }

                        Config.Shatterment.Anvil.Repair.setBaseCost(cost);

                        event.setCurrentItem(GUIComponent.Repair.Shatterment.base((int) Math.round(cost), Math.min((int) Math.round(cost), 64)));
                    }
            ),
            new GUIItem(
                    15,
                    GUIComponent.Repair.Shatterment.multiplier(
                            Config.Shatterment.Anvil.Repair.getCostMultiplier(),
                            Math.min((int) (Config.Shatterment.Anvil.Repair.getCostMultiplier() * 10), 64)
                    ),
                    event -> {
                        event.setCancelled(true);

                        double multiplier = Config.Shatterment.Anvil.Repair.getCostMultiplier();

                        switch (event.getClick()) {
                            case LEFT: {
                                multiplier = PrecisionUtils.round(multiplier + 0.1, 1);
                                break;
                            }
                            case RIGHT: {
                                multiplier = PrecisionUtils.round(multiplier - 0.1, 1);
                                break;
                            }
                            default:
                                return;
                        }

                        Config.Shatterment.Anvil.Repair.setCostMultiplier(multiplier);

                        event.setCurrentItem(GUIComponent.Repair.Shatterment.multiplier(multiplier, Math.min((int) (multiplier * 10), 64)));
                    }
            ),
            GUIBorderComponent.border(16),
            GUIBorderComponent.border(17)
    );
    private final Inventory inventory;

    /**
     * Constructs the shatterment repair GUI, creating and populating the inventory.
     */
    public ShattermentRepairGUI() {
        Inventory inventory = Bukkit.createInventory(this, 27, I18n.GUI.Repair.Shatterment.inventory());

        this.inventory = InventoryBuilder.fillItems(inventory, this.items);
    }

    /**
     * Delegates inventory click events to the appropriate GUI item handler based on the clicked slot.
     *
     * @param event the inventory click event
     */
    public void onInventoryClick(InventoryClickEvent event) {
        for (GUIItem item : this.items) {
            if (item.getSlot() == event.getSlot()) item.onClick(event);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
