package com.jankominek.disenchantment.config.migration.steps;

import com.jankominek.disenchantment.config.migration.IConfigMigration;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

/**
 * Migration step 2: copies all existing keys into the new template while
 * removing the deprecated {@code logging-enable} and {@code logging-level} keys.
 */
public class Migration2 implements IConfigMigration {
    /**
     * {@inheritDoc}
     */
    public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate) {
        Set<String> keys = oldConfig.getKeys(true);
        keys.remove("migration");

        keys.remove("logging-enable");
        keys.remove("logging-level");

        ConfigUtils.copyKeys(keys, oldConfig, configTemplate);

        return configTemplate;
    }
}
