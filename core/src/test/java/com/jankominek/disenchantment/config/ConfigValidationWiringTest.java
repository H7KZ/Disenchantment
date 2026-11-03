package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.commands.impl.DisenchantMaterials;
import com.jankominek.disenchantment.commands.impl.Reload;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The validator runs on reload, its result reaches the reload sender and the diagnostic report, and
 * material parsing in commands accepts the same forms as the config.
 */
class ConfigValidationWiringTest extends DisenchantmentTestBase {

    private void editConfigFile(String from, String to) throws IOException {
        File file = new File(plugin.getDataFolder(), "config.yml");
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.contains(from), "template no longer contains: " + from);
        Files.writeString(file.toPath(), content.replace(from, to), StandardCharsets.UTF_8);
    }

    @Test
    void givenBadValueOnDisk_whenReload_thenValidatorRunsAndSenderIsTold() throws IOException {
        editConfigFile("level: INFO", "level: LOUD");
        PlayerMock admin = server.addPlayer("Admin");

        Reload.execute(admin, new String[]{"reload"});

        List<ConfigIssue> issues = ConfigValidator.getLastIssues();
        assertEquals(1, issues.size(), issues.toString());
        assertEquals("logging.level", issues.get(0).path());
        assertNotNull(admin.nextMessage(), "reload confirmation");
        String warning = admin.nextMessage();
        assertNotNull(warning);
        assertTrue(warning.contains("1 config problem found"), warning);
    }

    @Test
    void givenCleanConfig_whenReload_thenNoExtraMessageAndIssuesCleared() throws IOException {
        editConfigFile("level: INFO", "level: LOUD");
        PlayerMock admin = server.addPlayer("Admin");
        Reload.execute(admin, new String[]{"reload"});
        assertFalse(ConfigValidator.getLastIssues().isEmpty());

        editConfigFile("level: LOUD", "level: INFO");
        PlayerMock second = server.addPlayer("Second");
        Reload.execute(second, new String[]{"reload"});

        assertTrue(ConfigValidator.getLastIssues().isEmpty());
        assertNotNull(second.nextMessage());
        assertNull(second.nextMessage());
    }

    @Test
    void givenIssues_whenDiagnosticReport_thenListedUnderPluginConfiguration() throws IOException {
        editConfigFile("level: INFO", "level: LOUD");
        plugin.reload();

        String report = DiagnosticUtils.getReport(false, null);

        assertTrue(report.contains("Config Problems: 1"), report);
        assertTrue(report.contains("logging.level: unknown value"), report);
    }

    @Test
    void givenCleanConfig_whenDiagnosticReport_thenNoProblemsLine() {
        plugin.reload();

        assertTrue(DiagnosticUtils.getReport(false, null).contains("Config Problems: None"));
    }

    @Test
    void givenMaterialNameInAnyForm_whenToggledByCommand_thenResolvedWithMatchMaterial() {
        PlayerMock admin = server.addPlayer("Admin");

        DisenchantMaterials.execute(admin, new String[]{"disenchant:materials", "MINECRAFT:Bow"});
        assertEquals(List.of(Material.BOW), Config.Disenchantment.getDisabledMaterials());

        DisenchantMaterials.execute(admin, new String[]{"disenchant:materials", "bow"});
        assertTrue(Config.Disenchantment.getDisabledMaterials().isEmpty());
    }

    @Test
    void givenUnknownMaterial_whenToggledByCommand_thenInvalidArgumentAndNoChange() {
        PlayerMock admin = server.addPlayer("Admin");

        DisenchantMaterials.execute(admin, new String[]{"disenchant:materials", "diamnd_sword"});

        assertEquals(I18n.Messages.invalidArgument(), admin.nextMessage());
        assertTrue(Config.Disenchantment.getDisabledMaterials().isEmpty());
    }

    @Test
    void givenHandWrittenUnknownMaterial_whenAnotherIsToggledByCommand_thenTheUnknownEntryIsKept() {
        setConfig("disenchantment.materials.list", List.of("MODDED_THING"));
        PlayerMock admin = server.addPlayer("Admin");

        DisenchantMaterials.execute(admin, new String[]{"disenchant:materials", "bow"});

        assertEquals(List.of("BOW", "MODDED_THING"), com.jankominek.disenchantment.Disenchantment.config.getStringList("disenchantment.materials.list"));
    }

    @Test
    void givenWorldNotLoadedAtEnable_whenServerLoadEvent_thenWorldIssueReported() {
        world();
        setConfig("shatterment.worlds.list", List.of("world", "loaded_by_multiverse_later"));
        ConfigValidator.validateAndLog(false);
        assertTrue(ConfigValidator.getLastIssues().stream().noneMatch(i -> i.path().equals("shatterment.worlds.list")));

        server.getPluginManager().callEvent(new org.bukkit.event.server.ServerLoadEvent(org.bukkit.event.server.ServerLoadEvent.LoadType.STARTUP));

        assertTrue(ConfigValidator.getLastIssues().stream().anyMatch(i -> i.path().equals("shatterment.worlds.list")),
                ConfigValidator.getLastIssues().toString());
    }
}
