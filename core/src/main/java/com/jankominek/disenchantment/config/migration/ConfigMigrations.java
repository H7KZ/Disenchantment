package com.jankominek.disenchantment.config.migration;

import com.jankominek.disenchantment.config.migration.steps.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import static com.jankominek.disenchantment.Disenchantment.logger;

/**
 * Orchestrates sequential configuration migrations between schema versions.
 * Loads the appropriate migration template for each version step and delegates
 * to the corresponding {@link IConfigMigration} implementation.
 */
public class ConfigMigrations {
    private static final Map<Integer, IConfigMigration> migrations = Map.of(
            1, new Migration1(),
            2, new Migration2(),
            3, new Migration3(),
            4, new Migration4(),
            5, new Migration5(),
            6, new Migration6(),
            7, new Migration7()
    );

    /**
     * Applies all pending configuration migrations from the old version to the new version.
     *
     * @param plugin    the plugin instance used to load migration template resources
     * @param oldConfig the existing configuration to migrate
     * @param newConfig the latest default configuration containing the target version
     * @return the fully migrated configuration
     */
    public static FileConfiguration apply(Plugin plugin, FileConfiguration oldConfig, FileConfiguration newConfig) {
        int oldVersion = oldConfig.getInt("migration", 0);
        int newVersion = newConfig.getInt("migration", 0);

        if (oldVersion < newVersion) {
            logger.info("Config migration: " + oldVersion + " → " + newVersion + " (" + (newVersion - oldVersion) + " step(s) to apply)");
        } else {
            logger.info("Config migration: up to date (version=" + oldVersion + ")");
        }

        FileConfiguration updatedConfig = oldConfig;

        for (int i = oldVersion + 1; i <= newVersion; i++) {
            logger.info("Config migration: applying step " + i + "...");
            FileConfiguration configTemplate = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("migrations/" + i + ".yml")), StandardCharsets.UTF_8));

            if (migrations.containsKey(i)) {
                updatedConfig = migrations.get(i).migrate(updatedConfig, configTemplate);
                logger.info("Config migration: step " + i + " complete");
            } else {
                logger.warning("Config migration: no handler registered for step " + i + " — skipped");
            }
        }

        return updatedConfig;
    }
}
