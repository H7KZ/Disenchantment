package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.guis.GUIBorderComponent;
import com.jankominek.disenchantment.guis.GUIComponent;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.InventoryBuilder;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.jankominek.disenchantment.utils.MaterialUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Comparator;
import java.util.List;

public class MaterialsGUI implements InventoryHolder {
    private final int[] freeSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private final Inventory inventory;
    private int page;
    private List<Material> materials;
    private GUIItem[] items = ArrayUtils.addAll(
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

                        event.getWhoClicked().openInventory(new MaterialsGUI(this.page - 1).getInventory());
                    }
            ),
            new GUIItem(
                    49,
                    GUIComponent.Materials.help(),
                    event -> event.setCancelled(true)
            ),
            new GUIItem(
                    51,
                    GUIComponent.next(),
                    event -> {
                        event.setCancelled(true);

                        if (this.page == this.materials.size() / 28) return;

                        event.getWhoClicked().openInventory(new MaterialsGUI(this.page + 1).getInventory());
                    }
            )
    );

    public MaterialsGUI(int page) {
        this.page = page;
        this.materials = MaterialUtils.getMaterials()
                .stream()
                .filter(Material::isItem)
                .filter(m -> !m.isAir())
                .filter(EnchantmentUtils::canItemBeEnchanted)
                .sorted(Comparator.comparing(m -> m.getKey().getKey()))
                .toList();

        String title = I18n.GUI.Materials.inventory() + " " + (page + 1) + "/" + (materials.size() / 28 + 1);

        Inventory inventory = Bukkit.createInventory(this, 54, title);

        this.items = ArrayUtils.addAll(this.items, this.getMaterialsItems(materials));

        this.inventory = InventoryBuilder.fillItems(inventory, this.items);
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
                    GUIComponent.Materials.material(material, disabledMaterials.contains(material)),
                    event -> {
                        event.setCancelled(true);

                        if (disabledMaterials.contains(material)) disabledMaterials.remove(material);
                        else disabledMaterials.add(material);

                        Config.Disenchantment.setDisabledMaterials(disabledMaterials);

                        event.setCurrentItem(GUIComponent.Materials.material(material, disabledMaterials.contains(material)));
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
