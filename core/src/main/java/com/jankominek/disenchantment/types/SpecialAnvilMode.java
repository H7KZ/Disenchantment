package com.jankominek.disenchantment.types;

/**
 * Which anvils may perform plugin operations (disenchant, shatter, ...). Vanilla anvil use
 * (repair, rename, combine) is never restricted by any mode.
 */
public enum SpecialAnvilMode {
    /**
     * Every anvil works. This is the default and the historical behaviour.
     */
    ALL,

    /**
     * Only anvils placed from the special anvil item work. The placement is remembered per chunk.
     */
    MARKED,

    /**
     * Only anvils that stand directly on the configured {@code block-below} material work.
     * Needs no stored data, so it also works for anvils that already exist in the world.
     */
    BLOCK_BELOW;

    /**
     * Parses a configured mode value case-insensitively. Any unknown or {@code null} value falls
     * back to {@link #ALL}, the behaviour-preserving default. Never logs, so it is safe to call
     * on every read; use {@link #match(String)} to tell a typo from a valid value.
     *
     * @param name the raw config string (may be {@code null})
     * @return the matching mode, or {@link #ALL} when unrecognised
     */
    public static SpecialAnvilMode fromConfig(String name) {
        SpecialAnvilMode matched = match(name);
        return matched != null ? matched : ALL;
    }

    /**
     * Strictly parses a mode value case-insensitively.
     *
     * @param name the raw string (may be {@code null})
     * @return the matching mode, or {@code null} when unrecognised
     */
    public static SpecialAnvilMode match(String name) {
        if (name != null) {
            for (SpecialAnvilMode mode : values()) {
                if (mode.name().equalsIgnoreCase(name.trim())) return mode;
            }
        }
        return null;
    }
}
