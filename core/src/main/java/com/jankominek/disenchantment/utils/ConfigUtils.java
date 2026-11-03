package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.migration.ConfigMigrations;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import static com.jankominek.disenchantment.Disenchantment.logger;
import static com.jankominek.disenchantment.Disenchantment.plugin;

/**
 * Utility class for managing plugin configuration files, including setup,
 * migration, and locale file extraction.
 */
public class ConfigUtils {
    /**
     * Copies values from an old configuration to a new template for each key present in both.
     * Handles all YAML-supported value types (string, int, boolean, double, list, etc.).
     *
     * @param keys           the set of keys to copy
     * @param oldConfig      the source configuration
     * @param configTemplate the target configuration template
     */
    public static void copyKeys(Set<String> keys, FileConfiguration oldConfig, FileConfiguration configTemplate) {
        for (String key : keys) {
            if (!oldConfig.contains(key)) continue;

            if (oldConfig.isString(key)) configTemplate.set(key, oldConfig.getString(key));
            else if (oldConfig.isInt(key)) configTemplate.set(key, oldConfig.getInt(key));
            else if (oldConfig.isBoolean(key)) configTemplate.set(key, oldConfig.getBoolean(key));
            else if (oldConfig.isDouble(key)) configTemplate.set(key, oldConfig.getDouble(key));
            else if (oldConfig.isLong(key)) configTemplate.set(key, oldConfig.getLong(key));
            else if (oldConfig.isList(key)) configTemplate.set(key, oldConfig.getList(key));
            else if (oldConfig.isVector(key)) configTemplate.set(key, oldConfig.getVector(key));
            else if (oldConfig.isOfflinePlayer(key)) configTemplate.set(key, oldConfig.getOfflinePlayer(key));
            else if (oldConfig.isItemStack(key)) configTemplate.set(key, oldConfig.getItemStack(key));
            else if (oldConfig.isColor(key)) configTemplate.set(key, oldConfig.getColor(key));
            else if (oldConfig.isLocation(key)) configTemplate.set(key, oldConfig.getLocation(key));
            else if (oldConfig.isConfigurationSection(key))
                configTemplate.set(key, oldConfig.getConfigurationSection(key));
        }
    }

