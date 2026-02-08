package com.jankominek.disenchantment.guis;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Stream;

/**
 * Provides factory methods for creating border GUI items used to frame inventory GUIs.
 * Borders are made of gray stained glass panes that cancel click events.
 */
public class GUIBorderComponent {
    private static void cancelOnClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    /**
     * Creates a gray stained glass pane item with an empty display name, used as a border filler.
     *
     * @return the border {@link ItemStack}
     */
    public static ItemStack borderItem() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ").addAllFlags().build();
    }

    /**
     * Creates a single border GUI item at the specified inventory slot.
     *
     * @param slot the inventory slot index
     * @return a {@link GUIItem} border element
     */
    public static GUIItem border(int slot) {
        return new GUIItem(slot, borderItem(), GUIBorderComponent::cancelOnClick);
    }

    /**
     * Creates a row of border GUI items using a custom item, spanning from start to end slot (inclusive).
     *
     * @param start the starting slot index
     * @param end   the ending slot index (inclusive)
     * @param item  the {@link ItemStack} to use for each border slot
     * @return an array of {@link GUIItem} border elements
     */
    public static GUIItem[] rowBorder(int start, int end, ItemStack item) {
        GUIItem[] border = new GUIItem[end - start + 1];

        for (int i = start; i <= end; i++) {
            border[i - start] = new GUIItem(i, item, GUIBorderComponent::cancelOnClick);
        }

        return border;
    }

    /**
     * Creates a row of border GUI items using the default border item, spanning from start to end slot (inclusive).
     *
     * @param start the starting slot index
     * @param end   the ending slot index (inclusive)
     * @return an array of {@link GUIItem} border elements
     */
    public static GUIItem[] rowBorder(int start, int end) {
        return rowBorder(start, end, borderItem());
    }

    /**
     * Creates a full border for a 9x3 (27-slot) inventory with no removed slots.
     *
     * @return an array of {@link GUIItem} border elements
     */
    public static GUIItem[] border9x3() {
        return border9x3(new Integer[]{});
    }

    /**
     * Creates a border for a 9x3 (27-slot) inventory, excluding specified slot indices.
     *
     * @param remove slot indices to exclude from the border
     * @return an array of {@link GUIItem} border elements
     */
    public static GUIItem[] border9x3(Integer[] remove) {
        GUIItem[] border = rowBorder(0, 8);
        border = (GUIItem[]) ArrayUtils.add(border, border(9));
        border = (GUIItem[]) ArrayUtils.add(border, border(17));
        border = (GUIItem[]) ArrayUtils.addAll(border, rowBorder(18, 26));

        border = Stream.of(border).filter(item -> !ArrayUtils.contains(remove, item.getSlot())).toArray(GUIItem[]::new);

        return border;
    }

    /**
     * Creates a full border for a 9x4 (36-slot) inventory with no removed slots.
     *
     * @return an array of {@link GUIItem} border elements
     */
    public static GUIItem[] border9x4() {
        return border9x4(new Integer[]{});
    }

    /**
     * Creates a border for a 9x4 (36-slot) inventory, excluding specified slot indices.
     *
     * @param remove slot indices to exclude from the border
     * @return an array of {@link GUIItem} border elements
     */
    public static GUIItem[] border9x4(Integer[] remove) {
        GUIItem[] border = rowBorder(0, 8);
        border = (GUIItem[]) ArrayUtils.add(border, border(9));
        border = (GUIItem[]) ArrayUtils.add(border, border(17));
        border = (GUIItem[]) ArrayUtils.add(border, border(18));
        border = (GUIItem[]) ArrayUtils.add(border, border(26));
        border = (GUIItem[]) ArrayUtils.addAll(border, rowBorder(27, 35));

        border = Stream.of(border).filter(item -> !ArrayUtils.contains(remove, item.getSlot())).toArray(GUIItem[]::new);

        return border;
    }

    /**
     * Creates a full border for a 9x5 (45-slot) inventory with no removed slots.
     *
     * @return an array of {@link GUIItem} border elements
     */
    public static GUIItem[] border9x5() {
        return border9x5(new Integer[]{});
    }

    /**
     * Creates a border for a 9x5 (45-slot) inventory, excluding specified slot indices.
     *
     * @param remove slot indices to exclude from the border
     * @return an array of {@link GUIItem} border elements
     */
    public static GUIItem[] border9x5(Integer[] remove) {
        GUIItem[] border = rowBorder(0, 8);
        border = (GUIItem[]) ArrayUtils.add(border, border(9));
        border = (GUIItem[]) ArrayUtils.add(border, border(17));
        border = (GUIItem[]) ArrayUtils.add(border, border(18));
        border = (GUIItem[]) ArrayUtils.add(border, border(26));
        border = (GUIItem[]) ArrayUtils.add(border, border(27));
        border = (GUIItem[]) ArrayUtils.add(border, border(35));
        border = (GUIItem[]) ArrayUtils.addAll(border, rowBorder(36, 44));

        border = Stream.of(border).filter(item -> !ArrayUtils.contains(remove, item.getSlot())).toArray(GUIItem[]::new);

        return border;
    }

    /**
     * Creates a full border for a 9x6 (54-slot) inventory with no removed slots.
     *
     * @return an array of {@link GUIItem} border elements
     */
    public static GUIItem[] border9x6() {
        return border9x6(new Integer[]{});
    }

    /**
     * Creates a border for a 9x6 (54-slot) inventory, excluding specified slot indices.
     *
     * @param remove slot indices to exclude from the border
     * @return an array of {@link GUIItem} border elements
     */
    public static GUIItem[] border9x6(Integer[] remove) {
        GUIItem[] border = rowBorder(0, 8);
        border = (GUIItem[]) ArrayUtils.add(border, border(9));
        border = (GUIItem[]) ArrayUtils.add(border, border(17));
        border = (GUIItem[]) ArrayUtils.add(border, border(18));
        border = (GUIItem[]) ArrayUtils.add(border, border(26));
        border = (GUIItem[]) ArrayUtils.add(border, border(27));
        border = (GUIItem[]) ArrayUtils.add(border, border(35));
        border = (GUIItem[]) ArrayUtils.add(border, border(36));
        border = (GUIItem[]) ArrayUtils.add(border, border(44));
        border = (GUIItem[]) ArrayUtils.addAll(border, rowBorder(45, 53));

        border = Stream.of(border).filter(item -> !ArrayUtils.contains(remove, item.getSlot())).toArray(GUIItem[]::new);

        return border;
    }
}
