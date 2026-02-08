package com.jankominek.disenchantment.types;

/**
 * Indicates whether a {@link PermissionType} is currently active or has been
 * deprecated in favor of a newer permission node. Deprecated permissions still
 * function but log a warning to encourage migration to the new permission system.
 */
public enum PermissionStateType {
    /**
     * The permission node is current and actively supported.
     */
    ACTIVE,
    /**
     * The permission node is deprecated and will log a migration warning when used.
     */
    DEPRECATED,
}
