package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.guis.GUIBorderComponent;
import com.jankominek.disenchantment.guis.GUIComponent;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.InventoryBuilder;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Paginated GUI for configuring enchantment states (enable, disable, keep, delete)
 * for both disenchantment and shatterment. Left-click cycles the disenchantment state;
 * right-click cycles the shatterment state.
 */
public class EnchantmentsGUI implements InventoryHolder {
    private final int[] freeSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private final Inventory inventory;
    private int page;
    private List<Enchantment> enchantments;
    private GUIItem[] items = org.apache.commons.lang3.ArrayUtils.addAll(
            GUIBorderComponent.border9x6(new Integer[]{0, 49}),
            new GUIItem(
                    0,
                    GUIComponent.back(),
                    event -> {
                        event.setCancelled(true);

                        event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
                    }
            ),
            new GUIItem(
                    47,
                    GUIComponent.previous(),
                    event -> {
                        event.setCancelled(true);

                        if (this.page == 0) return;

                        event.getWhoClicked().openInventory(new EnchantmentsGUI(this.page - 1).getInventory());
                    }
            ),
            new GUIItem(
                    49,
                    GUIComponent.Enchantments.help(),
                    event -> event.setCancelled(true)
            ),
            new GUIItem(
                    51,
                    GUIComponent.next(),
                    event -> {
                        event.setCancelled(true);

                        if (this.page == this.enchantments.size() / 28) return;

                        event.getWhoClicked().openInventory(new EnchantmentsGUI(this.page + 1).getInventory());
                    }
            )
    );

    /**
     * Constructs the enchantments GUI for the specified page, populating it with registered enchantments.
     *
     * @param page the zero-based page index
     */
    public EnchantmentsGUI(int page) {
        this.page = page;
        this.enchantments = EnchantmentUtils.getRegisteredEnchantments();
        this.enchantments.sort(Comparator.comparing(e -> e.getKey().getKey()));

        String title = I18n.GUI.Enchantments.inventory() + " " + (page + 1) + "/" + (this.enchantments.size() / 28 + 1);

        Inventory inventory = Bukkit.createInventory(this, 54, title);

        this.items = ArrayUtils.addAll(this.items, this.fillPageWithEnchantments());

        this.inventory = InventoryBuilder.fillItems(inventory, this.items);
    }

    /**
     * Creates GUI items for the enchantments on the current page, each with click handlers
     * to toggle disenchantment and shatterment states.
     *
     * @return an array of {@link GUIItem} elements for the current page
     */
    public final GUIItem[] fillPageWithEnchantments() {
        int pageSize = Math.min(this.enchantments.size() - (this.page * 28), 28);

        GUIItem[] worldItems = new GUIItem[pageSize];

        HashMap<String, EnchantmentStateType> disenchantmentEnchantmentsStates = Config.Disenchantment.getEnchantmentStates();
        HashMap<String, EnchantmentStateType> shattermentEnchantmentsStates = Config.Shatterment.getEnchantmentStates();

        for (int i = 0; i < pageSize; i++) {
            int slot = i + this.page * 28;

            Enchantment enchantment = this.enchantments.get(slot);

            if (enchantment == null) continue;

            AtomicReference<EnchantmentStateType> disenchantmentState = new AtomicReference<>(disenchantmentEnchantmentsStates.getOrDefault(enchantment.getKey().getKey(), EnchantmentStateType.ENABLE));
            AtomicReference<EnchantmentStateType> shattermentState = new AtomicReference<>(shattermentEnchantmentsStates.getOrDefault(enchantment.getKey().getKey(), EnchantmentStateType.ENABLE));

            worldItems[i] = new GUIItem(
                    freeSlots[i],
                    GUIComponent.Enchantments.enchantment(
                            ChatColor.GRAY + "" + ChatColor.BOLD + enchantment.getKey().getKey(),
                            disenchantmentState.get(),
                            shattermentState.get()
                    ),
                    event -> {
                        event.setCancelled(true);

                        switch (event.getClick()) {
                            case LEFT: {
                                EnchantmentStateType newDisenchantmentStateType = EnchantmentStateType.getNextState(disenchantmentState.get());
                                disenchantmentState.set(newDisenchantmentStateType);

                                if (newDisenchantmentStateType.equals(EnchantmentStateType.ENABLE))
                                    disenchantmentEnchantmentsStates.remove(enchantment.getKey().getKey());
                                else if (!disenchantmentEnchantmentsStates.containsKey(enchantment.getKey().getKey()))
                                    disenchantmentEnchantmentsStates.put(enchantment.getKey().getKey(), newDisenchantmentStateType);
                                else
                                    disenchantmentEnchantmentsStates.replace(enchantment.getKey().getKey(), newDisenchantmentStateType);

                                Config.Disenchantment.setEnchantmentStates(disenchantmentEnchantmentsStates);

                                break;
                            }
                            case RIGHT: {
                                EnchantmentStateType newShattermentState = EnchantmentStateType.getNextState(shattermentState.get());
                                shattermentState.set(newShattermentState);

                                if (newShattermentState.equals(EnchantmentStateType.ENABLE))
                                    shattermentEnchantmentsStates.remove(enchantment.getKey().getKey());
                                else if (!shattermentEnchantmentsStates.containsKey(enchantment.getKey().getKey()))
                                    shattermentEnchantmentsStates.put(enchantment.getKey().getKey(), newShattermentState);
                                else
                                    shattermentEnchantmentsStates.replace(enchantment.getKey().getKey(), newShattermentState);

                                Config.Shatterment.setEnchantmentStates(shattermentEnchantmentsStates);

                                break;
                            }
                            default:
                                return;
                        }

                        event.setCurrentItem(GUIComponent.Enchantments.enchantment(
                                ChatColor.GRAY + "" + ChatColor.BOLD + enchantment.getKey().getKey(),
                                disenchantmentState.get(),
                                shattermentState.get()
                        ));
                    }
            );
        }

        return worldItems;
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
