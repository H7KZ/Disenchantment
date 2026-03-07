package com.jankominek.disenchantment.config.migration.steps;

import com.jankominek.disenchantment.config.migration.IConfigMigration;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

/**
 * Migration step 7: copies all existing keys (except the migration version marker)
 * into the new configuration template, introducing the new {@code economy} sections
 * under {@code disenchantment} and {@code shatterment} with default values
 * ({@code enabled: false}, {@code cost: 100.0}, {@code show-cost: true},
 * {@code charge-message: true}).
 */
public class Migration7 implements IConfigMigration {
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
