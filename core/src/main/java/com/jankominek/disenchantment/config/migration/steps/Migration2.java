package com.jankominek.disenchantment.config.migration.steps;

import com.jankominek.disenchantment.config.migration.IConfigMigration;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

public class Migration2 implements IConfigMigration {
    public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate) {
        ConfigUtils.copyKeys(new String[]{
                "enabled",
                "disenchantment.enabled",
                "disenchantment.disabled-worlds",
                "disenchantment.disabled-materials",
                "disenchantment.disabled-enchantments",
                "disenchantment.anvil.sound.enabled",
                "disenchantment.anvil.sound.volume",
                "disenchantment.anvil.sound.pitch",
                "disenchantment.anvil.repair.reset",
                "disenchantment.anvil.repair.cost",
                "disenchantment.anvil.repair.base",
                "disenchantment.anvil.repair.multiply",
                "shatterment.enabled",
                "shatterment.disabled-worlds",
                "shatterment.disabled-enchantments",
                "shatterment.anvil.sound.enabled",
                "shatterment.anvil.sound.volume",
                "shatterment.anvil.sound.pitch",
                "shatterment.anvil.repair.reset",
                "shatterment.anvil.repair.cost",
                "shatterment.anvil.repair.base",
                "shatterment.anvil.repair.multiply"
        }, oldConfig, configTemplate);

        return configTemplate;
    }
}
