package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.guis.GUIBorderComponent;
import com.jankominek.disenchantment.guis.GUIComponent;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.InventoryBuilder;
import com.jankominek.disenchantment.types.AnvilFeature;
import com.jankominek.disenchantment.utils.PrecisionUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * GUI for configuring anvil repair cost settings for a given {@link AnvilFeature}.
 * Allows toggling repair cost, repair cost reset, and adjusting base cost and multiplier values.
 */
public class RepairGUI implements InventoryHolder {
    private final AnvilFeature feature;
    private final GUIItem[] items;
    private final Inventory inventory;

    /**
     * Constructs the repair GUI for the specified anvil feature, creating and populating the inventory.
     *
     * @param feature the anvil feature this GUI configures
     */
    public RepairGUI(AnvilFeature feature) {
        this.feature = feature;

        this.items = ArrayUtils.addAll(
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
                        isCostEnabled() ? costEnabled() : costDisabled(),
                        event -> {
                            event.setCancelled(true);

                            boolean repairCostEnabled = !isCostEnabled();

                            setCostEnabled(repairCostEnabled);

                            event.setCurrentItem(repairCostEnabled ? costEnabled() : costDisabled());
                        }
                ),
                GUIBorderComponent.border(12),
                new GUIItem(
                        13,
                        isResetEnabled() ? resetEnabled() : resetDisabled(),
                        event -> {
                            event.setCancelled(true);

                            boolean repairResetEnabled = !isResetEnabled();

                            setResetEnabled(repairResetEnabled);

                            event.setCurrentItem(repairResetEnabled ? resetEnabled() : resetDisabled());
                        }
                ),
                new GUIItem(
                        14,
                        base(
                                (int) Math.round(getBaseCost()),
                                Math.min((int) Math.round(getBaseCost()), 64)
                        ),
                        event -> {
                            event.setCancelled(true);

                            double cost = getBaseCost();

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

                            setBaseCost(cost);

                            event.setCurrentItem(base((int) Math.round(cost), Math.min((int) Math.round(cost), 64)));
                        }
                ),
                new GUIItem(
                        15,
                        multiplier(
                                getCostMultiplier(),
                                Math.min((int) (getCostMultiplier() * 10), 64)
                        ),
                        event -> {
                            event.setCancelled(true);

                            double multiplier = getCostMultiplier();

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

                            setCostMultiplier(multiplier);

                            event.setCurrentItem(multiplier(multiplier, Math.min((int) (multiplier * 10), 64)));
                        }
                ),
                new GUIItem(
                        16,
                        maxCost(getMaxCost(), getMaxCost() < 0 ? 1 : Math.min(Math.max(getMaxCost(), 1), 64)),
                        event -> {
                            event.setCancelled(true);

                            int cost = getMaxCost();

                            switch (event.getClick()) {
                                case LEFT: {
                                    cost = cost < 0 ? 0 : cost + 1;
                                    break;
                                }
                                case RIGHT: {
                                    cost = cost <= 0 ? -1 : cost - 1;
                                    break;
                                }
                                default:
                                    return;
                            }

                            setMaxCost(cost);

                            event.setCurrentItem(maxCost(cost, cost < 0 ? 1 : Math.min(Math.max(cost, 1), 64)));
                        }
                )
        );

        String title = switch (feature) {
            case DISENCHANTMENT -> I18n.GUI.Repair.Disenchantment.inventory();
            case SHATTERMENT -> I18n.GUI.Repair.Shatterment.inventory();
        };

