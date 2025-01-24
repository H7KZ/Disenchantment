package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.InventoryBuilder;
import com.jankominek.disenchantment.guis.ItemBuilder;
import com.jankominek.disenchantment.guis.components.GUIElements;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.jankominek.disenchantment.utils.MaterialUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MaterialsGUI implements InventoryHolder {
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
                                    ChatColor.GRAY + "Click on a material to toggle it on/off",
                                    ChatColor.GRAY + "To edit other materials that aren't listed",
                                    ChatColor.GRAY + "use the command [/disenchantment disenchantment_materials]",
                                    ChatColor.GREEN + "Enabled" + ChatColor.GRAY + " = Material can be disenchanted",
                                    ChatColor.RED + "Disabled" + ChatColor.GRAY + " = Material cannot be disenchanted"
                            ))
                    ),
                    GUIElements::cancelOnClick
            )
    );

    public MaterialsGUI(int page) {
        List<Material> materials = MaterialUtils.getMaterials()
                .stream()
                .filter(Material::isItem)
                .filter(m -> !m.isAir())
                .filter(EnchantmentUtils::canItemBeEnchanted)
                .sorted(Comparator.comparing(m -> m.getKey().getKey()))
                .toList();

        this.page = page;
        this.title = "Disenchantment | Materials " + (page + 1) + "/" + (materials.size() / 28 + 1);

        Inventory inv = Bukkit.createInventory(this, this.size, this.title);

        this.inventory = InventoryBuilder.fillItems(inv, this.items);
        this.items = ArrayUtils.addAll(this.items, this.getMaterialsItems(materials));

        InventoryBuilder.fillItems(this.inventory, this.items);

        if (materials.size() > 28) {
            this.items = Arrays.stream(this.items.clone()).filter(item -> item.getSlot() != 47 && item.getSlot() != 51).toArray(GUIItem[]::new);

            this.items = ArrayUtils.addAll(
                    this.items,
                    new GUIItem(47, GUIElements.previousPageItem(), event -> {
                        event.setCancelled(true);

                        if (this.page == 0) return;

                        event.getWhoClicked().openInventory(new MaterialsGUI(this.page - 1).getInventory());
                    }),
                    new GUIItem(51, GUIElements.nextPageItem(), event -> {
                        event.setCancelled(true);

                        if (this.page == materials.size() / 28) return;

                        event.getWhoClicked().openInventory(new MaterialsGUI(this.page + 1).getInventory());
                    })
            );

            InventoryBuilder.fillItems(this.inventory, this.items);
        }
    }

    public final GUIItem[] getMaterialsItems(List<Material> materials) {
        int pageSize = Math.min(materials.size() - (this.page * 28), 28);

        GUIItem[] materialItems = new GUIItem[pageSize];

        List<Material> disabledMaterials = Config.Disenchantment.getDisabledMaterials();

        for (int i = 0; i < pageSize; i++) {
            int slot = i + this.page * 28;

            Material material = materials.get(slot);

            if (material == null) continue;

            materialItems[i] = new GUIItem(
                    freeSlots[i],
                    new ItemBuilder(material)
                            .setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + material.getKey().getKey())
                            .setLore(disabledMaterials.contains(material) ? ChatColor.RED + "Disabled" : ChatColor.GREEN + "Enabled")
                            .build(),
                    event -> {
                        event.setCancelled(true);

                        if (disabledMaterials.contains(material)) disabledMaterials.remove(material);
                        else disabledMaterials.add(material);

                        Config.Disenchantment.setDisabledMaterials(disabledMaterials);

                        event.setCurrentItem(
                                new ItemBuilder(material)
                                        .setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + material.getKey().getKey())
                                        .setLore(disabledMaterials.contains(material) ? ChatColor.RED + "Disabled" : ChatColor.GREEN + "Enabled")
                                        .build()
                        );
                    }
            );
        }

        return materialItems;
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
