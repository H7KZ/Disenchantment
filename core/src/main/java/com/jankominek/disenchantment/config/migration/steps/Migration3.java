package com.jankominek.disenchantment.config.migration.steps;

import com.jankominek.disenchantment.config.migration.IConfigMigration;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;

public class Migration3 implements IConfigMigration {
    public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate) {
        Set<String> keys = oldConfig.getKeys(true);
        keys.remove("migration");

        keys.remove("disenchantment.enchantments-states");

        ConfigUtils.copyKeys(keys, oldConfig, configTemplate);

        if (oldConfig.contains("disenchantment.enchantments-states")) {
            List<String> list = oldConfig.getStringList("disenchantment.enchantments-states");
            List<String> newList = configTemplate.getStringList("disenchantment.enchantments-states");

            for (String enchantment : list) {
                String[] split = enchantment.split(":");

                if (split.length != 2) continue;

                String enchantmentName = split[0];
                String state = split[1];

                if (state.equalsIgnoreCase("disabled")) state = "disable";
                else continue;

                newList.add(enchantmentName + ":" + state);
            }

            configTemplate.set("disenchantment.enchantments-states", newList);
        }

        return configTemplate;
    }
}
