package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.types.ConfigKeys;
import com.jankominek.disenchantment.types.RestrictionMode;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import static com.jankominek.disenchantment.Disenchantment.config;
import static com.jankominek.disenchantment.Disenchantment.logger;
import static com.jankominek.disenchantment.Disenchantment.plugin;

/**
 * Collects human-readable problems in the loaded {@code config.yml} without ever changing the file.
 * <p>
 * The {@link Config} getters are already total — they replace anything unusable with a safe default or clamp it —
 * so a bad value can never crash the plugin. The validator exists so the server owner finds out <em>that</em> a
 * value was replaced, and why. It reports wrong types, out-of-range numbers, unknown enum names, unknown
 * materials / worlds / enchantments, malformed enchantment-state entries, and unknown or misspelt option names
 * (compared against the bundled template).
 * <p>
 * It uses the same parsers as the getters ({@link ConfigSpec}, {@link ConfigValues}), so a warning always matches
 * what the plugin actually does. Run it with {@link #validateAndLog()} after the config and the plugin adapters
 * have loaded (enable and reload); the latest result is available through {@link #getLastIssues()}.
 */
public final class ConfigValidator {
    private static volatile List<ConfigIssue> lastIssues = List.of();

    private ConfigValidator() {
    }

    /**
     * The problems found by the most recent {@link #validateAndLog()} run.
     *
     * @return an unmodifiable list, empty when the config is clean or has not been validated yet
     */
    public static List<ConfigIssue> getLastIssues() {
        return lastIssues;
    }

    /**
     * Validates the live config against the bundled template, logs one warning per problem plus a one-line
     * summary, and remembers the result for {@link #getLastIssues()}. Never throws.
     *
     * @return the problems found (also available through {@link #getLastIssues()})
     */
    public static List<ConfigIssue> validateAndLog() {
        List<ConfigIssue> issues;

        try {
            issues = validate(config, loadTemplate());
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Config validation failed unexpectedly; the configuration is still usable", e);
            issues = List.of();
        }

        lastIssues = Collections.unmodifiableList(issues);

        for (ConfigIssue issue : issues) logger.warning("Config: " + issue.format());

        if (!issues.isEmpty()) {
            logger.warning("Config: " + issues.size() + (issues.size() == 1 ? " problem" : " problems")
                    + " found, see above. Unusable values are replaced by safe defaults; config.yml was not changed.");
        }

        return issues;
    }

    /**
     * Validates a configuration without logging.
     *
     * @param cfg      the configuration to check
     * @param template the bundled default configuration used to detect unknown option names, or {@code null} to skip that check
     * @return the problems found, in a stable order
     */
    public static List<ConfigIssue> validate(FileConfiguration cfg, FileConfiguration template) {
        List<ConfigIssue> issues = new ArrayList<>();

        if (template != null) checkUnknownKeys(cfg, template, "", issues);

        for (ConfigSpec spec : ConfigSpec.all()) {
            String problem = spec.problem(cfg.get(spec.key().getKey(), null));
            if (problem != null) issues.add(new ConfigIssue(spec.key().getKey(), problem));
        }

        checkLocales(cfg, issues);
        checkWebhook(cfg, issues);

        checkFeature(cfg, "disenchantment", issues);
        checkFeature(cfg, "shatterment", issues);

        String split = ConfigValues.splitCountProblem(cfg.get(ConfigKeys.SHATTERMENT_SPLIT_COUNT.getKey(), null));
        if (split != null) issues.add(new ConfigIssue(ConfigKeys.SHATTERMENT_SPLIT_COUNT.getKey(), split));

        checkGroups(cfg, issues);
        checkBedrock(cfg, issues);

        return issues;
    }

    // ----------------------------------------------------------------------------------------------------
    // Structure

    private static void checkUnknownKeys(ConfigurationSection user, ConfigurationSection template, String prefix, List<ConfigIssue> issues) {
        Set<String> known = template.getKeys(false);

        for (String key : user.getKeys(false)) {
            String path = prefix + key;

            if (known.contains(key)) {
                ConfigurationSection templateChild = template.getConfigurationSection(key);
                // An empty template section ({ }) is a user-defined map (chances, costs, groups): any keys are fine.
                if (templateChild != null && !templateChild.getKeys(false).isEmpty() && user.isConfigurationSection(key)) {
                    checkUnknownKeys(user.getConfigurationSection(key), templateChild, path + ".", issues);
                }
                continue;
            }

            String suggestion = closest(key, known);
            issues.add(new ConfigIssue(path, "unknown option" + (suggestion != null ? ", did you mean \"" + prefix + suggestion + "\"?" : "") + " It is ignored."));
        }
    }

