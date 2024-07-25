package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.guis.GUIItem;
import cz.kominekjan.disenchantment.guis.HeadBuilder;
import cz.kominekjan.disenchantment.guis.ItemBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DefaultGUIElements {
    public static void cancelOnClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    public static GUIItem[] rowBorder(int start, int end, ItemStack item) {
        GUIItem[] border = new GUIItem[end - start + 1];
        for (int i = start; i <= end; i++) {
            border[i - start] = new GUIItem(i, item, DefaultGUIElements::cancelOnClick);
        }
        return border;
    }

    public static GUIItem[] rowBorder(int start, int end) {
        return rowBorder(start, end, borderItem());
    }

    public static GUIItem[] border9x3() {
        return border9x3(new Integer[]{});
    }

    public static GUIItem[] border9x3(Integer[] remove) {
        GUIItem[] border = rowBorder(0, 8);
        border = ArrayUtils.add(border, new GUIItem(9, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(17, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.addAll(border, rowBorder(18, 26));

        for (int i = 0; i < remove.length; i++) {
            border = ArrayUtils.remove(border, i);
        }

        return border;
    }

    public static GUIItem[] border9x4() {
        return border9x4(new Integer[]{});
    }

    public static GUIItem[] border9x4(Integer[] remove) {
        GUIItem[] border = rowBorder(0, 8);
        border = ArrayUtils.add(border, new GUIItem(9, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(17, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(18, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(26, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.addAll(border, rowBorder(27, 35));

        for (int i = 0; i < remove.length; i++) {
            border = ArrayUtils.remove(border, i);
        }

        return border;
    }

    public static GUIItem[] border9x5() {
        return border9x5(new Integer[]{});
    }

    public static GUIItem[] border9x5(Integer[] remove) {
        GUIItem[] border = rowBorder(0, 8);
        border = ArrayUtils.add(border, new GUIItem(9, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(17, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(18, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(26, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(27, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(35, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.addAll(border, rowBorder(36, 44));

        for (int i = 0; i < remove.length; i++) {
            border = ArrayUtils.remove(border, i);
        }

        return border;
    }

    public static GUIItem[] border9x6() {
        return border9x6(new Integer[]{});
    }

    public static GUIItem[] border9x6(Integer[] remove) {
        GUIItem[] border = rowBorder(0, 8);
        border = ArrayUtils.add(border, new GUIItem(9, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(17, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(18, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(26, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(27, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(35, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(36, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(44, borderItem(), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.addAll(border, rowBorder(45, 53));

        for (int i = 0; i < remove.length; i++) {
            border = ArrayUtils.remove(border, i);
        }

        return border;
    }

    public static ItemStack borderItem() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ").addAllFlags().build();
    }

    public static ItemStack disabledPluginItem() {
        return new ItemBuilder(Material.RED_TERRACOTTA).setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Disabled").setLore(ChatColor.GRAY + "Click to enable the plugin").addAllFlags().build();
    }

    public static ItemStack enabledPluginItem() {
        return new ItemBuilder(Material.LIME_TERRACOTTA).setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Enabled").setLore(ChatColor.GRAY + "Click to disable the plugin").addAllFlags().build();
    }

    public static ItemStack worldsItem() {
        return new ItemBuilder(Material.FILLED_MAP).setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Worlds").setLore(ChatColor.GRAY + "Enable or disable specific worlds").addAllFlags().build();
    }

    public static ItemStack repairItem() {
        return new ItemBuilder(Material.NETHERITE_SCRAP).setDisplayName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Repair").setLore(ChatColor.GRAY + "Edit repair cost").addAllFlags().build();
    }

    public static ItemStack enchantmentsItem() {
        return new ItemBuilder(Material.ENCHANTING_TABLE).setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Enchantments").setLore(ChatColor.GRAY + "Edit enchantments").addAllFlags().build();
    }

    public static ItemStack materialsItem() {
        return new ItemBuilder(Material.BLADE_POTTERY_SHERD).setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Materials").setLore(ChatColor.GRAY + "Edit materials").addAllFlags().build();
    }

    public static ItemStack soundItem() {
        return new ItemBuilder(Material.JUKEBOX).setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + "Sound").setLore(ChatColor.GRAY + "Edit anvil sound volume and pitch").addAllFlags().build();
    }

    public static ItemStack spigotItem() {
        return new ItemBuilder(Material.FLOWER_BANNER_PATTERN).setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Plugin info").setLore(ChatColor.GRAY + "Redirect to plugin's site").addAllFlags().build();
    }

    public static ItemStack backItem() {
        return new ItemBuilder(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE).setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Back to navigation").addAllFlags().build();
    }

    public static ItemStack previousPageItem() {
        return new ItemBuilder(Material.FIREWORK_STAR).setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Previous page").addAllFlags().build();
    }

    public static ItemStack nextPageItem() {
        return new ItemBuilder(Material.FIREWORK_STAR).setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Next page").addAllFlags().build();
    }

    public static ItemStack infoItem(String name, ArrayList<String> lore) {
        return new ItemBuilder(Material.OAK_HANGING_SIGN).setDisplayName(name).setLore(lore).addAllFlags().build();
    }

    public static ItemStack infoItem(String name, String lore) {
        return infoItem(name, new ArrayList<>(Collections.singletonList(lore)));
    }

    public static ItemStack headWorldItem(String name, String texture, ArrayList<String> lore) {
        return new HeadBuilder().setTexture(texture).setDisplayName(name).setLore(lore).addAllFlags().build();
    }

    public static ItemStack headWorldItem(String name, String texture, String lore) {
        return headWorldItem(name, texture, new ArrayList<>(Collections.singletonList(lore)));
    }

    public static ItemStack enchantmentItem(String name, ArrayList<String> lore) {
        return new ItemBuilder(Material.ENCHANTED_BOOK).setDisplayName(name).setLore(lore).addAllFlags().build();
    }

    public static ItemStack enchantmentItem(String name, String lore) {
        return enchantmentItem(name, new ArrayList<>(Collections.singletonList(lore)));
    }
}
