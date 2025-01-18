package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.guis.GUIElements;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.InventoryBuilder;
import com.jankominek.disenchantment.utils.PrecisionUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.stream.Stream;

public class ShattermentRepairGUI implements InventoryHolder {
    private final Integer size = 27;
    private final String title = "Shatterment | Repair";
    private final GUIItem[] items = Stream.of(
            GUIElements.border9x3(new Integer[]{0}),
            new GUIItem(0, GUIElements.backItem(), event -> {
                event.setCancelled(true);
                event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
            }),
            GUIElements.border(10),
            new GUIItem(11, Config.Shatterment.Anvil.Repair.isCostEnabled() ? GUIElements.enabledShattermentRepairCostItem() : GUIElements.disabledShattermentRepairCostItem(), event -> {
                event.setCancelled(true);

                boolean repairCostEnabled = !Config.Shatterment.Anvil.Repair.isCostEnabled();

                Config.Shatterment.Anvil.Repair.setCostEnabled(repairCostEnabled);

                event.setCurrentItem(repairCostEnabled ? GUIElements.enabledShattermentRepairCostItem() : GUIElements.disabledShattermentRepairCostItem());
            }),
            GUIElements.border(12),
            new GUIItem(13, Config.Shatterment.Anvil.Repair.isResetEnabled() ? GUIElements.enabledShattermentRepairCostResetItem() : GUIElements.disabledShattermentRepairCostResetItem(), event -> {
                event.setCancelled(true);

                boolean repairResetEnabled = !Config.Shatterment.Anvil.Repair.isResetEnabled();

                Config.Shatterment.Anvil.Repair.setResetEnabled(repairResetEnabled);

                event.setCurrentItem(repairResetEnabled ? GUIElements.enabledShattermentRepairCostResetItem() : GUIElements.disabledShattermentRepairCostResetItem());
            }),
            new GUIItem(14, GUIElements.shattermentRepairCostBaseItem((int) Math.round(Config.Shatterment.Anvil.Repair.getBaseCost()), Math.min((int) Math.round(Config.Shatterment.Anvil.Repair.getBaseCost()), 64)), event -> {
                event.setCancelled(true);

                double cost = Config.Shatterment.Anvil.Repair.getBaseCost();

                switch (event.getClick()) {
                    case LEFT:
                        cost = PrecisionUtils.round(cost + 1, 1);
                        break;
                    case RIGHT:
                        cost = PrecisionUtils.round(cost - 1, 1);
                        break;
                    default:
                        return;
                }

                Config.Shatterment.Anvil.Repair.setBaseCost(cost);

                event.setCurrentItem(GUIElements.shattermentRepairCostBaseItem((int) Math.round(cost), Math.min((int) Math.round(cost), 64)));
            }),
            new GUIItem(15, GUIElements.shattermentRepairCostMultiplierItem(Config.Shatterment.Anvil.Repair.getCostMultiplier(), Math.min((int) (Config.Shatterment.Anvil.Repair.getCostMultiplier() * 10), 64)), event -> {
                event.setCancelled(true);

                double multiplier = Config.Shatterment.Anvil.Repair.getCostMultiplier();

                switch (event.getClick()) {
                    case LEFT:
                        multiplier = PrecisionUtils.round(multiplier + 0.1, 1);
                        break;
                    case RIGHT:
                        multiplier = PrecisionUtils.round(multiplier - 0.1, 1);
                        break;
                    default:
                        return;
                }

                Config.Shatterment.Anvil.Repair.setCostMultiplier(multiplier);

                event.setCurrentItem(GUIElements.shattermentRepairCostMultiplierItem(multiplier, Math.min((int) (multiplier * 10), 64)));
            }),
            GUIElements.border(16),
            GUIElements.border(17)
    ).toArray(GUIItem[]::new);
    private final Inventory inventory;

    public ShattermentRepairGUI() {
        Inventory inv = Bukkit.createInventory(this, this.size, this.title);

        this.inventory = InventoryBuilder.fillItems(inv, this.items);
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