    private static void checkLocales(FileConfiguration cfg, List<ConfigIssue> issues) {
        Object raw = cfg.get(ConfigKeys.LOCALES.getKey(), null);
        String path = ConfigKeys.LOCALES.getKey();

        if (raw != null && !(raw instanceof Collection)) {
            issues.add(new ConfigIssue(path, "expected a list of locale codes but found " + ConfigValues.describe(raw) + "; using " + String.join(", ", Config.getLocales())));
        } else {
            for (String locale : ConfigValues.toStringList(raw)) {
                if (!locale.matches(ConfigSpec.LOCALE_PATTERN))
                    issues.add(new ConfigIssue(path, "invalid locale code \"" + locale + "\" (letters, digits, '_' and '-' only); ignored"));
            }
        }

        String locale = Config.getLocale();
        boolean available = false;
        try {
            available = new File(plugin.getDataFolder(), "locales/" + locale + ".yml").exists() || plugin.getResource("locales/" + locale + ".yml") != null;
        } catch (RuntimeException ignored) {
            available = true; // cannot tell (plugin not initialised): do not guess
        }
        if (!available) {
            issues.add(new ConfigIssue(ConfigKeys.LOCALE.getKey(), "there is no locale file \"" + locale + ".yml\" in plugins/Disenchantment/locales; falling back to \"en\""));
        }
    }

    private static void checkWebhook(FileConfiguration cfg, List<ConfigIssue> issues) {
        String webhook = ConfigValues.scalar(cfg.get(ConfigKeys.LOGGING_DISCORD_WEBHOOK.getKey(), null));
        if (webhook != null && !webhook.startsWith("https://") && !webhook.startsWith("http://")) {
            issues.add(new ConfigIssue(ConfigKeys.LOGGING_DISCORD_WEBHOOK.getKey(), "does not look like a URL (expected https://...); crash reports will not be delivered"));
        }
    }

    private static void checkFeature(FileConfiguration cfg, String feature, List<ConfigIssue> issues) {
        checkRestriction(cfg, feature + ".worlds", false, issues);
        checkRestriction(cfg, feature + ".materials", true, issues);

        // enchantment states
        String statesPath = feature + ".enchantments-states";
        Object rawStates = cfg.get(statesPath, null);
        if (rawStates != null && !(rawStates instanceof Collection)) {
            issues.add(new ConfigIssue(statesPath, "expected a list of \"enchantment:state\" entries but found " + ConfigValues.describe(rawStates)));
        }
        List<String> problems = new ArrayList<>();
        Set<String> foreign = foreignNamespaceKeys(rawStates);
        for (String key : ConfigValues.parseEnchantmentStates(rawStates, problems).keySet().stream().sorted().toList()) {
            if (!foreign.contains(key)) checkEnchantmentKey(statesPath, key, issues);
        }
        addAll(statesPath, problems, issues);

        // chances
        String chancesPath = feature + ".enchantment-chances";
        Object rawChances = cfg.get(chancesPath, null);
        if (rawChances != null && !ConfigValues.isMap(rawChances)) {
            issues.add(new ConfigIssue(chancesPath, "expected a section of \"enchantment: chance\" pairs but found " + ConfigValues.describe(rawChances) + "; ignored (every enchantment always succeeds)"));
        }
        problems = new ArrayList<>();
        for (String key : ConfigValues.parseChances(rawChances, problems).keySet().stream().sorted().toList()) {
            checkEnchantmentKey(chancesPath, key, issues);
        }
        addAll(chancesPath, problems, issues);

        // per-enchantment costs
        String costsPath = feature + ".anvil.repair.enchantment-costs";
        Object rawCosts = cfg.get(costsPath, null);
        if (rawCosts != null && !ConfigValues.isMap(rawCosts)) {
            issues.add(new ConfigIssue(costsPath, "expected a section of \"enchantment: cost\" pairs but found " + ConfigValues.describe(rawCosts) + "; ignored (the formula applies)"));
        }
        problems = new ArrayList<>();
        ConfigValues.parseXpCosts(rawCosts, problems);
        ConfigValues.parseEconomyCosts(rawCosts, problems);
        addAll(costsPath, problems, issues);
        for (String key : ConfigValues.asMap(rawCosts).keySet().stream().map(k -> k.trim().toLowerCase(Locale.ROOT)).filter(k -> !k.isEmpty()).sorted().toList()) {
            checkEnchantmentKey(costsPath, key, issues);
        }
    }

