package com.jankominek.disenchantment.config.migration.steps;

import com.jankominek.disenchantment.config.migration.IConfigMigration;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

/**
 * Migration step 10: copies all existing keys (except the migration version marker)
 * into the new configuration template, introducing the new {@code enchantment-chances}
 * map and the {@code anvil.repair.max-cost} key (defaulting to {@code -1}, no cap) for
 * both disenchantment and shatterment.
 */
public class Migration10 implements IConfigMigration {
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
