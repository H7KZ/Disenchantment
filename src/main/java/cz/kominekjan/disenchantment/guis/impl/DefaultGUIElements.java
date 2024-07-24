package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.guis.GUIItem;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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
        return rowBorder(start, end, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
    }

    public static GUIItem[] border9x3() {
        GUIItem[] border = rowBorder(0, 8);
        border = ArrayUtils.add(border, new GUIItem(9, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(17, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.addAll(border, rowBorder(18, 26));
        return border;
    }

    public static GUIItem[] border9x4() {
        GUIItem[] border = rowBorder(0, 8);
        border = ArrayUtils.add(border, new GUIItem(9, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(17, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(18, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(26, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.addAll(border, rowBorder(27, 35));
        return border;
    }

    public static GUIItem[] border9x5() {
        GUIItem[] border = rowBorder(0, 8);
        border = ArrayUtils.add(border, new GUIItem(9, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(17, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(18, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(26, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(27, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.add(border, new GUIItem(35, new ItemStack(Material.GRAY_STAINED_GLASS_PANE), DefaultGUIElements::cancelOnClick));
        border = ArrayUtils.addAll(border, rowBorder(36, 44));
        return border;
    }
}
