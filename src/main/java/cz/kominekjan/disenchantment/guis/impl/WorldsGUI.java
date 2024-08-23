package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.guis.GUIItem;
import cz.kominekjan.disenchantment.guis.InventoryBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorldsGUI implements InventoryHolder {
    private final Integer size = 54;
    private final String title;
    private final HashMap<World.Environment, String> worldHeads = new HashMap<>() {
        {
            put(World.Environment.NORMAL, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWVhYTlhYzE1NzU4ZDUxNzdhODk2NjA1OTg1ZTk4YmVhYzhmZWUwZTZiMmM2OGE4ZGMxZjNjOTFjMDc5ZmI4OSJ9fX0=");
            put(World.Environment.NETHER, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzkzYmZjNDMxOTAwNzIzZjdmYTI4Nzg2NDk2MzgwMTdjZTYxNWQ4ZDhjYWI4ZDJmMDcwYTYxZWIxYWEwMGQwMiJ9fX0=");
            put(World.Environment.THE_END, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGFlYTI3YWE5M2M5NDRjNDYzMDJiZjRlYTkyMjg4NWZlZWE3MDlmNDFjMWRmNzYxNjMzMmViMjQ2ZjkwZDM4ZSJ9fX0=");
            put(World.Environment.CUSTOM, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM4Y2YzZjhlNTRhZmMzYjNmOTFkMjBhNDlmMzI0ZGNhMTQ4NjAwN2ZlNTQ1Mzk5MDU1NTI0YzE3OTQxZjRkYyJ9fX0=");
        }
    };
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
                                    ChatColor.GRAY + "Click on a world to toggle it on/off",
                                    ChatColor.GRAY + "Left click for " + ChatColor.GOLD + "Disenchantment",
                                    ChatColor.GRAY + "Right click for " + ChatColor.AQUA + "Shatterment",
                                    ChatColor.GREEN + "Enabled" + ChatColor.GRAY + " = Enabled in the world",
                                    ChatColor.RED + "Disabled" + ChatColor.GRAY + " = Disabled in the world"
                            ))
                    ),
                    DefaultGUIElements::cancelOnClick
            )
    );

    public WorldsGUI(int page) {
        List<World> worlds = Bukkit.getWorlds();

        this.page = page;
        this.title = "Worlds " + (page + 1) + "/" + (worlds.size() / 28 + 1);

        Inventory inv = Bukkit.createInventory(this, this.size, this.title);

        this.inventory = InventoryBuilder.fillItems(inv, this.items);
        this.items = ArrayUtils.addAll(this.items, this.getWorldsItems(worlds));

        InventoryBuilder.fillItems(this.inventory, this.items);

        if (worlds.size() > 28) {
            this.items = Arrays.stream(this.items.clone()).filter(item -> item.getSlot() != 47 && item.getSlot() != 51).toArray(GUIItem[]::new);

            this.items = ArrayUtils.addAll(
                    this.items,
                    new GUIItem(47, DefaultGUIElements.previousPageItem(), event -> {
                        event.setCancelled(true);

                        if (this.page == 0) return;

                        event.getWhoClicked().openInventory(new WorldsGUI(this.page - 1).getInventory());
                    }),
                    new GUIItem(51, DefaultGUIElements.nextPageItem(), event -> {
                        event.setCancelled(true);

                        if (this.page == worlds.size() / 28) return;

                        event.getWhoClicked().openInventory(new WorldsGUI(this.page + 1).getInventory());
                    })
            );

            InventoryBuilder.fillItems(this.inventory, this.items);
        }
    }

    public final GUIItem[] getWorldsItems(List<World> worlds) {
        int pageSize = Math.min(worlds.size() - (this.page * 28), 28);

        GUIItem[] worldItems = new GUIItem[pageSize];

        List<World> disenchantmentDisabledWorlds = Config.Disenchantment.getDisabledWorlds();
        List<World> shattermentDisabledWorlds = Config.Shatterment.getDisabledWorlds();

        for (int i = 0; i < pageSize; i++) {
            int slot = i + this.page * 28;

            World world = worlds.get(slot);

            if (world == null) continue;

            World.Environment environment = world.getEnvironment();

            AtomicBoolean disenchantmentDisabled = new AtomicBoolean(Config.Disenchantment.getDisabledWorlds().contains(world));
            AtomicBoolean shattermentDisabled = new AtomicBoolean(Config.Shatterment.getDisabledWorlds().contains(world));

            worldItems[i] = new GUIItem(
                    freeSlots[i],
                    DefaultGUIElements.headWorldItem(
                            ChatColor.GRAY + "" + ChatColor.BOLD + world.getName(),
                            this.worldHeads.get(environment),
                            ChatColor.GRAY + "Disenchantment: " + (disenchantmentDisabled.get() ? ChatColor.RED + "Disabled" : ChatColor.GREEN + "Enabled"),
                            ChatColor.GRAY + "Shatterment: " + (shattermentDisabled.get() ? ChatColor.RED + "Disabled" : ChatColor.GREEN + "Enabled")
                    ),
                    event -> {
                        event.setCancelled(true);

                        switch (event.getClick()) {
                            case ClickType.LEFT:
                                disenchantmentDisabled.set(!disenchantmentDisabled.get());

                                if (disenchantmentDisabled.get()) disenchantmentDisabledWorlds.add(world);
                                else disenchantmentDisabledWorlds.remove(world);

                                Config.Disenchantment.setDisabledWorlds(disenchantmentDisabledWorlds);

                                break;
                            case ClickType.RIGHT:
                                shattermentDisabled.set(!shattermentDisabled.get());

                                if (shattermentDisabled.get()) shattermentDisabledWorlds.add(world);
                                else shattermentDisabledWorlds.remove(world);

                                Config.Shatterment.setDisabledWorlds(shattermentDisabledWorlds);

                                break;
                            default:
                                return;
                        }

                        event.setCurrentItem(DefaultGUIElements.headWorldItem(
                                ChatColor.GRAY + "" + ChatColor.BOLD + world.getName(),
                                this.worldHeads.get(environment),
                                ChatColor.GRAY + "Disenchantment: " + (disenchantmentDisabled.get() ? ChatColor.RED + "Disabled" : ChatColor.GREEN + "Enabled"),
                                ChatColor.GRAY + "Shatterment: " + (shattermentDisabled.get() ? ChatColor.RED + "Disabled" : ChatColor.GREEN + "Enabled")
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
