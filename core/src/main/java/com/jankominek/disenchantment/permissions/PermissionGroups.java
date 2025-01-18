package com.jankominek.disenchantment.permissions;

import com.jankominek.disenchantment.utils.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

import java.util.Arrays;

public enum PermissionGroups {
    DISENCHANT_EVENT(
            Permissions.ALL,
            Permissions.ANVIL_ALL,
            Permissions.ANVIL_DISENCHANT,
            Permissions.DEPRECATED_ANVIL_DISENCHANT
    ),
    SHATTER_EVENT(
            Permissions.ALL,
            Permissions.ANVIL_ALL,
            Permissions.ANVIL_SHATTER,
            Permissions.DEPRECATED_ANVIL_SHATTER
    ),

    COMMAND_GUI(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_GUI
    ),
    COMMAND_HELP(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_HELP
    ),
    COMMAND_STATUS(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_STATUS
    ),
    COMMAND_TOGGLE(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_TOGGLE
    ),
    COMMAND_DISENCHANT_ENCHANTMENTS(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_DISENCHANT_ENCHANTMENTS,
            Permissions.DEPRECATED_COMMAND_DISENCHANT_ENCHANTMENTS,
            Permissions.DEPRECATED_COMMAND_DISENCHANT_ENCHANTMENTS2
    ),
    COMMAND_DISENCHANT_WORLDS(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_DISENCHANT_WORLDS
    ),
    COMMAND_DISENCHANT_MATERIALS(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_DISENCHANT_MATERIALS,
            Permissions.DEPRECATED_COMMAND_DISENCHANT_MATERIALS,
            Permissions.DEPRECATED_COMMAND_DISENCHANT_MATERIALS2
    ),
    COMMAND_DISENCHANT_REPAIR(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_DISENCHANT_REPAIR,
            Permissions.DEPRECATED_COMMAND_DISENCHANT_REPAIR,
            Permissions.DEPRECATED_COMMAND_DISENCHANT_REPAIR2
    ),
    COMMAND_DISENCHANT_SOUND(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_DISENCHANT_SOUND,
            Permissions.DEPRECATED_COMMAND_DISENCHANT_SOUND,
            Permissions.DEPRECATED_COMMAND_DISENCHANT_SOUND2
    ),
    COMMAND_SHATTER_ENCHANTMENTS(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_SHATTER_ENCHANTMENTS,
            Permissions.DEPRECATED_COMMAND_SHATTER_ENCHANTMENTS
    ),
    COMMAND_SHATTER_WORLDS(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_SHATTER_WORLDS,
            Permissions.DEPRECATED_COMMAND_SHATTER_WORLDS
    ),
    COMMAND_SHATTER_REPAIR(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_SHATTER_REPAIR,
            Permissions.DEPRECATED_COMMAND_SHATTER_REPAIR
    ),
    COMMAND_SHATTER_SOUND(
            Permissions.ALL,
            Permissions.COMMAND_ALL,
            Permissions.COMMAND_SHATTER_SOUND,
            Permissions.DEPRECATED_COMMAND_SHATTER_SOUND
    ),

    GUI(
            Permissions.ALL,
            Permissions.GUI_ALL,
            Permissions.GUI
    ),
    GUI_STATUS(
            Permissions.ALL,
            Permissions.GUI_ALL,
            Permissions.GUI_STATUS,
            Permissions.DEPRECATED_GUI_TOGGLE
    ),
    GUI_WORLDS(
            Permissions.ALL,
            Permissions.GUI_ALL,
            Permissions.GUI_WORLDS,
            Permissions.DEPRECATED_GUI_SHATTER_WORLDS
    ),
    GUI_ENCHANTMENTS(
            Permissions.ALL,
            Permissions.GUI_ALL,
            Permissions.GUI_ENCHANTMENTS
    ),
    GUI_MATERIALS(
            Permissions.ALL,
            Permissions.GUI_ALL,
            Permissions.GUI_MATERIALS,
            Permissions.DEPRECATED_GUI_DISENCHANT_MATERIALS
    ),
    GUI_DISENCHANT_REPAIR(
            Permissions.ALL,
            Permissions.GUI_ALL,
            Permissions.GUI_DISENCHANT_REPAIR,
            Permissions.DEPRECATED_GUI_DISENCHANT_REPAIR,
            Permissions.DEPRECATED_GUI_DISENCHANT_REPAIR2
    ),
    GUI_DISENCHANT_SOUND(
            Permissions.ALL,
            Permissions.GUI_ALL,
            Permissions.GUI_DISENCHANT_SOUND,
            Permissions.DEPRECATED_GUI_DISENCHANT_SOUND,
            Permissions.DEPRECATED_GUI_DISENCHANT_SOUND2
    ),
    GUI_SHATTER_REPAIR(
            Permissions.ALL,
            Permissions.GUI_ALL,
            Permissions.GUI_SHATTER_REPAIR,
            Permissions.DEPRECATED_GUI_SHATTER_REPAIR
    ),
    GUI_SHATTER_SOUND(
            Permissions.ALL,
            Permissions.GUI_ALL,
            Permissions.GUI_SHATTER_SOUND,
            Permissions.DEPRECATED_GUI_SHATTER_SOUND
    ),
    ;

    private final Permissions[] parts;

    PermissionGroups(Permissions... parts) {
        this.parts = parts;
    }

    public boolean checkPermission(Permissible permissible, boolean feedback) {
        if (Arrays.stream(parts).anyMatch(p -> p.checkPermission(permissible))) return true;

        if (feedback && (permissible instanceof CommandSender sender))
            sender.sendMessage(TextUtils.textWithPrefixError("You do not have permission to use this feature."));

        return false;
    }

    public boolean checkPermission(Permissible permissible) {
        return checkPermission(permissible, true);
    }
}
