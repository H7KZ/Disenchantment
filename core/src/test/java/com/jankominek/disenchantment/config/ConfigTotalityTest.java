package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.types.LogLevelType;
import com.jankominek.disenchantment.types.RestrictionMode;
import com.jankominek.disenchantment.types.SplitCountRange;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every {@link Config} getter must be total: whatever the YAML contains, it neither throws nor returns a value
 * that would break a caller.
 */
class ConfigTotalityTest extends DisenchantmentTestBase {

    // ----------------------------------------------------------------------------------------------------
    // Junk against every scalar option

    static Stream<Object> junk() {
        return Stream.of(
                "abc", "", "   ", "NaN", "Infinity", "-5", "1e999", "true",
                List.of("a", "b"), List.of(), Map.of("x", 1), Map.of("min", "q"),
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                -1, -1000000, Integer.MAX_VALUE, Long.MAX_VALUE, 1e300, -1e300, 0, 2.5, true, false
        );
    }

    @ParameterizedTest
    @MethodSource("junk")
    void givenJunkValue_whenResolvingAnyScalarOption_thenResultIsAlwaysValidAndInRange(Object junk) {
        for (ConfigSpec spec : ConfigSpec.all()) {
            String path = spec.key().getKey();
            setConfig(path, junk);

            Object value = assertDoesNotThrow(() -> spec.resolve(Disenchantment.config.get(path)), path);

            switch (spec.kind()) {
                case BOOLEAN -> assertInstanceOf(Boolean.class, value, path);
                case DECIMAL -> {
                    double d = (Double) value;
                    assertTrue(Double.isFinite(d), path + " must be finite but was " + d);
                    assertTrue(d >= spec.min() && d <= spec.max(), path + " out of range: " + d);
                }
                case INTEGER -> {
                    int i = (Integer) value;
                    assertTrue(i >= spec.min() && i <= spec.max(), path + " out of range: " + i);
                }
                case ENUM -> assertTrue(spec.enumType().isInstance(value), path);
                case STRING -> {
                    String s = (String) value;
                    assertNotNull(s, path);
                    if (spec.pattern() != null) assertTrue(s.matches(spec.pattern()), path + " violates pattern: " + s);
                }
            }

            setConfig(path, null);
        }
    }

