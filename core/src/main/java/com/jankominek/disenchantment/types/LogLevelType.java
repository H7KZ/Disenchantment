package com.jankominek.disenchantment.types;

/**
 * Log verbosity levels for the Disenchantment plugin.
 * <ul>
 *   <li>{@link #NONE}  – suppresses all non-fatal startup output</li>
 *   <li>{@link #INFO}  – logs a startup summary (NMS module, activated plugin adapters)</li>
 *   <li>{@link #DEBUG} – logs everything, including per-operation details</li>
 * </ul>
 * Levels are ordered by ascending verbosity; {@link #isAtLeast} can be used to check
 * whether the configured level is verbose enough to emit a given message.
 */
public enum LogLevelType {
    NONE,
    INFO,
    DEBUG;

    /**
     * Returns {@code true} if this level is at least as verbose as the given {@code minimum}.
     *
     * @param minimum the minimum required verbosity level
     * @return {@code true} if this level is equal to or more verbose than {@code minimum}
     */
    public boolean isAtLeast(LogLevelType minimum) {
        return this.ordinal() >= minimum.ordinal();
    }
}
