package cz.kominekjan.disenchantment.permission;

import cz.kominekjan.disenchantment.Disenchantment;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

/**
 * Permission part is an abstraction of a permission with special condition (PermissionType).
 */
public enum PermissionPart {
    // Global Permissions
    ALL("disenchantment.all"),
    ALL_GUI("disenchantment.gui.all"),
    ALL_CMD("disenchantment.command.all"),
    ALL_ANVIL("disenchantment.anvil"), // Maybe replace with disenchantment.anvil.all ?

    // Event Permissions
    DISENCHANTMENT("disenchantment.anvil.disenchant"),
    LEGACY_DISENCHANTMENT("disenchantment.anvil.item", PermissionType.LEGACY),

    SHATTER("disenchantment.anvil.shatter"),
    LEGACY_SHATTER("disenchantment.anvil.split_book", PermissionType.LEGACY),

    // Disenchantment commands Permissions
    DISENCHANTMENT_REPAIR_CMD("disenchantment.command.disenchantment_repair"),
    LEGACY_DISENCHANTMENT_REPAIR_CMD("disenchantment.command.repair", PermissionType.LEGACY),

    DISENCHANTMENT_MATERIALS_CMD("disenchantment.command.disenchant_materials"),
    LEGACY_DISENCHANTMENT_MATERIALS_CMD("disenchantment.command.materials", PermissionType.LEGACY),

    DISENCHANTMENT_SOUND_CMD("disenchantment.command.disenchantment_sound"),
    LEGACY_DISENCHANTMENT_SOUND_CMD("disenchantment.command.sound", PermissionType.LEGACY),

    DISENCHANTMENT_ENCHANTMENTS_CMD("disenchantment.command.disenchant_enchantments"),
    LEGACY_DISENCHANTMENT_ENCHANTMENTS_CMD("disenchantment.command.enchantments", PermissionType.LEGACY),

    DISENCHANTMENT_WORLDS_CMD("disenchantment.command.disenchant_worlds"),

    // Shatterment commands Permissions
    SHATTER_REPAIR_CMD("disenchantment.command.shatterment_repair"),

    SHATTER_SOUND_CMD("disenchantment.command.shatterment_sound"),

    SHATTER_ENCHANTMENTS_CMD("disenchantment.command.shatterment_enchantments"),

    SHATTER_WORLDS_CMD("disenchantment.command.shatterment_worlds"),

    // Other commands permissions
    TOGGLE_CMD("disenchantment.command.toggle"),
    STATUS_CMD("disenchantment.command.status"),

    HELP_CMD("disenchantment.command.help"),

    GUI_CMD("disenchantment.command.gui"),

    // Gui Permissions
    USE_GUI("disenchantment.gui"),

    PLUGIN_STATUS("disenchantment.gui.status"),

    DISENCHANTMENT_REPAIR_GUI("disenchantment.gui.disenchantment_repair"),
    DISENCHANTMENT_SOUND_GUI("disenchantment.gui.disenchantment_sound"),

    WORLD_TOGGLE_GUI("disenchantment.gui.worlds"),
    LEGACY_WORLD_TOGGLE_GUI("disenchantment.gui.toggle", PermissionType.LEGACY),

    SHATTER_WORLD_TOGGLE_GUI("disenchantment.gui.shatter_worlds"),

    DISENCHANTMENT_MATERIAL_TOGGLE_GUI("disenchantment.gui.disenchant_materials"),
    LEGACY_DISENCHANTMENT_MATERIAL_TOGGLE_GUI("disenchantment.gui.materials", PermissionType.LEGACY),

    ENCHANTMENT_TOGGLE_GUI("disenchantment.gui.enchantments"),

    // Alternates permissions (- instead of _)
    DISENCHANTMENT_REPAIR_GUI_ALTERNATIVE("disenchantment.gui.disenchantment-repair", PermissionType.ALTERNATIVE),
    DISENCHANTMENT_SOUND_GUI_ALTERNATIVE("disenchantment.gui.disenchantment-sound", PermissionType.ALTERNATIVE),
    ;

    private final String permission;
    private final PermissionType type;

    PermissionPart(@NotNull String permission, @NotNull PermissionType type) {
        this.permission = permission;
        this.type = type;
    }

    PermissionPart(String permission) {
        this(permission, PermissionType.DEFAULT);
    }

    public boolean checkPermission(Permissible permissible){
        boolean hasPermission = permissible.hasPermission(permission);

        if(!hasPermission) return false;

        switch (type){
            case LEGACY -> sendLegacyMessage();
            case ALTERNATIVE -> sendAlternateMessage();
        }

        return true;
    }


    private static long LAST_LEGACY_WARNING = 0;
    // minimum delay before throwing a warning again. (currently 15minute)
    private static final long LEGACY_WARN_DELAY_MILLIS = 15 * 60 * 1000;

    private void sendLegacyMessage() {
        long time = System.currentTimeMillis();

        if (time <= LAST_LEGACY_WARNING) return;

        LAST_LEGACY_WARNING = time + LEGACY_WARN_DELAY_MILLIS;

        Disenchantment.logger.severe(ChatColor.DARK_RED + "/!\\ Caution /!\\");
        Disenchantment.logger.severe("It look like you are using deprecated permission ( " + permission + ")");
        Disenchantment.logger.severe("Deprecated permission is planned for removal and should be changed asap.");
        Disenchantment.logger.severe("");
        Disenchantment.logger.severe("Here is a list of old permission mapped with the new permission.");
        Disenchantment.logger.severe("disenchantment.anvil.item -> disenchantment.anvil.disenchant");
        Disenchantment.logger.severe("disenchantment.anvil.split_book -> disenchantment.anvil.shatter");
        Disenchantment.logger.severe("disenchantment.gui.toggle -> disenchantment.gui.worlds");
        Disenchantment.logger.severe("");
        Disenchantment.logger.severe("disenchantment.command.repair -> disenchantment.command.disenchantment_repair & disenchantment.command.shatterment_repair");
        Disenchantment.logger.severe("disenchantment.command.disenchant_materials -> disenchantment.command.disenchant_materials & disenchantment.command.shatterment_materials");
        Disenchantment.logger.severe("disenchantment.command.sound -> disenchantment.command.disenchantment_sound & disenchantment.command.shatterment_sound");
        Disenchantment.logger.severe("disenchantment.command.enchantments -> disenchantment.command.disenchant_enchantments & disenchantment.command.shattermen_enchantments");
        Disenchantment.logger.severe("");
    }


    private static long LAST_ALTERNATE_WARNING = 0;
    // minimum delay before throwing a warning again. (currently 15minute)
    private static final long ALTERNATE_WARN_DELAY_MILLIS = 15 * 60 * 1000;

    private void sendAlternateMessage() {
        long time = System.currentTimeMillis();

        if (time <= LAST_ALTERNATE_WARNING) return;

        LAST_ALTERNATE_WARNING = time + ALTERNATE_WARN_DELAY_MILLIS;

        Disenchantment.logger.severe(ChatColor.DARK_RED + "/!\\ Caution /!\\");
        Disenchantment.logger.severe("It look like you are using a misspelled permission ( " + permission + ")");
        Disenchantment.logger.severe("It was previously necessary for some part of the plugin but now is replaced with " +
                permission.replaceAll("-", "_"));
        Disenchantment.logger.severe("");
    }

}
