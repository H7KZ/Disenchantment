package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigValidatorTest extends DisenchantmentTestBase {

    private List<ConfigIssue> validate() {
        YamlConfiguration template = YamlConfiguration.loadConfiguration(
                new InputStreamReader(plugin.getResource("config.yml"), StandardCharsets.UTF_8));
        return ConfigValidator.validate(Disenchantment.config, template);
    }

    private ConfigIssue issueAt(List<ConfigIssue> issues, String path) {
        return issues.stream().filter(i -> i.path().equals(path)).findFirst()
                .orElseThrow(() -> new AssertionError("no issue at " + path + " in " + issues));
    }

    private boolean hasIssueAt(List<ConfigIssue> issues, String path) {
        return issues.stream().anyMatch(i -> i.path().equals(path));
    }

    // ----------------------------------------------------------------------------------------------------

    @Test
    void givenBundledDefaultConfig_whenValidate_thenNoIssues() {
        assertEquals(List.of(), validate());
    }

    @Test
    void givenEveryCommentedExampleStyleValue_whenValidate_thenNoIssues() {
        world();
        setConfig("disenchantment.enchantments-states", List.of("sharpness:keep", "minecraft:mending:delete", "other_plugin:custom:disable"));
        setConfig("disenchantment.enchantment-chances.mending", 0.5);
        setConfig("disenchantment.anvil.repair.enchantment-costs.sharpness", 5);
        setConfig("disenchantment.anvil.repair.enchantment-costs.mending.xp", 10);
        setConfig("disenchantment.anvil.repair.enchantment-costs.mending.economy", 500);
        setConfig("enchantment-groups.combat", List.of("sharpness", "minecraft:smite"));
        setConfig("disenchantment.materials.list", List.of("DIAMOND_SWORD", "minecraft:bow"));
        setConfig("disenchantment.worlds.list", List.of("world"));
        setConfig("shatterment.split-count.min", 1);
        setConfig("shatterment.split-count.max", 3);

        assertEquals(List.of(), validate());
    }

    // ----------------------------------------------------------------------------------------------------
    // Unknown / misspelt keys

    @Test
    void givenTopLevelTypo_whenValidate_thenSuggestsTheRealSection() {
        setConfig("disenchantmnet.enabled", false);

        ConfigIssue issue = issueAt(validate(), "disenchantmnet");

        assertTrue(issue.message().contains("unknown option"));
        assertTrue(issue.message().contains("\"disenchantment\""), issue.message());
    }

    @Test
    void givenNestedTypo_whenValidate_thenReportedWithFullPathAndSuggestion() {
        setConfig("disenchantment.anvil.sund.volume", 0.5);
        setConfig("shatterment.econmy.cost", 5);

        List<ConfigIssue> issues = validate();

        assertTrue(issueAt(issues, "disenchantment.anvil.sund").message().contains("\"disenchantment.anvil.sound\""));
        assertTrue(issueAt(issues, "shatterment.econmy").message().contains("\"shatterment.economy\""));
    }

    @Test
    void givenKeyWithNothingSimilar_whenValidate_thenReportedWithoutSuggestion() {
        setConfig("zzzzzzzzzzzzzzzz", 1);

        ConfigIssue issue = issueAt(validate(), "zzzzzzzzzzzzzzzz");

        assertFalse(issue.message().contains("did you mean"));
    }

    @Test
    void givenUserDefinedMaps_whenValidate_thenTheirKeysAreNotTreatedAsTypos() {
        setConfig("enchantment-groups.anything_goes", List.of("sharpness"));
        setConfig("shatterment.enchantment-chances.fire_aspect", 0.1);
        setConfig("shatterment.anvil.repair.enchantment-costs.fire_aspect", 3);

        assertEquals(List.of(), validate());
    }

    // ----------------------------------------------------------------------------------------------------
    // Scalars

    @Test
    void givenWrongTypesAndRanges_whenValidate_thenEachOptionExplainsTheReplacement() {
        setConfig("disenchantment.anvil.sound.volume", "loud");
        setConfig("disenchantment.anvil.sound.pitch", 9);
        setConfig("shatterment.economy.cost", -5);
        setConfig("shatterment.anvil.repair.max-cost", -9);
        setConfig("disenchantment.cooldown.seconds", 1.5);
        setConfig("enabled", "maybe");
        setConfig("logging.level", "LOUD");
        setConfig("event-priorities.shatter", "SOMETIMES");
        setConfig("disenchantment.economy.cost", Double.NaN);

        List<ConfigIssue> issues = validate();

        assertTrue(issueAt(issues, "disenchantment.anvil.sound.volume").message().contains("using default 1"));
        assertTrue(issueAt(issues, "disenchantment.anvil.sound.pitch").message().contains("using 2"));
        assertTrue(issueAt(issues, "shatterment.economy.cost").message().contains("using 0"));
        assertTrue(issueAt(issues, "shatterment.anvil.repair.max-cost").message().contains("using -1"));
        assertTrue(issueAt(issues, "disenchantment.cooldown.seconds").message().contains("not a whole number"));
        assertTrue(issueAt(issues, "enabled").message().contains("true or false"));
        assertTrue(issueAt(issues, "logging.level").message().contains("valid: NONE, INFO, DEBUG"));
        assertTrue(issueAt(issues, "event-priorities.shatter").message().contains("using default HIGHEST"));
        assertTrue(issueAt(issues, "disenchantment.economy.cost").message().contains("finite number"));
    }

    @Test
    void givenLenientButUnusualValues_whenValidate_thenNotReported() {
        setConfig("enabled", "true");
        setConfig("disenchantment.anvil.sound.volume", "0,5");
        setConfig("event-priorities.disenchant", "high");

        assertEquals(List.of(), validate());
    }

    @Test
    void givenBadLocaleSettings_whenValidate_thenReported() {
        setConfig("locale", "../hack");
        setConfig("locales", List.of("cs", "bad/code"));

        List<ConfigIssue> issues = validate();

        assertTrue(hasIssueAt(issues, "locale"));
        assertTrue(issueAt(issues, "locales").message().contains("bad/code"));
    }

    @Test
    void givenMissingLocaleFile_whenValidate_thenWarnsAboutFallback() {
        setConfig("locale", "xx");

        assertTrue(issueAt(validate(), "locale").message().contains("falling back"));
    }

    @Test
    void givenNonUrlWebhook_whenValidate_thenReported() {
        setConfig("logging.discord-webhook", "not a url");

        assertTrue(hasIssueAt(validate(), "logging.discord-webhook"));

        setConfig("logging.discord-webhook", "https://discord.com/api/webhooks/1/x");
        assertFalse(hasIssueAt(validate(), "logging.discord-webhook"));
    }

    // ----------------------------------------------------------------------------------------------------
    // Restriction lists

    @Test
    void givenUnknownMode_whenValidate_thenReported() {
        setConfig("disenchantment.worlds.mode", "WHITELIST");

        assertTrue(issueAt(validate(), "disenchantment.worlds.mode").message().contains("using DENYLIST"));
    }

    @Test
    void givenAllowlistWithEmptyList_whenValidate_thenWarnedOnce() {
        setConfig("shatterment.materials.mode", "allowlist");

        List<ConfigIssue> issues = validate();

        assertEquals(1, issues.stream().filter(i -> i.path().equals("shatterment.materials")).count());
        assertTrue(issueAt(issues, "shatterment.materials").message().contains("blocks the feature entirely"));
    }

    @Test
    void givenUnknownMaterials_whenValidate_thenReportedAndKnownFormsAccepted() {
        setConfig("disenchantment.materials.list", List.of("DIAMOND_SWORD", "diamond_pickaxe", "minecraft:bow", "diamnd_sword", "LEGACY_WOOD_SWORD"));

        List<ConfigIssue> issues = validate().stream().filter(i -> i.path().equals("disenchantment.materials.list")).toList();

        assertEquals(2, issues.size(), issues.toString());
        assertTrue(issues.get(0).message().contains("diamnd_sword"));
        assertTrue(issues.get(1).message().contains("LEGACY_WOOD_SWORD"));
    }

    @Test
    void givenUnloadedWorld_whenValidate_thenPhrasedAsNotCurrentlyLoaded() {
        world();
        setConfig("shatterment.worlds.list", List.of("world", "nether_later"));

        List<ConfigIssue> issues = validate().stream().filter(i -> i.path().equals("shatterment.worlds.list")).toList();

        assertEquals(1, issues.size());
        assertTrue(issues.get(0).message().contains("\"nether_later\" is not currently loaded"));
    }

    @Test
    void givenNoWorldsLoadedYet_whenValidate_thenWorldNamesAreNotQuestioned() {
        assertTrue(server.getWorlds().isEmpty());
        setConfig("shatterment.worlds.list", List.of("whatever"));

        assertFalse(hasIssueAt(validate(), "shatterment.worlds.list"));
    }

    @Test
    void givenWrongShapesForRestrictions_whenValidate_thenReported() {
        setConfig("disenchantment.worlds", List.of("world"));
        setConfig("shatterment.materials.list", "BOW");
        setConfig("shatterment.worlds.list", List.of("a", List.of("nested"), 3));

        List<ConfigIssue> issues = validate();

        assertTrue(issueAt(issues, "disenchantment.worlds").message().contains("expected a section"));
        assertTrue(issueAt(issues, "shatterment.materials.list").message().contains("single entry"));
        assertTrue(issueAt(issues, "shatterment.worlds.list").message().contains("not plain text"));
    }

    @Test
    void givenDuplicateListEntries_whenValidate_thenReported() {
        setConfig("disenchantment.materials.list", List.of("BOW", "bow"));

        assertTrue(issueAt(validate(), "disenchantment.materials.list").message().contains("more than once"));
    }

    // ----------------------------------------------------------------------------------------------------
    // Enchantments

    @Test
    void givenMalformedStates_whenValidate_thenEachBadEntryIsReported() {
        setConfig("disenchantment.enchantments-states", List.of("sharpness:keep", "unbreaking", "minecraft:looting", "power:sometimes", "efficiency:keep:extra:more"));

        List<String> messages = validate().stream().filter(i -> i.path().equals("disenchantment.enchantments-states")).map(ConfigIssue::message).toList();

        assertEquals(4, messages.size(), messages.toString());
        assertTrue(messages.stream().anyMatch(m -> m.contains("\"unbreaking\" is not in")));
        assertTrue(messages.stream().anyMatch(m -> m.contains("\"minecraft:looting\"") && m.contains("unknown state")));
        assertTrue(messages.stream().anyMatch(m -> m.contains("unknown state \"sometimes\"")));
        assertTrue(messages.stream().anyMatch(m -> m.contains("efficiency:keep:extra:more")));
    }

    @Test
    void givenUnknownEnchantmentKeys_whenValidate_thenSoftWarningsEverywhereTheyAreUsed() {
        setConfig("disenchantment.enchantments-states", List.of("sharpnes:keep"));
        setConfig("shatterment.enchantment-chances.mendin", 0.5);
        setConfig("shatterment.anvil.repair.enchantment-costs.fortun", 5);
        setConfig("enchantment-groups.combat", List.of("sharpness", "smitee"));

        List<ConfigIssue> issues = validate();

        assertTrue(issueAt(issues, "disenchantment.enchantments-states").message().contains("unknown enchantment \"sharpnes\""));
        assertTrue(issueAt(issues, "shatterment.enchantment-chances").message().contains("\"mendin\""));
        assertTrue(issueAt(issues, "shatterment.anvil.repair.enchantment-costs").message().contains("\"fortun\""));
        assertTrue(issueAt(issues, "enchantment-groups.combat").message().contains("\"smitee\""));
        assertTrue(issueAt(issues, "enchantment-groups.combat").message().contains("custom enchantment plugin"));
    }

    @Test
    void givenKeysFromOtherNamespaces_whenValidate_thenTheyCannotBeVerifiedAndAreAccepted() {
        setConfig("disenchantment.enchantments-states", List.of("ecoenchants:telekinesis:keep"));
        setConfig("enchantment-groups.custom", List.of("someplugin:thing"));

        assertEquals(List.of(), validate());
    }

    @Test
    void givenBadChancesAndCosts_whenValidate_thenReported() {
        setConfig("disenchantment.enchantment-chances.sharpness", "half");
        setConfig("disenchantment.enchantment-chances.mending", 3);
        setConfig("disenchantment.anvil.repair.enchantment-costs.fortune", -2);
        setConfig("disenchantment.anvil.repair.enchantment-costs.power", "free");
        setConfig("disenchantment.anvil.repair.enchantment-costs.efficiency.economy", "lots");

        List<String> chances = validate().stream().filter(i -> i.path().equals("disenchantment.enchantment-chances")).map(ConfigIssue::message).toList();
        List<String> costs = validate().stream().filter(i -> i.path().equals("disenchantment.anvil.repair.enchantment-costs")).map(ConfigIssue::message).toList();

        assertEquals(2, chances.size(), chances.toString());
        assertTrue(chances.stream().anyMatch(m -> m.contains("\"sharpness\"") && m.contains("always succeeds")));
        assertTrue(chances.stream().anyMatch(m -> m.contains("\"mending\"") && m.contains("clamped to 1")));
        assertEquals(3, costs.size(), costs.toString());
    }

    @Test
    void givenNonMapChancesCostsAndGroups_whenValidate_thenShapeIsReported() {
        setConfig("shatterment.enchantment-chances", List.of("a"));
        setConfig("shatterment.anvil.repair.enchantment-costs", 4);
        setConfig("enchantment-groups", "combat");
        setConfig("disenchantment.enchantments-states", 4);

        List<ConfigIssue> issues = validate();

        assertTrue(issueAt(issues, "shatterment.enchantment-chances").message().contains("expected a section"));
        assertTrue(issueAt(issues, "shatterment.anvil.repair.enchantment-costs").message().contains("expected a section"));
        assertTrue(issueAt(issues, "enchantment-groups").message().contains("expected a section"));
        assertTrue(issueAt(issues, "disenchantment.enchantments-states").message().contains("expected a list"));
    }

    @Test
    void givenBrokenGroups_whenValidate_thenReported() {
        setConfig("enchantment-groups.fine", List.of("sharpness"));
        setConfig("enchantment-groups.empty", List.of());
        setConfig("enchantment-groups.broken", Map.of("a", "b"));

        List<ConfigIssue> issues = validate();

        assertTrue(issues.stream().anyMatch(i -> i.path().equals("enchantment-groups") && i.message().contains("group \"broken\" must be a list")));
        assertTrue(issues.stream().anyMatch(i -> i.path().equals("enchantment-groups") && i.message().contains("\"empty\" has no members")));
    }

    // ----------------------------------------------------------------------------------------------------
    // Split count

    @Test
    void givenBadSplitCounts_whenValidate_thenExplained() {
        setConfig("shatterment.split-count", "abc");
        assertTrue(issueAt(validate(), "shatterment.split-count").message().contains("using 1"));

        setConfig("shatterment.split-count", 0);
        assertTrue(issueAt(validate(), "shatterment.split-count").message().contains("between 1 and 255"));

        setConfig("shatterment.split-count", null);
        setConfig("shatterment.split-count.min", 5);
        setConfig("shatterment.split-count.max", 2);
        assertTrue(issueAt(validate(), "shatterment.split-count").message().contains("swapped"));

        setConfig("shatterment.split-count", 2);
        assertFalse(hasIssueAt(validate(), "shatterment.split-count"));
    }

    // ----------------------------------------------------------------------------------------------------
    // Logging + exposure

    @Test
    void givenProblems_whenValidateAndLog_thenIssuesAreExposedForDiagnostics() {
        setConfig("disenchantment.anvil.sound.volume", "loud");
        setConfig("disenchantmnet.enabled", false);

        List<ConfigIssue> issues = ConfigValidator.validateAndLog();

        assertEquals(2, issues.size(), issues.toString());
        assertEquals(issues, ConfigValidator.getLastIssues());
        assertThrows(UnsupportedOperationException.class, () -> ConfigValidator.getLastIssues().add(new ConfigIssue("x", "y")));
    }

    @Test
    void givenCleanConfig_whenValidateAndLog_thenNoIssuesAndNothingStale() {
        setConfig("disenchantment.anvil.sound.volume", "loud");
        assertFalse(ConfigValidator.validateAndLog().isEmpty());

        setConfig("disenchantment.anvil.sound.volume", 1.0);
        assertTrue(ConfigValidator.validateAndLog().isEmpty());
        assertTrue(ConfigValidator.getLastIssues().isEmpty());
    }

    @Test
    void givenIssue_whenFormat_thenPathAndMessage() {
        assertEquals("a.b: broken", new ConfigIssue("a.b", "broken").format());
    }

    @Test
    void givenEnabledPlugin_whenStartedWithDefaultConfig_thenValidatorRanAndFoundNothing() {
        // enable() calls ConfigValidator.validateAndLog(); the bundled config must always be clean.
        assertTrue(ConfigValidator.getLastIssues().isEmpty());
    }

    @Test
    void givenKnownAndUnknownKeys_whenIsKnownEnchantment_thenOnlyDefinitelyUnknownIsRejected() {
        assertTrue(ConfigValidator.isKnownEnchantment("sharpness"));
        assertTrue(ConfigValidator.isKnownEnchantment("MINECRAFT:Sharpness"));
        assertTrue(ConfigValidator.isKnownEnchantment("plugin:custom"));
        assertFalse(ConfigValidator.isKnownEnchantment("sharpnes"));
        assertFalse(ConfigValidator.isKnownEnchantment("has space"));
        assertFalse(ConfigValidator.isKnownEnchantment(null));
    }

    @Test
    void givenCustomRecipeSymbol_whenValidate_thenNotReportedAsUnknown() {
        setConfig("special-anvils.recipe.ingredients.I", "IRON_INGOT");

        assertTrue(validate().stream().noneMatch(issue -> issue.path().startsWith("special-anvils.recipe.ingredients")),
                "User-added recipe symbols are used by the recipe and must not be flagged");
    }

    @Test
    void givenEraseDisabledAndUnknownCatalyst_whenValidate_thenNotReported() {
        setConfig("erase.enabled", false);
        setConfig("erase.catalysts", List.of("NOT_A_MATERIAL"));

        assertTrue(validate().stream().noneMatch(issue -> issue.path().startsWith("erase.catalysts")),
                "Catalysts are only checked while erase is enabled (ECHO_SHARD is missing before 1.19)");
    }
}
