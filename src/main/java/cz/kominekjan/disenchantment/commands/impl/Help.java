package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import cz.kominekjan.disenchantment.permission.PermissionGoal;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Help {
    public static final Command command = new Command(
            "help",
            PermissionGoal.HELP_CMD,
            "You don't have permission to use this command.",
            new String[]{"1", "2", "3", "4", "5", "6", "7", "8"},
            false,
            Help::execute
    );

    public static void execute(CommandSender s, String[] args) {
        if (args.length == 0 || args.length == 1) {
            s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 1/8");
            defaultCommand(s);
            return;
        }

        switch (args[1]) {
            case "2": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 2/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disench status", "Show where the plugin is enabled or disabled."));
                s.sendMessage(CreateHelpTextComponent("/disench toggle", "Toggle the plugin on/off."));
                break;
            }
            case "3": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 3/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disench disenchant_enchantments", "Show current list of the disabled enchantments."));
                s.sendMessage(CreateHelpTextComponent("/disench disenchant_enchantments [enchantment name] ['enable', 'keep' or 'cancel']", "Change configuration of disabled enchantments."));
                s.sendMessage(CreateHelpTextComponent("/disench disenchant_worlds", "Show current list of the disabled worlds."));
                s.sendMessage(CreateHelpTextComponent("/disench disenchant_worlds [world]", "Toggle specific world to enable/disable disenchant. To enable the world, run the same command."));
                s.sendMessage(CreateHelpTextComponent("/disench disenchant_materials", "Show current list of the disabled materials."));
                s.sendMessage(CreateHelpTextComponent("/disench disenchant_materials [material]", "Change the list of disabled materials. To enable the material, run the same command."));
                break;
            }
            case "4": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 4/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disench disenchant_repair", "Show current configuration of repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disench disenchant_repair [enable or disable]", "Enable/disable the repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disench disenchant_repair reset [enable or disable]", "Enable/disable the reset of repair cost to 0 after disenchanting."));
                s.sendMessage(CreateHelpTextComponent("/disench disenchant_repair base [int]", "Change the value of base repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disench disenchant_repair multiply [float]", "Change the value of multiplication of repair cost based on the enchantment level and number of enchantments."));
                break;
            }
            case "5": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 5/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disench disenchant_sound", "Show current configuration of anvil sound."));
                s.sendMessage(CreateHelpTextComponent("/disench disenchant_sound [enable or disable]", "Enable/disable the repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disench disenchant_sound volume [float]", "Change the value of anvil sound volume."));
                s.sendMessage(CreateHelpTextComponent("/disench disenchant_sound pitch [float]", "Change the value of anvil sound pitch."));
                break;
            }
            case "6": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 6/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disench shatter_enchantments", "Show current list of the disabled enchantments."));
                s.sendMessage(CreateHelpTextComponent("/disench shatter_enchantments [enchantment name] ['enable', 'keep' or 'cancel']", "Change configuration of disabled enchantments."));
                s.sendMessage(CreateHelpTextComponent("/disench shatter_worlds", "Show current list of the disabled worlds."));
                s.sendMessage(CreateHelpTextComponent("/disench shatter_worlds [world]", "Toggle specific world to enable/disable disenchant. To enable the world, run the same command."));
                break;
            }
            case "7": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 7/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disench shatter_repair", "Show current configuration of repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disench shatter_repair [enable or disable]", "Enable/disable the repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disench shatter_repair reset [enable or disable]", "Enable/disable the reset of repair cost to 0 after disenchanting."));
                s.sendMessage(CreateHelpTextComponent("/disench shatter_repair base [int]", "Change the value of base repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disench shatter_repair multiply [float]", "Change the value of multiplication of repair cost based on the enchantment level and number of enchantments."));
                break;
            }
            case "8": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 8/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disench shatter_sound", "Show current configuration of anvil sound."));
                s.sendMessage(CreateHelpTextComponent("/disench shatter_sound [enable or disable]", "Enable/disable the repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disench shatter_sound volume [float]", "Change the value of anvil sound volume."));
                s.sendMessage(CreateHelpTextComponent("/disench shatter_sound pitch [float]", "Change the value of anvil sound pitch."));
                break;
            }
            default: {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 1/4");
                defaultCommand(s);
            }
        }
    }

    private static void defaultCommand(CommandSender s) {
        s.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[ /disenchantment, /disenchant, /disench ]");
        s.sendMessage("");

        s.sendMessage(CreateHelpTextComponent("/disench help [page]", "Show this help message."));
        s.sendMessage(CreateHelpTextComponent("/disench gui", "Open the GUI for configuration."));
    }

    private static String CreateHelpTextComponent(String command, String description) {
        String builder = "";

        builder += ChatColor.DARK_GRAY + command;
        builder += ChatColor.GRAY + " : ";
        builder += ChatColor.GRAY + description;

        return builder;
    }
}
