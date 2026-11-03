package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.config.migration.steps.Migration13;
import com.jankominek.disenchantment.types.BedrockDetectMethod;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The {@code bedrock:} section: defaults, total getters, validation warnings and the migration template.
 */
class BedrockConfigTest extends DisenchantmentTestBase {

    private YamlConfiguration bundled(String resource) {
        return YamlConfiguration.loadConfiguration(new InputStreamReader(
                Objects.requireNonNull(plugin.getResource(resource)), StandardCharsets.UTF_8));
    }

    // -> defaults

    @Test
    void givenBundledDefaults_thenFallbackIsOffAndBedrockOnly() {
        assertFalse(Config.Bedrock.isFallbackEnabled());
        assertFalse(Config.Bedrock.isAllowJavaPlayers());
        assertFalse(Config.Bedrock.isOpenOnSneakClick());
        assertEquals(List.of(BedrockDetectMethod.FLOODGATE, BedrockDetectMethod.GEYSER, BedrockDetectMethod.UUID, BedrockDetectMethod.PREFIX),
                Config.Bedrock.getDetect());
        assertEquals(List.of(".", "*"), Config.Bedrock.getPrefixes());
    }

    @Test
    void givenKeysAbsent_whenReading_thenDefaultsApply() {
        for (String key : List.of("fallback-enabled", "allow-java-players", "detect", "prefixes", "open-on-sneak-click")) {
            setConfig("bedrock." + key, null);
        }

        assertFalse(Config.Bedrock.isFallbackEnabled());
        assertFalse(Config.Bedrock.isAllowJavaPlayers());
        assertFalse(Config.Bedrock.isOpenOnSneakClick());
        assertEquals(Config.Bedrock.DEFAULT_DETECT, Config.Bedrock.getDetect());
        assertEquals(Config.Bedrock.DEFAULT_PREFIXES, Config.Bedrock.getPrefixes());
    }

    // -> detect

    @Test
    void givenLowercaseAndHyphenatedNames_whenReadingDetect_thenParsedLeniently() {
        setConfig("bedrock.detect", List.of("uuid", "Floodgate", " prefix "));

        assertEquals(List.of(BedrockDetectMethod.UUID, BedrockDetectMethod.FLOODGATE, BedrockDetectMethod.PREFIX), Config.Bedrock.getDetect());
    }

    @Test
    void givenUnknownAndDuplicateEntries_whenReadingDetect_thenSkippedAndDeduplicated() {
        setConfig("bedrock.detect", List.of("FLOODGATE", "bogus", "floodgate", "GEYSER"));

        assertEquals(List.of(BedrockDetectMethod.FLOODGATE, BedrockDetectMethod.GEYSER), Config.Bedrock.getDetect());
    }

    @Test
    void givenExplicitEmptyList_whenReadingDetect_thenDetectionIsOff() {
        setConfig("bedrock.detect", List.of());

        assertEquals(List.of(), Config.Bedrock.getDetect());
    }

    @Test
    void givenSingleScalar_whenReadingDetect_thenOneMethod() {
        setConfig("bedrock.detect", "UUID");

        assertEquals(List.of(BedrockDetectMethod.UUID), Config.Bedrock.getDetect());
    }

    @Test
    void givenMapOrNumber_whenReadingDetect_thenDefaults() {
        setConfig("bedrock.detect", Map.of("a", 1));
        assertEquals(Config.Bedrock.DEFAULT_DETECT, Config.Bedrock.getDetect());
    }

    // -> prefixes

    @Test
    void givenBlankEntries_whenReadingPrefixes_thenSkipped() {
        setConfig("bedrock.prefixes", List.of(".", "", "   ", "*", "."));

        assertEquals(List.of(".", "*"), Config.Bedrock.getPrefixes());
    }

    @Test
    void givenMap_whenReadingPrefixes_thenDefaults() {
        setConfig("bedrock.prefixes", Map.of("x", "y"));

        assertEquals(Config.Bedrock.DEFAULT_PREFIXES, Config.Bedrock.getPrefixes());
    }

    // -> totality

    static java.util.stream.Stream<Object> junk() {
        return ConfigTotalityTest.junk();
    }

    @ParameterizedTest
    @MethodSource("junk")
    void givenJunkEverywhere_whenReadingAnyBedrockGetter_thenNothingThrows(Object junk) {
        for (String key : List.of("fallback-enabled", "allow-java-players", "detect", "prefixes", "open-on-sneak-click")) {
            setConfig("bedrock." + key, junk);
        }

        assertDoesNotThrow(() -> {
            Config.Bedrock.isFallbackEnabled();
            Config.Bedrock.isAllowJavaPlayers();
            Config.Bedrock.isOpenOnSneakClick();
            assertNotNull(Config.Bedrock.getDetect());
            assertNotNull(Config.Bedrock.getPrefixes());
            assertFalse(Config.Bedrock.getPrefixes().contains(""), "a blank prefix would match every player");
        });
    }

