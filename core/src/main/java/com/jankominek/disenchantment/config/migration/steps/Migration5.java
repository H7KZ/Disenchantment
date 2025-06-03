package com.jankominek.disenchantment.config.migration.steps;

import com.jankominek.disenchantment.config.migration.IConfigMigration;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Migration5 implements IConfigMigration {
    public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate) {
        Set<String> keys = oldConfig.getKeys(true);
        keys.remove("migration");

        keys.remove("disenchantment.enchantments-states");
        keys.remove("shatterment.enchantments-states");

        System.out.println(keys);

        ConfigUtils.copyKeys(keys, oldConfig, configTemplate);

        if (oldConfig.contains("disenchantment.enchantments-states")) {
            List<String> list = oldConfig.getStringList("disenchantment.enchantments-states");

            configTemplate.set("disenchantment.enchantments-states", this.migrateEnchantmentStates(list));
        }

        if (oldConfig.contains("shatterment.enchantments-states")) {
            List<String> list = oldConfig.getStringList("shatterment.enchantments-states");

            configTemplate.set("shatterment.enchantments-states", this.migrateEnchantmentStates(list));
        }

        return configTemplate;
    }

    private List<String> migrateEnchantmentStates(List<String> oldStates) {
        List<String> newList = new ArrayList<>();

        for (String enchantmentState : oldStates) {
            String[] split = enchantmentState.split(":");

            if (split.length != 2) continue;

            String enchantmentName = split[0];
            String state = split[1];

            if (state.equalsIgnoreCase("disabled")) state = "disable";
            else if (state.equalsIgnoreCase("cancel")) state = "disable";
            else continue;

            System.out.println("Migration5: Migrating enchantment state: " + enchantmentName + " to " + state);

            newList.add(enchantmentName + ":" + state);
        }

        return newList;
    }
}
