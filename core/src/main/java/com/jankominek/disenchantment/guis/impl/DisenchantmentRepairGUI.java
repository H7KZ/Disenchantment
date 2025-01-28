package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
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
    private final Integer size = 27;
    private final String title = "Disenchantment | Repair";
    private final GUIItem[] items = ArrayUtils.addAll(
            GUIComponent.border9x3(new Integer[]{0}),
            new GUIItem(0, GUIComponent.backItem(), event -> {
                event.setCancelled(true);
                event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
            }),
            GUIComponent.border(10),
            new GUIItem(11, Config.Disenchantment.Anvil.Repair.isCostEnabled() ? GUIComponent.enabledDisenchantmentRepairCostItem() : GUIComponent.disabledDisenchantmentRepairCostItem(), event -> {
                event.setCancelled(true);

                boolean repairCostEnabled = !Config.Disenchantment.Anvil.Repair.isCostEnabled();

                Config.Disenchantment.Anvil.Repair.setCostEnabled(repairCostEnabled);

                event.setCurrentItem(repairCostEnabled ? GUIComponent.enabledDisenchantmentRepairCostItem() : GUIComponent.disabledDisenchantmentRepairCostItem());
            }),
            GUIComponent.border(12),
            new GUIItem(13, Config.Disenchantment.Anvil.Repair.isResetEnabled() ? GUIComponent.enabledDisenchantmentRepairCostResetItem() : GUIComponent.disabledDisenchantmentRepairCostResetItem(), event -> {
                event.setCancelled(true);

                boolean repairResetEnabled = !Config.Disenchantment.Anvil.Repair.isResetEnabled();

                Config.Disenchantment.Anvil.Repair.setResetEnabled(repairResetEnabled);

                event.setCurrentItem(repairResetEnabled ? GUIComponent.enabledDisenchantmentRepairCostResetItem() : GUIComponent.disabledDisenchantmentRepairCostResetItem());
            }),
            new GUIItem(14, GUIComponent.disenchantmentRepairCostBaseItem((int) Math.round(Config.Disenchantment.Anvil.Repair.getBaseCost()), Math.min((int) Math.round(Config.Disenchantment.Anvil.Repair.getBaseCost()), 64)), event -> {
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

                event.setCurrentItem(GUIComponent.disenchantmentRepairCostBaseItem((int) Math.round(cost), Math.min((int) Math.round(cost), 64)));
            }),
            new GUIItem(15, GUIComponent.disenchantmentRepairCostMultiplierItem(Config.Disenchantment.Anvil.Repair.getCostMultiplier(), Math.min((int) (Config.Disenchantment.Anvil.Repair.getCostMultiplier() * 10), 64)), event -> {
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

                event.setCurrentItem(GUIComponent.disenchantmentRepairCostMultiplierItem(multiplier, Math.min((int) (multiplier * 10), 64)));
            }),
            GUIComponent.border(16),
            GUIComponent.border(17)
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
