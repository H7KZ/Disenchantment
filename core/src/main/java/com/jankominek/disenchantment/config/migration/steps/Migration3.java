package com.jankominek.disenchantment.config.migration.steps;

import com.jankominek.disenchantment.config.migration.IConfigMigration;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

public class Migration3 implements IConfigMigration {
    public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate) {
        Set<String> keys = oldConfig.getKeys(true);

        ConfigUtils.copyKeys(keys, oldConfig, configTemplate);

        return configTemplate;
    }
}
