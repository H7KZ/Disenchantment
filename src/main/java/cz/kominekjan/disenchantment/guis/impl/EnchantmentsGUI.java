package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.Disenchantment;
import cz.kominekjan.disenchantment.config.EnchantmentStatus;
import cz.kominekjan.disenchantment.guis.GUIItem;
import cz.kominekjan.disenchantment.guis.InventoryBuilder;
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

import static cz.kominekjan.disenchantment.config.Config.*;

public class EnchantmentsGUI implements InventoryHolder {
    private final Integer[] freeSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private final Integer page;
    private final Integer size = 54;
    private final String title;
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
                                    ChatColor.GREEN + "Enabled" + ChatColor.GRAY + " = Enchantment can be removed from items",
                                    ChatColor.GOLD + "Keep" + ChatColor.GRAY + " = Enchantment stays on items",
                                    ChatColor.RED + "Cancel" + ChatColor.GRAY + " = Cancels the entire disenchantment process"
                            ))
                    ),
                    DefaultGUIElements::cancelOnClick
            )
    );
    private Inventory inventory;

    public EnchantmentsGUI(int page) {
        List<Enchantment> enchantments = Registry.ENCHANTMENT.stream().sorted(Comparator.comparing(e -> e.getKey().getKey())).toList();

        this.page = page;
        this.title = "Enchantments " + (page + 1) + "/" + (enchantments.size() / 28 + 1);

        Inventory inv = Bukkit.createInventory(this, this.size, this.title);

        this.inventory = InventoryBuilder.fillItems(inv, this.items);
        this.items = ArrayUtils.addAll(this.items, this.getEnchantmentsItems(enchantments));
        this.inventory = InventoryBuilder.fillItems(this.inventory, this.items);

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

            this.inventory = InventoryBuilder.fillItems(this.inventory, this.items);
        }
    }

    public final GUIItem[] getEnchantmentsItems(List<Enchantment> enchantments) {
        int pageSize = Math.min(enchantments.size() - (this.page * 28), 28);

        GUIItem[] worldItems = new GUIItem[pageSize];

        Map<Enchantment, EnchantmentStatus> statuses = getEnchantmentStatus();

        for (int i = 0; i < pageSize; i++) {
            int slot = i + this.page * 28;

            Enchantment enchantment = enchantments.get(slot);

            if (enchantment == null) continue;

            EnchantmentStatus status = statuses.getOrDefault(enchantment, EnchantmentStatus.ENABLED);

            String lore = status.getDisplayName();

            worldItems[i] = new GUIItem(
                    freeSlots[i],
                    DefaultGUIElements.enchantmentItem(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + enchantment.getKey().getKey(), lore),
                    event -> {
                        event.setCancelled(true);

                        EnchantmentStatus oldStatus = getEnchantmentStatus().getOrDefault(enchantment, EnchantmentStatus.ENABLED);
                        EnchantmentStatus newStatus = EnchantmentStatus.getNextStatus(oldStatus);

                        String newLore = newStatus.getDisplayName();
                        setEnchantmentsStatus(enchantment, newStatus);

                        event.setCurrentItem(DefaultGUIElements.enchantmentItem(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + enchantment.getKey().getKey(), newLore));
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