    @ParameterizedTest
    @MethodSource("junk")
    void givenJunkValue_whenCallingEveryConfigGetter_thenNothingThrows(Object junk) {
        for (ConfigSpec spec : ConfigSpec.all()) setConfig(spec.key().getKey(), junk);
        for (String path : List.of(
                "locales", "enchantment-groups",
                "disenchantment.worlds.list", "disenchantment.worlds.mode", "disenchantment.materials.list", "disenchantment.materials.mode",
                "disenchantment.enchantments-states", "disenchantment.enchantment-chances", "disenchantment.anvil.repair.enchantment-costs",
                "shatterment.worlds.list", "shatterment.worlds.mode", "shatterment.materials.list", "shatterment.materials.mode",
                "shatterment.enchantments-states", "shatterment.enchantment-chances", "shatterment.anvil.repair.enchantment-costs",
                "shatterment.split-count")) {
            setConfig(path, junk);
        }

        assertDoesNotThrow(() -> {
            Config.isPluginEnabled();
            Config.isUpdateCheckerEnabled();
            Config.getOperationsRetentionDays();
            Config.getLocales();
            Config.getLocale();
            Config.getEnchantmentGroups();
            Config.EventPriorities.getDisenchantEvent();
            Config.EventPriorities.getDisenchantClickEvent();
            Config.EventPriorities.getShatterEvent();
            Config.EventPriorities.getShatterClickEvent();
            Config.Logging.getLevel();
            Config.Logging.isSaveReportsEnabled();
            Config.Logging.isOperationsEnabled();
            Config.Logging.getDiscordWebhook();
            Config.CostMultiplier.isLuckPermsEnabled();
            Config.Cooldown.getSeconds();
            Config.Cooldown.isUseCmiEnabled();
            Config.Cooldown.isUseEssentialsEnabled();

            Config.Disenchantment.isEnabled();
            Config.Disenchantment.getWorldsMode();
            Config.Disenchantment.getDisabledWorlds();
            Config.Disenchantment.getMaterialsMode();
            Config.Disenchantment.getDisabledMaterials();
            Config.Disenchantment.getEnchantmentStates();
            Config.Disenchantment.getEnchantmentChances();
            Config.Disenchantment.getEnchantmentChance("sharpness");
            Config.Disenchantment.Economy.isEnabled();
            Config.Disenchantment.Economy.getCost();
            Config.Disenchantment.Anvil.isBypassTooExpensiveEnabled();
            Config.Disenchantment.Anvil.Sound.isEnabled();
            Config.Disenchantment.Anvil.Sound.getVolume();
            Config.Disenchantment.Anvil.Sound.getPitch();
            Config.Disenchantment.Anvil.Repair.getBaseCost();
            Config.Disenchantment.Anvil.Repair.getCostMultiplier();
            Config.Disenchantment.Anvil.Repair.getEnchantmentCosts();
            Config.Disenchantment.Anvil.Repair.getEnchantmentEconomyCosts();
            Config.Disenchantment.Anvil.Repair.getMaxCost();

            Config.Shatterment.isEnabled();
            Config.Shatterment.getWorldsMode();
            Config.Shatterment.getDisabledWorlds();
            Config.Shatterment.getMaterialsMode();
            Config.Shatterment.getDisabledMaterials();
            Config.Shatterment.getEnchantmentStates();
            Config.Shatterment.getEnchantmentChances();
            Config.Shatterment.getEnchantmentChance("sharpness");
            Config.Shatterment.getSplitCount();
            Config.Shatterment.Economy.getCost();
            Config.Shatterment.Anvil.Sound.getVolume();
            Config.Shatterment.Anvil.Sound.getPitch();
            Config.Shatterment.Anvil.Repair.getBaseCost();
            Config.Shatterment.Anvil.Repair.getCostMultiplier();
            Config.Shatterment.Anvil.Repair.getEnchantmentCosts();
            Config.Shatterment.Anvil.Repair.getEnchantmentEconomyCosts();
            Config.Shatterment.Anvil.Repair.getMaxCost();
        });

        assertTrue(Config.getLocale().matches(ConfigSpec.LOCALE_PATTERN));
        assertNotNull(Config.Logging.getDiscordWebhook());
        assertTrue(Config.Shatterment.getSplitCount().min() >= 1);
    }

    // ----------------------------------------------------------------------------------------------------
    // Sound, costs, numbers

    static Stream<Arguments> volumes() {
        return Stream.of(
                Arguments.of(-1.0, 0.0), Arguments.of(0.0, 0.0), Arguments.of(0.5, 0.5), Arguments.of(2.0, 2.0),
                Arguments.of(50, 10.0), Arguments.of(Double.NaN, 1.0), Arguments.of(Double.POSITIVE_INFINITY, 1.0),
                Arguments.of("abc", 1.0), Arguments.of("0.5", 0.5), Arguments.of("0,5", 0.5), Arguments.of(List.of(1), 1.0)
        );
    }

    @ParameterizedTest
    @MethodSource("volumes")
    void givenVolume_whenGetVolume_thenFiniteAndClamped(Object configured, double expected) {
        setConfig("disenchantment.anvil.sound.volume", configured);
        setConfig("shatterment.anvil.sound.volume", configured);

        assertEquals(expected, Config.Disenchantment.Anvil.Sound.getVolume());
        assertEquals(expected, Config.Shatterment.Anvil.Sound.getVolume());
    }

