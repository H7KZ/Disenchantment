package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.InventoryBuilder;
import com.jankominek.disenchantment.guis.components.GUIElements;
import com.jankominek.disenchantment.utils.PrecisionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class DisenchantmentRepairGUI implements InventoryHolder {
    private final Integer size = 27;
    private final String title = "Disenchantment | Repair";
    private final GUIItem[] items = ArrayUtils.addAll(
            GUIElements.border9x3(new Integer[]{0}),
            new GUIItem(0, GUIElements.backItem(), event -> {
                event.setCancelled(true);
                event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
            }),
            GUIElements.border(10),
            new GUIItem(11, Config.Disenchantment.Anvil.Repair.isCostEnabled() ? GUIElements.enabledDisenchantmentRepairCostItem() : GUIElements.disabledDisenchantmentRepairCostItem(), event -> {
                event.setCancelled(true);

                boolean repairCostEnabled = !Config.Disenchantment.Anvil.Repair.isCostEnabled();

                Config.Disenchantment.Anvil.Repair.setCostEnabled(repairCostEnabled);

                event.setCurrentItem(repairCostEnabled ? GUIElements.enabledDisenchantmentRepairCostItem() : GUIElements.disabledDisenchantmentRepairCostItem());
            }),
            GUIElements.border(12),
            new GUIItem(13, Config.Disenchantment.Anvil.Repair.isResetEnabled() ? GUIElements.enabledDisenchantmentRepairCostResetItem() : GUIElements.disabledDisenchantmentRepairCostResetItem(), event -> {
                event.setCancelled(true);

                boolean repairResetEnabled = !Config.Disenchantment.Anvil.Repair.isResetEnabled();

                Config.Disenchantment.Anvil.Repair.setResetEnabled(repairResetEnabled);

                event.setCurrentItem(repairResetEnabled ? GUIElements.enabledDisenchantmentRepairCostResetItem() : GUIElements.disabledDisenchantmentRepairCostResetItem());
            }),
            new GUIItem(14, GUIElements.disenchantmentRepairCostBaseItem((int) Math.round(Config.Disenchantment.Anvil.Repair.getBaseCost()), Math.min((int) Math.round(Config.Disenchantment.Anvil.Repair.getBaseCost()), 64)), event -> {
                event.setCancelled(true);

                double cost = Config.Disenchantment.Anvil.Repair.getBaseCost();

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

                Config.Disenchantment.Anvil.Repair.setBaseCost(cost);

                event.setCurrentItem(GUIElements.disenchantmentRepairCostBaseItem((int) Math.round(cost), Math.min((int) Math.round(cost), 64)));
            }),
            new GUIItem(15, GUIElements.disenchantmentRepairCostMultiplierItem(Config.Disenchantment.Anvil.Repair.getCostMultiplier(), Math.min((int) (Config.Disenchantment.Anvil.Repair.getCostMultiplier() * 10), 64)), event -> {
                event.setCancelled(true);

                double multiplier = Config.Disenchantment.Anvil.Repair.getCostMultiplier();

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

                Config.Disenchantment.Anvil.Repair.setCostMultiplier(multiplier);

                event.setCurrentItem(GUIElements.disenchantmentRepairCostMultiplierItem(multiplier, Math.min((int) (multiplier * 10), 64)));
            }),
            GUIElements.border(16),
            GUIElements.border(17)
    );
    private final Inventory inventory;

    public DisenchantmentRepairGUI() {
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
