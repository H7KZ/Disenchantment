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

    @Test
    void givenV12Config_whenMigrated_thenFeedbackDefaultsPresent() {
        FileConfiguration result = new Migration13().migrate(v12Config(), template13());

        assertTrue(result.getBoolean("feedback.particles.enabled"));
        assertEquals(List.of("ENCHANT", "ENCHANTMENT_TABLE"), result.getStringList("feedback.particles.types"));
        assertEquals(List.of("SMOKE", "SMOKE_NORMAL"), result.getStringList("feedback.particles.erase-types"));
        assertEquals(30, result.getInt("feedback.particles.count"));
        assertEquals(0.4, result.getDouble("feedback.particles.spread"));
        assertEquals("PLAYER", result.getString("feedback.particles.scope"));
        assertTrue(result.getBoolean("feedback.failure-sound.enabled"));
        assertEquals("entity.villager.no", result.getString("feedback.failure-sound.sound"));
        assertEquals(1.0, result.getDouble("feedback.failure-sound.volume"));
        assertEquals(1.0, result.getDouble("feedback.failure-sound.pitch"));
        assertTrue(result.getBoolean("feedback.not-enough-xp-message"));
        assertTrue(result.getBoolean("feedback.preview-lore"));
    }

    @Test
    void givenV12Config_whenMigrated_thenEraseDefaultsPresent() {
        FileConfiguration result = new Migration13().migrate(v12Config(), template13());

        assertFalse(result.getBoolean("erase.enabled"), "Erase mode is opt-in");
        assertEquals(List.of("ECHO_SHARD"), result.getStringList("erase.catalysts"));
        assertTrue(result.getBoolean("erase.consume-catalyst"));
        assertFalse(result.getBoolean("erase.erase-curses"));
        assertTrue(result.getBoolean("erase.anvil.sound.enabled"));
        assertEquals(1.0, result.getDouble("erase.anvil.sound.volume"));
        assertEquals(1.0, result.getDouble("erase.anvil.sound.pitch"));
        assertFalse(result.getBoolean("erase.anvil.repair.reset"));
        assertTrue(result.getBoolean("erase.anvil.repair.cost"));
        assertEquals(10, result.getInt("erase.anvil.repair.base"));
        assertEquals(0.25, result.getDouble("erase.anvil.repair.multiply"));
        assertEquals(-1, result.getInt("erase.anvil.repair.max-cost"));
        assertFalse(result.getBoolean("erase.anvil.bypass-too-expensive"));
        assertFalse(result.getBoolean("erase.economy.enabled"));
        assertEquals(100.0, result.getDouble("erase.economy.cost"));
        assertTrue(result.getBoolean("erase.economy.show-cost"));
        assertTrue(result.getBoolean("erase.economy.charge-message"));
        assertEquals("HIGHEST", result.getString("event-priorities.erase"));
        assertEquals("HIGHEST", result.getString("event-priorities.erase-click"));
    }

    @Test
    void givenUserCustomisedErase_whenMigrated_thenTheirValuesWin() {
        YamlConfiguration old = v12Config();
        old.set("erase.enabled", true);
        old.set("erase.catalysts", List.of("AMETHYST_SHARD"));
        old.set("event-priorities.erase", "LOW");

        FileConfiguration result = new Migration13().migrate(old, template13());

        assertTrue(result.getBoolean("erase.enabled"));
        assertEquals(List.of("AMETHYST_SHARD"), result.getStringList("erase.catalysts"));
        assertEquals("LOW", result.getString("event-priorities.erase"));
        assertEquals("HIGHEST", result.getString("event-priorities.erase-click"));
    }

    @Test
    void givenBundledConfig_whenReadThroughEraseAccessors_thenDefaults() {
        assertFalse(Config.Erase.isEnabled());
        assertEquals(List.of("ECHO_SHARD"), Config.Erase.getCatalysts());
        assertTrue(Config.Erase.isConsumeCatalyst());
        assertFalse(Config.Erase.isEraseCurses());
        assertEquals(org.bukkit.event.EventPriority.HIGHEST, Config.Erase.getPrepareEventPriority());
        assertEquals(org.bukkit.event.EventPriority.HIGHEST, Config.Erase.getClickEventPriority());
        assertEquals(10.0, Config.Erase.Anvil.Repair.getBaseCost());
        assertEquals(0.25, Config.Erase.Anvil.Repair.getCostMultiplier());
    }

    @Test
    void givenInvalidErasePriority_whenRead_thenFallsBackToHighest() {
        setConfig("event-priorities.erase", "NOPE");

        assertEquals(org.bukkit.event.EventPriority.HIGHEST, Config.Erase.getPrepareEventPriority());
    }

    @Test
    void givenUserCustomisedFeedback_whenMigrated_thenTheirValuesWin() {
        YamlConfiguration old = v12Config();
        old.set("feedback.particles.enabled", false);
        old.set("feedback.failure-sound.sound", "block.anvil.land");

        FileConfiguration result = new Migration13().migrate(old, template13());

        assertFalse(result.getBoolean("feedback.particles.enabled"));
        assertEquals("block.anvil.land", result.getString("feedback.failure-sound.sound"));
        assertEquals(30, result.getInt("feedback.particles.count"), "Untouched siblings keep the template default");
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
