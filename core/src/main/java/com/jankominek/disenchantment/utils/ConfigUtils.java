package com.jankominek.disenchantment.utils;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigUtils {
    public static void copyKeys(String[] keys, FileConfiguration oldConfig, FileConfiguration configTemplate) {
        for (String key : keys) {
            if (!oldConfig.contains(key)) continue;

            if (oldConfig.isString(key)) configTemplate.set(key, oldConfig.getString(key));
            else if (oldConfig.isInt(key)) configTemplate.set(key, oldConfig.getInt(key));
            else if (oldConfig.isBoolean(key)) configTemplate.set(key, oldConfig.getBoolean(key));
            else if (oldConfig.isDouble(key)) configTemplate.set(key, oldConfig.getDouble(key));
            else if (oldConfig.isLong(key)) configTemplate.set(key, oldConfig.getLong(key));
            else if (oldConfig.isList(key)) configTemplate.set(key, oldConfig.getList(key));
            else if (oldConfig.isVector(key)) configTemplate.set(key, oldConfig.getVector(key));
            else if (oldConfig.isOfflinePlayer(key)) configTemplate.set(key, oldConfig.getOfflinePlayer(key));
            else if (oldConfig.isItemStack(key)) configTemplate.set(key, oldConfig.getItemStack(key));
            else if (oldConfig.isColor(key)) configTemplate.set(key, oldConfig.getColor(key));
            else if (oldConfig.isLocation(key)) configTemplate.set(key, oldConfig.getLocation(key));
            else if (oldConfig.isConfigurationSection(key))
                configTemplate.set(key, oldConfig.getConfigurationSection(key));
        }
    }
}
