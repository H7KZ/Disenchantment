package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.anvils.SpecialAnvilRecipe;
import com.jankominek.disenchantment.types.ParticleScopeType;
import com.jankominek.disenchantment.types.SpecialAnvilMode;
import com.jankominek.disenchantment.utils.EraseUtils;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Totality and validation of the feedback, erase, special-anvils and grindstone sections.
 */
class ConfigNewSectionsTest extends DisenchantmentTestBase {

    private List<ConfigIssue> validate() {
        return ConfigValidator.validate(Disenchantment.config, null);
    }

    private List<ConfigIssue> issuesAt(String path) {
        return validate().stream().filter(i -> i.path().equals(path)).toList();
    }

    // ----------------------------------------------------------------------------------------------------
    // Totality

    @ParameterizedTest
    @MethodSource("com.jankominek.disenchantment.config.ConfigTotalityTest#junk")
    void givenJunkEverywhere_whenCallingTheNewGetters_thenNothingThrowsAndValuesAreValid(Object junk) {
        for (ConfigSpec spec : ConfigSpec.all()) setConfig(spec.key().getKey(), junk);
        for (String path : List.of(
                "feedback.particles.types", "feedback.particles.erase-types", "erase.catalysts",
                "erase.anvil.repair.enchantment-costs", "grindstone.repair.enchantment-costs", "grindstone.transfer.repair.enchantment-costs",
                "special-anvils.block-below", "special-anvils.item.lore", "special-anvils.recipe.shape", "special-anvils.recipe.ingredients")) {
            setConfig(path, junk);
        }

        assertDoesNotThrow(() -> {
            Config.Feedback.isNotEnoughXpMessageEnabled();
            Config.Feedback.isPreviewLoreEnabled();
            Config.Feedback.Particles.isEnabled();
            Config.Feedback.Particles.getTypes();
            Config.Feedback.Particles.getEraseTypes();
            Config.Feedback.Particles.getScope();
            Config.Feedback.FailureSound.isEnabled();

            Config.Erase.isEnabled();
            Config.Erase.getCatalysts();
            EraseUtils.getCatalysts();
            Config.Erase.isConsumeCatalyst();
            Config.Erase.isEraseCurses();
            Config.Erase.getPrepareEventPriority();
            Config.Erase.getClickEventPriority();
            Config.Erase.Economy.getCost();
            Config.Erase.Anvil.Sound.getVolume();
            Config.Erase.Anvil.Repair.getEnchantmentCosts();
            Config.Erase.Anvil.Repair.getEnchantmentEconomyCosts();

            Config.SpecialAnvils.getMode();
            Config.SpecialAnvils.getBlockBelow();
            Config.SpecialAnvils.getItemName();
            Config.SpecialAnvils.getItemLore();
            Config.SpecialAnvils.getRecipeShape();
            Config.SpecialAnvils.getRecipeIngredients();
            SpecialAnvilRecipe.validate();

            Config.Grindstone.isEnabled();
            Config.Grindstone.getPrepareEventPriority();
            Config.Grindstone.getClickEventPriority();
            Config.Grindstone.Sound.getVolume();
            Config.Grindstone.Repair.getBaseCost();
            Config.Grindstone.Repair.getEnchantmentCosts();
            Config.Grindstone.Repair.getEnchantmentEconomyCosts();
            Config.Grindstone.Economy.getCost();
            Config.Grindstone.Transfer.Repair.getEnchantmentCosts();
            Config.Grindstone.Transfer.Economy.getCost();
        });

        int particles = Config.Feedback.Particles.getCount();
        assertTrue(particles >= 1 && particles <= 500);
        assertTrue(Config.Feedback.Particles.getSpread() >= 0.0 && Double.isFinite(Config.Feedback.Particles.getSpread()));
        float volume = Config.Feedback.FailureSound.getVolume();
        float pitch = Config.Feedback.FailureSound.getPitch();
        assertTrue(Float.isFinite(volume) && volume >= 0f);
        assertTrue(pitch >= 0.5f && pitch <= 2.0f);
        assertTrue(Config.Feedback.FailureSound.getSound().matches(ConfigSpec.SOUND_KEY_PATTERN));
        int max = Config.Grindstone.Transfer.getMaxEnchantments();
        assertTrue(max >= 1 && max <= 64);
        assertNotNull(Config.SpecialAnvils.getBlockBelow());
        assertNotNull(Config.SpecialAnvils.getMode());
        assertNotNull(Config.SpecialAnvils.getItemName());
        assertNotNull(Config.Feedback.Particles.getScope());
        assertDoesNotThrow(() -> validate());
    }

