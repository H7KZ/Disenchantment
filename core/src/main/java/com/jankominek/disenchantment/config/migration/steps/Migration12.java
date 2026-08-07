package com.jankominek.disenchantment.config.migration.steps;

import com.jankominek.disenchantment.config.migration.IConfigMigration;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

/**
 * Migration step 12: restructures the flat {@code disabled-worlds} / {@code disabled-materials}
 * denylists into the nested {@code worlds: {mode, list}} / {@code materials: {mode, list}} blocks
 * for both features. The old lists become the new {@code list} values and the {@code mode} defaults
 * to {@code DENYLIST}, so an upgraded server behaves exactly as before.
 */
public class Migration12 implements IConfigMigration {
    /**
     * {@inheritDoc}
     */
    public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate) {
        Set<String> keys = oldConfig.getKeys(true);
        keys.remove("migration");

        ConfigUtils.copyKeys(keys, oldConfig, configTemplate);

        migrateRestrictionList(oldConfig, configTemplate, "disenchantment", "disabled-worlds", "worlds");
        migrateRestrictionList(oldConfig, configTemplate, "disenchantment", "disabled-materials", "materials");
        migrateRestrictionList(oldConfig, configTemplate, "shatterment", "disabled-worlds", "worlds");
        migrateRestrictionList(oldConfig, configTemplate, "shatterment", "disabled-materials", "materials");

        return configTemplate;
    }

    /**
     * Moves an old flat restriction list into its nested {@code mode}/{@code list} block, drops the
     * stale flat key, and ensures a {@code DENYLIST} mode is present (preserving prior behaviour).
     */
    private void migrateRestrictionList(FileConfiguration oldConfig, FileConfiguration configTemplate, String feature, String oldLeaf, String newBlock) {
        String oldKey = feature + "." + oldLeaf;
        String listKey = feature + "." + newBlock + ".list";
        String modeKey = feature + "." + newBlock + ".mode";

        if (oldConfig.contains(oldKey)) {
            configTemplate.set(listKey, oldConfig.getStringList(oldKey));
        }

        // Drop the stale flat key so it does not linger alongside the new nested block.
        configTemplate.set(oldKey, null);

        if (!configTemplate.isString(modeKey)) {
            configTemplate.set(modeKey, "DENYLIST");
        }
    }
}