    private static void checkRestriction(FileConfiguration cfg, String base, boolean materials, List<ConfigIssue> issues) {
        Object section = cfg.get(base, null);
        if (section == null) return;

        if (!ConfigValues.isMap(section)) {
            issues.add(new ConfigIssue(base, "expected a section with \"mode\" and \"list\" but found " + ConfigValues.describe(section) + "; using DENYLIST with an empty list"));
            return;
        }

        RestrictionMode mode = RestrictionMode.DENYLIST;
        Object rawMode = cfg.get(base + ".mode", null);
        if (rawMode != null) {
            RestrictionMode matched = RestrictionMode.match(ConfigValues.scalar(rawMode));
            if (matched == null) {
                issues.add(new ConfigIssue(base + ".mode", "unknown mode " + ConfigValues.describe(rawMode) + " (valid: DENYLIST, ALLOWLIST); using DENYLIST"));
            } else {
                mode = matched;
            }
        }

        String listPath = base + ".list";
        Object rawList = cfg.get(listPath, null);
        if (rawList != null && !(rawList instanceof Collection)) {
            issues.add(new ConfigIssue(listPath, "expected a list but found " + ConfigValues.describe(rawList)
                    + (ConfigValues.scalar(rawList) != null ? "; treating it as a single entry" : "; ignored")));
        }

        List<String> names = ConfigValues.toStringList(rawList);
        if (rawList instanceof Collection<?> collection && collection.size() != names.size()) {
            issues.add(new ConfigIssue(listPath, (collection.size() - names.size()) + " entries are not plain text; ignored"));
        }

        if (mode == RestrictionMode.ALLOWLIST && names.isEmpty()) {
            issues.add(new ConfigIssue(base, "mode is ALLOWLIST but the list is empty; this blocks the feature entirely (nothing is allowed)"));
        }

        Set<String> seen = new LinkedHashSet<>();
        boolean worldsLoaded = !Bukkit.getWorlds().isEmpty();

        for (String name : names) {
            if (!seen.add(name.toLowerCase(Locale.ROOT))) {
                issues.add(new ConfigIssue(listPath, "\"" + name + "\" is listed more than once"));
            } else if (materials && ConfigValues.matchMaterial(name) == null) {
                issues.add(new ConfigIssue(listPath, "unknown material \"" + name + "\"; ignored (use names like DIAMOND_SWORD or minecraft:diamond_sword)"));
            } else if (!materials && worldsLoaded && Bukkit.getWorld(name) == null) {
                issues.add(new ConfigIssue(listPath, "world \"" + name + "\" is not currently loaded; it is ignored until a world with that name loads"));
            }
        }
    }

    private static void checkBedrock(FileConfiguration cfg, List<ConfigIssue> issues) {
        Object detect = cfg.get(ConfigKeys.BEDROCK_DETECT.getKey(), null);
        String detectPath = ConfigKeys.BEDROCK_DETECT.getKey();
        if (detect != null && !(detect instanceof Collection) && ConfigValues.scalar(detect) == null) {
            issues.add(new ConfigIssue(detectPath, "expected a list of detection methods but found " + ConfigValues.describe(detect)
                    + "; using " + Config.Bedrock.DEFAULT_DETECT));
        } else if (detect != null) {
            List<String> unknown = new ArrayList<>();
            Config.Bedrock.parseDetect(detect, unknown);
            for (String entry : unknown) {
                issues.add(new ConfigIssue(detectPath, "unknown detection method \"" + entry + "\" (valid: FLOODGATE, GEYSER, UUID, PREFIX); ignored"));
            }
        }

        Object prefixes = cfg.get(ConfigKeys.BEDROCK_PREFIXES.getKey(), null);
        if (prefixes != null && !(prefixes instanceof Collection) && ConfigValues.scalar(prefixes) == null) {
            issues.add(new ConfigIssue(ConfigKeys.BEDROCK_PREFIXES.getKey(), "expected a list of name prefixes but found "
                    + ConfigValues.describe(prefixes) + "; using " + Config.Bedrock.DEFAULT_PREFIXES));
        }
    }

