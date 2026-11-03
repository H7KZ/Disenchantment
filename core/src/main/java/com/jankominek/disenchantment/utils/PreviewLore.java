package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.jankominek.disenchantment.Disenchantment.plugin;

/**
 * Lore lines the plugin adds to the prepared result book while it sits in the anvil — the preview of what
 * transfers and what it costs, and the "some enchantments may not transfer" warning.
 * <p>
 * The lines are appended after any existing lore and counted in the item's persistent data under
 * {@code disenchantment:preview_lines}. {@link #strip(ItemStack)} removes exactly that many trailing
 * lines and the key again, so the book the player finally receives carries no trace of the preview.
 * Every click handler must call {@code strip} before delivering the result or cloning it into a Post event.
 */
public final class PreviewLore {

    private PreviewLore() {
    }

    private static NamespacedKey key() {
        return new NamespacedKey(plugin, "preview_lines");
    }

    /**
     * Appends {@code lines} to the lore of {@code item} and records how many lines are preview lines.
     * Lines added by an earlier call are kept and counted together.
     *
     * @param item  the result book (modified in place)
     * @param lines the already-colored lines to append; {@code null} or empty does nothing
     * @return {@code item}
     */
    public static ItemStack append(ItemStack item, List<String> lines) {
        if (item == null || lines == null || lines.isEmpty()) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        Integer existing = data.get(key(), PersistentDataType.INTEGER);

        List<String> lore = meta.hasLore() && meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.addAll(lines);

        meta.setLore(lore);
        data.set(key(), PersistentDataType.INTEGER, (existing == null ? 0 : existing) + lines.size());
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Removes the preview lines (and the marker key) from {@code item}, leaving any other lore intact.
     * The lore becomes {@code null} when nothing else is left. Idempotent: an item without the marker is
     * returned untouched.
     *
     * @param item the item to clean (modified in place); {@code null} is tolerated
     * @return {@code item}
     */
    public static ItemStack strip(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        Integer count = data.get(key(), PersistentDataType.INTEGER);
        if (count == null) return item;

        List<String> lore = meta.hasLore() && meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        int keep = Math.max(0, lore.size() - Math.max(0, count));
        lore = new ArrayList<>(lore.subList(0, keep));

        meta.setLore(lore.isEmpty() ? null : lore);
        data.remove(key());
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Returns {@code true} when {@code item} still carries preview lines.
     *
     * @param item the item to inspect
     * @return whether the marker key is present
     */
    public static boolean hasPreview(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(key(), PersistentDataType.INTEGER);
    }

    /**
     * Builds the preview lines of a prepared operation: a "Transfers: ..." line and a "Cost: ..." line.
     * Returns an empty list when {@code feedback.preview-lore} is off.
     *
     * @param p                the player previewing the operation
     * @param enchantments     the enchantments the result book carries
     * @param eventType        DISENCHANTMENT or SHATTERMENT, selects the cost formula
     * @param economyEnabled   whether the feature charges money
     * @param economyShowCost  whether the feature shows the money cost to the player
     * @param economyCost      the money cost of the operation
     * @return the lines to append, possibly empty
     */
    public static List<String> previewLines(Player p, List<IPluginEnchantment> enchantments, AnvilEventType eventType,
                                            boolean economyEnabled, boolean economyShowCost, double economyCost) {
        List<String> lines = new ArrayList<>();
        if (!Config.Feedback.isPreviewLoreEnabled() || enchantments.isEmpty()) return lines;

        List<String> names = new ArrayList<>();
        for (IPluginEnchantment enchantment : enchantments) {
            names.add(describe(enchantment));
        }
        lines.add(I18n.Messages.previewTransfers(String.join(", ", names)));

        boolean creative = p.getGameMode() == GameMode.CREATIVE;
        int levels = creative ? 0 : AnvilCostUtils.countAnvilCost(enchantments, eventType, p);
        boolean showMoney = !creative && economyEnabled && economyShowCost && EconomyUtils.isAvailable() && economyCost > 0;

        if (levels > 0 && showMoney) {
            lines.add(I18n.Messages.previewCostLevelsMoney(String.valueOf(levels), EconomyUtils.format(economyCost)));
        } else if (levels > 0) {
            lines.add(I18n.Messages.previewCostLevels(String.valueOf(levels)));
        } else if (showMoney) {
            lines.add(I18n.Messages.previewCostMoney(EconomyUtils.format(economyCost)));
        }

        return lines;
    }

    /**
     * Formats an enchantment as "Sharpness V": the key without namespace, underscores to spaces, capitalized.
     *
     * @param enchantment the enchantment
     * @return the display text
     */
    public static String describe(IPluginEnchantment enchantment) {
        String key = enchantment.getKey() == null ? "" : enchantment.getKey();
        int colon = key.indexOf(':');
        if (colon >= 0) key = key.substring(colon + 1);

        StringBuilder name = new StringBuilder();
        for (String word : key.toLowerCase(Locale.ROOT).split("_")) {
            if (word.isEmpty()) continue;
            if (name.length() > 0) name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }

        return name + " " + roman(enchantment.getLevel());
    }

    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    // Vanilla shows levels 1-10 as roman numerals and everything else as a plain number.
    private static String roman(int level) {
        return level >= 1 && level <= ROMAN.length ? ROMAN[level - 1] : String.valueOf(level);
    }
}
