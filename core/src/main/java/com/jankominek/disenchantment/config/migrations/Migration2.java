package com.jankominek.disenchantment.config.migrations;

import com.jankominek.disenchantment.config.IConfigMigration;
import org.bukkit.configuration.file.FileConfiguration;

public class Migration2 implements IConfigMigration {
    public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate) {
        if (oldConfig.contains("enabled"))
            configTemplate.set("enabled", oldConfig.getBoolean("enabled"));

        if (oldConfig.contains("disenchantment.enabled"))
            configTemplate.set("disenchantment.enabled", oldConfig.getBoolean("disenchantment.enabled"));
        if (oldConfig.contains("disenchantment.disabled-worlds"))
            configTemplate.set("disenchantment.disabled-worlds", oldConfig.getStringList("disenchantment.disabled-worlds"));
        if (oldConfig.contains("disenchantment.disabled-materials"))
            configTemplate.set("disenchantment.disabled-materials", oldConfig.getStringList("disenchantment.disabled-materials"));
        if (oldConfig.contains("disenchantment.disabled-enchantments"))
            configTemplate.set("disenchantment.disabled-enchantments", oldConfig.getStringList("disenchantment.disabled-enchantments"));
        if (oldConfig.contains("disenchantment.anvil.sound.enabled"))
            configTemplate.set("disenchantment.anvil.sound.enabled", oldConfig.getBoolean("disenchantment.anvil.sound.enabled"));
        if (oldConfig.contains("disenchantment.anvil.sound.volume"))
            configTemplate.set("disenchantment.anvil.sound.volume", oldConfig.getDouble("disenchantment.anvil.sound.volume"));
        if (oldConfig.contains("disenchantment.anvil.sound.pitch"))
            configTemplate.set("disenchantment.anvil.sound.pitch", oldConfig.getDouble("disenchantment.anvil.sound.pitch"));
        if (oldConfig.contains("disenchantment.anvil.repair.reset"))
            configTemplate.set("disenchantment.anvil.repair.reset", oldConfig.getBoolean("disenchantment.anvil.repair.reset"));
        if (oldConfig.contains("disenchantment.anvil.repair.cost"))
            configTemplate.set("disenchantment.anvil.repair.cost", oldConfig.getBoolean("disenchantment.anvil.repair.cost"));
        if (oldConfig.contains("disenchantment.anvil.repair.base"))
            configTemplate.set("disenchantment.anvil.repair.base", oldConfig.getDouble("disenchantment.anvil.repair.base"));
        if (oldConfig.contains("disenchantment.anvil.repair.multiply"))
            configTemplate.set("disenchantment.anvil.repair.multiply", oldConfig.getDouble("disenchantment.anvil.repair.multiply"));

        if (oldConfig.contains("shatterment.enabled"))
            configTemplate.set("shatterment.enabled", oldConfig.getBoolean("shatterment.enabled"));
        if (oldConfig.contains("shatterment.disabled-worlds"))
            configTemplate.set("shatterment.disabled-worlds", oldConfig.getStringList("shatterment.disabled-worlds"));
        if (oldConfig.contains("shatterment.disabled-enchantments"))
            configTemplate.set("shatterment.disabled-enchantments", oldConfig.getStringList("shatterment.disabled-enchantments"));
        if (oldConfig.contains("shatterment.anvil.sound.enabled"))
            configTemplate.set("shatterment.anvil.sound.enabled", oldConfig.getBoolean("shatterment.anvil.sound.enabled"));
        if (oldConfig.contains("shatterment.anvil.sound.volume"))
            configTemplate.set("shatterment.anvil.sound.volume", oldConfig.getDouble("shatterment.anvil.sound.volume"));
        if (oldConfig.contains("shatterment.anvil.sound.pitch"))
            configTemplate.set("shatterment.anvil.sound.pitch", oldConfig.getDouble("shatterment.anvil.sound.pitch"));
        if (oldConfig.contains("shatterment.anvil.repair.reset"))
            configTemplate.set("shatterment.anvil.repair.reset", oldConfig.getBoolean("shatterment.anvil.repair.reset"));
        if (oldConfig.contains("shatterment.anvil.repair.cost"))
            configTemplate.set("shatterment.anvil.repair.cost", oldConfig.getBoolean("shatterment.anvil.repair.cost"));
        if (oldConfig.contains("shatterment.anvil.repair.base"))
            configTemplate.set("shatterment.anvil.repair.base", oldConfig.getDouble("shatterment.anvil.repair.base"));
        if (oldConfig.contains("shatterment.anvil.repair.multiply"))
            configTemplate.set("shatterment.anvil.repair.multiply", oldConfig.getDouble("shatterment.anvil.repair.multiply"));

        return configTemplate;
    }
}
