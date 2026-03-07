package com.jankominek.disenchantment.config.migration.steps;

import com.jankominek.disenchantment.config.migration.IConfigMigration;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

/**
 * Migration step 6: copies all existing keys (except the migration version marker)
 * into the new configuration template, introducing the new {@code logging} section
 * with its default values ({@code level: INFO}, {@code save-reports: true}).
 */
public class Migration6 implements IConfigMigration {
    /**
     * {@inheritDoc}
     */
    public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate) {
        Set<String> keys = oldConfig.getKeys(true);
        keys.remove("migration");

        ConfigUtils.copyKeys(keys, oldConfig, configTemplate);

        return configTemplate;
    }
}