    @Test
    void givenQuotedBooleanStrings_whenReadingFlags_thenParsed() {
        setConfig("bedrock.fallback-enabled", "true");
        setConfig("bedrock.allow-java-players", "TRUE");

        assertTrue(Config.Bedrock.isFallbackEnabled());
        assertTrue(Config.Bedrock.isAllowJavaPlayers());
    }

    @Test
    void givenUnparseableFlag_whenReadingFlags_thenOff() {
        setConfig("bedrock.fallback-enabled", "maybe");
        setConfig("bedrock.allow-java-players", List.of("yes"));

        assertFalse(Config.Bedrock.isFallbackEnabled());
        assertFalse(Config.Bedrock.isAllowJavaPlayers(), "a junk value must never open the menu to Java players");
    }

    // -> validator

    @Test
    void givenBundledDefaults_whenValidating_thenNoBedrockProblems() {
        List<ConfigIssue> issues = ConfigValidator.validate(Disenchantment.config, bundled("config.yml"));

        assertTrue(issues.stream().noneMatch(i -> i.path().startsWith("bedrock")), issues.toString());
    }

    @Test
    void givenUnknownDetectMethod_whenValidating_thenReported() {
        setConfig("bedrock.detect", List.of("FLOODGATE", "RADAR"));

        List<ConfigIssue> issues = ConfigValidator.validate(Disenchantment.config, bundled("config.yml"));

        assertTrue(issues.stream().anyMatch(i -> i.path().equals("bedrock.detect") && i.message().contains("RADAR")), issues.toString());
    }

    @Test
    void givenWrongTypes_whenValidating_thenReported() {
        setConfig("bedrock.detect", Map.of("a", 1));
        setConfig("bedrock.prefixes", Map.of("a", 1));
        setConfig("bedrock.fallback-enabled", "perhaps");

        List<ConfigIssue> issues = ConfigValidator.validate(Disenchantment.config, bundled("config.yml"));

        assertTrue(issues.stream().anyMatch(i -> i.path().equals("bedrock.detect")), issues.toString());
        assertTrue(issues.stream().anyMatch(i -> i.path().equals("bedrock.prefixes")), issues.toString());
        assertTrue(issues.stream().anyMatch(i -> i.path().equals("bedrock.fallback-enabled")), issues.toString());
    }

    @Test
    void givenMisspeltOption_whenValidating_thenSuggestionOffered() {
        setConfig("bedrock.fallback-enable", true);

        List<ConfigIssue> issues = ConfigValidator.validate(Disenchantment.config, bundled("config.yml"));

        assertTrue(issues.stream().anyMatch(i -> i.path().equals("bedrock.fallback-enable") && i.message().contains("bedrock.fallback-enabled")), issues.toString());
    }

    // -> migration template

    @Test
    void givenMigration13Template_thenBedrockSectionMatchesTheBundledConfig() {
        YamlConfiguration config = bundled("config.yml");
        YamlConfiguration template = bundled("migrations/13.yml");

        assertEquals(13, template.getInt("migration"));
        for (String key : config.getConfigurationSection("bedrock").getKeys(false)) {
            assertEquals(config.get("bedrock." + key), template.get("bedrock." + key), key);
        }
        assertEquals(config.getConfigurationSection("bedrock").getKeys(false), template.getConfigurationSection("bedrock").getKeys(false));
    }

    @Test
    void givenOlderConfig_whenMigratedTo13_thenBedrockDefaultsArePresent() {
        YamlConfiguration old = new YamlConfiguration();
        old.set("migration", 12);
        old.set("enabled", false);

        FileConfiguration result = new Migration13().migrate(old, bundled("migrations/13.yml"));

        assertFalse(result.getBoolean("bedrock.fallback-enabled"));
        assertFalse(result.getBoolean("bedrock.allow-java-players"));
        assertEquals(List.of("FLOODGATE", "GEYSER", "UUID", "PREFIX"), result.getStringList("bedrock.detect"));
        assertEquals(List.of(".", "*"), result.getStringList("bedrock.prefixes"));
        assertFalse(result.getBoolean("bedrock.open-on-sneak-click"));
    }

    @Test
    void givenBedrockValuesInOldConfig_whenMigratedTo13_thenKept() {
        YamlConfiguration old = new YamlConfiguration();
        old.set("migration", 12);
        old.set("bedrock.fallback-enabled", true);
        old.set("bedrock.prefixes", List.of("+"));

        FileConfiguration result = new Migration13().migrate(old, bundled("migrations/13.yml"));

        assertTrue(result.getBoolean("bedrock.fallback-enabled"));
        assertEquals(List.of("+"), result.getStringList("bedrock.prefixes"));
    }
}