    static Stream<Arguments> counts() {
        return Stream.of(Arguments.of(0, 1), Arguments.of(-5, 1), Arguments.of(30, 30), Arguments.of(9999, 500),
                Arguments.of("12", 12), Arguments.of("many", 30), Arguments.of(Double.NaN, 30), Arguments.of(List.of(1), 30));
    }

    @ParameterizedTest
    @MethodSource("counts")
    void givenParticleCount_whenRead_thenWithinOneAndFiveHundred(Object configured, int expected) {
        setConfig("feedback.particles.count", configured);

        assertEquals(expected, Config.Feedback.Particles.getCount());
    }

    @Test
    void givenSpreadAndScope_whenBroken_thenSafeDefaults() {
        setConfig("feedback.particles.spread", -3);
        assertEquals(0.0, Config.Feedback.Particles.getSpread());
        setConfig("feedback.particles.spread", Double.NaN);
        assertEquals(0.4, Config.Feedback.Particles.getSpread());
        setConfig("feedback.particles.spread", 999);
        assertEquals(16.0, Config.Feedback.Particles.getSpread());

        setConfig("feedback.particles.scope", " world ");
        assertEquals(ParticleScopeType.WORLD, Config.Feedback.Particles.getScope());
        setConfig("feedback.particles.scope", "everyone");
        assertEquals(ParticleScopeType.PLAYER, Config.Feedback.Particles.getScope());
        setConfig("feedback.particles.scope", List.of("WORLD"));
        assertEquals(ParticleScopeType.PLAYER, Config.Feedback.Particles.getScope());
    }

    @Test
    void givenParticleLists_whenBroken_thenFallbackOrEmptyAsConfigured() {
        setConfig("feedback.particles.types", 12.5);
        assertEquals(List.of("12.5"), Config.Feedback.Particles.getTypes());

        setConfig("feedback.particles.types", Map.of("a", "b"));
        assertEquals(List.of("ENCHANT", "ENCHANTMENT_TABLE"), Config.Feedback.Particles.getTypes());

        setConfig("feedback.particles.types", List.of());
        assertEquals(List.of(), Config.Feedback.Particles.getTypes(), "an explicit empty list switches the particles off");

        setConfig("feedback.particles.erase-types", "SMOKE");
        assertEquals(List.of("SMOKE"), Config.Feedback.Particles.getEraseTypes());
    }

    @Test
    void givenFailureSound_whenBroken_thenDefaultKeyAndClampedVolume() {
        setConfig("feedback.failure-sound.sound", "ENTITY_VILLAGER_NO");
        assertEquals("entity.villager.no", Config.Feedback.FailureSound.getSound());
        setConfig("feedback.failure-sound.sound", "  block.anvil.land ");
        assertEquals("block.anvil.land", Config.Feedback.FailureSound.getSound());
        setConfig("feedback.failure-sound.sound", List.of("x"));
        assertEquals("entity.villager.no", Config.Feedback.FailureSound.getSound());

        setConfig("feedback.failure-sound.volume", -2);
        setConfig("feedback.failure-sound.pitch", 9);
        assertEquals(0f, Config.Feedback.FailureSound.getVolume());
        assertEquals(2f, Config.Feedback.FailureSound.getPitch());
    }

