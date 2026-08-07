package com.jankominek.disenchantment.types;

/**
 * How a restriction {@code list} (worlds or materials) gates an anvil operation.
 * <p>
 * A restriction list is configured as a nested {@code mode} + {@code list} block, e.g.
 * <pre>
 * worlds:
 *   mode: DENYLIST
 *   list: [ ]
 * </pre>
 */
public enum RestrictionMode {
    /**
     * The operation is allowed everywhere / for every material <em>except</em> the entries in
     * the list. An empty list allows everything. This is the historical behaviour of the
     * old flat {@code disabled-worlds} / {@code disabled-materials} keys.
     */
    DENYLIST,

    /**
     * The operation is allowed <em>only</em> for the entries in the list. An empty list allows
     * nothing (the feature is effectively disabled), which the plugin warns about at startup.
     */
    ALLOWLIST;

    /**
     * Parses a configured mode value case-insensitively. Any unknown or {@code null} value
     * falls back to {@link #DENYLIST} (the safe, behaviour-preserving default). Callers that
     * want to surface a misconfiguration should validate the raw value separately — this
     * method never logs, so it is safe to call on every read.
     *
     * @param name the raw config string (may be {@code null})
     * @return the matching mode, or {@link #DENYLIST} when unrecognised
     */
    public static RestrictionMode fromConfig(String name) {
        RestrictionMode matched = match(name);
        return matched != null ? matched : DENYLIST;
    }

    /**
     * Strictly parses a mode value case-insensitively, returning {@code null} when the value is
     * unrecognised. Used by commands to distinguish a valid mode from a typo (unlike
     * {@link #fromConfig(String)}, which silently falls back to {@link #DENYLIST}).
     *
     * @param name the raw string (may be {@code null})
     * @return the matching mode, or {@code null} when unrecognised
     */
    public static RestrictionMode match(String name) {
        if (name != null) {
            for (RestrictionMode mode : values()) {
                if (mode.name().equalsIgnoreCase(name.trim())) return mode;
            }
        }
        return null;
    }

    /**
     * Returns {@code true} when the given value is restricted (blocked) under this mode.
     *
     * @param present whether the value is present in the restriction list
     * @return {@code true} if the operation must be blocked for that value
     */
    public boolean isRestricted(boolean present) {
        return this == ALLOWLIST ? !present : present;
    }

    /**
     * Returns the opposite mode — used by the GUI toggle buttons to cycle between the two modes.
     *
     * @return {@link #ALLOWLIST} when this is {@link #DENYLIST}, and vice versa
     */
    public RestrictionMode toggled() {
        return this == DENYLIST ? ALLOWLIST : DENYLIST;
    }
}
