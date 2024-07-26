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

    public static GUIItem border(int slot) {
        return new GUIItem(slot, borderItem(), DefaultGUIElements::cancelOnClick);
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
        border = ArrayUtils.add(border, border(9));
        border = ArrayUtils.add(border, border(17));
        border = ArrayUtils.addAll(border, rowBorder(18, 26));

        border = new ArrayList<>(Arrays.asList(border)).stream().filter(item -> !ArrayUtils.contains(remove, item.getSlot())).toArray(GUIItem[]::new);

        return border;
    }

    public static GUIItem[] border9x4() {
        return border9x4(new Integer[]{});
    }

    public static GUIItem[] border9x4(Integer[] remove) {
        GUIItem[] border = rowBorder(0, 8);
        border = ArrayUtils.add(border, border(9));
        border = ArrayUtils.add(border, border(17));
        border = ArrayUtils.add(border, border(18));
        border = ArrayUtils.add(border, border(26));
        border = ArrayUtils.addAll(border, rowBorder(27, 35));

        border = new ArrayList<>(Arrays.asList(border)).stream().filter(item -> !ArrayUtils.contains(remove, item.getSlot())).toArray(GUIItem[]::new);

        return border;
    }

    public static GUIItem[] border9x5() {
        return border9x5(new Integer[]{});
    }

    public static GUIItem[] border9x5(Integer[] remove) {
        GUIItem[] border = rowBorder(0, 8);
        border = ArrayUtils.add(border, border(9));
        border = ArrayUtils.add(border, border(17));
        border = ArrayUtils.add(border, border(18));
        border = ArrayUtils.add(border, border(26));
        border = ArrayUtils.add(border, border(27));
        border = ArrayUtils.add(border, border(35));
        border = ArrayUtils.addAll(border, rowBorder(36, 44));

        border = new ArrayList<>(Arrays.asList(border)).stream().filter(item -> !ArrayUtils.contains(remove, item.getSlot())).toArray(GUIItem[]::new);

        return border;
    }

    public static GUIItem[] border9x6() {
        return border9x6(new Integer[]{});
    }

    public static GUIItem[] border9x6(Integer[] remove) {
        GUIItem[] border = rowBorder(0, 8);
        border = ArrayUtils.add(border, border(9));
        border = ArrayUtils.add(border, border(17));
        border = ArrayUtils.add(border, border(18));
        border = ArrayUtils.add(border, border(26));
        border = ArrayUtils.add(border, border(27));
        border = ArrayUtils.add(border, border(35));
        border = ArrayUtils.add(border, border(36));
        border = ArrayUtils.add(border, border(44));
        border = ArrayUtils.addAll(border, rowBorder(45, 53));

        border = new ArrayList<>(Arrays.asList(border)).stream().filter(item -> !ArrayUtils.contains(remove, item.getSlot())).toArray(GUIItem[]::new);

        return border;
    }

    public static ItemStack borderItem() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ").addAllFlags().build();
    }

    public static ItemStack disabledItem(String name, ArrayList<String> lore) {
        return new ItemBuilder(Material.RED_TERRACOTTA).setDisplayName(name).setLore(lore).addAllFlags().build();
    }

    public static ItemStack enabledItem(String name, ArrayList<String> lore) {
        return new ItemBuilder(Material.LIME_TERRACOTTA).setDisplayName(name).setLore(lore).addAllFlags().build();
    }

    public static ItemStack disabledItem(String name, String lore) {
        return disabledItem(name, new ArrayList<>(Collections.singletonList(lore)));
    }

    public static ItemStack enabledItem(String name, String lore) {
        return enabledItem(name, new ArrayList<>(Collections.singletonList(lore)));
    }

    public static ItemStack disabledPluginItem() {
        return disabledItem(
                ChatColor.GRAY + "" + ChatColor.BOLD + "Plugin",
                new ArrayList<>(Arrays.asList(
                        ChatColor.RED + "Disabled",
                        ChatColor.GRAY + "Click to enable the plugin"
                ))
        );
    }

    public static ItemStack enabledPluginItem() {
        return enabledItem(
                ChatColor.GRAY + "" + ChatColor.BOLD + "Plugin",
                new ArrayList<>(Arrays.asList(
                        ChatColor.GREEN + "Enabled",
                        ChatColor.GRAY + "Click to disable the plugin"
                ))
        );
    }

    public static ItemStack worldsItem() {
        return new ItemBuilder(Material.FILLED_MAP).setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Worlds").setLore(ChatColor.GRAY + "Enable or disable specific worlds").addAllFlags().build();
    }

    public static ItemStack repairItem() {
        return new ItemBuilder(Material.NETHERITE_SCRAP).setDisplayName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Repair").setLore(ChatColor.GRAY + "Edit repair cost").addAllFlags().build();
    }

    public static ItemStack enchantmentsItem() {
        return new ItemBuilder(Material.ENCHANTING_TABLE).setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Enchantments").setLore(ChatColor.GRAY + "Edit enchantment behaviour").addAllFlags().build();
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

    public static ItemStack disabledRepairCostItem() {
        return disabledItem(
                ChatColor.GRAY + "" + ChatColor.BOLD + "Repair cost",
                new ArrayList<>(Arrays.asList(
                        ChatColor.RED + "Disabled",
                        ChatColor.GRAY + "Click to enable repair cost"
                ))
        );
    }

    public static ItemStack enabledRepairCostItem() {
        return enabledItem(
                ChatColor.GRAY + "" + ChatColor.BOLD + "Repair cost",
                new ArrayList<>(Arrays.asList(
                        ChatColor.GREEN + "Enabled",
                        ChatColor.GRAY + "Click to disable repair cost"
                ))
        );
    }

    public static ItemStack disabledRepairCostResetItem() {
        return disabledItem(
                ChatColor.GRAY + "" + ChatColor.BOLD + "Repair cost reset",
                new ArrayList<>(Arrays.asList(
                        ChatColor.RED + "Disabled",
                        ChatColor.GRAY + "Click to enable repair cost reset"
                ))
        );
    }

    public static ItemStack enabledRepairCostResetItem() {
        return enabledItem(
                ChatColor.GRAY + "" + ChatColor.BOLD + "Repair cost reset",
                new ArrayList<>(Arrays.asList(
                        ChatColor.GREEN + "Enabled",
                        ChatColor.GRAY + "Click to disable repair cost reset"
                ))
        );
    }

    public static ItemStack repairCostBaseItem(final int cost, final int amount) {
        return new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setAmount(amount)
                .setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Base repair cost")
                .setLore(
                        new ArrayList<>(Arrays.asList(
                                ChatColor.GRAY + "Current cost: " + ChatColor.WHITE + cost,
                                ChatColor.GRAY + "LEFT CLICK to increase by 1",
                                ChatColor.GRAY + "RIGHT CLICK to decrease by 1"
                        ))
                )
                .addAllFlags()
                .build();
    }

    public static ItemStack repairCostMultiplierItem(final double cost, final int amount) {
        return new ItemBuilder(Material.DRAGON_BREATH)
                .setAmount(amount)
                .setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Repair cost multiplier")
                .setLore(
                        new ArrayList<>(Arrays.asList(
                                ChatColor.GRAY + "Current multiplier: " + ChatColor.WHITE + cost,
                                ChatColor.GRAY + "LEFT CLICK to increase by 0.1",
                                ChatColor.GRAY + "RIGHT CLICK to decrease by 0.1",
                                ChatColor.GRAY + "Calculated by: [ base + (level * multiply) ]"
                        ))
                )
                .addAllFlags()
                .build();
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

    public static ItemStack disabledAnvilSoundItem() {
        return disabledItem(
                ChatColor.GRAY + "" + ChatColor.BOLD + "Anvil sound",
                new ArrayList<>(Arrays.asList(
                        ChatColor.RED + "Disabled",
                        ChatColor.GRAY + "Click to enable anvil sound"
                ))
        );
    }

    public static ItemStack enabledAnvilSoundItem() {
        return enabledItem(
                ChatColor.GRAY + "" + ChatColor.BOLD + "Anvil sound",
                new ArrayList<>(Arrays.asList(
                        ChatColor.GREEN + "Enabled",
                        ChatColor.GRAY + "Click to disable anvil sound"
                ))
        );
    }

    public static ItemStack anvilSoundVolumeItem(final double cost, final int amount) {
        return new ItemBuilder(Material.FIRE_CHARGE)
                .setAmount(amount)
                .setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Anvil sound volume")
                .setLore(
                        new ArrayList<>(Arrays.asList(
                                ChatColor.GRAY + "Current volume: " + ChatColor.WHITE + cost,
                                ChatColor.GRAY + "LEFT CLICK to increase by 0.1",
                                ChatColor.GRAY + "RIGHT CLICK to decrease by 0.1"
                        ))
                )
                .addAllFlags()
                .build();
    }

    public static ItemStack anvilSoundPitchItem(final double cost, final int amount) {
        return new ItemBuilder(Material.SLIME_BALL)
                .setAmount(amount)
                .setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Anvil sound pitch")
                .setLore(
                        new ArrayList<>(Arrays.asList(
                                ChatColor.GRAY + "Current pitch: " + ChatColor.WHITE + cost,
                                ChatColor.GRAY + "LEFT CLICK to increase by 0.1",
                                ChatColor.GRAY + "RIGHT CLICK to decrease by 0.1"
                        ))
                )
                .addAllFlags()
                .build();
    }
}