    @Test
    void givenEraseCatalysts_whenOfEveryShape_thenResolvedWithMatchMaterial() {
        setConfig("erase.catalysts", List.of("echo_shard", "MINECRAFT:Amethyst_Shard", "BOOK", "nope", "ENCHANTED_BOOK"));
        EraseUtils.clearCache();
        assertEquals(java.util.Set.of(Material.ECHO_SHARD, Material.AMETHYST_SHARD), EraseUtils.getCatalysts());

        setConfig("erase.catalysts", "diamond");
        EraseUtils.clearCache();
        assertEquals(java.util.Set.of(Material.DIAMOND), EraseUtils.getCatalysts());

        setConfig("erase.catalysts", Map.of("a", "b"));
        EraseUtils.clearCache();
        assertEquals(java.util.Set.of(Material.ECHO_SHARD), EraseUtils.getCatalysts());
    }

    @Test
    void givenEraseCostsAndPriorities_whenBroken_thenSkippedAndDefaulted() {
        setConfig("erase.anvil.repair.enchantment-costs.sharpness", "x");
        setConfig("erase.anvil.repair.enchantment-costs.Mending", 4);
        setConfig("erase.anvil.repair.enchantment-costs.power.economy", -3);
        setConfig("event-priorities.erase", "low");
        setConfig("event-priorities.erase-click", "nonsense");

        assertEquals(Map.of("mending", 4), Config.Erase.Anvil.Repair.getEnchantmentCosts());
        assertEquals(Map.of("power", 0.0), Config.Erase.Anvil.Repair.getEnchantmentEconomyCosts());
        assertEquals(EventPriority.LOW, Config.Erase.getPrepareEventPriority());
        assertEquals(EventPriority.HIGHEST, Config.Erase.getClickEventPriority());
    }

    @Test
    void givenSpecialAnvilValues_whenBroken_thenDefaults() {
        setConfig("special-anvils.mode", List.of("MARKED"));
        assertEquals(SpecialAnvilMode.ALL, Config.SpecialAnvils.getMode());

        setConfig("special-anvils.block-below", "MINECRAFT:EMERALD_BLOCK");
        assertEquals(Material.EMERALD_BLOCK, Config.SpecialAnvils.getBlockBelow());
        setConfig("special-anvils.block-below", "diamond");
        assertEquals(Material.LAPIS_BLOCK, Config.SpecialAnvils.getBlockBelow());
        setConfig("special-anvils.block-below", 12);
        assertEquals(Material.LAPIS_BLOCK, Config.SpecialAnvils.getBlockBelow());

        setConfig("special-anvils.item.name", List.of("a"));
        assertEquals("&dEnchanter's Anvil", Config.SpecialAnvils.getItemName());
        setConfig("special-anvils.item.name", "");
        assertEquals("&dEnchanter's Anvil", Config.SpecialAnvils.getItemName());

        setConfig("special-anvils.recipe.ingredients.X", Map.of("a", "b"));
        assertFalse(Config.SpecialAnvils.getRecipeIngredients().containsKey('X'));
    }

    @Test
    void givenGrindstoneValues_whenBroken_thenClamped() {
        setConfig("grindstone.transfer.max-enchantments", 0);
        assertEquals(1, Config.Grindstone.Transfer.getMaxEnchantments());
        setConfig("grindstone.transfer.max-enchantments", 1000);
        assertEquals(64, Config.Grindstone.Transfer.getMaxEnchantments());
        setConfig("grindstone.transfer.max-enchantments", "lots");
        assertEquals(16, Config.Grindstone.Transfer.getMaxEnchantments());

        setConfig("grindstone.transfer.repair.base", "x");
        assertEquals(15.0, Config.Grindstone.Transfer.Repair.getBaseCost());
        setConfig("grindstone.repair.max-cost", -50);
        assertEquals(-1, Config.Grindstone.Repair.getMaxCost());
        setConfig("grindstone.economy.cost", -1);
        assertEquals(0.0, Config.Grindstone.Economy.getCost());
        setConfig("grindstone.sound.pitch", 0);
        assertEquals(0.5, Config.Grindstone.Sound.getPitch());

        setConfig("grindstone.transfer.repair.enchantment-costs.Fortune", 3);
        setConfig("grindstone.transfer.repair.enchantment-costs.power", "free");
        assertEquals(Map.of("fortune", 3), Config.Grindstone.Transfer.Repair.getEnchantmentCosts());
    }

