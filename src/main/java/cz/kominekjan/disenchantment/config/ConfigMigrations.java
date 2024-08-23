package cz.kominekjan.disenchantment.config;

import cz.kominekjan.disenchantment.config.migrations.MigrateEnchantmentStatuses;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigMigrations {
    private static final IConfigMigration[] migrations = {
        new MigrateEnchantmentStatuses()
    };

    public static void apply(FileConfiguration oldConfig, FileConfiguration newConfig) {
        for (IConfigMigration migration : migrations) {
            newConfig = migration.migrate(oldConfig, newConfig);
        }
    }
}
