package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.guis.GUIItem;
import cz.kominekjan.disenchantment.guis.InventoryBuilder;
import cz.kominekjan.disenchantment.utils.PrecisionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import static cz.kominekjan.disenchantment.config.Config.*;

public class RepairGUI implements InventoryHolder {
    private final GUIItem[] items = ArrayUtils.addAll(
            DefaultGUIElements.border9x3(new Integer[]{0}),
            new GUIItem(0, DefaultGUIElements.backItem(), event -> {
                event.setCancelled(true);
                event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
            }),
            DefaultGUIElements.border(10),
            new GUIItem(11, getEnableRepairCost() ? DefaultGUIElements.enabledRepairCostItem() : DefaultGUIElements.disabledRepairCostItem(), event -> {
                event.setCancelled(true);

                boolean repairCostEnabled = !getEnableRepairCost();

                setEnableRepairCost(repairCostEnabled);

                event.setCurrentItem(repairCostEnabled ? DefaultGUIElements.enabledRepairCostItem() : DefaultGUIElements.disabledRepairCostItem());
            }),
            DefaultGUIElements.border(12),
            new GUIItem(13, getEnableRepairReset() ? DefaultGUIElements.enabledRepairCostResetItem() : DefaultGUIElements.disabledRepairCostResetItem(), event -> {
                event.setCancelled(true);

                boolean repairResetEnabled = !getEnableRepairReset();

                setEnableRepairReset(repairResetEnabled);

                event.setCurrentItem(repairResetEnabled ? DefaultGUIElements.enabledRepairCostResetItem() : DefaultGUIElements.disabledRepairCostResetItem());
            }),
            new GUIItem(14, DefaultGUIElements.repairCostBaseItem((int) Math.round(getBaseRepairCost()), Math.min((int) Math.round(getBaseRepairCost()), 64)), event -> {
                event.setCancelled(true);

                ClickType clickType = event.getClick();

                if (clickType.isLeftClick()) {
                    setBaseRepairCost(PrecisionUtils.round(getBaseRepairCost() + 1, 1));
                } else if (clickType.isRightClick()) {
                    setBaseRepairCost(PrecisionUtils.round(getBaseRepairCost() - 1, 1));
                }

                event.setCurrentItem(DefaultGUIElements.repairCostBaseItem((int) Math.round(getBaseRepairCost()), Math.min((int) Math.round(getBaseRepairCost()), 64)));
            }),
            new GUIItem(15, DefaultGUIElements.repairCostMultiplierItem(getRepairCostMultiplier(), Math.min((int) (getRepairCostMultiplier() * 10), 64)), event -> {
                event.setCancelled(true);

                ClickType clickType = event.getClick();

                if (clickType.isLeftClick()) {
                    setRepairCostMultiplier(PrecisionUtils.round(getRepairCostMultiplier() + 0.1, 1));
                } else if (clickType.isRightClick()) {
                    setRepairCostMultiplier(PrecisionUtils.round(getRepairCostMultiplier() - 0.1, 1));
                }

                event.setCurrentItem(DefaultGUIElements.repairCostMultiplierItem(getRepairCostMultiplier(), Math.min((int) (getRepairCostMultiplier() * 10), 64)));
            }),
            DefaultGUIElements.border(16),
            DefaultGUIElements.border(17)
    );
    private final Integer size = 27;
    private final String title = "Repair cost settings";
    private final Inventory inventory;

    public RepairGUI() {
        Inventory inv = Bukkit.createInventory(this, this.size, this.title);

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