    // ----------------------------------------------------------------------------------------------------
    // Validator: each problem exactly once

    @Test
    void givenBundledDefaults_whenValidatingNewSections_thenNoIssues() {
        assertEquals(List.of(), validate());
    }

    @Test
    void givenBadScalarsInNewSections_whenValidate_thenOneIssueEach() {
        setConfig("feedback.particles.count", 0);
        setConfig("feedback.particles.scope", "EVERYONE");
        setConfig("feedback.failure-sound.pitch", 7);
        setConfig("erase.anvil.sound.volume", "loud");
        setConfig("grindstone.transfer.max-enchantments", 500);
        setConfig("event-priorities.grindstone", "SOMETIME");
        setConfig("special-anvils.mode", "MARKD");

        for (String path : List.of("feedback.particles.count", "feedback.particles.scope", "feedback.failure-sound.pitch",
                "erase.anvil.sound.volume", "grindstone.transfer.max-enchantments", "event-priorities.grindstone", "special-anvils.mode")) {
            assertEquals(1, issuesAt(path).size(), path + " -> " + validate());
        }
        assertTrue(issuesAt("special-anvils.mode").get(0).message().contains("valid: ALL, MARKED, BLOCK_BELOW"));
    }

    @Test
    void givenBadEraseCatalysts_whenValidate_thenEachReportedOnce() {
        setConfig("erase.enabled", true);
        setConfig("erase.catalysts", List.of("BOOK", "not_a_material", "minecraft:echo_shard"));

        List<String> messages = issuesAt("erase.catalysts").stream().map(ConfigIssue::message).toList();

        assertEquals(2, messages.size(), messages.toString());
        assertTrue(messages.stream().anyMatch(m -> m.contains("unknown material \"not_a_material\"")));
        assertTrue(messages.stream().anyMatch(m -> m.contains("BOOK cannot be a catalyst")));
    }

    @Test
    void givenNoUsableCatalyst_whenEraseEnabled_thenDisabledWarningOnlyWhenEnabled() {
        setConfig("erase.catalysts", List.of("BOOK"));
        assertFalse(issuesAt("erase.catalysts").stream().anyMatch(i -> i.message().contains("erase mode is disabled")));

        setConfig("erase.enabled", true);
        assertEquals(1, issuesAt("erase.catalysts").stream().filter(i -> i.message().contains("erase mode is disabled")).count());

        setConfig("erase.catalysts", Map.of("a", "b"));
        assertTrue(issuesAt("erase.catalysts").get(0).message().contains("expected a list of materials"));
    }

    @Test
    void givenBadBlockBelow_whenValidate_thenReportedOnce() {
        setConfig("special-anvils.block-below", "DIAMOND");
        assertEquals(1, issuesAt("special-anvils.block-below").size());
        assertTrue(issuesAt("special-anvils.block-below").get(0).message().contains("using LAPIS_BLOCK"));

        setConfig("special-anvils.block-below", "");
        assertEquals(0, issuesAt("special-anvils.block-below").size());

        setConfig("special-anvils.block-below", List.of("x"));
        assertEquals(1, issuesAt("special-anvils.block-below").size());
    }

