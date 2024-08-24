package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.guis.GUIItem;
import cz.kominekjan.disenchantment.guis.InventoryBuilder;
import cz.kominekjan.disenchantment.types.EnchantmentState;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
    private GUIItem[] items = ArrayUtils.addAll(
            DefaultGUIElements.border9x6(new Integer[]{0, 49}),
            new GUIItem(0, DefaultGUIElements.backItem(), event -> {
                event.setCancelled(true);
                event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
            }),
            new GUIItem(
                    49,
                    DefaultGUIElements.infoItem(
                            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Help",
                            new ArrayList<>(Arrays.asList(
                                    ChatColor.GRAY + "Click on an enchantment to change its behavior",
                                    ChatColor.GRAY + "Left click for " + ChatColor.GOLD + "Disenchantment",
                                    ChatColor.GRAY + "Right click for " + ChatColor.AQUA + "Shatterment",
                                    ChatColor.GREEN + "Enabled" + ChatColor.GRAY + " = Enchantment can be removed from items",
                                    ChatColor.GOLD + "Keep" + ChatColor.GRAY + " = Enchantment stays on items",
                                    ChatColor.RED + "Cancel" + ChatColor.GRAY + " = Cancels the entire disenchantment process"
                            ))
                    ),
                    DefaultGUIElements::cancelOnClick
            )
    );

    public EnchantmentsGUI(int page) {
        List<Enchantment> enchantments = Registry.ENCHANTMENT.stream().sorted(Comparator.comparing(e -> e.getKey().getKey())).toList();

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
                    new GUIItem(47, DefaultGUIElements.previousPageItem(), event -> {
                        event.setCancelled(true);

                        if (this.page == 0) return;

                        event.getWhoClicked().openInventory(new EnchantmentsGUI(this.page - 1).getInventory());
                    }),
                    new GUIItem(51, DefaultGUIElements.nextPageItem(), event -> {
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

        HashMap<Enchantment, EnchantmentState> disenchantmentEnchantmentsStates = Config.Disenchantment.getEnchantmentStates();
        HashMap<Enchantment, EnchantmentState> shattermentEnchantmentsStates = Config.Shatterment.getEnchantmentStates();

        for (int i = 0; i < pageSize; i++) {
            int slot = i + this.page * 28;

            Enchantment enchantment = enchantments.get(slot);

            if (enchantment == null) continue;

            EnchantmentState disenchantmentState = disenchantmentEnchantmentsStates.getOrDefault(enchantment, EnchantmentState.ENABLED);
            EnchantmentState shattermentState = shattermentEnchantmentsStates.getOrDefault(enchantment, EnchantmentState.ENABLED);

            worldItems[i] = new GUIItem(
                    freeSlots[i],
                    DefaultGUIElements.enchantmentItem(
                            ChatColor.GRAY + "" + ChatColor.BOLD + enchantment.getKey().getKey(),
                            ChatColor.GRAY + "Disenchantment: " + disenchantmentState.getDisplayName(),
                            ChatColor.GRAY + "Shatterment: " + shattermentState.getDisplayName()
                    ),
                    event -> {
                        event.setCancelled(true);

                        switch (event.getClick()) {
                            case LEFT:
                                EnchantmentState newDisenchantmentState = EnchantmentState.getNextState(disenchantmentState);

                                if (newDisenchantmentState.equals(EnchantmentState.ENABLED))
                                    disenchantmentEnchantmentsStates.remove(enchantment);
                                else if (!disenchantmentEnchantmentsStates.containsKey(enchantment))
                                    disenchantmentEnchantmentsStates.put(enchantment, newDisenchantmentState);
                                else disenchantmentEnchantmentsStates.replace(enchantment, newDisenchantmentState);

                                Config.Disenchantment.setEnchantmentStates(disenchantmentEnchantmentsStates);

                                break;
                            case RIGHT:
                                EnchantmentState newShattermentState = EnchantmentState.getNextState(shattermentState);

                                if (newShattermentState.equals(EnchantmentState.ENABLED))
                                    shattermentEnchantmentsStates.remove(enchantment);
                                else if (!shattermentEnchantmentsStates.containsKey(enchantment))
                                    shattermentEnchantmentsStates.put(enchantment, newShattermentState);
                                else shattermentEnchantmentsStates.replace(enchantment, newShattermentState);

                                Config.Shatterment.setEnchantmentStates(shattermentEnchantmentsStates);

                                break;
                            default:
                                return;
                        }

                        event.setCurrentItem(DefaultGUIElements.enchantmentItem(
                                ChatColor.GRAY + "" + ChatColor.BOLD + enchantment.getKey().getKey(),
                                ChatColor.GRAY + "Disenchantment: " + disenchantmentState.getDisplayName(),
                                ChatColor.GRAY + "Shatterment: " + shattermentState.getDisplayName()
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
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
