package cz.kominekjan.disenchantment.config.migrations;

import cz.kominekjan.disenchantment.Disenchantment;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

// Wait some release before removing use of previous permission
public class PermissionMigration {

    private static long LAST_WARNING = 0;
    // minimum delay before throwing a warning again. (default 15minute)
    private static final long WARN_DELAY_MILLIS = 15 * 60 * 1000;

    public static boolean checkDeprecatedPermission(Player player, String permissionNode) {
        if (!player.hasPermission(permissionNode)) return false;

        long time = System.currentTimeMillis();

        if (time <= LAST_WARNING) return true;

        LAST_WARNING = time + WARN_DELAY_MILLIS;

        Disenchantment.logger.severe(ChatColor.DARK_RED + "/!\\ Caution /!\\");
        Disenchantment.logger.severe("It look like you are using deprecated permission ( " + permissionNode + ")");
        Disenchantment.logger.severe("Deprecated permission is planned for removal and should be changed asap.");
        Disenchantment.logger.severe("");
        Disenchantment.logger.severe("Here is a list of old permission mapped with the new permission.");
        Disenchantment.logger.severe("disenchantment.anvil.item -> disenchantment.anvil.disenchant");
        Disenchantment.logger.severe("disenchantment.anvil.split_book -> disenchantment.anvil.shatter");
        Disenchantment.logger.severe("");

        return true;
    }

}