    @Test
    void givenInvalidRecipe_whenValidate_thenReportedOnlyWhenEnabled() {
        setConfig("special-anvils.recipe.shape", List.of("EEE", "EA"));
        assertEquals(0, issuesAt("special-anvils.recipe").size(), "recipe disabled: nothing to report");

        setConfig("special-anvils.recipe.enabled", true);
        assertEquals(1, issuesAt("special-anvils.recipe").size());
        assertTrue(issuesAt("special-anvils.recipe").get(0).message().contains("same length"));
        assertFalse(SpecialAnvilRecipe.validate().isEmpty());

        setConfig("special-anvils.recipe.shape", List.of("EEE", "EAE", "EEE"));
        setConfig("special-anvils.recipe.ingredients.E", "EMERALDD");
        List<String> messages = issuesAt("special-anvils.recipe").stream().map(ConfigIssue::message).toList();
        assertEquals(1, messages.size(), messages.toString());
        assertTrue(messages.get(0).contains("'E'") && messages.get(0).contains("EMERALDD"));

        setConfig("special-anvils.recipe.ingredients.E", "minecraft:emerald");
        assertEquals(0, issuesAt("special-anvils.recipe").size());
    }

    @Test
    void givenParticleListsWithNoUsableName_whenValidate_thenReportedOnce() {
        setConfig("feedback.particles.types", List.of("NOT_A_PARTICLE", "ALSO_NOT"));
        assertEquals(1, issuesAt("feedback.particles.types").size());

        setConfig("feedback.particles.types", List.of("NOT_A_PARTICLE", "FLAME"));
        assertEquals(0, issuesAt("feedback.particles.types").size(), "one resolvable name is enough (legacy + current names)");

        setConfig("feedback.particles.types", List.of());
        assertEquals(0, issuesAt("feedback.particles.types").size());

        setConfig("feedback.particles.erase-types", Map.of("a", "b"));
        assertEquals(1, issuesAt("feedback.particles.erase-types").size());
    }

    @Test
    void givenBadCostsInNewSections_whenValidate_thenReportedAtTheirOwnPath() {
        setConfig("erase.anvil.repair.enchantment-costs.sharpnes", 3);
        setConfig("grindstone.repair.enchantment-costs.mending", "free");
        setConfig("grindstone.transfer.repair.enchantment-costs", 4);

        assertEquals(1, issuesAt("erase.anvil.repair.enchantment-costs").size());
        assertTrue(issuesAt("erase.anvil.repair.enchantment-costs").get(0).message().contains("\"sharpnes\""));
        assertEquals(1, issuesAt("grindstone.repair.enchantment-costs").size());
        assertTrue(issuesAt("grindstone.transfer.repair.enchantment-costs").get(0).message().contains("expected a section"));
    }

    @Test
    void givenTypoInNewSections_whenValidateWithTemplate_thenSuggested() {
        setConfig("grindstone.transfr.enabled", true);
        setConfig("special-anvils.recipe.shap", List.of("E"));
        setConfig("feedback.particles.cont", 4);
        setConfig("erase.catalysts2", List.of());

        org.bukkit.configuration.file.YamlConfiguration template = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                new java.io.InputStreamReader(plugin.getResource("config.yml"), java.nio.charset.StandardCharsets.UTF_8));
        List<ConfigIssue> issues = ConfigValidator.validate(Disenchantment.config, template);

        assertTrue(issues.stream().anyMatch(i -> i.path().equals("grindstone.transfr") && i.message().contains("\"grindstone.transfer\"")), issues.toString());
        assertTrue(issues.stream().anyMatch(i -> i.path().equals("special-anvils.recipe.shap") && i.message().contains("\"special-anvils.recipe.shape\"")));
        assertTrue(issues.stream().anyMatch(i -> i.path().equals("feedback.particles.cont") && i.message().contains("count")));
        assertTrue(issues.stream().anyMatch(i -> i.path().equals("erase.catalysts2")));
    }
}
