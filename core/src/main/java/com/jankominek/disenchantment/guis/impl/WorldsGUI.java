package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.guis.GUIBorderComponent;
import com.jankominek.disenchantment.guis.GUIComponent;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.InventoryBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Paginated GUI for configuring which worlds have disenchantment and shatterment disabled.
 * Left-click toggles disenchantment; right-click toggles shatterment for each world.
 */
public class WorldsGUI implements InventoryHolder {
    private final int[] freeSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private final Inventory inventory;
    private int page;
    private List<World> worlds;
    private GUIItem[] items = ArrayUtils.addAll(
            GUIBorderComponent.border9x6(new Integer[]{0, 47, 49, 51}),
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

                        event.getWhoClicked().openInventory(new WorldsGUI(this.page - 1).getInventory());
                    }
            ),
            new GUIItem(
                    49,
                    GUIComponent.Worlds.help(),
                    event -> event.setCancelled(true)
            ),
            new GUIItem(
                    51,
                    GUIComponent.next(),
                    event -> {
                        event.setCancelled(true);

                        if (this.page == this.worlds.size() / 28) return;

                        event.getWhoClicked().openInventory(new WorldsGUI(this.page + 1).getInventory());
                    }
            )
    );

    /**
     * Constructs the worlds GUI for the specified page, populating it with server worlds.
     *
     * @param page the zero-based page index
     */
    public WorldsGUI(int page) {
        this.page = page;
        this.worlds = Bukkit.getWorlds();

        String title = I18n.GUI.Worlds.inventory() + " " + (page + 1) + "/" + (this.worlds.size() / 28 + 1);

        Inventory inventory = Bukkit.createInventory(this, 54, title);

        this.items = ArrayUtils.addAll(this.items, this.fillPageWithWorlds());

        this.inventory = InventoryBuilder.fillItems(inventory, this.items);
    }

    /**
     * Creates GUI items for the worlds on the current page, each with click handlers
     * to toggle disenchantment and shatterment disabled status.
     *
     * @return an array of {@link GUIItem} elements for the current page
     */
    public final GUIItem[] fillPageWithWorlds() {
        int pageSize = Math.min(this.worlds.size() - (this.page * 28), 28);

        GUIItem[] worldItems = new GUIItem[pageSize];

        List<World> disenchantmentDisabledWorlds = Config.Disenchantment.getDisabledWorlds();
        List<World> shattermentDisabledWorlds = Config.Shatterment.getDisabledWorlds();

        for (int i = 0; i < pageSize; i++) {
            int slot = i + this.page * 28;

            World world = this.worlds.get(slot);

            if (world == null) continue;

            World.Environment environment = world.getEnvironment();

            AtomicBoolean disenchantmentDisabled = new AtomicBoolean(Config.Disenchantment.getDisabledWorlds().contains(world));
            AtomicBoolean shattermentDisabled = new AtomicBoolean(Config.Shatterment.getDisabledWorlds().contains(world));

            worldItems[i] = new GUIItem(
                    freeSlots[i],
                    GUIComponent.Worlds.worldByEnvironment(
                            environment,
                            ChatColor.GRAY + "" + ChatColor.BOLD + world.getName(),
                            disenchantmentDisabled.get(),
                            shattermentDisabled.get()
                    ),
                    event -> {
                        event.setCancelled(true);

                        switch (event.getClick()) {
                            case LEFT: {
                                disenchantmentDisabled.set(!disenchantmentDisabled.get());

                                if (disenchantmentDisabled.get()) disenchantmentDisabledWorlds.add(world);
                                else disenchantmentDisabledWorlds.remove(world);

                                Config.Disenchantment.setDisabledWorlds(disenchantmentDisabledWorlds);

                                break;
                            }
                            case RIGHT: {
                                shattermentDisabled.set(!shattermentDisabled.get());

                                if (shattermentDisabled.get()) shattermentDisabledWorlds.add(world);
                                else shattermentDisabledWorlds.remove(world);

                                Config.Shatterment.setDisabledWorlds(shattermentDisabledWorlds);

                                break;
                            }
                            default:
                                return;
                        }

                        event.setCurrentItem(
                                GUIComponent.Worlds.worldByEnvironment(
                                        environment,
                                        ChatColor.GRAY + "" + ChatColor.BOLD + world.getName(),
                                        disenchantmentDisabled.get(),
                                        shattermentDisabled.get()
                                )
                        );
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
