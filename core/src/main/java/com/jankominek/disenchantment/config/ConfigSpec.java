package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.types.ConfigKeys;
import com.jankominek.disenchantment.types.LogLevelType;
import org.bukkit.event.EventPriority;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Declarative description of every scalar option in {@code config.yml}: its type, default and allowed range.
 * <p>
 * This is the single source of truth shared by the {@link Config} getters (which call {@link #resolve(Object)}
 * to turn <em>any</em> raw YAML value into a safe one) and the {@link ConfigValidator} (which calls
 * {@link #problem(Object)} to explain what was wrong). Because both go through the same object, the warning and
 * the behaviour can never disagree.
 *
 * @param key      the config key
 * @param kind     the value type
 * @param def      the default (a {@link Boolean}, {@link Double}, {@link Integer}, {@link String} or enum constant)
 * @param min      inclusive lower bound for {@link Kind#DECIMAL} / {@link Kind#INTEGER}
 * @param max      inclusive upper bound for {@link Kind#DECIMAL} / {@link Kind#INTEGER}
 * @param enumType the enum class for {@link Kind#ENUM}, otherwise {@code null}
 * @param pattern  regex a {@link Kind#STRING} must match entirely, or {@code null} for any string
 */
public record ConfigSpec(ConfigKeys key, Kind kind, Object def, double min, double max, Class<? extends Enum<?>> enumType, String pattern) {
    /**
     * The value type of an option.
     */
    public enum Kind {BOOLEAN, DECIMAL, INTEGER, ENUM, STRING}

    private static final Map<ConfigKeys, ConfigSpec> SPECS = new EnumMap<>(ConfigKeys.class);

    /**
     * Characters allowed in a locale code; it is used as a file name, so path separators are never accepted.
     */
    public static final String LOCALE_PATTERN = "[A-Za-z0-9_-]{1,32}";

    static {
        bool(ConfigKeys.ENABLED, true);
        bool(ConfigKeys.UPDATE_CHECKER, true);
        string(ConfigKeys.LOCALE, "en", LOCALE_PATTERN);

        enumeration(ConfigKeys.LOGGING_LEVEL, LogLevelType.INFO);
        bool(ConfigKeys.LOGGING_SAVE_REPORTS, true);
        bool(ConfigKeys.LOGGING_OPERATIONS, false);
        integer(ConfigKeys.LOGGING_OPERATIONS_RETENTION_DAYS, 0, 0, 36500);
        string(ConfigKeys.LOGGING_DISCORD_WEBHOOK, "", null);

        enumeration(ConfigKeys.EVENT_PRIORITIES_DISENCHANT, EventPriority.HIGHEST);
        enumeration(ConfigKeys.EVENT_PRIORITIES_DISENCHANT_CLICK, EventPriority.HIGHEST);
        enumeration(ConfigKeys.EVENT_PRIORITIES_SHATTER, EventPriority.HIGHEST);
        enumeration(ConfigKeys.EVENT_PRIORITIES_SHATTER_CLICK, EventPriority.HIGHEST);

        bool(ConfigKeys.COST_MULTIPLIER_LUCKPERMS, false);
        integer(ConfigKeys.COOLDOWN_SECONDS, 0, 0, 31_536_000);
        bool(ConfigKeys.COOLDOWN_USE_CMI, false);
        bool(ConfigKeys.COOLDOWN_USE_ESSENTIALS, false);

        bool(ConfigKeys.DISENCHANTMENT_ENABLED, true);
        bool(ConfigKeys.DISENCHANTMENT_ANVIL_SOUND_ENABLED, true);
        decimal(ConfigKeys.DISENCHANTMENT_ANVIL_VOLUME, 1.0, 0.0, 10.0);
        decimal(ConfigKeys.DISENCHANTMENT_ANVIL_PITCH, 1.0, 0.5, 2.0);
        bool(ConfigKeys.DISENCHANTMENT_REPAIR_RESET_ENABLED, false);
        bool(ConfigKeys.DISENCHANTMENT_REPAIR_COST_ENABLED, true);
        decimal(ConfigKeys.DISENCHANTMENT_REPAIR_COST_BASE, 10.0, -1_000_000.0, 1_000_000.0);
        decimal(ConfigKeys.DISENCHANTMENT_REPAIR_COST_MULTIPLIER, 0.25, -1_000_000.0, 1_000_000.0);
        integer(ConfigKeys.DISENCHANTMENT_REPAIR_MAX_COST, -1, -1, Integer.MAX_VALUE);
        bool(ConfigKeys.DISENCHANTMENT_ANVIL_BYPASS_TOO_EXPENSIVE, false);
        bool(ConfigKeys.DISENCHANTMENT_ECONOMY_ENABLED, false);
        decimal(ConfigKeys.DISENCHANTMENT_ECONOMY_COST, 100.0, 0.0, 1e12);
        bool(ConfigKeys.DISENCHANTMENT_ECONOMY_SHOW_COST, true);
        bool(ConfigKeys.DISENCHANTMENT_ECONOMY_CHARGE_MESSAGE, true);

        bool(ConfigKeys.SHATTERMENT_ENABLED, true);
        bool(ConfigKeys.SHATTERMENT_ANVIL_SOUND_ENABLED, true);
        decimal(ConfigKeys.SHATTERMENT_ANVIL_VOLUME, 1.0, 0.0, 10.0);
        decimal(ConfigKeys.SHATTERMENT_ANVIL_PITCH, 1.0, 0.5, 2.0);
        bool(ConfigKeys.SHATTERMENT_REPAIR_RESET_ENABLED, false);
        bool(ConfigKeys.SHATTERMENT_REPAIR_COST_ENABLED, true);
        decimal(ConfigKeys.SHATTERMENT_REPAIR_COST_BASE, 10.0, -1_000_000.0, 1_000_000.0);
        decimal(ConfigKeys.SHATTERMENT_REPAIR_COST_MULTIPLIER, 0.25, -1_000_000.0, 1_000_000.0);
        integer(ConfigKeys.SHATTERMENT_REPAIR_MAX_COST, -1, -1, Integer.MAX_VALUE);
        bool(ConfigKeys.SHATTERMENT_ANVIL_BYPASS_TOO_EXPENSIVE, false);
        bool(ConfigKeys.SHATTERMENT_ECONOMY_ENABLED, false);
        decimal(ConfigKeys.SHATTERMENT_ECONOMY_COST, 100.0, 0.0, 1e12);
        bool(ConfigKeys.SHATTERMENT_ECONOMY_SHOW_COST, true);
        bool(ConfigKeys.SHATTERMENT_ECONOMY_CHARGE_MESSAGE, true);
    }

    private static void bool(ConfigKeys key, boolean def) {
        SPECS.put(key, new ConfigSpec(key, Kind.BOOLEAN, def, 0, 0, null, null));
    }

    private static void decimal(ConfigKeys key, double def, double min, double max) {
        SPECS.put(key, new ConfigSpec(key, Kind.DECIMAL, def, min, max, null, null));
    }

    private static void integer(ConfigKeys key, int def, int min, int max) {
        SPECS.put(key, new ConfigSpec(key, Kind.INTEGER, def, min, max, null, null));
    }

    private static <E extends Enum<E>> void enumeration(ConfigKeys key, E def) {
        SPECS.put(key, new ConfigSpec(key, Kind.ENUM, def, 0, 0, def.getDeclaringClass(), null));
    }

    private static void string(ConfigKeys key, String def, String pattern) {
        SPECS.put(key, new ConfigSpec(key, Kind.STRING, def, 0, 0, null, pattern));
    }

    /**
     * Looks up the spec of a scalar option.
     *
     * @param key the config key
     * @return the spec
     * @throws IllegalArgumentException if the key has no scalar spec (a programming error, not a config error)
     */
    public static ConfigSpec of(ConfigKeys key) {
        ConfigSpec spec = SPECS.get(key);
        if (spec == null) throw new IllegalArgumentException("No ConfigSpec registered for " + key);
        return spec;
    }

    /**
     * All registered scalar specs, in key declaration order.
     *
     * @return an unmodifiable view of the specs
     */
    public static Collection<ConfigSpec> all() {
        return Collections.unmodifiableCollection(SPECS.values());
    }

    /**
     * Turns any raw YAML value into a valid value for this option: the parsed value when it is usable
     * (numbers clamped into range), otherwise the default. Never throws.
     *
     * @param raw the raw value from the config, possibly {@code null}
     * @return a {@link Boolean}, {@link Double}, {@link Integer}, {@link String} or enum constant
     */
    public Object resolve(Object raw) {
        switch (kind) {
            case BOOLEAN: {
                Boolean b = ConfigValues.toBoolean(raw);
                return b != null ? b : def;
            }
            case DECIMAL: {
                Double d = ConfigValues.toDouble(raw);
                return d == null ? def : Math.max(min, Math.min(max, d));
            }
            case INTEGER: {
                Double d = ConfigValues.toDouble(raw);
                return d == null ? def : (int) Math.max(min, Math.min(max, d));
            }
            case ENUM: {
                Enum<?> e = matchEnum(raw);
                return e != null ? e : def;
            }
            default: {
                String s = ConfigValues.scalar(raw);
                return s == null || (pattern != null && !s.matches(pattern)) ? def : s;
            }
        }
    }

    /**
     * Explains what is wrong with a raw value, or returns {@code null} when it is absent or acceptable.
     * A value that is accepted after leniency (e.g. the quoted string {@code "true"}) is not a problem.
     *
     * @param raw the raw value from the config, {@code null} when the key is not set
     * @return a human-readable problem including the replacement used, or {@code null}
     */
    public String problem(Object raw) {
        if (raw == null) return null;

        switch (kind) {
            case BOOLEAN:
                if (ConfigValues.toBoolean(raw) != null) return null;
                return "expected true or false but found " + ConfigValues.describe(raw) + "; using default " + def;
            case DECIMAL: {
                Double d = ConfigValues.toDouble(raw);
                if (d == null)
                    return "expected a finite number but found " + ConfigValues.describe(raw) + "; using default " + fmt(def);
                if (d < min || d > max)
                    return fmt(d) + " is outside the allowed range " + fmt(min) + " to " + fmt(max) + "; using " + fmt(Math.max(min, Math.min(max, d)));
                return null;
            }
            case INTEGER: {
                Double d = ConfigValues.toDouble(raw);
                if (d == null)
                    return "expected a whole number but found " + ConfigValues.describe(raw) + "; using default " + def;
                if (d < min || d > max)
                    return fmt(d) + " is outside the allowed range " + fmt(min) + " to " + fmt(max) + "; using " + (int) Math.max(min, Math.min(max, d));
                if (d != Math.rint(d))
                    return fmt(d) + " is not a whole number; using " + d.intValue();
                return null;
            }
            case ENUM:
                if (matchEnum(raw) != null) return null;
                return "unknown value " + ConfigValues.describe(raw) + " (valid: " + enumNames() + "); using default " + def;
            default: {
                String s = ConfigValues.scalar(raw);
                if (s == null && raw instanceof String && pattern == null) return null; // an empty string is a valid "unset"
                if (s == null)
                    return "expected text but found " + ConfigValues.describe(raw) + "; using default " + ConfigValues.describe(def);
                if (pattern != null && !s.matches(pattern))
                    return ConfigValues.describe(raw) + " is not valid here (letters, digits, '_' and '-' only); using default " + ConfigValues.describe(def);
                return null;
            }
        }
    }

    private Enum<?> matchEnum(Object raw) {
        String s = ConfigValues.scalar(raw);
        if (s == null) return null;
        String normalised = s.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        for (Enum<?> constant : enumType.getEnumConstants()) {
            if (constant.name().equals(normalised)) return constant;
        }
        return null;
    }

    private String enumNames() {
        StringBuilder names = new StringBuilder();
        for (Enum<?> constant : enumType.getEnumConstants()) {
            if (names.length() > 0) names.append(", ");
            names.append(constant.name());
        }
        return names.toString();
    }

    private static String fmt(Object value) {
        if (value instanceof Double d && d == Math.rint(d) && Math.abs(d) < 1e15) return String.valueOf(d.longValue());
        return String.valueOf(value);
    }
}
