package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.types.ConfigKeys;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.types.SplitCountRange;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jankominek.disenchantment.Disenchantment.config;

/**
 * Total (never-throwing) readers for raw {@code config.yml} values.
 * <p>
 * Every method accepts whatever YAML produced — a string where a number was expected, a list where a map was
 * expected, {@code null}, NaN, a huge number — and returns a usable value. The structural parsers
 * ({@link #parseEnchantmentStates}, {@link #parseChances}, ...) take an optional {@code problems} sink: the
 * {@link Config} getters pass {@code null} (silent), the {@link ConfigValidator} passes a list to collect
 * human-readable messages. Both therefore see exactly the same interpretation of the file.
 */
public final class ConfigValues {
    /**
     * Upper bound for {@code split-count}; a book can never hold more enchantments than this anyway.
     */
    public static final int MAX_SPLIT_COUNT = 255;

    /**
     * Upper bound for a per-enchantment XP cost override.
     */
    public static final int MAX_XP_COST = 1_000_000;

    /**
     * Upper bound for a per-enchantment economy cost override.
     */
    public static final double MAX_ECONOMY_COST = 1e12;

    private static final Pattern SPLIT_RANGE = Pattern.compile("^\\s*(\\d+)\\s*(?:-|\\.\\.|to)\\s*(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);

    private ConfigValues() {
    }

    // ----------------------------------------------------------------------------------------------------
    // Scalar options (see ConfigSpec)

    /**
     * Reads a boolean option; a missing or unusable value yields the spec default.
     *
     * @param key the config key (must have a {@link ConfigSpec})
     * @return the effective value
     */
    public static boolean bool(ConfigKeys key) {
        return (Boolean) ConfigSpec.of(key).resolve(raw(key));
    }

    /**
     * Reads a decimal option, finite and clamped into the spec range; unusable values yield the default.
     *
     * @param key the config key (must have a {@link ConfigSpec})
     * @return the effective value
     */
    public static double decimal(ConfigKeys key) {
        return (Double) ConfigSpec.of(key).resolve(raw(key));
    }

    /**
     * Reads a whole-number option clamped into the spec range; unusable values yield the default.
     *
     * @param key the config key (must have a {@link ConfigSpec})
     * @return the effective value
     */
    public static int integer(ConfigKeys key) {
        return (Integer) ConfigSpec.of(key).resolve(raw(key));
    }

    /**
     * Reads an enum option case-insensitively ({@code -} and spaces count as {@code _}); unknown values yield the default.
     *
     * @param key  the config key (must have a {@link ConfigSpec})
     * @param type the enum class of the option
     * @param <E>  the enum type
     * @return the effective constant
     */
    public static <E extends Enum<E>> E enumeration(ConfigKeys key, Class<E> type) {
        return type.cast(ConfigSpec.of(key).resolve(raw(key)));
    }

    /**
     * Reads a text option; unusable values (lists, maps, values failing the spec pattern) yield the default.
     *
     * @param key the config key (must have a {@link ConfigSpec})
     * @return the effective, trimmed text (never {@code null})
     */
    public static String string(ConfigKeys key) {
        return (String) ConfigSpec.of(key).resolve(raw(key));
    }

    /**
     * Reads the raw YAML value of a key (the bundled default when the key is missing from the user's file).
     *
     * @param key the config key
     * @return the raw value, possibly {@code null}
     */
    public static Object raw(ConfigKeys key) {
        return config.get(key.getKey());
    }

    // ----------------------------------------------------------------------------------------------------
    // Primitive parsers

    /**
     * Parses a boolean leniently: a real boolean, or the strings {@code true} / {@code false} (any case).
     *
     * @param raw any value
     * @return the boolean, or {@code null} when the value is not boolean-like
     */
    public static Boolean toBoolean(Object raw) {
        if (raw instanceof Boolean b) return b;
        if (raw instanceof String s) {
            String t = s.trim();
            if (t.equalsIgnoreCase("true")) return Boolean.TRUE;
            if (t.equalsIgnoreCase("false")) return Boolean.FALSE;
        }
        return null;
    }

    /**
     * Parses a finite number leniently: any {@link Number}, or a numeric string ({@code ","} is accepted as the
     * decimal separator). NaN and the infinities are rejected.
     *
     * @param raw any value
     * @return the number, or {@code null} when the value is not a finite number
     */
    public static Double toDouble(Object raw) {
        double d;

        if (raw instanceof Number n) {
            d = n.doubleValue();
        } else if (raw instanceof String s) {
            String t = s.trim().replace(',', '.');
            if (t.isEmpty()) return null;
            try {
                d = Double.parseDouble(t);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }

        return Double.isNaN(d) || Double.isInfinite(d) ? null : d;
    }

    /**
     * Parses a whole number leniently (see {@link #toDouble(Object)}); fractions are truncated and values
     * outside the {@code int} range are saturated.
     *
     * @param raw any value
     * @return the integer, or {@code null} when the value is not a finite number
     */
    public static Integer toInt(Object raw) {
        Double d = toDouble(raw);
        if (d == null) return null;
        return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, d));
    }

