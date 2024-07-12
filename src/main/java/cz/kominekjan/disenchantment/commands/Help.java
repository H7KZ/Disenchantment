package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Help {
    public static final CommandUnit unit = new CommandUnit("help", "disenchantment.help", "You don't have permission to use this command.", new String[]{"1", "2", "3", "4"}, false, Help::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 0 || args.length == 1) {
            s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 1/4");
            defaultCommand(s);
            return;
        }

        switch (args[1]) {
            case "2": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 2/4");
                s.sendMessage("");
                s.sendMessage(CreateHelpTextComponent("/disench repair", "Show current configuration of repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disench repair [enable or disable]", "Enable/disable the repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disench repair reset [enable or disable]", "Enable/disable the reset of repair cost to 0 after disenchanting."));
                s.sendMessage(CreateHelpTextComponent("/disench repair base [int]", "Change the value of base repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disench repair multiply [float]", "Change the value of multiplication of repair cost based on the enchantment level and number of enchantments."));
                break;
            }
            case "3": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 3/4");
                s.sendMessage("");
                s.sendMessage(CreateHelpTextComponent("/disench enchantments", "Show current list of the disabled enchantments."));
                s.sendMessage(CreateHelpTextComponent("/disench enchantments [enchantment name] [keep or cancel]", "Change configuration of disabled enchantments. Keep - for keeping the enchantment on the item x Cancel - for canceling the operation of disenchanting. To enable the enchantment, run the same command."));
                s.sendMessage(CreateHelpTextComponent("/disench items", "Show current list of the disabled items."));
                s.sendMessage(CreateHelpTextComponent("/disench items [material]", "Change the list of disabled items. To enable the item, run the same command."));
                break;
            }
            case "4": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 4/4");
                s.sendMessage("");
                s.sendMessage(CreateHelpTextComponent("/disench sound", "Show current configuration of anvil sound."));
                s.sendMessage(CreateHelpTextComponent("/disench sound [enable or disable]", "Enable/disable the repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disench sound volume [float]", "Change the value of anvil sound volume."));
                s.sendMessage(CreateHelpTextComponent("/disench sound pitch [float]", "Change the value of anvil sound pitch."));
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
        s.sendMessage(CreateHelpTextComponent("/disench status", "Show where the plugin is enabled or disabled."));
        s.sendMessage(CreateHelpTextComponent("/disench toggle", "Toggle the plugin on/off."));
        s.sendMessage(CreateHelpTextComponent("/disench toggle [world]", "Toggle specific world on/off."));
    }

    private static String CreateHelpTextComponent(String command, String description) {
        String builder = "";
        builder += ChatColor.DARK_GRAY + command;
        builder += ChatColor.GRAY + " : ";
        builder += ChatColor.GRAY + description;

        return builder;
    }
}
