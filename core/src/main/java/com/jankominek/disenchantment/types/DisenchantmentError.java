package com.jankominek.disenchantment.types;

/**
 * Stable error codes for player-facing messages.
 * Never reorder or reuse a code number.
 */
public enum DisenchantmentError {
    PERMISSION_DENIED("DISE-001"),
    WORLD_DISABLED("DISE-002"),
    MATERIAL_DISABLED("DISE-003"),
    NOT_ENOUGH_XP("DISE-004"),
    NOT_ENOUGH_MONEY("DISE-005"),
    ECONOMY_NOT_AVAILABLE("DISE-006"),
    CANCELLED_BY_EVENT("DISE-007"),
    UNEXPECTED_ERROR("DISE-008");

    private final String code;

    DisenchantmentError(String code) {
        this.code = code;
    }

    /**
     * Returns the stable error code string (e.g. {@code "DISE-001"}).
     *
     * @return the error code
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the error code wrapped in brackets, suitable for log output (e.g. {@code "[DISE-001]"}).
     */
    @Override
    public String toString() {
        return "[" + code + "]";
    }
}
