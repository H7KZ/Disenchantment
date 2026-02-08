package com.jankominek.disenchantment.config.migration;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Contract for a single configuration migration step.
 * Implementations transform an old configuration into a new schema
 * using a provided template.
 */
public interface IConfigMigration {
    /**
     * Migrates configuration values from an old schema into a new template.
     *
     * @param oldConfig      the existing configuration to migrate from
     * @param configTemplate the target template with the new schema defaults
     * @return the populated configuration in the new schema format
     */
    FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate);
}