    /**
     * Converts a scalar (string, number, boolean, character) to a trimmed string.
     *
     * @param raw any value
     * @return the trimmed text, or {@code null} for {@code null}, blank text, lists and maps
     */
    public static String scalar(Object raw) {
        if (raw instanceof String || raw instanceof Number || raw instanceof Boolean || raw instanceof Character) {
            String s = String.valueOf(raw).trim();
            return s.isEmpty() ? null : s;
        }
        return null;
    }

    /**
     * Converts a value to a list of non-blank trimmed strings. A single scalar becomes a one-element list;
     * non-scalar elements are skipped.
     *
     * @param raw any value
     * @return the strings (never {@code null})
     */
    public static List<String> toStringList(Object raw) {
        List<String> out = new ArrayList<>();

        if (raw instanceof Collection<?> collection) {
            for (Object element : collection) {
                String s = scalar(element);
                if (s != null) out.add(s);
            }
        } else {
            String s = scalar(raw);
            if (s != null) out.add(s);
        }

        return out;
    }

    /**
     * Views a configuration section or a plain map as a string-keyed map.
     *
     * @param raw any value
     * @return the entries in file order, empty when the value is neither a section nor a map
     */
    public static Map<String, Object> asMap(Object raw) {
        Map<String, Object> out = new LinkedHashMap<>();

        if (raw instanceof ConfigurationSection section) {
            out.putAll(section.getValues(false));
        } else if (raw instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) out.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }

