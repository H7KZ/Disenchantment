package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.config.migration.ConfigMigrations;
import com.jankominek.disenchantment.config.migration.steps.Migration13;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies migration step 13 adds {@code update-checker} and {@code logging.operations-retention-days}
 * without disturbing any existing value.
 */
class Migration13Test extends DisenchantmentTestBase {

    private YamlConfiguration template13() {
        return YamlConfiguration.loadConfiguration(new InputStreamReader(
                Objects.requireNonNull(plugin.getResource("migrations/13.yml")), StandardCharsets.UTF_8));
    }

    private YamlConfiguration v12Config() {
        YamlConfiguration old = new YamlConfiguration();
        old.set("migration", 12);
        old.set("enabled", false);
        old.set("locale", "cs");
        old.set("logging.level", "DEBUG");
        old.set("logging.operations", true);
        old.set("logging.discord-webhook", "https://example.invalid/hook");
        old.set("disenchantment.worlds.mode", "ALLOWLIST");
        old.set("disenchantment.worlds.list", List.of("world_a"));
        old.set("disenchantment.anvil.repair.base", 42.0);
        old.set("disenchantment.anvil.repair.enchantment-costs.mending.xp", 10);
        old.set("disenchantment.anvil.repair.enchantment-costs.mending.economy", 500.0);
        old.set("disenchantment.enchantment-chances.sharpness", 0.5);
        old.set("enchantment-groups.combat", List.of("sharpness", "smite"));
        return old;
    }

    // -> new keys

    @Test
    void givenV12Config_whenMigrated_thenNewKeysGetDefaults() {
        FileConfiguration result = new Migration13().migrate(v12Config(), template13());

        assertTrue(result.getBoolean("update-checker"), "Update checker defaults to on (previous behaviour)");
        assertEquals(0, result.getInt("logging.operations-retention-days"), "Retention defaults to keep-forever");
        assertEquals(13, result.getInt("migration"));
    }

    // -> existing values preserved

    @Test
    void givenV12Config_whenMigrated_thenExistingValuesPreserved() {
        FileConfiguration result = new Migration13().migrate(v12Config(), template13());

        assertFalse(result.getBoolean("enabled"));
        assertEquals("cs", result.getString("locale"));
        assertEquals("DEBUG", result.getString("logging.level"));
        assertTrue(result.getBoolean("logging.operations"));
        assertEquals("https://example.invalid/hook", result.getString("logging.discord-webhook"));
        assertEquals("ALLOWLIST", result.getString("disenchantment.worlds.mode"));
        assertEquals(List.of("world_a"), result.getStringList("disenchantment.worlds.list"));
        assertEquals(42.0, result.getDouble("disenchantment.anvil.repair.base"));
        assertEquals(10, result.getInt("disenchantment.anvil.repair.enchantment-costs.mending.xp"));
        assertEquals(500.0, result.getDouble("disenchantment.anvil.repair.enchantment-costs.mending.economy"));
        assertEquals(0.5, result.getDouble("disenchantment.enchantment-chances.sharpness"));
        assertEquals(List.of("sharpness", "smite"), result.getStringList("enchantment-groups.combat"));
    }

    @Test
    void givenUserAlreadySetNewKeys_whenMigrated_thenTheirValuesWin() {
        YamlConfiguration old = v12Config();
        old.set("update-checker", false);
        old.set("logging.operations-retention-days", 30);

        FileConfiguration result = new Migration13().migrate(old, template13());

        assertFalse(result.getBoolean("update-checker"));
        assertEquals(30, result.getInt("logging.operations-retention-days"));
    }

    // -> chain through the real orchestrator against the bundled config.yml

