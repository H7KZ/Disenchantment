package cz.kominekjan.disenchantment.config.migrations;

import cz.kominekjan.disenchantment.config.IConfigMigration;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Migration1Base implements IConfigMigration {
    public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate) {
        if (oldConfig.contains("enabled")) configTemplate.set("enabled", oldConfig.getBoolean("enabled"));
        if (oldConfig.contains("enable-logging"))
            configTemplate.set("logging-enable", oldConfig.getBoolean("enable-logging"));
        if (oldConfig.contains("logging-level"))
            configTemplate.set("logging-level", oldConfig.getString("logging-level"));

        if (oldConfig.contains("enabled"))
            configTemplate.set("disenchantment.enabled", oldConfig.getBoolean("enabled"));
        if (oldConfig.contains("disabled-worlds"))
            configTemplate.set("disenchantment.disabled-worlds", oldConfig.getStringList("disabled-worlds"));
        if (oldConfig.contains("disabled-materials"))
            configTemplate.set("disenchantment.disabled-materials", oldConfig.getStringList("disabled-materials"));
        if (oldConfig.contains("disabled-enchantments")) {
            List<String> list = oldConfig.getStringList("disabled-enchantments");
            List<String> newList = new java.util.ArrayList<>();

            for (String enchantmentState : list) {
                String[] split = enchantmentState.split(":");

                if (split.length != 2) continue;

                String enchantment = split[0];
                boolean state = Boolean.parseBoolean(split[1]);

                newList.add(enchantment + ":" + (state ? "keep" : "cancel"));
            }

            configTemplate.set("disenchantment.enchantments-states", newList);
        }
        if (oldConfig.contains("enable-anvil-sound"))
            configTemplate.set("disenchantment.anvil.sound.enabled", oldConfig.getBoolean("enable-anvil-sound"));
        if (oldConfig.contains("anvil-volume"))
            configTemplate.set("disenchantment.anvil.sound.volume", oldConfig.getDouble("anvil-volume"));
        if (oldConfig.contains("anvil-pitch"))
            configTemplate.set("disenchantment.anvil.sound.pitch", oldConfig.getDouble("anvil-pitch"));
        if (oldConfig.contains("enable-repair-reset"))
            configTemplate.set("disenchantment.anvil.repair.reset", oldConfig.getBoolean("enable-repair-reset"));
        if (oldConfig.contains("enable-repair-cost"))
            configTemplate.set("disenchantment.anvil.repair.cost", oldConfig.getBoolean("enable-repair-cost"));
        if (oldConfig.contains("base"))
            configTemplate.set("disenchantment.anvil.repair.base", oldConfig.getDouble("base"));
        if (oldConfig.contains("multiply"))
            configTemplate.set("disenchantment.anvil.repair.multiply", oldConfig.getDouble("multiply"));

        if (oldConfig.contains("enabled")) configTemplate.set("shatterment.enabled", oldConfig.getBoolean("enabled"));
        if (oldConfig.contains("disabled-worlds"))
            configTemplate.set("shatterment.disabled-worlds", oldConfig.getStringList("disabled-worlds"));
        if (oldConfig.contains("disabled-enchantments")) {
            List<String> list = oldConfig.getStringList("disabled-enchantments");
            List<String> newList = new java.util.ArrayList<>();

            for (String enchantmentState : list) {
                String[] split = enchantmentState.split(":");

                if (split.length != 2) continue;

                String enchantment = split[0];
                boolean state = Boolean.parseBoolean(split[1]);

                newList.add(enchantment + ":" + (state ? "keep" : "cancel"));
            }

            configTemplate.set("shatterment.enchantments-states", newList);
        }
        if (oldConfig.contains("enable-anvil-sound"))
            configTemplate.set("shatterment.anvil.sound.enabled", oldConfig.getBoolean("enable-anvil-sound"));
        if (oldConfig.contains("anvil-volume"))
            configTemplate.set("shatterment.anvil.sound.volume", oldConfig.getDouble("anvil-volume"));
        if (oldConfig.contains("anvil-pitch"))
            configTemplate.set("shatterment.anvil.sound.pitch", oldConfig.getDouble("anvil-pitch"));
        if (oldConfig.contains("enable-repair-reset"))
            configTemplate.set("shatterment.anvil.repair.reset", oldConfig.getBoolean("enable-repair-reset"));
        if (oldConfig.contains("enable-repair-cost"))
            configTemplate.set("shatterment.anvil.repair.cost", oldConfig.getBoolean("enable-repair-cost"));
        if (oldConfig.contains("base"))
            configTemplate.set("shatterment.anvil.repair.base", oldConfig.getDouble("base"));
        if (oldConfig.contains("multiply"))
            configTemplate.set("shatterment.anvil.repair.multiply", oldConfig.getDouble("multiply"));

        return configTemplate;
    }
}
