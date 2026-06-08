package com.jankominek.disenchantment.plugins.placeholderapi;

import com.jankominek.disenchantment.config.Config;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import static com.jankominek.disenchantment.Disenchantment.plugin;

public class DisenchantmentPlaceholderExpansion extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "disenchantment";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        return switch (params.toLowerCase()) {
            case "enabled"            -> String.valueOf(Config.isPluginEnabled());
            case "disenchant_enabled" -> String.valueOf(Config.Disenchantment.isEnabled());
            case "shatter_enabled"    -> String.valueOf(Config.Shatterment.isEnabled());
            case "version"            -> plugin.getDescription().getVersion();
            default                   -> null;
        };
    }
}
