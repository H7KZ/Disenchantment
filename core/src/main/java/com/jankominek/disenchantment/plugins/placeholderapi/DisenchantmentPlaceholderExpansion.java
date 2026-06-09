package com.jankominek.disenchantment.plugins.placeholderapi;

import com.jankominek.disenchantment.config.Config;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import static com.jankominek.disenchantment.Disenchantment.plugin;

/**
 * Registers Disenchantment's PlaceholderAPI expansion under the {@code disenchantment}
 * identifier. Activated automatically on startup when PlaceholderAPI is present.
 *
 * <p>Available placeholders:</p>
 * <ul>
 *   <li>{@code %disenchantment_enabled%} — {@code "true"} / {@code "false"}: whether the
 *       plugin is globally enabled</li>
 *   <li>{@code %disenchantment_disenchant_enabled%} — {@code "true"} / {@code "false"}:
 *       whether the disenchanting feature is enabled</li>
 *   <li>{@code %disenchantment_shatter_enabled%} — {@code "true"} / {@code "false"}:
 *       whether the shattering (book splitting) feature is enabled</li>
 *   <li>{@code %disenchantment_version%} — the plugin version string (e.g. {@code "6.4.0"})</li>
 * </ul>
 *
 * <p>{@link #persist()} returns {@code true} so PlaceholderAPI does not unregister this
 * expansion when the plugin reloads.</p>
 */
public class DisenchantmentPlaceholderExpansion extends PlaceholderExpansion {

    /**
     * @return the placeholder identifier prefix ({@code "disenchantment"})
     */
    @Override
    public String getIdentifier() {
        return "disenchantment";
    }

    /**
     * @return the plugin author(s) from {@code plugin.yml}
     */
    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    /**
     * @return the plugin version from {@code plugin.yml}
     */
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * Returns {@code true} so this expansion survives plugin reloads without being
     * re-registered by PlaceholderAPI.
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Resolves a placeholder string for the given player. The {@code player} parameter
     * is unused because all values are global (not player-specific).
     *
     * @param player unused
     * @param params the placeholder suffix (e.g. {@code "enabled"}, {@code "version"})
     * @return the resolved string, or {@code null} for unknown placeholders
     */
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        return switch (params.toLowerCase()) {
            case "enabled" -> String.valueOf(Config.isPluginEnabled());
            case "disenchant_enabled" -> String.valueOf(Config.Disenchantment.isEnabled());
            case "shatter_enabled" -> String.valueOf(Config.Shatterment.isEnabled());
            case "version" -> plugin.getDescription().getVersion();
            default -> null;
        };
    }
}
