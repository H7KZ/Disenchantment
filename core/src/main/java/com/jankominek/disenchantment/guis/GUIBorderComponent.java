package com.jankominek.disenchantment.guis;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Stream;

public class GUIBorderComponent {
    private static void cancelOnClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    public static ItemStack borderItem() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ").addAllFlags().build();
    }

    public static GUIItem border(int slot) {
        return new GUIItem(slot, borderItem(), GUIBorderComponent::cancelOnClick);
    }

    public static GUIItem[] rowBorder(int start, int end, ItemStack item) {
        GUIItem[] border = new GUIItem[end - start + 1];

        for (int i = start; i <= end; i++) {
            border[i - start] = new GUIItem(i, item, GUIBorderComponent::cancelOnClick);
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
        border = (GUIItem[]) ArrayUtils.add(border, border(9));
        border = (GUIItem[]) ArrayUtils.add(border, border(17));
        border = (GUIItem[]) ArrayUtils.addAll(border, rowBorder(18, 26));

        border = Stream.of(border).filter(item -> !ArrayUtils.contains(remove, item.getSlot())).toArray(GUIItem[]::new);

        return border;
    }

    public static GUIItem[] border9x4() {
        return border9x4(new Integer[]{});
    }

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

    public static GUIItem[] border9x5() {
        return border9x5(new Integer[]{});
    }

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

    public static GUIItem[] border9x6() {
        return border9x6(new Integer[]{});
    }

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
