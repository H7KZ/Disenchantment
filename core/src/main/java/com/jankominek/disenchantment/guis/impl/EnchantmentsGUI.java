package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.guis.GUIElements;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class EnchantmentsGUI implements InventoryHolder {
    private final Integer size = 54;
    private final String title;
    private final Integer[] freeSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private final Integer page;
    private final Inventory inventory;
    private GUIItem[] items = org.apache.commons.lang3.ArrayUtils.addAll(
            GUIElements.border9x6(new Integer[]{0, 49}),
            new GUIItem(0, GUIElements.backItem(), event -> {
                event.setCancelled(true);
                event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
            }),
            new GUIItem(
                    49,
                    GUIElements.infoItem(
                            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Help",
                            new ArrayList<>(Arrays.asList(
                                    ChatColor.GRAY + "Click on an enchantment to change its behavior",
                                    ChatColor.GRAY + "Left click for " + ChatColor.GOLD + "Disenchantment",
                                    ChatColor.GRAY + "Right click for " + ChatColor.AQUA + "Shatterment",
                                    ChatColor.GREEN + "Enabled" + ChatColor.GRAY + " = Enchantment can be removed from items.",
                                    ChatColor.GOLD + "Keep" + ChatColor.GRAY + " = Enchantment stays on items.",
                                    ChatColor.YELLOW + "Delete" + ChatColor.GRAY + " = Enchantment is removed from item and destroyed.",
                                    ChatColor.RED + "Cancel" + ChatColor.GRAY + " = Cancels the entire disenchantment process."
                            ))
                    ),
                    GUIElements::cancelOnClick
            )
    );

    public EnchantmentsGUI(int page) {
        List<Enchantment> enchantments = EnchantmentUtils.getRegisteredEnchantments();
        enchantments.sort(Comparator.comparing(e -> e.getKey().getKey()));

        this.page = page;
        this.title = "Enchantments " + (page + 1) + "/" + (enchantments.size() / 28 + 1);

        Inventory inv = Bukkit.createInventory(this, this.size, this.title);

        this.inventory = InventoryBuilder.fillItems(inv, this.items);
        this.items = ArrayUtils.addAll(this.items, this.getEnchantmentsItems(enchantments));

        InventoryBuilder.fillItems(this.inventory, this.items);

        if (enchantments.size() > 28) {
            this.items = Arrays.stream(this.items.clone()).filter(item -> item.getSlot() != 47 && item.getSlot() != 51).toArray(GUIItem[]::new);

            this.items = ArrayUtils.addAll(
                    this.items,
                    new GUIItem(47, GUIElements.previousPageItem(), event -> {
                        event.setCancelled(true);

                        if (this.page == 0) return;

                        event.getWhoClicked().openInventory(new EnchantmentsGUI(this.page - 1).getInventory());
                    }),
                    new GUIItem(51, GUIElements.nextPageItem(), event -> {
                        event.setCancelled(true);

                        if (this.page == enchantments.size() / 28) return;

                        event.getWhoClicked().openInventory(new EnchantmentsGUI(this.page + 1).getInventory());
                    })
            );

            InventoryBuilder.fillItems(this.inventory, this.items);
        }
    }

    public final GUIItem[] getEnchantmentsItems(List<Enchantment> enchantments) {
        int pageSize = Math.min(enchantments.size() - (this.page * 28), 28);

        GUIItem[] worldItems = new GUIItem[pageSize];

        HashMap<Enchantment, EnchantmentStateType> disenchantmentEnchantmentsStates = Config.Disenchantment.getEnchantmentStates();
        HashMap<Enchantment, EnchantmentStateType> shattermentEnchantmentsStates = Config.Shatterment.getEnchantmentStates();

        for (int i = 0; i < pageSize; i++) {
            int slot = i + this.page * 28;

            Enchantment enchantment = enchantments.get(slot);

            if (enchantment == null) continue;

            AtomicReference<EnchantmentStateType> disenchantmentState = new AtomicReference<>(disenchantmentEnchantmentsStates.getOrDefault(enchantment, EnchantmentStateType.ENABLED));
            AtomicReference<EnchantmentStateType> shattermentState = new AtomicReference<>(shattermentEnchantmentsStates.getOrDefault(enchantment, EnchantmentStateType.ENABLED));

            worldItems[i] = new GUIItem(
                    freeSlots[i],
                    GUIElements.enchantmentItem(
                            ChatColor.GRAY + "" + ChatColor.BOLD + enchantment.getKey().getKey(),
                            ChatColor.GRAY + "Disenchantment: " + disenchantmentState.get().getDisplayName(),
                            ChatColor.GRAY + "Shatterment: " + shattermentState.get().getDisplayName()
                    ),
                    event -> {
                        event.setCancelled(true);

                        switch (event.getClick()) {
                            case LEFT:
                                EnchantmentStateType newDisenchantmentStateType = EnchantmentStateType.getNextState(disenchantmentState.get());
                                disenchantmentState.set(newDisenchantmentStateType);

                                if (newDisenchantmentStateType.equals(EnchantmentStateType.ENABLED))
                                    disenchantmentEnchantmentsStates.remove(enchantment);
                                else if (!disenchantmentEnchantmentsStates.containsKey(enchantment))
                                    disenchantmentEnchantmentsStates.put(enchantment, newDisenchantmentStateType);
                                else disenchantmentEnchantmentsStates.replace(enchantment, newDisenchantmentStateType);

                                Config.Disenchantment.setEnchantmentStates(disenchantmentEnchantmentsStates);

                                break;
                            case RIGHT:
                                EnchantmentStateType newShattermentState = EnchantmentStateType.getNextState(shattermentState.get());
                                shattermentState.set(newShattermentState);

                                if (newShattermentState.equals(EnchantmentStateType.ENABLED))
                                    shattermentEnchantmentsStates.remove(enchantment);
                                else if (!shattermentEnchantmentsStates.containsKey(enchantment))
                                    shattermentEnchantmentsStates.put(enchantment, newShattermentState);
                                else shattermentEnchantmentsStates.replace(enchantment, newShattermentState);

                                Config.Shatterment.setEnchantmentStates(shattermentEnchantmentsStates);

                                break;
                            default:
                                return;
                        }

                        event.setCurrentItem(GUIElements.enchantmentItem(
                                ChatColor.GRAY + "" + ChatColor.BOLD + enchantment.getKey().getKey(),
                                ChatColor.GRAY + "Disenchantment: " + disenchantmentState.get().getDisplayName(),
                                ChatColor.GRAY + "Shatterment: " + shattermentState.get().getDisplayName()
                        ));
                    }
            );
        }

        return worldItems;
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