    static Stream<Arguments> pitches() {
        return Stream.of(
                Arguments.of(0.1, 0.5), Arguments.of(-3, 0.5), Arguments.of(0.5, 0.5), Arguments.of(1.25, 1.25),
                Arguments.of(2.0, 2.0), Arguments.of(5, 2.0), Arguments.of(Double.NaN, 1.0), Arguments.of("high", 1.0)
        );
    }

    @ParameterizedTest
    @MethodSource("pitches")
    void givenPitch_whenGetPitch_thenWithinVanillaRange(Object configured, double expected) {
        setConfig("disenchantment.anvil.sound.pitch", configured);
        setConfig("shatterment.anvil.sound.pitch", configured);

        assertEquals(expected, Config.Disenchantment.Anvil.Sound.getPitch());
        assertEquals(expected, Config.Shatterment.Anvil.Sound.getPitch());
    }

    @Test
    void givenNegativeOrBrokenEconomyCost_whenGetCost_thenNeverNegativeAndNeverNaN() {
        setConfig("disenchantment.economy.cost", -50);
        assertEquals(0.0, Config.Disenchantment.Economy.getCost());

        setConfig("disenchantment.economy.cost", Double.NaN);
        assertEquals(100.0, Config.Disenchantment.Economy.getCost());

        setConfig("shatterment.economy.cost", "free");
        assertEquals(100.0, Config.Shatterment.Economy.getCost());

        setConfig("shatterment.economy.cost", 12.5);
        assertEquals(12.5, Config.Shatterment.Economy.getCost());
    }

