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

public class DisenchantmentRepairGUI implements InventoryHolder {
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
                    Config.Disenchantment.Anvil.Repair.isCostEnabled() ? GUIComponent.Repair.Disenchantment.Cost.enabled() : GUIComponent.Repair.Disenchantment.Cost.disabled(),
                    event -> {
                        event.setCancelled(true);

                        boolean repairCostEnabled = !Config.Disenchantment.Anvil.Repair.isCostEnabled();

                        Config.Disenchantment.Anvil.Repair.setCostEnabled(repairCostEnabled);

                        event.setCurrentItem(repairCostEnabled ? GUIComponent.Repair.Disenchantment.Cost.enabled() : GUIComponent.Repair.Disenchantment.Cost.disabled());
                    }
            ),
            GUIBorderComponent.border(12),
            new GUIItem(
                    13,
                    Config.Disenchantment.Anvil.Repair.isResetEnabled() ? GUIComponent.Repair.Disenchantment.Reset.enabled() : GUIComponent.Repair.Disenchantment.Reset.disabled(),
                    event -> {
                        event.setCancelled(true);

                        boolean repairResetEnabled = !Config.Disenchantment.Anvil.Repair.isResetEnabled();

                        Config.Disenchantment.Anvil.Repair.setResetEnabled(repairResetEnabled);

                        event.setCurrentItem(repairResetEnabled ? GUIComponent.Repair.Disenchantment.Reset.enabled() : GUIComponent.Repair.Disenchantment.Reset.disabled());
                    }
            ),
            new GUIItem(
                    14,
                    GUIComponent.Repair.Disenchantment.base(
                            (int) Math.round(Config.Disenchantment.Anvil.Repair.getBaseCost()),
                            Math.min((int) Math.round(Config.Disenchantment.Anvil.Repair.getBaseCost()), 64)
                    ),
                    event -> {
                        event.setCancelled(true);

                        double cost = Config.Disenchantment.Anvil.Repair.getBaseCost();

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

                        Config.Disenchantment.Anvil.Repair.setBaseCost(cost);

                        event.setCurrentItem(GUIComponent.Repair.Disenchantment.base((int) Math.round(cost), Math.min((int) Math.round(cost), 64)));
                    }
            ),
            new GUIItem(
                    15,
                    GUIComponent.Repair.Disenchantment.multiplier(
                            Config.Disenchantment.Anvil.Repair.getCostMultiplier(),
                            Math.min((int) (Config.Disenchantment.Anvil.Repair.getCostMultiplier() * 10), 64)
                    ),
                    event -> {
                        event.setCancelled(true);

                        double multiplier = Config.Disenchantment.Anvil.Repair.getCostMultiplier();

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

                        Config.Disenchantment.Anvil.Repair.setCostMultiplier(multiplier);

                        event.setCurrentItem(GUIComponent.Repair.Disenchantment.multiplier(multiplier, Math.min((int) (multiplier * 10), 64)));
                    }
            ),
            GUIBorderComponent.border(16),
            GUIBorderComponent.border(17)
    );
    private final Inventory inventory;

    public DisenchantmentRepairGUI() {
        Inventory inventory = Bukkit.createInventory(this, 27, I18n.GUI.Repair.Disenchantment.inventory());

        this.inventory = InventoryBuilder.fillItems(inventory, this.items);
    }

    public void onInventoryClick(InventoryClickEvent event) {
        for (GUIItem item : this.items) {
            if (item.getSlot() == event.getSlot()) item.onClick(event);
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
