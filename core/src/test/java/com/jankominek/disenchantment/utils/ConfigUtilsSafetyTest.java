package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.commands.impl.Reload;
import com.jankominek.disenchantment.config.Config;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies a YAML syntax error in config.yml never results in the user's file being overwritten,
 * and that migrations leave a backup behind.
 */
class ConfigUtilsSafetyTest extends DisenchantmentTestBase {

    private static final String BROKEN_YAML = "enabled: [unclosed\nlocale: \"en\"\n  bad: : indent\n";

    private File configFile() {
        return new File(plugin.getDataFolder(), "config.yml");
    }

    private void write(String content) throws IOException {
        Files.writeString(configFile().toPath(), content, StandardCharsets.UTF_8);
    }

    private String read() throws IOException {
        return Files.readString(configFile().toPath(), StandardCharsets.UTF_8);
    }

    private File[] brokenCopies() {
        File[] found = plugin.getDataFolder().listFiles((dir, name) -> name.startsWith("config.yml.broken-"));
        return found == null ? new File[0] : found;
    }

    // -> syntax error handling

    @Test
    void givenBrokenYaml_whenSetupConfig_thenFileIsNotOverwritten() throws IOException {
        write(BROKEN_YAML);

        boolean loaded = ConfigUtils.setupConfig();

        assertFalse(loaded, "setupConfig must report that the on-disk config was not loaded");
        assertEquals(BROKEN_YAML, read(), "A broken config.yml must never be rewritten");
    }

    @Test
    void givenBrokenYaml_whenSetupConfig_thenBrokenCopyIsSaved() throws IOException {
        write(BROKEN_YAML);

        ConfigUtils.setupConfig();

        File[] copies = brokenCopies();
        assertEquals(1, copies.length, "Exactly one config.yml.broken-<timestamp> copy expected");
        assertEquals(BROKEN_YAML, Files.readString(copies[0].toPath(), StandardCharsets.UTF_8));
    }

    @Test
    void givenBrokenYaml_whenSetupConfig_thenBundledDefaultsAreUsed() throws IOException {
        write(BROKEN_YAML);

        ConfigUtils.setupConfig();

        assertTrue(Config.isPluginEnabled(), "Defaults must be active in memory");
        assertEquals("en", Config.getLocale());
        assertTrue(Config.Disenchantment.isEnabled());
        assertTrue(Disenchantment.config.getInt("migration") >= 12);
        assertEquals(10.0, Config.Disenchantment.Anvil.Repair.getBaseCost());
    }

    @Test
    void givenBrokenYaml_whenConfigSetter_thenDiskIsStillUntouched() throws IOException {
        write(BROKEN_YAML);
        ConfigUtils.setupConfig();

        assertTrue(Config.isReadOnly());
        Config.Disenchantment.setEnabled(false);

        assertEquals(BROKEN_YAML, read(), "In-game changes must not overwrite the broken file");
    }

    @Test
    void givenBrokenYamlThenFixed_whenSetupConfigAgain_thenReadOnlyIsLifted() throws IOException {
        write(BROKEN_YAML);
        ConfigUtils.setupConfig();
        assertTrue(Config.isReadOnly());

        write(new String(Objects.requireNonNull(plugin.getResource("config.yml")).readAllBytes(), StandardCharsets.UTF_8));
        boolean loaded = ConfigUtils.setupConfig();

        assertTrue(loaded);
        assertFalse(Config.isReadOnly());
    }

    // -> validateConfigFile

    @Test
    void givenValidConfig_whenValidate_thenNull() {
        assertNull(ConfigUtils.validateConfigFile());
    }

    @Test
    void givenBrokenConfig_whenValidate_thenParserMessage() throws IOException {
        write(BROKEN_YAML);

        String error = ConfigUtils.validateConfigFile();

        assertNotNull(error);
        assertFalse(error.isBlank());
    }

    // -> reload command

    @Test
    void givenBrokenConfig_whenReloadCommand_thenSenderGetsFailureAndConfigUntouched() throws IOException {
        PlayerMock admin = server.addPlayer("Admin");
        admin.addAttachment(plugin, "disenchantment.command.reload", true);
        Config.Disenchantment.setEnabled(false);
        assertFalse(Config.Disenchantment.isEnabled());
        String brokenFromRunningState = BROKEN_YAML;
        write(brokenFromRunningState);

        Reload.execute(admin, new String[]{"reload"});

        assertEquals(brokenFromRunningState, read(), "Reload must not overwrite a broken config");
        assertEquals(0, brokenCopies().length, "Pre-check must not touch the filesystem");
        assertFalse(Config.Disenchantment.isEnabled(), "Running settings must be kept when reload is refused");
        String msg = admin.nextMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("syntax error"), "Sender must be told the reload failed, got: " + msg);
    }

    // -> migration backup

    @Test
    void givenOutdatedConfig_whenSetupConfig_thenBackupCreatedAndMigrated() throws IOException {
        String old = "migration: 11\nenabled: false\nlocale: \"cs\"\n";
        write(old);

        boolean loaded = ConfigUtils.setupConfig();

        assertTrue(loaded);
        File bak = new File(plugin.getDataFolder(), "config.yml.bak");
        assertTrue(bak.exists(), "config.yml.bak must be written before migrated config is saved");
        assertEquals(old, Files.readString(bak.toPath(), StandardCharsets.UTF_8));
        assertFalse(Config.isPluginEnabled(), "User value must survive the migration");
        assertEquals("cs", Config.getLocale());
        assertNotEquals(old, read(), "Migrated config must be saved");
    }

    @Test
    void givenCurrentConfig_whenSetupConfig_thenNoBackupAndFileUntouched() throws IOException {
        File bak = new File(plugin.getDataFolder(), "config.yml.bak");
        Files.deleteIfExists(bak.toPath());
        String before = read();

        ConfigUtils.setupConfig();

        assertFalse(bak.exists(), "No backup when nothing was migrated");
        assertEquals(before, read());
    }
}
