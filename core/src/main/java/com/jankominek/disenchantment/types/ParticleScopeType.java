package com.jankominek.disenchantment.types;

/**
 * Who sees the success particles of a disenchant, shatter or erase operation.
 */
public enum ParticleScopeType {
    /**
     * Only the player who performed the operation sees the particles.
     */
    PLAYER,
    /**
     * Every player near the anvil sees the particles.
     */
    WORLD;

    /**
     * Parses a config value, falling back to {@link #PLAYER} for missing or unknown values.
     *
     * @param value the raw config string
     * @return the matching scope, never {@code null}
     */
    public static ParticleScopeType fromConfig(String value) {
        if (value == null) return PLAYER;

        for (ParticleScopeType scope : values()) {
            if (scope.name().equalsIgnoreCase(value.trim())) return scope;
        }

        return PLAYER;
    }
}
