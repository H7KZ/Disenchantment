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

/**
 * GUI for configuring economy settings for a given {@link AnvilFeature}.
 * Allows toggling economy on/off, adjusting cost, and toggling show-cost and charge-message.
 */
public class EconomyGUI implements InventoryHolder {
    private final AnvilFeature feature;
    private final GUIItem[] items;
    private final Inventory inventory;

    /**
     * Constructs the economy GUI for the specified anvil feature, creating and populating the inventory.
     *
     * @param feature the anvil feature this GUI configures
     */
    public EconomyGUI(AnvilFeature feature) {
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
                GUIBorderComponent.border(9),
                new GUIItem(
                        10,
                        isEnabled() ? enabledItem() : disabledItem(),
                        event -> {
                            event.setCancelled(true);

                            boolean economyEnabled = !isEnabled();

                            setEnabled(economyEnabled);

                            event.setCurrentItem(economyEnabled ? enabledItem() : disabledItem());
                        }
                ),
                GUIBorderComponent.border(11),
                new GUIItem(
                        12,
                        cost(getCost(), Math.min((int) Math.round(getCost()), 64)),
                        event -> {
                            event.setCancelled(true);

                            double cost = getCost();

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

                            setCost(cost);

                            event.setCurrentItem(cost(cost, Math.min((int) Math.round(cost), 64)));
                        }
                ),
                GUIBorderComponent.border(13),
                new GUIItem(
                        14,
                        isShowCostEnabled() ? showCostEnabledItem() : showCostDisabledItem(),
                        event -> {
                            event.setCancelled(true);

                            boolean showCostEnabled = !isShowCostEnabled();

                            setShowCostEnabled(showCostEnabled);

                            event.setCurrentItem(showCostEnabled ? showCostEnabledItem() : showCostDisabledItem());
                        }
                ),
                GUIBorderComponent.border(15),
                new GUIItem(
                        16,
                        isChargeMessageEnabled() ? chargeMessageEnabledItem() : chargeMessageDisabledItem(),
                        event -> {
                            event.setCancelled(true);

                            boolean chargeMessageEnabled = !isChargeMessageEnabled();

                            setChargeMessageEnabled(chargeMessageEnabled);

                            event.setCurrentItem(chargeMessageEnabled ? chargeMessageEnabledItem() : chargeMessageDisabledItem());
                        }
                ),
                GUIBorderComponent.border(17),
                new GUIItem(
                        22,
                        GUIComponent.back(),
                        event -> {
                            event.setCancelled(true);

                            event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
                        }
                )
        );

        String title = switch (feature) {
            case DISENCHANTMENT -> I18n.GUI.Economy.Disenchantment.inventory();
            case SHATTERMENT -> I18n.GUI.Economy.Shatterment.inventory();
        };

        Inventory inv = Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize(title));

        this.inventory = InventoryBuilder.fillItems(inv, this.items);
    }

    private boolean isEnabled() {
        return switch (feature) {
            case DISENCHANTMENT -> Config.Disenchantment.Economy.isEnabled();
            case SHATTERMENT -> Config.Shatterment.Economy.isEnabled();
        };
    }

    private void setEnabled(boolean value) {
        Config.beginBatch();
        try {
            switch (feature) {
                case DISENCHANTMENT -> Config.Disenchantment.Economy.setEnabled(value);
                case SHATTERMENT -> Config.Shatterment.Economy.setEnabled(value);
            }
        } finally {
            Config.commitBatch();
        }
    }

    private double getCost() {
        return switch (feature) {
            case DISENCHANTMENT -> Config.Disenchantment.Economy.getCost();
            case SHATTERMENT -> Config.Shatterment.Economy.getCost();
        };
    }

    private void setCost(double value) {
        Config.beginBatch();
        try {
            switch (feature) {
                case DISENCHANTMENT -> Config.Disenchantment.Economy.setCost(value);
                case SHATTERMENT -> Config.Shatterment.Economy.setCost(value);
            }
        } finally {
            Config.commitBatch();
        }
    }

    private boolean isShowCostEnabled() {
        return switch (feature) {
            case DISENCHANTMENT -> Config.Disenchantment.Economy.isShowCostEnabled();
            case SHATTERMENT -> Config.Shatterment.Economy.isShowCostEnabled();
        };
    }

    private void setShowCostEnabled(boolean value) {
        Config.beginBatch();
        try {
            switch (feature) {
                case DISENCHANTMENT -> Config.Disenchantment.Economy.setShowCostEnabled(value);
                case SHATTERMENT -> Config.Shatterment.Economy.setShowCostEnabled(value);
            }
        } finally {
            Config.commitBatch();
        }
    }

    private boolean isChargeMessageEnabled() {
        return switch (feature) {
            case DISENCHANTMENT -> Config.Disenchantment.Economy.isChargeMessageEnabled();
            case SHATTERMENT -> Config.Shatterment.Economy.isChargeMessageEnabled();
        };
    }

    private void setChargeMessageEnabled(boolean value) {
        Config.beginBatch();
        try {
            switch (feature) {
                case DISENCHANTMENT -> Config.Disenchantment.Economy.setChargeMessageEnabled(value);
                case SHATTERMENT -> Config.Shatterment.Economy.setChargeMessageEnabled(value);
            }
        } finally {
            Config.commitBatch();
        }
    }

    private org.bukkit.inventory.ItemStack enabledItem() {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Economy.Disenchantment.enabled();
            case SHATTERMENT -> GUIComponent.Economy.Shatterment.enabled();
        };
    }

    private org.bukkit.inventory.ItemStack disabledItem() {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Economy.Disenchantment.disabled();
            case SHATTERMENT -> GUIComponent.Economy.Shatterment.disabled();
        };
    }

    private org.bukkit.inventory.ItemStack cost(double cost, int amount) {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Economy.Disenchantment.cost(cost, amount);
            case SHATTERMENT -> GUIComponent.Economy.Shatterment.cost(cost, amount);
        };
    }

    private org.bukkit.inventory.ItemStack showCostEnabledItem() {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Economy.Disenchantment.showCostEnabled();
            case SHATTERMENT -> GUIComponent.Economy.Shatterment.showCostEnabled();
        };
    }

    private org.bukkit.inventory.ItemStack showCostDisabledItem() {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Economy.Disenchantment.showCostDisabled();
            case SHATTERMENT -> GUIComponent.Economy.Shatterment.showCostDisabled();
        };
    }

    private org.bukkit.inventory.ItemStack chargeMessageEnabledItem() {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Economy.Disenchantment.chargeMessageEnabled();
            case SHATTERMENT -> GUIComponent.Economy.Shatterment.chargeMessageEnabled();
        };
    }

    private org.bukkit.inventory.ItemStack chargeMessageDisabledItem() {
        return switch (feature) {
            case DISENCHANTMENT -> GUIComponent.Economy.Disenchantment.chargeMessageDisabled();
            case SHATTERMENT -> GUIComponent.Economy.Shatterment.chargeMessageDisabled();
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
    public Inventory getInventory() {
        return this.inventory;
    }
}
