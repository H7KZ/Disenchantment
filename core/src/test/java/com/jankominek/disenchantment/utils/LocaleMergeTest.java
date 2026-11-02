package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies existing locale files receive keys added by plugin updates without losing user edits.
 */
class LocaleMergeTest extends DisenchantmentTestBase {

    private File localeFile(String locale) {
        return new File(plugin.getDataFolder(), "locales/" + locale + ".yml");
    }

    private YamlConfiguration bundled(String locale) {
        return YamlConfiguration.loadConfiguration(new InputStreamReader(
                Objects.requireNonNull(plugin.getResource("locales/" + locale + ".yml")), StandardCharsets.UTF_8));
    }

    private Set<String> leafKeys(YamlConfiguration yaml) {
        Set<String> keys = new TreeSet<>();
        for (String key : yaml.getKeys(true)) if (!yaml.isConfigurationSection(key)) keys.add(key);
        return keys;
    }

    // -> merge behaviour

    @Test
    void givenOldLocaleMissingKeys_whenSetup_thenMissingKeysAddedAndUserValuesKept() throws IOException {
        YamlConfiguration old = new YamlConfiguration();
        old.set("prefix", "&c[Custom]&r");
        old.set("messages.invalid-argument", "&eMy own wording&r");
        old.save(localeFile("en"));

        ConfigUtils.setupLocaleConfigs();

        YamlConfiguration merged = YamlConfiguration.loadConfiguration(localeFile("en"));
        YamlConfiguration bundledEn = bundled("en");
        assertEquals("&c[Custom]&r", merged.getString("prefix"), "User-edited value must be preserved");
        assertEquals("&eMy own wording&r", merged.getString("messages.invalid-argument"));
        assertEquals(bundledEn.getString("messages.requires-permission"), merged.getString("messages.requires-permission"),
                "Key missing from the user's file must be filled from the bundled locale");
        assertEquals(leafKeys(bundledEn), leafKeys(merged));
    }

    @Test
    void givenCompleteLocale_whenSetup_thenFileIsNotRewritten() throws IOException {
        ConfigUtils.setupLocaleConfigs();
        String before = Files.readString(localeFile("cs").toPath(), StandardCharsets.UTF_8);
        long modified = localeFile("cs").lastModified();

        int added = ConfigUtils.mergeMissingLocaleKeys("cs", localeFile("cs"));

        assertEquals(0, added);
        assertEquals(before, Files.readString(localeFile("cs").toPath(), StandardCharsets.UTF_8));
        assertEquals(modified, localeFile("cs").lastModified());
    }

    @Test
    void givenUnparseableLocale_whenSetup_thenLeftUntouched() throws IOException {
        String broken = "prefix: [unclosed\n";
        Files.writeString(localeFile("en").toPath(), broken, StandardCharsets.UTF_8);

        int added = ConfigUtils.mergeMissingLocaleKeys("en", localeFile("en"));

        assertEquals(0, added);
        assertEquals(broken, Files.readString(localeFile("en").toPath(), StandardCharsets.UTF_8));
    }

    // -> bundled locales stay in sync

    @Test
    void bundledLocalesDefineTheSameKeys() {
        assertEquals(leafKeys(bundled("en")), leafKeys(bundled("cs")),
                "en.yml and cs.yml must define identical key sets");
    }
}
