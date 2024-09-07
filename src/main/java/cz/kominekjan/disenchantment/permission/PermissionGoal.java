package cz.kominekjan.disenchantment.permission;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

import java.util.Arrays;

import static cz.kominekjan.disenchantment.utils.TextUtils.textWithPrefixError;

public enum PermissionGoal {

    // Event permission goals
    DISENCHANTMENT_EVENT(PermissionPart.ALL, PermissionPart.ALL_ANVIL,
            PermissionPart.DISENCHANTMENT,
            PermissionPart.LEGACY_DISENCHANTMENT
    ),
    SHATTER_EVENT(PermissionPart.ALL, PermissionPart.ALL_ANVIL,
            PermissionPart.SHATTER,
            PermissionPart.LEGACY_SHATTER
    ),

    // Disenchantment commands permission goals
    DISENCHANTMENT_REPAIR_CDM(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.DISENCHANTMENT_REPAIR_CMD,
            PermissionPart.LEGACY_DISENCHANTMENT_REPAIR_CMD
    ),
    DISENCHANTMENT_MATERIAL_CDM(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.DISENCHANTMENT_MATERIALS_CMD,
            PermissionPart.LEGACY_DISENCHANTMENT_MATERIALS_CMD
    ),
    DISENCHANTMENT_SOUND_CMD(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.DISENCHANTMENT_SOUND_CMD,
            PermissionPart.LEGACY_DISENCHANTMENT_SOUND_CMD
    ),
    DISENCHANTMENT_ENCHANTMENTS_CMD(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.DISENCHANTMENT_ENCHANTMENTS_CMD,
            PermissionPart.LEGACY_DISENCHANTMENT_ENCHANTMENTS_CMD
    ),
    DISENCHANTMENT_WORLDS_CMD(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.DISENCHANTMENT_WORLDS_CMD
    ),

    // Shatterment commands permission goals
    SHATTER_REPAIR_CMD(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.SHATTER_REPAIR_CMD
    ),
    SHATTER_SOUND_CMD(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.SHATTER_SOUND_CMD
    ),
    SHATTER_ENCHANTMENTS_CMD(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.SHATTER_ENCHANTMENTS_CMD
    ),
    SHATTER_WORLDS_CMD(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.SHATTER_WORLDS_CMD
    ),

    // Other commands permission goals
    TOGGLE_CMD(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.TOGGLE_CMD
    ),
    STATUS_CMD(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.STATUS_CMD
    ),
    HELP_CMD(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.HELP_CMD
    ),
    GUI_CMD(PermissionPart.ALL, PermissionPart.ALL_CMD,
            PermissionPart.GUI_CMD
    ),

    // Gui permission goals
    GUI_USE(PermissionPart.ALL, PermissionPart.ALL_GUI,
            PermissionPart.USE_GUI
    ),

    GUI_EDIT_STATUS(PermissionPart.ALL, PermissionPart.ALL_GUI,
            PermissionPart.PLUGIN_STATUS
    ),

    GUI_EDIT_DISENCHANTMENT_REPAIR(PermissionPart.ALL, PermissionPart.ALL_GUI,
            PermissionPart.DISENCHANTMENT_REPAIR_GUI,
            PermissionPart.DISENCHANTMENT_REPAIR_GUI_ALTERNATIVE
    ),
    GUI_EDIT_SHATTERMENT_REPAIR(PermissionPart.ALL, PermissionPart.ALL_GUI,
            PermissionPart.SHATTERMENT_REPAIR_GUI
    ),
    GUI_EDIT_DISENCHANTMENT_SOUND(PermissionPart.ALL, PermissionPart.ALL_GUI,
            PermissionPart.DISENCHANTMENT_SOUND_GUI,
            PermissionPart.DISENCHANTMENT_SOUND_GUI_ALTERNATIVE
    ),
    GUI_EDIT_SHATTERMENT_SOUND(PermissionPart.ALL, PermissionPart.ALL_GUI,
            PermissionPart.SHATTERMENT_SOUND_GUI
    ),
    GUI_EDIT_DISENCHANTMENT_MATERIALS(PermissionPart.ALL, PermissionPart.ALL_GUI,
            PermissionPart.DISENCHANTMENT_MATERIAL_TOGGLE_GUI,
            PermissionPart.LEGACY_DISENCHANTMENT_MATERIAL_TOGGLE_GUI
    ),

    GUI_EDIT_ALLOWED_WORLDS(PermissionPart.ALL, PermissionPart.ALL_GUI,
            PermissionPart.WORLD_TOGGLE_GUI,
            PermissionPart.LEGACY_WORLD_TOGGLE_GUI
    ),

    GUI_EDIT_ENCHANTMENT_STATES(PermissionPart.ALL, PermissionPart.ALL_GUI,
            PermissionPart.ENCHANTMENT_TOGGLE_GUI
    ),
    ;

    private final PermissionPart[] parts;

    PermissionGoal(PermissionPart... parts) {
        this.parts = parts;
    }

    public boolean checkPermission(Permissible permissible, boolean feedback) {
        if (Arrays.stream(parts).anyMatch(p -> p.checkPermission(permissible))) return true;

        if (feedback && (permissible instanceof CommandSender sender))
            sender.sendMessage(textWithPrefixError("You don't have permission to use this feature."));

        return false;
    }

    public boolean checkPermission(Permissible permissible) {
        return checkPermission(permissible, false);
    }
}
