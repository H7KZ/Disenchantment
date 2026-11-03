package com.jankominek.disenchantment.types;

import java.util.Locale;

/**
 * A way to tell whether a player joined from Minecraft: Bedrock Edition (through Geyser), as listed in
 * {@code bedrock.detect}. The methods are tried in the configured order and the first hit wins.
 */
public enum BedrockDetectMethod {
    /**
     * Ask the Floodgate API ({@code FloodgateApi#isFloodgatePlayer}), if Floodgate is installed.
     */
    FLOODGATE,
    /**
     * Ask the Geyser API ({@code GeyserApi#isBedrockPlayer}), if Geyser runs on this server.
     */
    GEYSER,
    /**
     * Floodgate-style UUID: the most significant 64 bits are zero. Works when Floodgate only runs on the proxy.
     */
    UUID,
    /**
     * The player name starts with one of {@code bedrock.prefixes}.
     */
    PREFIX;

    /**
     * Matches a configured name leniently: case-insensitive, {@code -} and spaces count as {@code _}.
     *
     * @param raw the configured text
     * @return the method, or {@code null} when the text names none
     */
    public static BedrockDetectMethod match(String raw) {
        if (raw == null) return null;

        String normalised = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        for (BedrockDetectMethod method : values()) {
            if (method.name().equals(normalised)) return method;
        }

        return null;
    }
}
