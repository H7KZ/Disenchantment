package com.jankominek.disenchantment.types;

import com.jankominek.disenchantment.config.I18n;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

import java.util.stream.Stream;

/**
 * Groups related {@link PermissionType} entries together for permission checks.
 * Each group represents a logical action (e.g., using disenchant anvil, opening GUI)
 * and grants access if the player holds any one of the permissions in the group.
 * This allows backward compatibility with deprecated permission nodes.
 */
public enum PermissionGroupType {
    DISENCHANT_EVENT(
            PermissionType.ANVIL_ALL,
            PermissionType.ANVIL_DISENCHANT,
            PermissionType.DEPRECATED_ANVIL_DISENCHANT
    ),
    SHATTER_EVENT(
            PermissionType.ANVIL_ALL,
            PermissionType.ANVIL_SHATTER,
            PermissionType.DEPRECATED_ANVIL_SHATTER
    ),

    COMMAND_GUI(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_GUI
    ),
    COMMAND_HELP(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_HELP
    ),
    COMMAND_STATUS(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_STATUS
    ),
    COMMAND_TOGGLE(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_TOGGLE
    ),
    COMMAND_DISENCHANT_ENCHANTMENTS(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_DISENCHANT_ENCHANTMENTS,
            PermissionType.DEPRECATED_COMMAND_DISENCHANT_ENCHANTMENTS,
            PermissionType.DEPRECATED_COMMAND_DISENCHANT_ENCHANTMENTS2
    ),
    COMMAND_DISENCHANT_WORLDS(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_DISENCHANT_WORLDS
    ),
    COMMAND_DISENCHANT_MATERIALS(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_DISENCHANT_MATERIALS,
            PermissionType.DEPRECATED_COMMAND_DISENCHANT_MATERIALS,
            PermissionType.DEPRECATED_COMMAND_DISENCHANT_MATERIALS2
    ),
    COMMAND_DISENCHANT_REPAIR(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_DISENCHANT_REPAIR,
            PermissionType.DEPRECATED_COMMAND_DISENCHANT_REPAIR,
            PermissionType.DEPRECATED_COMMAND_DISENCHANT_REPAIR2
    ),
    COMMAND_DISENCHANT_SOUND(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_DISENCHANT_SOUND,
            PermissionType.DEPRECATED_COMMAND_DISENCHANT_SOUND,
            PermissionType.DEPRECATED_COMMAND_DISENCHANT_SOUND2
    ),
    COMMAND_SHATTER_ENCHANTMENTS(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_SHATTER_ENCHANTMENTS,
            PermissionType.DEPRECATED_COMMAND_SHATTER_ENCHANTMENTS
    ),
    COMMAND_SHATTER_WORLDS(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_SHATTER_WORLDS,
            PermissionType.DEPRECATED_COMMAND_SHATTER_WORLDS
    ),
    COMMAND_SHATTER_REPAIR(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_SHATTER_REPAIR,
            PermissionType.DEPRECATED_COMMAND_SHATTER_REPAIR
    ),
    COMMAND_SHATTER_SOUND(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_SHATTER_SOUND,
            PermissionType.DEPRECATED_COMMAND_SHATTER_SOUND
    ),
    COMMAND_DIAGNOSTIC(
            PermissionType.COMMAND_ALL,
            PermissionType.COMMAND_DIAGNOSTIC
    ),

    GUI(
            PermissionType.GUI_ALL,
            PermissionType.GUI
    ),
    GUI_STATUS(
            PermissionType.GUI_ALL,
            PermissionType.GUI_STATUS,
            PermissionType.DEPRECATED_GUI_TOGGLE
    ),
    GUI_WORLDS(
            PermissionType.GUI_ALL,
            PermissionType.GUI_WORLDS,
            PermissionType.DEPRECATED_GUI_SHATTER_WORLDS
    ),
    GUI_ENCHANTMENTS(
            PermissionType.GUI_ALL,
            PermissionType.GUI_ENCHANTMENTS
    ),
    GUI_MATERIALS(
            PermissionType.GUI_ALL,
            PermissionType.GUI_MATERIALS,
            PermissionType.DEPRECATED_GUI_DISENCHANT_MATERIALS
    ),
    GUI_DISENCHANT_REPAIR(
            PermissionType.GUI_ALL,
            PermissionType.GUI_DISENCHANT_REPAIR,
            PermissionType.DEPRECATED_GUI_DISENCHANT_REPAIR,
            PermissionType.DEPRECATED_GUI_DISENCHANT_REPAIR2
    ),
    GUI_DISENCHANT_SOUND(
            PermissionType.GUI_ALL,
            PermissionType.GUI_DISENCHANT_SOUND,
            PermissionType.DEPRECATED_GUI_DISENCHANT_SOUND,
            PermissionType.DEPRECATED_GUI_DISENCHANT_SOUND2
    ),
    GUI_SHATTER_REPAIR(
            PermissionType.GUI_ALL,
            PermissionType.GUI_SHATTER_REPAIR,
            PermissionType.DEPRECATED_GUI_SHATTER_REPAIR
    ),
    GUI_SHATTER_SOUND(
            PermissionType.GUI_ALL,
            PermissionType.GUI_SHATTER_SOUND,
            PermissionType.DEPRECATED_GUI_SHATTER_SOUND
    ),
    ;

    private final PermissionType[] permissions;

    PermissionGroupType(PermissionType... permissions) {
        this.permissions = permissions;
    }

    /**
     * Checks if the given permissible has any permission in this group.
     * Sends a "requires permission" message if the check fails.
     *
     * @param permissible the entity to check permissions for
     * @return {@code true} if the permissible holds at least one permission in this group
     */
    public boolean hasPermission(Permissible permissible) {
        return hasPermission(permissible, true);
    }

    /**
     * Checks if the given permissible has any permission in this group.
     *
     * @param permissible the entity to check permissions for
     * @param feedback    if {@code true}, sends a "requires permission" message on failure
     * @return {@code true} if the permissible holds at least one permission in this group
     */
    public boolean hasPermission(Permissible permissible, boolean feedback) {
        if (Stream.of(permissions).anyMatch(p -> p == PermissionType.ALL)) return true;
        if (Stream.of(permissions).anyMatch(p -> p.hasPermission(permissible))) return true;

        if (permissible instanceof CommandSender sender && feedback)
            sender.sendMessage(I18n.Messages.requiresPermission());

        return false;
    }
}
