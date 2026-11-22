package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.types.ConfigKeys;
import com.jankominek.disenchantment.types.LogLevelType;
import com.jankominek.disenchantment.types.ParticleScopeType;
import com.jankominek.disenchantment.types.SpecialAnvilMode;
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

    /**
     * A namespaced vanilla sound key such as {@code entity.villager.no} (lower case, as the client expects).
     */
    public static final String SOUND_KEY_PATTERN = "[a-z0-9_.:/-]{1,128}";

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

        // erase, special anvils, grindstone, feedback
        enumeration(ConfigKeys.EVENT_PRIORITIES_ERASE, EventPriority.HIGHEST);
        enumeration(ConfigKeys.EVENT_PRIORITIES_ERASE_CLICK, EventPriority.HIGHEST);
        enumeration(ConfigKeys.EVENT_PRIORITIES_GRINDSTONE, EventPriority.HIGHEST);
        enumeration(ConfigKeys.EVENT_PRIORITIES_GRINDSTONE_CLICK, EventPriority.HIGHEST);

        bool(ConfigKeys.FEEDBACK_PARTICLES_ENABLED, true);
        integer(ConfigKeys.FEEDBACK_PARTICLES_COUNT, 30, 1, 500);
        decimal(ConfigKeys.FEEDBACK_PARTICLES_SPREAD, 0.4, 0.0, 16.0);
        enumeration(ConfigKeys.FEEDBACK_PARTICLES_SCOPE, ParticleScopeType.PLAYER);
        bool(ConfigKeys.FEEDBACK_FAILURE_SOUND_ENABLED, true);
        string(ConfigKeys.FEEDBACK_FAILURE_SOUND_SOUND, "entity.villager.no", SOUND_KEY_PATTERN);
        decimal(ConfigKeys.FEEDBACK_FAILURE_SOUND_VOLUME, 1.0, 0.0, 10.0);
        decimal(ConfigKeys.FEEDBACK_FAILURE_SOUND_PITCH, 1.0, 0.5, 2.0);
        bool(ConfigKeys.FEEDBACK_NOT_ENOUGH_XP_MESSAGE, true);
        bool(ConfigKeys.FEEDBACK_PREVIEW_LORE, true);

        bool(ConfigKeys.ERASE_ENABLED, false);
        bool(ConfigKeys.ERASE_CONSUME_CATALYST, true);
        bool(ConfigKeys.ERASE_ERASE_CURSES, false);
        bool(ConfigKeys.ERASE_ANVIL_SOUND_ENABLED, true);
        decimal(ConfigKeys.ERASE_ANVIL_VOLUME, 1.0, 0.0, 10.0);
        decimal(ConfigKeys.ERASE_ANVIL_PITCH, 1.0, 0.5, 2.0);
        bool(ConfigKeys.ERASE_REPAIR_RESET_ENABLED, false);
        bool(ConfigKeys.ERASE_REPAIR_COST_ENABLED, true);
        decimal(ConfigKeys.ERASE_REPAIR_COST_BASE, 10.0, -1_000_000.0, 1_000_000.0);
        decimal(ConfigKeys.ERASE_REPAIR_COST_MULTIPLIER, 0.25, -1_000_000.0, 1_000_000.0);
        integer(ConfigKeys.ERASE_REPAIR_MAX_COST, -1, -1, Integer.MAX_VALUE);
        bool(ConfigKeys.ERASE_ANVIL_BYPASS_TOO_EXPENSIVE, false);
        bool(ConfigKeys.ERASE_ECONOMY_ENABLED, false);
        decimal(ConfigKeys.ERASE_ECONOMY_COST, 100.0, 0.0, 1e12);
        bool(ConfigKeys.ERASE_ECONOMY_SHOW_COST, true);
        bool(ConfigKeys.ERASE_ECONOMY_CHARGE_MESSAGE, true);

        enumeration(ConfigKeys.SPECIAL_ANVILS_MODE, SpecialAnvilMode.ALL);
        bool(ConfigKeys.SPECIAL_ANVILS_ALLOW_VIRTUAL_ANVILS, false);
        bool(ConfigKeys.SPECIAL_ANVILS_DROP_SPECIAL_ITEM, true);
        bool(ConfigKeys.SPECIAL_ANVILS_HINT_MESSAGE, false);
        string(ConfigKeys.SPECIAL_ANVILS_ITEM_NAME, "&dEnchanter's Anvil", null);
        bool(ConfigKeys.SPECIAL_ANVILS_RECIPE_ENABLED, false);

        bool(ConfigKeys.GRINDSTONE_ENABLED, false);
        bool(ConfigKeys.GRINDSTONE_CURSES, false);
        bool(ConfigKeys.GRINDSTONE_SOUND_ENABLED, true);
        decimal(ConfigKeys.GRINDSTONE_SOUND_VOLUME, 1.0, 0.0, 10.0);
        decimal(ConfigKeys.GRINDSTONE_SOUND_PITCH, 1.0, 0.5, 2.0);
        bool(ConfigKeys.GRINDSTONE_REPAIR_RESET_ENABLED, false);
        bool(ConfigKeys.GRINDSTONE_REPAIR_COST_ENABLED, true);
        decimal(ConfigKeys.GRINDSTONE_REPAIR_COST_BASE, 10.0, -1_000_000.0, 1_000_000.0);
        decimal(ConfigKeys.GRINDSTONE_REPAIR_COST_MULTIPLIER, 0.25, -1_000_000.0, 1_000_000.0);
        integer(ConfigKeys.GRINDSTONE_REPAIR_MAX_COST, -1, -1, Integer.MAX_VALUE);
        bool(ConfigKeys.GRINDSTONE_ECONOMY_ENABLED, false);
        decimal(ConfigKeys.GRINDSTONE_ECONOMY_COST, 100.0, 0.0, 1e12);
        bool(ConfigKeys.GRINDSTONE_ECONOMY_SHOW_COST, true);
        bool(ConfigKeys.GRINDSTONE_ECONOMY_CHARGE_MESSAGE, true);
        bool(ConfigKeys.GRINDSTONE_TRANSFER_ENABLED, false);
        bool(ConfigKeys.GRINDSTONE_TRANSFER_ALLOW_OVER_MAX, false);
        integer(ConfigKeys.GRINDSTONE_TRANSFER_MAX_ENCHANTMENTS, 16, 1, 64);
        bool(ConfigKeys.GRINDSTONE_TRANSFER_REPAIR_RESET_ENABLED, false);
        bool(ConfigKeys.GRINDSTONE_TRANSFER_REPAIR_COST_ENABLED, true);
        decimal(ConfigKeys.GRINDSTONE_TRANSFER_REPAIR_COST_BASE, 15.0, -1_000_000.0, 1_000_000.0);
        decimal(ConfigKeys.GRINDSTONE_TRANSFER_REPAIR_COST_MULTIPLIER, 0.25, -1_000_000.0, 1_000_000.0);
        integer(ConfigKeys.GRINDSTONE_TRANSFER_REPAIR_MAX_COST, -1, -1, Integer.MAX_VALUE);
        bool(ConfigKeys.GRINDSTONE_TRANSFER_ECONOMY_ENABLED, false);
        decimal(ConfigKeys.GRINDSTONE_TRANSFER_ECONOMY_COST, 100.0, 0.0, 1e12);
        bool(ConfigKeys.GRINDSTONE_TRANSFER_ECONOMY_SHOW_COST, true);
        bool(ConfigKeys.GRINDSTONE_TRANSFER_ECONOMY_CHARGE_MESSAGE, true);

        bool(ConfigKeys.BEDROCK_FALLBACK_ENABLED, false);
        bool(ConfigKeys.BEDROCK_ALLOW_JAVA_PLAYERS, false);
        bool(ConfigKeys.BEDROCK_OPEN_ON_SNEAK_CLICK, false);
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
                    return ConfigValues.describe(raw) + " is not a valid value (locale codes use letters, digits, '_' and '-'; sound keys are lower-case like entity.villager.no); using default " + ConfigValues.describe(def);
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
