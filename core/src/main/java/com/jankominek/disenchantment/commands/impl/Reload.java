package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "reload" subcommand which reloads the plugin configuration and locale files.
 */
public class Reload {
    /**
     * The command definition for the reload subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "reload",
            PermissionGroupType.COMMAND_RELOAD,
            new String[]{},
            false,
            Reload::execute,
            Reload::complete
    );

    /**
     * Reloads the plugin configuration and locale files.
     *
     * @param sender the command sender
     * @param args   the command arguments
     */
    public static void execute(CommandSender sender, String[] args) {
        ConfigUtils.setupConfig();
        ConfigUtils.setupLocaleConfigs();

        // Re-read locale file from disk into memory (setupLocaleConfigs only copies JAR→disk)
        String locale = Config.getLocale();
        File localeFile = new File(Disenchantment.plugin.getDataFolder(), "locales/" + locale + ".yml");
        if (!localeFile.exists() && !locale.equals("en")) {
            locale = "en";
            localeFile = new File(Disenchantment.plugin.getDataFolder(), "locales/en.yml");
        }
        if (localeFile.exists()) {
            Disenchantment.localeConfig = YamlConfiguration.loadConfiguration(localeFile);
        } else {
            var stream = Disenchantment.plugin.getResource("locales/" + locale + ".yml");
            if (stream != null) {
                Disenchantment.localeConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
            }
        }

        sender.sendMessage(I18n.getPrefix() + " Config reloaded.");
    }

    /**
     * No tab-completion arguments for this command.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return an empty list
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
