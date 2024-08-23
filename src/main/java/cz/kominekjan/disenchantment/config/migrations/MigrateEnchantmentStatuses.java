package cz.kominekjan.disenchantment.config.migrations;

import cz.kominekjan.disenchantment.config.IConfigMigration;
import org.bukkit.configuration.file.FileConfiguration;

public class MigrateEnchantmentStatuses implements IConfigMigration {
    public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration newConfig) {


        return newConfig;
    }
}
