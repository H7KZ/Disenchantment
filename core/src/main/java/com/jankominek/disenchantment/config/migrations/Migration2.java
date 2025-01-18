package com.jankominek.disenchantment.config.migrations;

import com.jankominek.disenchantment.config.IConfigMigration;
import org.bukkit.configuration.file.FileConfiguration;

public class Migration2 implements IConfigMigration {
    public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate) {
        if (oldConfig.contains("enable-logging"))
            configTemplate.set("enable-logging", null);
        if (oldConfig.contains("logging-level"))
            configTemplate.set("logging-level", null);

        return configTemplate;
    }
}