        return out;
    }

    /**
     * Whether the value is a YAML mapping.
     *
     * @param raw any value
     * @return {@code true} for a configuration section or a map
     */
    public static boolean isMap(Object raw) {
        return raw instanceof ConfigurationSection || raw instanceof Map;
    }

    /**
     * Short human-readable rendering of a raw value for messages ({@code "abc"}, {@code a list}, {@code 5}).
     *
     * @param raw any value
     * @return a description, never {@code null}
     */
    public static String describe(Object raw) {
        if (raw == null) return "nothing";
        if (raw instanceof String s) return "\"" + s + "\"";
        if (raw instanceof Collection) return "a list";
        if (isMap(raw)) return "a section";
        return String.valueOf(raw);
    }

    // ----------------------------------------------------------------------------------------------------
    // Restriction lists

    /**
     * Resolves a material list; unknown names are skipped (reported through {@code unknown}). Names are matched
     * with {@link Material#matchMaterial(String)}, so {@code diamond_sword}, {@code DIAMOND_SWORD} and
     * {@code minecraft:diamond_sword} are all accepted. Legacy materials are rejected.
     *
     * @param raw     the raw {@code list} value
     * @param unknown receives every name that did not resolve, or {@code null}
     * @return the distinct materials in file order
     */
    public static List<Material> parseMaterials(Object raw, List<String> unknown) {
        Set<Material> out = new LinkedHashSet<>();

        for (String name : toStringList(raw)) {
            Material material = matchMaterial(name);
            if (material == null) {
                if (unknown != null) unknown.add(name);
            } else {
                out.add(material);
            }
        }

        return new ArrayList<>(out);
    }

    /**
     * Null-safe, case-insensitive material lookup accepting {@code minecraft:} ids; legacy materials do not match.
     *
     * @param name a material name or namespaced id
     * @return the material, or {@code null} when unknown
     */
    public static Material matchMaterial(String name) {
        if (name == null || name.isBlank()) return null;

        try {
            String trimmed = name.trim();
            // Spigot only strips a lower-case "minecraft:" prefix; accept any case.
            if (trimmed.regionMatches(true, 0, "minecraft:", 0, 10)) trimmed = trimmed.substring(10);

            Material material = Material.matchMaterial(trimmed);
            return material == null || material.isLegacy() ? null : material;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Merges names that the plugin cannot resolve right now (a world that has not loaded yet, a misspelt or renamed
     * material) from the existing stored list into a list about to be written, so that editing a restriction list
     * through a command or GUI never silently drops entries the user wrote by hand.
     *
     * @param written  the resolved names that are about to be saved
     * @param existing the raw list currently stored in the config
     * @param resolves whether an existing name currently resolves (those are represented by {@code written})
     * @return {@code written} followed by the unresolved stored names, without duplicates
     */
    public static List<String> keepUnresolved(List<String> written, Object existing, java.util.function.Predicate<String> resolves) {
        List<String> merged = new ArrayList<>(written);

        for (String name : toStringList(existing)) {
            if (resolves.test(name)) continue;

            boolean present = false;
            for (String w : merged) {
                if (w.equalsIgnoreCase(name)) present = true;
            }
            if (!present) merged.add(name);
        }

        return merged;
    }

    // ----------------------------------------------------------------------------------------------------
    // Enchantment maps

    /**
     * Parses {@code enchantments-states}. Accepted entry shapes: {@code "key:state"}, {@code "ns:key:state"} and the
     * YAML mistake {@code - key: state} (a one-entry map). Keys are lower-cased. Entries with an unknown state, a
     * missing state or no key are skipped — they never map to {@code null}.
     *
     * @param raw      the raw value
     * @param problems receives one message per skipped/odd entry, or {@code null}
     * @return the states by enchantment key
     */
    public static HashMap<String, EnchantmentStateType> parseEnchantmentStates(Object raw, List<String> problems) {
        HashMap<String, EnchantmentStateType> states = new HashMap<>();
        List<String> entries = new ArrayList<>();

        if (raw instanceof Collection<?> collection) {
            for (Object element : collection) {
                if (isMap(element)) {
                    for (Map.Entry<String, Object> e : asMap(element).entrySet()) {
                        entries.add(e.getKey() + ":" + scalarOrEmpty(e.getValue()));
                    }
                } else {
                    String s = scalar(element);
                    if (s == null) {
                        report(problems, "entry " + describe(element) + " is not a \"key:state\" string; ignored");
                    } else {
                        entries.add(s);
                    }
                }
            }
        } else if (isMap(raw)) {
            for (Map.Entry<String, Object> e : asMap(raw).entrySet()) {
                entries.add(e.getKey() + ":" + scalarOrEmpty(e.getValue()));
            }
        } else if (raw != null) {
            String s = scalar(raw);
            if (s == null) report(problems, "expected a list of \"key:state\" entries but found " + describe(raw) + "; ignored");
            else entries.add(s);
        }

        for (String entry : entries) {
            String[] parts = entry.split(":", -1);
            for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();

            if (parts.length < 2 || parts.length > 3) {
                report(problems, "entry \"" + entry + "\" is not in \"key:state\" form; ignored");
                continue;
            }

            // "key:state" or "namespace:key:state" (the namespace is dropped)
            String key = (parts.length == 2 ? parts[0] : parts[1]).toLowerCase(Locale.ROOT);
            String stateName = parts.length == 2 ? parts[1] : parts[2];
            EnchantmentStateType state = EnchantmentStateType.getStateByName(stateName);

            if (key.isEmpty()) {
                report(problems, "entry \"" + entry + "\" has no enchantment key; ignored");
            } else if (state == null) {
                report(problems, "entry \"" + entry + "\" has " + (stateName.isEmpty() ? "no state" : "unknown state \"" + stateName + "\"")
                        + " (use enable, keep, delete or disable); ignored");
            } else {
                EnchantmentStateType previous = states.put(key, state);
                if (previous != null && previous != state)
                    report(problems, "enchantment \"" + key + "\" is listed more than once; the last entry (" + state.getConfigName() + ") wins");
            }
        }

        return states;
    }

    /**
     * Parses {@code enchantment-chances}. Entries whose value is not a finite number are skipped (a skipped entry
     * means "always succeeds", never "always fails"); values outside 0-1 are clamped. Keys are lower-cased.
     *
     * @param raw      the raw value
     * @param problems receives one message per odd entry, or {@code null}
     * @return the chances by enchantment key, each within [0, 1]
     */
    public static Map<String, Double> parseChances(Object raw, List<String> problems) {
        Map<String, Double> chances = new HashMap<>();

        for (Map.Entry<String, Object> entry : asMap(raw).entrySet()) {
            String key = entry.getKey().trim().toLowerCase(Locale.ROOT);
            if (key.isEmpty()) continue;

            Double d = toDouble(entry.getValue());
            if (d == null) {
                report(problems, "chance for \"" + key + "\" must be a number between 0 and 1 but is " + describe(entry.getValue()) + "; ignored (always succeeds)");
                continue;
            }
            if (d < 0.0 || d > 1.0) {
                report(problems, "chance for \"" + key + "\" is " + d + ", outside 0 to 1; clamped to " + Math.max(0.0, Math.min(1.0, d)));
            }
            chances.put(key, Math.max(0.0, Math.min(1.0, d)));
        }

        return chances;
    }

    /**
     * Parses the XP part of {@code enchantment-costs}: {@code sharpness: 5} or {@code mending: { xp: 10 }}.
     * Unusable values are skipped (the formula applies instead), negatives clamp to 0. Keys are lower-cased.
     *
     * @param raw      the raw value
     * @param problems receives one message per odd entry, or {@code null}
     * @return the XP cost override by enchantment key
     */
    public static Map<String, Integer> parseXpCosts(Object raw, List<String> problems) {
        Map<String, Integer> costs = new HashMap<>();

        for (Map.Entry<String, Object> entry : asMap(raw).entrySet()) {
            String key = entry.getKey().trim().toLowerCase(Locale.ROOT);
            if (key.isEmpty()) continue;

            Object value = entry.getValue();
            if (isMap(value)) {
                Map<String, Object> sub = asMap(value);
                if (!sub.containsKey("xp")) {
                    if (!sub.containsKey("economy"))
                        report(problems, "cost for \"" + key + "\" has neither \"xp\" nor \"economy\"; ignored");
                    continue;
                }
                value = sub.get("xp");
            }

            Double d = toDouble(value);
            if (d == null) {
                report(problems, "XP cost for \"" + key + "\" must be a whole number but is " + describe(value) + "; ignored (the formula applies)");
                continue;
            }
            if (d < 0 || d > MAX_XP_COST) {
                report(problems, "XP cost for \"" + key + "\" is " + d + ", outside 0 to " + MAX_XP_COST + "; clamped");
            }
            costs.put(key, (int) Math.max(0, Math.min(MAX_XP_COST, d)));
        }

        return costs;
    }

    /**
     * Parses the economy part of {@code enchantment-costs}: {@code mending: { economy: 500 }}. Unusable values are
     * skipped (the flat {@code economy.cost} applies), negatives clamp to 0. Keys are lower-cased.
     *
     * @param raw      the raw value
     * @param problems receives one message per odd entry, or {@code null}
     * @return the economy cost override by enchantment key
     */
    public static Map<String, Double> parseEconomyCosts(Object raw, List<String> problems) {
        Map<String, Double> costs = new HashMap<>();

        for (Map.Entry<String, Object> entry : asMap(raw).entrySet()) {
            String key = entry.getKey().trim().toLowerCase(Locale.ROOT);
            if (key.isEmpty() || !isMap(entry.getValue())) continue;

            Map<String, Object> sub = asMap(entry.getValue());
            if (!sub.containsKey("economy")) continue;

            Double d = toDouble(sub.get("economy"));
            if (d == null) {
                report(problems, "economy cost for \"" + key + "\" must be a number but is " + describe(sub.get("economy")) + "; ignored (the flat cost applies)");
                continue;
            }
            if (d < 0 || d > MAX_ECONOMY_COST) {
                report(problems, "economy cost for \"" + key + "\" is " + d + ", outside 0 to " + (long) MAX_ECONOMY_COST + "; clamped");
            }
            costs.put(key, Math.max(0.0, Math.min(MAX_ECONOMY_COST, d)));
        }

        return costs;
    }

    /**
     * Parses {@code enchantment-groups}: a map of group name to a list of enchantment keys. A single string is a
     * one-member group; anything else makes that group ignored. Names and members are lower-cased.
     *
     * @param raw      the raw value
     * @param problems receives one message per odd group, or {@code null}
     * @return members by group name
     */
    public static Map<String, List<String>> parseGroups(Object raw, List<String> problems) {
        Map<String, List<String>> groups = new HashMap<>();

        for (Map.Entry<String, Object> entry : asMap(raw).entrySet()) {
            String name = entry.getKey().trim().toLowerCase(Locale.ROOT);
            if (name.isEmpty()) continue;

            Object value = entry.getValue();
            if (!(value instanceof Collection) && scalar(value) == null) {
                report(problems, "group \"" + name + "\" must be a list of enchantment keys but is " + describe(value) + "; ignored");
                continue;
            }

            List<String> members = new ArrayList<>();
            for (String member : toStringList(value)) members.add(member.toLowerCase(Locale.ROOT));

            if (members.isEmpty()) report(problems, "group \"" + name + "\" has no members");
            groups.put(name, Collections.unmodifiableList(members));
        }

        return groups;
    }

    // ----------------------------------------------------------------------------------------------------
    // Split count

    /**
     * Parses {@code shatterment.split-count}. Accepted shapes: a number ({@code 2}), a map with {@code min} and/or
     * {@code max}, or a range string such as {@code "2-4"} (a reversed range like {@code "5-2"} is swapped).
     * Results are clamped to [1, {@value #MAX_SPLIT_COUNT}]; anything unusable yields a fixed 1.
     *
     * @param raw the raw value
     * @return a valid range, never {@code null}
     */
    public static SplitCountRange parseSplitCount(Object raw) {
        Integer min;
        Integer max;

        if (isMap(raw)) {
            Map<String, Object> map = asMap(raw);
            min = toInt(map.get("min"));
            max = toInt(map.get("max"));
            if (min == null) min = max;
            if (max == null) max = min;
        } else if (raw instanceof String s && SPLIT_RANGE.matcher(s).matches()) {
            Matcher m = SPLIT_RANGE.matcher(s);
            m.matches();
            min = parseDigits(m.group(1));
            max = parseDigits(m.group(2));
        } else {
            min = max = toInt(raw);
        }

        if (min == null || max == null) return new SplitCountRange(1, 1);

        return new SplitCountRange(clampSplit(min), clampSplit(max));
    }

    /**
     * Explains what is wrong with a raw {@code split-count}, or {@code null} when it is absent or fine.
     *
     * @param raw the raw value, {@code null} when not set
     * @return a human-readable problem including the replacement used, or {@code null}
     */
    public static String splitCountProblem(Object raw) {
        if (raw == null) return null;

        SplitCountRange used = parseSplitCount(raw);
        String replacement = "using " + (used.isFixed() ? String.valueOf(used.min()) : used.min() + "-" + used.max());

        if (isMap(raw)) {
            Map<String, Object> map = asMap(raw);
            if (toInt(map.get("min")) == null && toInt(map.get("max")) == null)
                return "expected a number or a section with \"min\" and \"max\" but found no usable bound; " + replacement;
            if ((map.containsKey("min") && toInt(map.get("min")) == null) || (map.containsKey("max") && toInt(map.get("max")) == null))
                return "\"min\" and \"max\" must be whole numbers; " + replacement;
        } else if (raw instanceof String s && SPLIT_RANGE.matcher(s).matches()) {
            // A range string is accepted, but the canonical form is a min/max section.
        } else if (toInt(raw) == null) {
            return "expected a whole number but found " + describe(raw) + "; " + replacement;
        }

        // Compare the unclamped bounds against the clamped result.
        Double rawMin = isMap(raw) ? toDouble(asMap(raw).get("min")) : toDouble(raw);
        Double rawMax = isMap(raw) ? toDouble(asMap(raw).get("max")) : rawMin;
        if (rawMin != null && rawMax != null && (rawMin < 1 || rawMax < 1 || rawMin > MAX_SPLIT_COUNT || rawMax > MAX_SPLIT_COUNT))
            return "split count must be between 1 and " + MAX_SPLIT_COUNT + "; " + replacement;
        if (rawMin != null && rawMax != null && rawMin > rawMax)
            return "min is greater than max; the bounds are swapped (" + replacement + ")";

        return null;
    }

    private static Integer parseDigits(String digits) {
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private static int clampSplit(int value) {
        return Math.max(1, Math.min(MAX_SPLIT_COUNT, value));
    }

    private static String scalarOrEmpty(Object value) {
        String s = scalar(value);
        return s == null ? "" : s;
    }

    private static void report(List<String> problems, String message) {
        if (problems != null) problems.add(message);
    }
}