    private static void checkGroups(FileConfiguration cfg, List<ConfigIssue> issues) {
        String path = ConfigKeys.ENCHANTMENT_GROUPS.getKey();
        Object raw = cfg.get(path, null);

        if (raw != null && !ConfigValues.isMap(raw)) {
            issues.add(new ConfigIssue(path, "expected a section of \"group: [enchantments]\" but found " + ConfigValues.describe(raw) + "; ignored"));
            return;
        }

        List<String> problems = new ArrayList<>();
        Map<String, List<String>> groups = ConfigValues.parseGroups(raw, problems);
        addAll(path, problems, issues);

        for (String name : groups.keySet().stream().sorted().toList()) {
            for (String member : groups.get(name)) checkEnchantmentKey(path + "." + name, member, issues);
        }
    }

    // ----------------------------------------------------------------------------------------------------
    // Helpers

    /**
     * Adds an issue when a key is not a vanilla enchantment. Keys in another namespace cannot be verified and are
     * accepted; bare keys that are not vanilla are reported softly because a custom enchantment plugin may provide them.
     */
    private static void checkEnchantmentKey(String path, String key, List<ConfigIssue> issues) {
        if (isKnownEnchantment(key)) return;
        issues.add(new ConfigIssue(path, "unknown enchantment \"" + key + "\" (not a vanilla enchantment; ignore this if it comes from a custom enchantment plugin)"));
    }

    /**
     * Whether a key is a vanilla enchantment, or cannot be verified (other namespace, registry unavailable).
     *
     * @param key a bare ({@code sharpness}) or namespaced ({@code minecraft:sharpness}) enchantment key
     * @return {@code false} only when the key is definitely not a vanilla enchantment
     */
    public static boolean isKnownEnchantment(String key) {
        if (key == null) return false;

        String lower = key.trim().toLowerCase(Locale.ROOT);
        int colon = lower.indexOf(':');
        if (colon >= 0 && !lower.substring(0, colon).equals("minecraft")) return true;

        try {
            NamespacedKey namespacedKey = NamespacedKey.fromString("minecraft:" + (colon >= 0 ? lower.substring(colon + 1) : lower));
            return namespacedKey != null && Registry.ENCHANTMENT.get(namespacedKey) != null;
        } catch (RuntimeException | LinkageError e) {
            return true;
        }
    }

    /**
     * The state parser drops the namespace of {@code ns:key:state} entries, so remember which keys were written
     * with a namespace other than {@code minecraft}: those belong to another plugin and cannot be verified.
     */
    private static Set<String> foreignNamespaceKeys(Object rawStates) {
        Set<String> foreign = new LinkedHashSet<>();
        List<String> entries = new ArrayList<>();

        if (rawStates instanceof Collection<?> collection) {
            for (Object element : collection) {
                if (ConfigValues.isMap(element)) {
                    ConfigValues.asMap(element).forEach((k, v) -> entries.add(k + ":" + v));
                } else if (ConfigValues.scalar(element) != null) {
                    entries.add(ConfigValues.scalar(element));
                }
            }
        }

        for (String entry : entries) {
            String[] parts = entry.split(":", -1);
            if (parts.length == 3 && !parts[0].trim().equalsIgnoreCase("minecraft")) foreign.add(parts[1].trim().toLowerCase(Locale.ROOT));
        }

        return foreign;
    }

    private static void addAll(String path, List<String> messages, List<ConfigIssue> issues) {
        for (String message : messages) issues.add(new ConfigIssue(path, message));
    }

    private static FileConfiguration loadTemplate() {
        InputStream stream = plugin.getResource("config.yml");
        if (stream == null) return null;
        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    /**
     * Closest known option name by edit distance (case-insensitive), or {@code null} when nothing is close enough
     * to be a plausible typo.
     */
    private static String closest(String key, Set<String> candidates) {
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        String lower = key.toLowerCase(Locale.ROOT);

        for (String candidate : candidates) {
            int distance = editDistance(lower, candidate.toLowerCase(Locale.ROOT));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }

        int tolerance = Math.max(2, key.length() / 4);
        return bestDistance <= tolerance ? best : null;
    }

    private static int editDistance(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) previous[j] = j;

        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[b.length()];
    }
}
