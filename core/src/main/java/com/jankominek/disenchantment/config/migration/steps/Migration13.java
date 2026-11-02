package com.jankominek.disenchantment.config.migration.steps;

import com.jankominek.disenchantment.config.migration.IConfigMigration;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Migration step 13: introduces the top-level {@code update-checker} toggle and
 * {@code logging.operations-retention-days}. All existing values are carried over; the two new keys
 * keep the defaults from the template ({@code true} and {@code 0}), so an upgraded server behaves
 * exactly as before.
 *
 * <p>Only leaf values are copied. Copying a whole section such as {@code logging} would replace the
 * template's section and drop the new default inside it.</p>
 */
public class Migration13 implements IConfigMigration {
    /**
     * {@inheritDoc}
     */
    public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate) {
        Set<String> keys = new LinkedHashSet<>();

        for (String key : oldConfig.getKeys(true)) {
            if (!oldConfig.isConfigurationSection(key)) keys.add(key);
        }

        keys.remove("migration");

        ConfigUtils.copyKeys(keys, oldConfig, configTemplate);

        return configTemplate;
    }
}