    @Test
    void givenV12Config_whenFullMigrationApplied_thenMatchesBundledSchema() {
        YamlConfiguration bundled = YamlConfiguration.loadConfiguration(new InputStreamReader(
                Objects.requireNonNull(plugin.getResource("config.yml")), StandardCharsets.UTF_8));
        assertEquals(13, bundled.getInt("migration"), "config.yml must declare the latest migration");

        FileConfiguration result = ConfigMigrations.apply(plugin, v12Config(), bundled);

        assertEquals(13, result.getInt("migration"));
        assertTrue(result.getBoolean("update-checker"));
        assertEquals(0, result.getInt("logging.operations-retention-days"));
        assertEquals("cs", result.getString("locale"));
        assertTrue(leafKeys(result).containsAll(leafKeys(bundled)),
                "Migrated config must contain every key of the bundled config.yml");
    }

    // -> special anvils section

    @Test
    void givenV12Config_whenMigrated_thenSpecialAnvilsGetDefaults() {
        FileConfiguration result = new Migration13().migrate(v12Config(), template13());

        assertEquals("ALL", result.getString("special-anvils.mode"), "Default keeps every anvil working");
        assertEquals("LAPIS_BLOCK", result.getString("special-anvils.block-below"));
        assertFalse(result.getBoolean("special-anvils.allow-virtual-anvils"));
        assertTrue(result.getBoolean("special-anvils.drop-special-item"));
        assertFalse(result.getBoolean("special-anvils.hint-message"));
        assertEquals("&dEnchanter's Anvil", result.getString("special-anvils.item.name"));
        assertFalse(result.getStringList("special-anvils.item.lore").isEmpty());
        assertFalse(result.getBoolean("special-anvils.recipe.enabled"));
        assertEquals(List.of("EEE", "EAE", "EEE"), result.getStringList("special-anvils.recipe.shape"));
        assertEquals("EMERALD", result.getString("special-anvils.recipe.ingredients.E"));
        assertEquals("ANVIL", result.getString("special-anvils.recipe.ingredients.A"));
    }

    @Test
    void givenUserAlreadySetSpecialAnvils_whenMigrated_thenTheirValuesWin() {
        YamlConfiguration old = v12Config();
        old.set("special-anvils.mode", "MARKED");
        old.set("special-anvils.recipe.enabled", true);

        FileConfiguration result = new Migration13().migrate(old, template13());

        assertEquals("MARKED", result.getString("special-anvils.mode"));
        assertTrue(result.getBoolean("special-anvils.recipe.enabled"));
        assertEquals("LAPIS_BLOCK", result.getString("special-anvils.block-below"), "untouched keys keep defaults");
    }

    // -> accessors

    @Test
    void givenDefaults_whenReadAccessors_thenDefaults() {
        assertTrue(Config.isUpdateCheckerEnabled());
        assertEquals(0, Config.getOperationsRetentionDays());
        assertEquals(0, Config.Logging.getOperationsRetentionDays());
    }

    @Test
    void givenCustomValues_whenReadAccessors_thenReflected() {
        setConfig("update-checker", false);
        setConfig("logging.operations-retention-days", 14);

        assertFalse(Config.isUpdateCheckerEnabled());
        assertEquals(14, Config.getOperationsRetentionDays());
    }

    @Test
    void givenNegativeRetention_whenRead_thenTreatedAsKeepForever() {
        setConfig("logging.operations-retention-days", -5);

        assertEquals(0, Config.getOperationsRetentionDays());
    }

    private static java.util.Set<String> leafKeys(FileConfiguration config) {
        java.util.Set<String> keys = new java.util.TreeSet<>();
        for (String key : config.getKeys(true)) if (!config.isConfigurationSection(key)) keys.add(key);
        return keys;
    }

    // -> locales follows the template, never a stale list from the old config

    @Test
    void givenV12ConfigWithStaleLocales_whenMigrated_thenTemplateLocalesKept() {
        YamlConfiguration old = v12Config();
        old.set("locales", List.of("en"));

        FileConfiguration result = new Migration13().migrate(old, template13());

        assertEquals(template13().getStringList("locales"), result.getStringList("locales"));
    }
}
