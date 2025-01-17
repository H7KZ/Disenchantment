package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.guis.GUIItem;
import cz.kominekjan.disenchantment.guis.InventoryBuilder;
import cz.kominekjan.disenchantment.utils.PrecisionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class DisenchantmentRepairGUI implements InventoryHolder {
    private final GUIItem[] items = ArrayUtils.addAll(
            DefaultGUIElements.border9x3(new Integer[]{0}),
            new GUIItem(0, DefaultGUIElements.backItem(), event -> {
                event.setCancelled(true);
                event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
            }),
            DefaultGUIElements.border(10),
            new GUIItem(11, Config.Disenchantment.Anvil.Repair.isCostEnabled() ? DefaultGUIElements.enabledDisenchantmentRepairCostItem() : DefaultGUIElements.disabledDisenchantmentRepairCostItem(), event -> {
                event.setCancelled(true);

                boolean repairCostEnabled = !Config.Disenchantment.Anvil.Repair.isCostEnabled();

                Config.Disenchantment.Anvil.Repair.setCostEnabled(repairCostEnabled);

                event.setCurrentItem(repairCostEnabled ? DefaultGUIElements.enabledDisenchantmentRepairCostItem() : DefaultGUIElements.disabledDisenchantmentRepairCostItem());
            }),
            DefaultGUIElements.border(12),
            new GUIItem(13, Config.Disenchantment.Anvil.Repair.isResetEnabled() ? DefaultGUIElements.enabledDisenchantmentRepairCostResetItem() : DefaultGUIElements.disabledDisenchantmentRepairCostResetItem(), event -> {
                event.setCancelled(true);

                boolean repairResetEnabled = !Config.Disenchantment.Anvil.Repair.isResetEnabled();

                Config.Disenchantment.Anvil.Repair.setResetEnabled(repairResetEnabled);

                event.setCurrentItem(repairResetEnabled ? DefaultGUIElements.enabledDisenchantmentRepairCostResetItem() : DefaultGUIElements.disabledDisenchantmentRepairCostResetItem());
            }),
            new GUIItem(14, DefaultGUIElements.disenchantmentRepairCostBaseItem((int) Math.round(Config.Disenchantment.Anvil.Repair.getBaseCost()), Math.min((int) Math.round(Config.Disenchantment.Anvil.Repair.getBaseCost()), 64)), event -> {
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

                event.setCurrentItem(DefaultGUIElements.disenchantmentRepairCostBaseItem((int) Math.round(cost), Math.min((int) Math.round(cost), 64)));
            }),
            new GUIItem(15, DefaultGUIElements.disenchantmentRepairCostMultiplierItem(Config.Disenchantment.Anvil.Repair.getCostMultiplier(), Math.min((int) (Config.Disenchantment.Anvil.Repair.getCostMultiplier() * 10), 64)), event -> {
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

                event.setCurrentItem(DefaultGUIElements.disenchantmentRepairCostMultiplierItem(multiplier, Math.min((int) (multiplier * 10), 64)));
            }),
            DefaultGUIElements.border(16),
            DefaultGUIElements.border(17)
    );
    private final Inventory inventory;

    public DisenchantmentRepairGUI() {
        String title = "Disenchantment | Repair";
        int size = 27;
        Inventory inv = Bukkit.createInventory(this, size, title);

        this.inventory = InventoryBuilder.fillItems(inv, this.items);
    }

    public void onInventoryClick(InventoryClickEvent event) {
        for (GUIItem item : this.items) {
            if (item.getSlot() == event.getSlot()) item.onClick(event);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
