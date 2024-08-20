package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.guis.GUIItem;
import cz.kominekjan.disenchantment.guis.InventoryBuilder;
import cz.kominekjan.disenchantment.guis.ItemBuilder;
import cz.kominekjan.disenchantment.utils.DisenchantUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static cz.kominekjan.disenchantment.config.Config.getDisabledMaterials;
import static cz.kominekjan.disenchantment.config.Config.setDisabledMaterials;

public class MaterialsGUI implements InventoryHolder {
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
                                    ChatColor.GRAY + "Click on a material to toggle it on/off",
                                    ChatColor.GREEN + "Enabled" + ChatColor.GRAY + " = Material can be disenchanted",
                                    ChatColor.RED + "Disabled" + ChatColor.GRAY + " = Material cannot be disenchanted",
                                    ChatColor.GRAY + "To edit other materials than listed",
                                    ChatColor.GRAY + "use the command [/disenchantment materials]"
                            ))
                    ),
                    DefaultGUIElements::cancelOnClick
            )
    );
    private Inventory inventory;

    public MaterialsGUI(int page) {
        List<Material> materials = Registry.MATERIAL
                .stream()
                .filter(Material::isItem)
                .filter(m -> !m.isAir())
                .filter(DisenchantUtils::canBeEnchanted)
                .sorted(Comparator.comparing(m -> m.getKey().getKey()))
                .toList();

        this.page = page;
        this.title = "Materials " + (page + 1) + "/" + (materials.size() / 28 + 1);

        Inventory inv = Bukkit.createInventory(this, this.size, this.title);

        this.inventory = InventoryBuilder.fillItems(inv, this.items);
        this.items = ArrayUtils.addAll(this.items, this.getMaterialsItems(materials));
        this.inventory = InventoryBuilder.fillItems(this.inventory, this.items);

        if (materials.size() > 28) {
            this.items = Arrays.stream(this.items.clone()).filter(item -> item.getSlot() != 47 && item.getSlot() != 51).toArray(GUIItem[]::new);

            this.items = ArrayUtils.addAll(
                    this.items,
                    new GUIItem(47, DefaultGUIElements.previousPageItem(), event -> {
                        event.setCancelled(true);

                        if (this.page == 0) return;

                        event.getWhoClicked().openInventory(new MaterialsGUI(this.page - 1).getInventory());
                    }),
                    new GUIItem(51, DefaultGUIElements.nextPageItem(), event -> {
                        event.setCancelled(true);

                        if (this.page == materials.size() / 28) return;

                        event.getWhoClicked().openInventory(new MaterialsGUI(this.page + 1).getInventory());
                    })
            );

            this.inventory = InventoryBuilder.fillItems(this.inventory, this.items);
        }
    }

    public final GUIItem[] getMaterialsItems(List<Material> materials) {
        int pageSize = Math.min(materials.size() - (this.page * 28), 28);

        GUIItem[] materialItems = new GUIItem[pageSize];

        for (int i = 0; i < pageSize; i++) {
            int slot = i + this.page * 28;

            Material material = materials.get(slot);

            if (material == null) continue;

            materialItems[i] = new GUIItem(
                    freeSlots[i],
                    new ItemBuilder(material)
                            .setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + material.getKey().getKey())
                            .setLore(getDisabledMaterials().contains(material) ? ChatColor.RED + "Disabled" : ChatColor.GREEN + "Enabled")
                            .build(),
                    event -> {
                        event.setCancelled(true);

                        List<Material> disabledMaterials = getDisabledMaterials();

                        if (disabledMaterials.contains(material)) {
                            disabledMaterials.remove(material);
                        } else {
                            disabledMaterials.add(material);
                        }

                        setDisabledMaterials(disabledMaterials);

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
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
