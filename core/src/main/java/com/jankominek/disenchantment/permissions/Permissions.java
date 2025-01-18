package com.jankominek.disenchantment.permissions;

import com.jankominek.disenchantment.types.PermissionType;
import org.bukkit.permissions.Permissible;

import static com.jankominek.disenchantment.Disenchantment.logger;
import static com.jankominek.disenchantment.types.PermissionType.ACTIVE;
import static com.jankominek.disenchantment.types.PermissionType.DEPRECATED;

public enum Permissions {
    ALL("disenchantment.all"),

    ANVIL_ALL("disenchantment.anvil.all"),
    ANVIL_DISENCHANT("disenchantment.anvil.disenchant"),
    ANVIL_SHATTER("disenchantment.anvil.shatter"),

    COMMAND_USE("disenchantment.command.use"),
    COMMAND_ALL("disenchantment.command.all"),
    COMMAND_GUI("disenchantment.command.gui"),
    COMMAND_HELP("disenchantment.command.help"),
    COMMAND_STATUS("disenchantment.command.status"),
    COMMAND_TOGGLE("disenchantment.command.toggle"),
    COMMAND_DISENCHANT_ENCHANTMENTS("disenchantment.command.disenchant.enchantments"),
    COMMAND_DISENCHANT_WORLDS("disenchantment.command.disenchant.worlds"),
    COMMAND_DISENCHANT_MATERIALS("disenchantment.command.disenchant.materials"),
    COMMAND_DISENCHANT_REPAIR("disenchantment.command.disenchant.repair"),
    COMMAND_DISENCHANT_SOUND("disenchantment.command.disenchant.sound"),
    COMMAND_SHATTER_ENCHANTMENTS("disenchantment.command.shatter.enchantments"),
    COMMAND_SHATTER_WORLDS("disenchantment.command.shatter.worlds"),
    COMMAND_SHATTER_REPAIR("disenchantment.command.shatter.repair"),
    COMMAND_SHATTER_SOUND("disenchantment.command.shatter.sound"),

    GUI("disenchantment.gui"),
    GUI_ALL("disenchantment.gui.all"),
    GUI_STATUS("disenchantment.gui.status"),
    GUI_WORLDS("disenchantment.gui.worlds"),
    GUI_ENCHANTMENTS("disenchantment.gui.enchantments"),
    GUI_MATERIALS("disenchantment.gui.materials"),
    GUI_DISENCHANT_REPAIR("disenchantment.gui.disenchant.repair"),
    GUI_DISENCHANT_SOUND("disenchantment.gui.disenchant.sound"),
    GUI_SHATTER_REPAIR("disenchantment.gui.shatter.repair"),
    GUI_SHATTER_SOUND("disenchantment.gui.shatter.sound"),


    /**
     * DEPRECATED PERMISSIONS
     */
    DEPRECATED_ANVIL_ALL("disenchantment.anvil", DEPRECATED),
    DEPRECATED_ANVIL_DISENCHANT("disenchantment.anvil.item", DEPRECATED),
    DEPRECATED_ANVIL_SHATTER("disenchantment.anvil.split_book", DEPRECATED),

    DEPRECATED_COMMAND_DISENCHANT_ENCHANTMENTS("disenchantment.command.enchantments", DEPRECATED),
    DEPRECATED_COMMAND_DISENCHANT_ENCHANTMENTS2("disenchantment.command.disenchant_enchantments", DEPRECATED),
    DEPRECATED_COMMAND_DISENCHANT_WORLDS("disenchantment.command.disenchant_worlds", DEPRECATED),
    DEPRECATED_COMMAND_DISENCHANT_MATERIALS("disenchantment.command.materials", DEPRECATED),
    DEPRECATED_COMMAND_DISENCHANT_MATERIALS2("disenchantment.command.disenchant_materials", DEPRECATED),
    DEPRECATED_COMMAND_DISENCHANT_REPAIR("disenchantment.command.repair", DEPRECATED),
    DEPRECATED_COMMAND_DISENCHANT_REPAIR2("disenchantment.command.disenchantment_repair", DEPRECATED),
    DEPRECATED_COMMAND_DISENCHANT_SOUND("disenchantment.command.sound", DEPRECATED),
    DEPRECATED_COMMAND_DISENCHANT_SOUND2("disenchantment.command.disenchantment_sound", DEPRECATED),
    DEPRECATED_COMMAND_SHATTER_ENCHANTMENTS("disenchantment.command.shatterment_enchantments", DEPRECATED),
    DEPRECATED_COMMAND_SHATTER_WORLDS("disenchantment.command.shatterment_worlds", DEPRECATED),
    DEPRECATED_COMMAND_SHATTER_REPAIR("disenchantment.command.shatterment_repair", DEPRECATED),
    DEPRECATED_COMMAND_SHATTER_SOUND("disenchantment.command.shatterment_sound", DEPRECATED),

    DEPRECATED_GUI_TOGGLE("disenchantment.gui.toggle", DEPRECATED),
    DEPRECATED_GUI_DISENCHANT_MATERIALS("disenchantment.gui.disenchant_materials", DEPRECATED),
    DEPRECATED_GUI_DISENCHANT_REPAIR("disenchantment.gui.disenchantment-repair", DEPRECATED),
    DEPRECATED_GUI_DISENCHANT_REPAIR2("disenchantment.gui.disenchantment_repair", DEPRECATED),
    DEPRECATED_GUI_DISENCHANT_SOUND("disenchantment.gui.disenchantment-sound", DEPRECATED),
    DEPRECATED_GUI_DISENCHANT_SOUND2("disenchantment.gui.disenchantment_sound", DEPRECATED),
    DEPRECATED_GUI_SHATTER_WORLDS("disenchantment.gui.shatter_worlds", DEPRECATED),
    DEPRECATED_GUI_SHATTER_REPAIR("disenchantment.gui.shatterment_repair", DEPRECATED),
    DEPRECATED_GUI_SHATTER_SOUND("disenchantment.gui.shatterment_sound", DEPRECATED),
    ;

    private static long DEPRECATED_LAST_WARNING = 0;
    private final String permission;
    private final PermissionType type;

    Permissions(String permission, PermissionType type) {
        this.permission = permission;
        this.type = type;
    }

    Permissions(String permission) {
        this(permission, ACTIVE);
    }

    public boolean checkPermission(Permissible permissible) {
        boolean hasPermission = permissible.hasPermission(permission);

        if (!hasPermission) return false;

        if (type == DEPRECATED) sendDeprecatedMessage();

        return true;
    }

    private void sendDeprecatedMessage() {
        long time = System.currentTimeMillis();

        if (time <= DEPRECATED_LAST_WARNING) return;

        DEPRECATED_LAST_WARNING = time + 30 * 60 * 1000; // 30 minutes

        logger.severe("/!\\ Caution /!\\");
        logger.severe("It look like you are using deprecated permission ( " + permission + ")");
        logger.severe("Checkout the new permission system at");
        logger.severe("the plugin's spigot page or the plugin's github.");
    }
}