    @Test
    void givenBrokenBaseAndMultiplier_whenGetCosts_thenDefaultsAreUsed() {
        setConfig("disenchantment.anvil.repair.base", "lots");
        setConfig("disenchantment.anvil.repair.multiply", Double.POSITIVE_INFINITY);

        assertEquals(10.0, Config.Disenchantment.Anvil.Repair.getBaseCost());
        assertEquals(0.25, Config.Disenchantment.Anvil.Repair.getCostMultiplier());
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, -100, Integer.MIN_VALUE})
    void givenMaxCostBelowMinusOne_whenGetMaxCost_thenNoCap(int configured) {
        setConfig("disenchantment.anvil.repair.max-cost", configured);
        setConfig("shatterment.anvil.repair.max-cost", configured);

        assertEquals(-1, Config.Disenchantment.Anvil.Repair.getMaxCost());
        assertEquals(-1, Config.Shatterment.Anvil.Repair.getMaxCost());
    }

    @Test
    void givenBrokenMaxCost_whenGetMaxCost_thenNoCap() {
        setConfig("disenchantment.anvil.repair.max-cost", "high");
        assertEquals(-1, Config.Disenchantment.Anvil.Repair.getMaxCost());

        setConfig("disenchantment.anvil.repair.max-cost", 30);
        assertEquals(30, Config.Disenchantment.Anvil.Repair.getMaxCost());
    }

    @Test
    void givenNegativeCooldownAndRetention_whenRead_thenZero() {
        setConfig("disenchantment.cooldown.seconds", -30);
        setConfig("logging.operations-retention-days", -7);

        assertEquals(0, Config.Cooldown.getSeconds());
        assertEquals(0, Config.getOperationsRetentionDays());
    }

    @Test
    void givenQuotedBooleans_whenRead_thenAccepted() {
        setConfig("enabled", "false");
        setConfig("disenchantment.enabled", " TRUE ");
        setConfig("shatterment.enabled", "banana");

        assertFalse(Config.isPluginEnabled());
        assertTrue(Config.Disenchantment.isEnabled());
        assertTrue(Config.Shatterment.isEnabled(), "unusable boolean falls back to the template default (true)");
    }

    // ----------------------------------------------------------------------------------------------------
    // Enums

    @ParameterizedTest
    @ValueSource(strings = {"low", "LOW", " Low ", "lOw"})
    void givenEventPriorityInAnyCase_whenRead_thenParsed(String configured) {
        setConfig("event-priorities.disenchant", configured);
        assertEquals(EventPriority.LOW, Config.EventPriorities.getDisenchantEvent());
    }

    @Test
    void givenUnknownEventPriority_whenRead_thenHighest() {
        setConfig("event-priorities.disenchant", "ULTRA");
        setConfig("event-priorities.disenchant-click", 7);
        setConfig("event-priorities.shatter", List.of("LOW"));
        setConfig("event-priorities.shatter-click", null);

        assertEquals(EventPriority.HIGHEST, Config.EventPriorities.getDisenchantEvent());
        assertEquals(EventPriority.HIGHEST, Config.EventPriorities.getDisenchantClickEvent());
        assertEquals(EventPriority.HIGHEST, Config.EventPriorities.getShatterEvent());
        assertEquals(EventPriority.HIGHEST, Config.EventPriorities.getShatterClickEvent());
    }

    @Test
    void givenLogLevelVariants_whenRead_thenCaseInsensitiveWithInfoFallback() {
        setConfig("logging.level", "debug");
        assertEquals(LogLevelType.DEBUG, Config.Logging.getLevel());

        setConfig("logging.level", " None ");
        assertEquals(LogLevelType.NONE, Config.Logging.getLevel());

        setConfig("logging.level", "verbose");
        assertEquals(LogLevelType.INFO, Config.Logging.getLevel());

        setConfig("logging.level", List.of("DEBUG"));
        assertEquals(LogLevelType.INFO, Config.Logging.getLevel());
    }

    @Test
    void givenTurkishDefaultLocale_whenParsingEnumsAndStates_thenStillMatches() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            setConfig("logging.level", "info");
            setDisenchantEnchantmentStates(List.of("sharpness:disable"));

            assertEquals(LogLevelType.INFO, Config.Logging.getLevel());
            assertEquals(EnchantmentStateType.DISABLE, Config.Disenchantment.getEnchantmentStates().get("sharpness"));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void givenModeInAnyCase_whenRead_thenParsedAndJunkIsDenylist() {
        setConfig("disenchantment.worlds.mode", " allowlist ");
        assertEquals(RestrictionMode.ALLOWLIST, Config.Disenchantment.getWorldsMode());

        setConfig("disenchantment.worlds.mode", "whitelist");
        assertEquals(RestrictionMode.DENYLIST, Config.Disenchantment.getWorldsMode());

        setConfig("shatterment.materials.mode", 12);
        assertEquals(RestrictionMode.DENYLIST, Config.Shatterment.getMaterialsMode());

        setConfig("shatterment.materials.mode", List.of("ALLOWLIST"));
        assertEquals(RestrictionMode.DENYLIST, Config.Shatterment.getMaterialsMode());
    }

    // ----------------------------------------------------------------------------------------------------
    // Locales

    @Test
    void givenPathTraversalLocale_whenGetLocale_thenEnglish() {
        setConfig("locale", "../../etc/passwd");
        assertEquals("en", Config.getLocale());

        setConfig("locale", List.of("cs"));
        assertEquals("en", Config.getLocale());

        setConfig("locale", "pt-BR");
        assertEquals("pt-BR", Config.getLocale());
    }

    @Test
    void givenBrokenLocalesList_whenGetLocales_thenSafe() {
        setConfig("locales", "cs");
        assertEquals(List.of("cs"), Config.getLocales());

        setConfig("locales", List.of("cs", "../x", "en", "cs", 5));
        assertEquals(List.of("cs", "en", "5"), Config.getLocales());

        setConfig("locales", Map.of("a", 1));
        assertEquals(List.of("cs", "en"), Config.getLocales());

        setConfig("locales", 42.5);
        assertEquals(List.of("cs", "en"), Config.getLocales());
    }

    // ----------------------------------------------------------------------------------------------------
    // Worlds / materials

    @Test
    void givenMaterialNamesInAnyForm_whenGetDisabledMaterials_thenMatchedLeniently() {
        setConfig("disenchantment.materials.list", List.of("diamond_sword", "MINECRAFT:NETHERITE_SWORD", "minecraft:bow", "Not_A_Thing", "", "LEGACY_WOOD_SWORD", "diamond_sword"));

        List<Material> materials = Config.Disenchantment.getDisabledMaterials();

        assertEquals(List.of(Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.BOW), materials);
    }

    @Test
    void givenNonListMaterialsAndWorlds_whenRead_thenSingleStringOrEmpty() {
        setConfig("disenchantment.materials.list", "bow");
        assertEquals(List.of(Material.BOW), Config.Disenchantment.getDisabledMaterials());

        setConfig("disenchantment.materials.list", Map.of("a", "b"));
        assertTrue(Config.Disenchantment.getDisabledMaterials().isEmpty());

        setConfig("shatterment.worlds.list", 12);
        assertTrue(Config.Shatterment.getDisabledWorlds().isEmpty());
    }

    @Test
    void givenUnresolvableEntries_whenSettingMaterialsAndWorlds_thenHandWrittenEntriesAreKept() {
        world();
        setConfig("disenchantment.materials.list", List.of("diamond_sword", "NOT_A_MATERIAL"));
        setConfig("disenchantment.worlds.list", List.of("world", "world_loaded_later"));

        Config.Disenchantment.setDisabledMaterials(List.of(Material.BOW));
        Config.Disenchantment.setDisabledWorlds(List.of(world()));

        assertEquals(List.of("BOW", "NOT_A_MATERIAL"), Disenchantment.config.getStringList("disenchantment.materials.list"));
        assertEquals(List.of("world", "world_loaded_later"), Disenchantment.config.getStringList("disenchantment.worlds.list"));
        assertEquals(List.of(Material.BOW), Config.Disenchantment.getDisabledMaterials());
    }

    @Test
    void givenNormalToggle_whenSettingEmptyLists_thenResolvableEntriesAreRemoved() {
        setConfig("disenchantment.materials.list", List.of("diamond_sword"));

        Config.Disenchantment.setDisabledMaterials(List.of());

        assertTrue(Disenchantment.config.getStringList("disenchantment.materials.list").isEmpty());
    }

    // ----------------------------------------------------------------------------------------------------
    // Enchantment states

    @Test
    void givenEveryEntryShape_whenGetEnchantmentStates_thenOnlyValidEntriesSurvive() {
        setDisenchantEnchantmentStates(List.of(
                "sharpness:keep",
                "minecraft:mending:delete",
                "  Fortune : Disable ",
                "unbreaking",                 // missing state
                "minecraft:looting",          // namespaced key without state (must not become key=minecraft)
                "efficiency:nonsense",        // unknown state
                "a:b:c:d",                    // too many parts
                ":keep",                      // no key
                "",                           // blank
                "power:enable"
        ));

        Map<String, EnchantmentStateType> states = Config.Disenchantment.getEnchantmentStates();

        assertEquals(Map.of(
                "sharpness", EnchantmentStateType.KEEP,
                "mending", EnchantmentStateType.DELETE,
                "fortune", EnchantmentStateType.DISABLE,
                "power", EnchantmentStateType.ENABLE
        ), states);
        assertFalse(states.containsValue(null));
    }

    @Test
    void givenYamlMapMistakeInStates_whenGetEnchantmentStates_thenStillUnderstood() {
        // "- sharpness: keep" parses as a one-entry map, not as the string "sharpness:keep"
        setConfig("shatterment.enchantments-states", List.of(Map.of("sharpness", "keep"), Map.of("minecraft:mending", "delete")));
        assertEquals(EnchantmentStateType.KEEP, Config.Shatterment.getEnchantmentStates().get("sharpness"));
        assertEquals(EnchantmentStateType.DELETE, Config.Shatterment.getEnchantmentStates().get("mending"));

        setConfig("shatterment.enchantments-states.power", "disable");
        assertEquals(EnchantmentStateType.DISABLE, Config.Shatterment.getEnchantmentStates().get("power"));
    }

    @Test
    void givenNonListStates_whenGetEnchantmentStates_thenEmptyOrSingleEntry() {
        setConfig("disenchantment.enchantments-states", 12);
        assertTrue(Config.Disenchantment.getEnchantmentStates().isEmpty());

        setConfig("disenchantment.enchantments-states", "sharpness:keep");
        assertEquals(EnchantmentStateType.KEEP, Config.Disenchantment.getEnchantmentStates().get("sharpness"));

        setConfig("disenchantment.enchantments-states", List.of(List.of("a"), 5, true));
        assertTrue(Config.Disenchantment.getEnchantmentStates().isEmpty());
    }

    // ----------------------------------------------------------------------------------------------------
    // Chances / costs

    @Test
    void givenBadChanceValues_whenGetEnchantmentChance_thenBrokenEntriesNeverBlockTheOperation() {
        setConfig("disenchantment.enchantment-chances.sharpness", "half");
        setConfig("disenchantment.enchantment-chances.mending", Double.NaN);
        setConfig("disenchantment.enchantment-chances.fortune", 50);
        setConfig("disenchantment.enchantment-chances.power", -2);
        setConfig("disenchantment.enchantment-chances.Looting", "0,25");
        setConfig("disenchantment.enchantment-chances.unbreaking", List.of(1));

        assertEquals(1.0, Config.Disenchantment.getEnchantmentChance("sharpness"), "a non-number must mean 'always succeeds', not 0");
        assertEquals(1.0, Config.Disenchantment.getEnchantmentChance("mending"));
        assertEquals(1.0, Config.Disenchantment.getEnchantmentChance("fortune"));
        assertEquals(0.0, Config.Disenchantment.getEnchantmentChance("power"));
        assertEquals(0.25, Config.Disenchantment.getEnchantmentChance("minecraft:looting"));
        assertEquals(1.0, Config.Disenchantment.getEnchantmentChance("unbreaking"));
    }

    @Test
    void givenNonMapChances_whenGetEnchantmentChances_thenEmpty() {
        setConfig("shatterment.enchantment-chances", List.of("sharpness"));
        assertTrue(Config.Shatterment.getEnchantmentChances().isEmpty());

        setConfig("shatterment.enchantment-chances", "sharpness");
        assertTrue(Config.Shatterment.getEnchantmentChances().isEmpty());
    }

    @Test
    void givenBadEnchantmentCosts_whenGetEnchantmentCosts_thenUnusableEntriesAreSkippedNotZeroed() {
        setConfig("disenchantment.anvil.repair.enchantment-costs.sharpness", "five");
        setConfig("disenchantment.anvil.repair.enchantment-costs.mending.xp", "ten");
        setConfig("disenchantment.anvil.repair.enchantment-costs.mending.economy", "lots");
        setConfig("disenchantment.anvil.repair.enchantment-costs.fortune", -4);
        setConfig("disenchantment.anvil.repair.enchantment-costs.Power", 7);
        setConfig("disenchantment.anvil.repair.enchantment-costs.efficiency.economy", -5);
        setConfig("disenchantment.anvil.repair.enchantment-costs.unbreaking", List.of(1));

        Map<String, Integer> xp = Config.Disenchantment.Anvil.Repair.getEnchantmentCosts();
        Map<String, Double> economy = Config.Disenchantment.Anvil.Repair.getEnchantmentEconomyCosts();

        assertFalse(xp.containsKey("sharpness"), "an unparseable XP cost must not become a free 0");
        assertFalse(xp.containsKey("mending"));
        assertFalse(xp.containsKey("unbreaking"));
        assertEquals(0, xp.get("fortune"));
        assertEquals(7, xp.get("power"));
        assertFalse(economy.containsKey("mending"));
        assertEquals(0.0, economy.get("efficiency"));
    }

    @Test
    void givenNonMapCosts_whenGetEnchantmentCosts_thenEmpty() {
        setConfig("shatterment.anvil.repair.enchantment-costs", 12);

        assertTrue(Config.Shatterment.Anvil.Repair.getEnchantmentCosts().isEmpty());
        assertTrue(Config.Shatterment.Anvil.Repair.getEnchantmentEconomyCosts().isEmpty());
    }

    // ----------------------------------------------------------------------------------------------------
    // Groups

    @Test
    void givenGroupsOfEveryShape_whenGetEnchantmentGroups_thenOnlyUsableGroupsSurvive() {
        setConfig("enchantment-groups.Combat", List.of("Sharpness", "smite", 5, ""));
        setConfig("enchantment-groups.single", "efficiency");
        setConfig("enchantment-groups.empty", List.of());
        setConfig("enchantment-groups.broken", Map.of("a", "b"));
        setConfig("enchantment-groups.number", 4);

        Map<String, List<String>> groups = Config.getEnchantmentGroups();

        assertEquals(List.of("sharpness", "smite", "5"), groups.get("combat"));
        assertEquals(List.of("efficiency"), groups.get("single"));
        assertEquals(List.of(), groups.get("empty"));
        assertFalse(groups.containsKey("broken"));
        assertTrue(groups.containsKey("number"), "a scalar is a one-member group");
    }

    @Test
    void givenGroupsNotAMap_whenGetEnchantmentGroups_thenEmpty() {
        setConfig("enchantment-groups", List.of("a", "b"));
        assertTrue(Config.getEnchantmentGroups().isEmpty());

        setConfig("enchantment-groups", "combat");
        assertTrue(Config.getEnchantmentGroups().isEmpty());
    }

    // ----------------------------------------------------------------------------------------------------
    // Split count

    static Stream<Arguments> splitCounts() {
        return Stream.of(
                Arguments.of(3, 3, 3),
                Arguments.of(0, 1, 1),
                Arguments.of(-4, 1, 1),
                Arguments.of("-4", 1, 1),
                Arguments.of("abc", 1, 1),
                Arguments.of("", 1, 1),
                Arguments.of("5-2", 2, 5),
                Arguments.of("2-5", 2, 5),
                Arguments.of(" 2 - 5 ", 2, 5),
                Arguments.of("2..4", 2, 4),
                Arguments.of("3 to 6", 3, 6),
                Arguments.of("4", 4, 4),
                Arguments.of("2,5", 2, 2),
                Arguments.of(2.7, 2, 2),
                Arguments.of(Double.NaN, 1, 1),
                Arguments.of(100000, 255, 255),
                Arguments.of("1-99999999999999", 1, 255),
                Arguments.of(List.of(1, 2), 1, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("splitCounts")
    void givenSplitCountOfAnyShape_whenGetSplitCount_thenAlwaysAValidRange(Object configured, int min, int max) {
        setConfig("shatterment.split-count", configured);

        assertEquals(new SplitCountRange(min, max), Config.Shatterment.getSplitCount());
    }

    @Test
    void givenPartialOrBrokenSplitMap_whenGetSplitCount_thenMissingBoundMirrorsTheOther() {
        setConfig("shatterment.split-count", null);
        setConfig("shatterment.split-count.min", 3);
        assertEquals(new SplitCountRange(3, 3), Config.Shatterment.getSplitCount());

        setConfig("shatterment.split-count", null);
        setConfig("shatterment.split-count.max", 4);
        assertEquals(new SplitCountRange(4, 4), Config.Shatterment.getSplitCount());

        setConfig("shatterment.split-count", null);
        setConfig("shatterment.split-count.min", 6);
        setConfig("shatterment.split-count.max", 2);
        assertEquals(new SplitCountRange(2, 6), Config.Shatterment.getSplitCount());

        setConfig("shatterment.split-count", null);
        setConfig("shatterment.split-count.min", "x");
        setConfig("shatterment.split-count.max", "y");
        assertEquals(new SplitCountRange(1, 1), Config.Shatterment.getSplitCount());
    }
}
