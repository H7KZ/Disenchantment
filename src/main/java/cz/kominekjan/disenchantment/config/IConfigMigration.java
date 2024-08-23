package cz.kominekjan.disenchantment.config;

import org.bukkit.configuration.file.FileConfiguration;

public interface IConfigMigration {
    FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration newConfig);
}
