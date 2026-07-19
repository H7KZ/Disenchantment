package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.guis.GUIBorderComponent;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.InventoryBuilder;
import com.jankominek.disenchantment.guis.ItemBuilder;
import com.jankominek.disenchantment.stats.StatsCache;
import com.jankominek.disenchantment.stats.StatsManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * GUI screen displaying aggregate operation statistics from StatsCache.
 * Shows total disenchants, shatters, XP spent, money spent, and top enchantment.
 */
public class StatsGUI implements InventoryHolder {

    private final GUIItem[] items;
    private final Inventory inventory;

    public StatsGUI() {
        StatsCache cache = StatsManager.getInstance() != null
                ? StatsManager.getInstance().getCache() : null;

        long disenchants = cache != null ? cache.getTotalDisenchants() : 0;
        long shatters = cache != null ? cache.getTotalShatters() : 0;
        long xpSpent = cache != null ? cache.getTotalXpSpent() : 0;
        double money = cache != null ? cache.getTotalMoneySpent() : 0.0;
        String topEnch = cache != null ? cache.getTopEnchantment() : "none";

        this.items = ArrayUtils.addAll(
                GUIBorderComponent.border9x3(new Integer[]{0, 10, 12, 14, 16, 22}),
                new GUIItem(
                        0,
                        new ItemBuilder(Material.ARROW)
                                .setDisplayName("§7Back")
                                .addAllFlags()
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
                        }
                ),
                new GUIItem(
                        10,
                        new ItemBuilder(Material.BOOK)
                                .setDisplayName("§b§lDisenchants")
                                .setLore(List.of("§7Total: §f" + disenchants))
                                .addAllFlags()
                                .build(),
                        event -> event.setCancelled(true)
                ),
                new GUIItem(
                        12,
                        new ItemBuilder(Material.BOOK)
                                .setDisplayName("§d§lShatters")
                                .setLore(List.of("§7Total: §f" + shatters))
                                .addAllFlags()
                                .build(),
                        event -> event.setCancelled(true)
                ),
                new GUIItem(
                        14,
                        new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                                .setDisplayName("§a§lXP Spent")
                                .setLore(List.of("§7Levels: §f" + xpSpent))
                                .addAllFlags()
                                .build(),
                        event -> event.setCancelled(true)
                ),
                new GUIItem(
                        16,
                        new ItemBuilder(Material.GOLD_INGOT)
                                .setDisplayName("§6§lMoney Spent")
                                .setLore(List.of("§7Total: §f" + String.format("%.2f", money)))
                                .addAllFlags()
                                .build(),
                        event -> event.setCancelled(true)
                ),
                new GUIItem(
                        22,
                        new ItemBuilder(Material.ENCHANTED_BOOK)
                                .setDisplayName("§e§lTop Enchantment")
                                .setLore(List.of("§7Key: §f" + topEnch))
                                .addAllFlags()
                                .build(),
                        event -> event.setCancelled(true)
                )
        );

        Inventory inv = Bukkit.createInventory(this, 27,
                LegacyComponentSerializer.legacySection().deserialize(I18n.GUI.Stats.inventory()));
        this.inventory = InventoryBuilder.fillItems(inv, this.items);
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
