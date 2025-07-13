package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.migration.ConfigMigrations;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import static com.jankominek.disenchantment.Disenchantment.logger;
import static com.jankominek.disenchantment.Disenchantment.plugin;

public class ConfigUtils {
    public static void copyKeys(Set<String> keys, FileConfiguration oldConfig, FileConfiguration configTemplate) {
        for (String key : keys) {
            if (!oldConfig.contains(key)) continue;

            if (oldConfig.isString(key)) configTemplate.set(key, oldConfig.getString(key));
            else if (oldConfig.isInt(key)) configTemplate.set(key, oldConfig.getInt(key));
            else if (oldConfig.isBoolean(key)) configTemplate.set(key, oldConfig.getBoolean(key));
            else if (oldConfig.isDouble(key)) configTemplate.set(key, oldConfig.getDouble(key));
            else if (oldConfig.isLong(key)) configTemplate.set(key, oldConfig.getLong(key));
            else if (oldConfig.isList(key)) configTemplate.set(key, oldConfig.getList(key));
            else if (oldConfig.isVector(key)) configTemplate.set(key, oldConfig.getVector(key));
            else if (oldConfig.isOfflinePlayer(key)) configTemplate.set(key, oldConfig.getOfflinePlayer(key));
            else if (oldConfig.isItemStack(key)) configTemplate.set(key, oldConfig.getItemStack(key));
            else if (oldConfig.isColor(key)) configTemplate.set(key, oldConfig.getColor(key));
            else if (oldConfig.isLocation(key)) configTemplate.set(key, oldConfig.getLocation(key));
            else if (oldConfig.isConfigurationSection(key))
                configTemplate.set(key, oldConfig.getConfigurationSection(key));
        }
    }

    public static void setupConfig() {
        plugin.saveDefaultConfig();

        FileConfiguration oldConfig;

        try {
            oldConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load config.yml", e);
            throw new RuntimeException("Failed to load config.yml", e);
        }

        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("config.yml")), StandardCharsets.UTF_8));

        FileConfiguration updatedConfig = ConfigMigrations.apply(plugin, oldConfig, newConfig);

        try {
            updatedConfig.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not save config.yml", e);
            throw new RuntimeException("Failed to save config.yml", e);
        }

        plugin.reloadConfig();
    }

    public static void setupLocaleConfigs() {
        for (String locale : Config.getLocales()) {
            File localeFile = new File(plugin.getDataFolder(), "locales/" + locale + ".yml");

            try {
                InputStream in = plugin.getResource("locales/" + locale + ".yml");
                if (in != null) {
                    YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8)).save(localeFile);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not save " + locale + ".yml", e);
            }
        }
    }
}