    /**
     * Strictly parses {@code config.yml} on disk without touching it. Unlike
     * {@link YamlConfiguration#loadConfiguration(File)}, a syntax error is reported instead of being
     * swallowed into an empty configuration.
     *
     * @return {@code null} if the file is missing or parses cleanly, otherwise the parser's error message
     */
    public static String validateConfigFile() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) return null;

        try {
            new YamlConfiguration().load(configFile);
            return null;
        } catch (InvalidConfigurationException | IOException e) {
            return e.getMessage() == null ? e.toString() : e.getMessage();
        }
    }

    /**
     * Initializes the plugin configuration. Saves the default config if not present, strictly parses
     * the existing file, applies pending migrations (writing a {@code config.yml.bak} backup first),
     * saves the result and reloads the configuration.
     *
     * <p>If {@code config.yml} contains a YAML syntax error the file is never overwritten: it is copied to
     * {@code config.yml.broken-<timestamp>}, the failure is logged at SEVERE, and the bundled defaults are
     * used in memory. In that state config writes are suppressed (see {@link Config#isReadOnly()}) until a
     * later successful load.</p>
     *
     * @return {@code true} if the on-disk config was loaded, {@code false} if the bundled defaults are in use
     */
    public static boolean setupConfig() {
        plugin.saveDefaultConfig();

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        YamlConfiguration oldConfig = new YamlConfiguration();
        boolean loaded = true;

        try {
            oldConfig.load(configFile);
        } catch (InvalidConfigurationException | IOException e) {
            loaded = false;
            logger.severe("config.yml could not be parsed and will NOT be modified: " + e.getMessage());
            File broken = backupBrokenConfig(configFile);
            if (broken != null) logger.severe("A copy of the broken file was saved to " + broken.getName());
            logger.severe("Using the bundled default configuration until config.yml is fixed. "
                    + "Config changes made in-game will not be saved. Fix the file and run /disenchantment reload.");
        }

        YamlConfiguration newConfig = loadBundledConfig();

        if (loaded) {
            applyAndSave(configFile, oldConfig, newConfig);
        }

        plugin.reloadConfig();

        if (!loaded) {
            // reloadConfig() re-read the broken file (parsed as empty); replace its contents with the bundled defaults.
            FileConfiguration current = plugin.getConfig();
            for (String key : Set.copyOf(current.getKeys(false))) current.set(key, null);
            for (String key : newConfig.getKeys(true)) {
                if (!newConfig.isConfigurationSection(key)) current.set(key, newConfig.get(key));
            }
        }

        Config.setReadOnly(!loaded);
        Disenchantment.config = plugin.getConfig();
        Disenchantment.enabled = Config.isPluginEnabled();
        Config.invalidateCaches();
        Config.validateRestrictions();
        EraseUtils.validateConfig();

        return loaded;
    }

    private static YamlConfiguration loadBundledConfig() {
        return YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("config.yml")), StandardCharsets.UTF_8));
    }

    /**
     * Runs migrations and persists the result. A {@code config.yml.bak} copy of the previous file is written
     * before anything is overwritten. If migrating or backing up fails the on-disk file is left untouched.
     */
    private static void applyAndSave(File configFile, FileConfiguration oldConfig, FileConfiguration newConfig) {
        int oldVersion = oldConfig.getInt("migration", 0);
        int newVersion = newConfig.getInt("migration", 0);

        FileConfiguration updatedConfig;

        try {
            updatedConfig = ConfigMigrations.apply(plugin, oldConfig, newConfig);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Config migration failed; config.yml was left untouched", e);
            return;
        }

        // Already current: nothing was migrated, so do not rewrite the user's file.
        if (oldVersion >= newVersion) return;

        File backup = new File(configFile.getParentFile(), "config.yml.bak");

        try {
            Files.copy(configFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Config migration: previous config.yml backed up to " + backup.getName());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not back up config.yml; migrated config was NOT saved: " + e.getMessage());
            return;
        }

        try {
            updatedConfig.save(configFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save config.yml", e);
            throw new RuntimeException("Failed to save config.yml", e);
        }
    }

    /**
     * Copies an unparseable config file to {@code config.yml.broken-<timestamp>} next to the original.
     *
     * @return the copy, or {@code null} if it could not be written
     */
    private static File backupBrokenConfig(File configFile) {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        File target = new File(configFile.getParentFile(), "config.yml.broken-" + stamp);

        try {
            Files.copy(configFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            logger.warning("Could not back up broken config.yml: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extracts bundled locale YAML files into the plugin's data folder. A locale file that does not
     * exist yet is copied from the JAR. An existing one is never overwritten (custom translations are
     * preserved), but any keys it is missing relative to the bundled version, for example keys added by
     * a plugin update, are merged in via {@link #mergeMissingLocaleKeys(String, File)}.
     */
    public static void setupLocaleConfigs() {
        for (String locale : Config.getLocales()) {
            File localeFile = new File(plugin.getDataFolder(), "locales/" + locale + ".yml");
            DiagnosticUtils.debug("CONFIG", "Locale setup: extracting " + locale + ".yml → " + localeFile.getPath());

            if (localeFile.exists()) {
                DiagnosticUtils.debug("CONFIG", "Locale setup: " + locale + ".yml already exists, merging missing keys (preserving custom translations)");
                mergeMissingLocaleKeys(locale, localeFile);
                continue;
            }

            try {
                plugin.saveResource("locales/" + locale + ".yml", false);
                DiagnosticUtils.debug("CONFIG", "Locale setup: saved " + locale + ".yml");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not save " + locale + ".yml", e);
            }
        }
    }

    /**
     * Adds every key present in the bundled {@code locales/<locale>.yml} but absent from the user's file,
     * keeping all of the user's existing values untouched. The file is only rewritten if something was added.
     * An unparseable user file is left as is.
     *
     * @param locale     the locale code, e.g. {@code "en"}
     * @param localeFile the user's locale file in the data folder
     * @return the number of keys added
     */
    public static int mergeMissingLocaleKeys(String locale, File localeFile) {
        InputStream stream = plugin.getResource("locales/" + locale + ".yml");
        if (stream == null) return 0;

        YamlConfiguration bundled = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        YamlConfiguration user = new YamlConfiguration();

        try {
            user.load(localeFile);
        } catch (InvalidConfigurationException | IOException e) {
            logger.warning("Locale " + locale + ".yml could not be parsed and was left untouched: " + e.getMessage());
            return 0;
        }

        int added = 0;

        for (String key : bundled.getKeys(true)) {
            if (bundled.isConfigurationSection(key) || user.contains(key, true)) continue;
            user.set(key, bundled.get(key));
            added++;
        }

        if (added == 0) return 0;

        try {
            user.save(localeFile);
            logger.info("Locale " + locale + ".yml: added " + added + " missing key(s) from the bundled locale");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not update locale " + locale + ".yml with missing keys", e);
            return 0;
        }

        return added;
    }
}
