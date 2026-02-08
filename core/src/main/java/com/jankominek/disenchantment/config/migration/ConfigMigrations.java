package com.jankominek.disenchantment.config.migration;

import com.jankominek.disenchantment.config.migration.steps.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

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
            5, new Migration5()
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

        FileConfiguration updatedConfig = oldConfig;

        for (int i = oldVersion + 1; i <= newVersion; i++) {
            FileConfiguration configTemplate = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("migrations/" + i + ".yml")), StandardCharsets.UTF_8));

            if (migrations.containsKey(i)) {
                updatedConfig = migrations.get(i).migrate(updatedConfig, configTemplate);
            }
        }

        return updatedConfig;
    }
}
