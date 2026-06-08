package com.jankominek.disenchantment.config.migration.steps;

import com.jankominek.disenchantment.config.migration.IConfigMigration;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

/**
 * Migration step 8: copies all existing keys (except the migration version marker)
 * into the new configuration template, introducing the new {@code enchantment-costs}
 * maps under {@code disenchantment.anvil.repair} and {@code shatterment.anvil.repair}
 * with empty default values.
 */
public class Migration8 implements IConfigMigration {
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