        Inventory inv = Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize(title));

        this.inventory = InventoryBuilder.fillItems(inv, this.items);
    }

    private boolean isCostEnabled() {
        return switch (feature) {
            case DISENCHANTMENT -> Config.Disenchantment.Anvil.Repair.isCostEnabled();
            case SHATTERMENT -> Config.Shatterment.Anvil.Repair.isCostEnabled();
        };
    }

    private void setCostEnabled(boolean value) {
        Config.beginBatch();
        try {
            switch (feature) {
                case DISENCHANTMENT -> Config.Disenchantment.Anvil.Repair.setCostEnabled(value);
                case SHATTERMENT -> Config.Shatterment.Anvil.Repair.setCostEnabled(value);
            }
        } finally {
            Config.commitBatch();
        }
    }

    private boolean isResetEnabled() {
        return switch (feature) {
            case DISENCHANTMENT -> Config.Disenchantment.Anvil.Repair.isResetEnabled();
            case SHATTERMENT -> Config.Shatterment.Anvil.Repair.isResetEnabled();
        };
    }

    private void setResetEnabled(boolean value) {
        Config.beginBatch();
        try {
            switch (feature) {
                case DISENCHANTMENT -> Config.Disenchantment.Anvil.Repair.setResetEnabled(value);
                case SHATTERMENT -> Config.Shatterment.Anvil.Repair.setResetEnabled(value);
            }
        } finally {
            Config.commitBatch();
        }
    }

    private double getBaseCost() {
        return switch (feature) {
            case DISENCHANTMENT -> Config.Disenchantment.Anvil.Repair.getBaseCost();
            case SHATTERMENT -> Config.Shatterment.Anvil.Repair.getBaseCost();
        };
    }

    private void setBaseCost(double value) {
        Config.beginBatch();
        try {
            switch (feature) {
                case DISENCHANTMENT -> Config.Disenchantment.Anvil.Repair.setBaseCost(value);
                case SHATTERMENT -> Config.Shatterment.Anvil.Repair.setBaseCost(value);
            }
        } finally {
            Config.commitBatch();
        }
    }

    private double getCostMultiplier() {
        return switch (feature) {
            case DISENCHANTMENT -> Config.Disenchantment.Anvil.Repair.getCostMultiplier();
            case SHATTERMENT -> Config.Shatterment.Anvil.Repair.getCostMultiplier();
        };
    }

    private void setCostMultiplier(double value) {
        Config.beginBatch();
        try {
            switch (feature) {
                case DISENCHANTMENT -> Config.Disenchantment.Anvil.Repair.setCostMultiplier(value);
                case SHATTERMENT -> Config.Shatterment.Anvil.Repair.setCostMultiplier(value);
            }
        } finally {
            Config.commitBatch();
        }
    }

    private org.bukkit.inventory.ItemStack costEnabled() {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Repair.Disenchantment.Cost.enabled();
            case SHATTERMENT -> GUIComponent.Repair.Shatterment.Cost.enabled();
        };
    }

    private org.bukkit.inventory.ItemStack costDisabled() {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Repair.Disenchantment.Cost.disabled();
            case SHATTERMENT -> GUIComponent.Repair.Shatterment.Cost.disabled();
        };
    }

    private org.bukkit.inventory.ItemStack resetEnabled() {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Repair.Disenchantment.Reset.enabled();
            case SHATTERMENT -> GUIComponent.Repair.Shatterment.Reset.enabled();
        };
    }

    private org.bukkit.inventory.ItemStack resetDisabled() {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Repair.Disenchantment.Reset.disabled();
            case SHATTERMENT -> GUIComponent.Repair.Shatterment.Reset.disabled();
        };
    }

    private int getMaxCost() {
        return switch (feature) {
            case DISENCHANTMENT -> Config.Disenchantment.Anvil.Repair.getMaxCost();
            case SHATTERMENT -> Config.Shatterment.Anvil.Repair.getMaxCost();
        };
    }

    private void setMaxCost(int value) {
        Config.beginBatch();
        try {
            switch (feature) {
                case DISENCHANTMENT -> Config.Disenchantment.Anvil.Repair.setMaxCost(value);
                case SHATTERMENT -> Config.Shatterment.Anvil.Repair.setMaxCost(value);
            }
        } finally {
            Config.commitBatch();
        }
    }

    private org.bukkit.inventory.ItemStack base(int cost, int amount) {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Repair.Disenchantment.base(cost, amount);
            case SHATTERMENT -> GUIComponent.Repair.Shatterment.base(cost, amount);
        };
    }

    private org.bukkit.inventory.ItemStack multiplier(double multiplier, int amount) {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Repair.Disenchantment.multiplier(multiplier, amount);
            case SHATTERMENT -> GUIComponent.Repair.Shatterment.multiplier(multiplier, amount);
        };
    }

    private org.bukkit.inventory.ItemStack maxCost(int maxCost, int amount) {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Repair.Disenchantment.maxCost(maxCost, amount);
            case SHATTERMENT -> GUIComponent.Repair.Shatterment.maxCost(maxCost, amount);
        };
    }

    /**
     * Returns the anvil feature this GUI is configured for.
     *
     * @return the anvil feature
     */
    public AnvilFeature getFeature() {
        return feature;
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
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
