package com.jankominek.disenchantment.config;

/**
 * One human-readable problem found in {@code config.yml} by the {@link ConfigValidator}.
 * A problem never stops the plugin: the offending value is replaced by a safe default (or clamped)
 * at read time, and the file on disk is never modified.
 *
 * @param path    the dot-path of the offending option, e.g. {@code disenchantment.anvil.sound.volume}
 * @param message what is wrong and what the plugin does instead
 */
public record ConfigIssue(String path, String message) {
    /**
     * Formats the issue as a single log/diagnostic line.
     *
     * @return {@code "<path>: <message>"}
     */
    public String format() {
        return path + ": " + message;
    }
}
