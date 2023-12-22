package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Help {
    public static final CommandUnit unit = new CommandUnit("help", "disenchantment.help", "You don't have permission to use this command.", new String[]{"1", "2", "3", "4"}, false, Help::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(ChatColor.GOLD + String.valueOf(ChatColor.BOLD) + "Disenchantment Help page 1/4");
            defaultCommand(s);
            return;
        }

        switch (args[1]) {
            case "2": {
                s.sendMessage(ChatColor.GOLD + String.valueOf(ChatColor.BOLD) + "Disenchantment Help page 2/4");
                s.sendMessage(ChatColor.DARK_GRAY + "|");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench repair" + ChatColor.RESET + " - " + ChatColor.GRAY + "Show current configuration of repair cost.");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench repair [ enable or disable ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Enable/disable the repair cost.");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench repair reset [ enable or disable ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Enable/disable the reset of repair cost to 0 after disenchanting.");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench repair base [ int ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Change the value of base repair cost.");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench repair multiply [ float ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Change the value of multiplication of repair cost based on the enchantment level and number of enchantments.");
                break;
            }
            case "3": {
                s.sendMessage(ChatColor.GOLD + String.valueOf(ChatColor.BOLD) + "Disenchantment Help page 3/4");
                s.sendMessage(ChatColor.DARK_GRAY + "|");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench enchantments" + ChatColor.RESET + " - " + ChatColor.GRAY + "Show current list of the disabled enchantments.");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench enchantments [ enchantment name ] [ enchantment level ] [ keep or cancel ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Change configuration of disabled enchantments. Keep - for keeping the enchantment on the item x Cancel - for canceling the operation of disenchanting. To enable the enchantment, run the same command.");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench items" + ChatColor.RESET + " - " + ChatColor.GRAY + "Show current list of the disabled items.");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench items [ material (item) ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Change the list of disabled items. To enable the item, run the same command.");
                break;
            }
            case "4": {
                s.sendMessage(ChatColor.GOLD + String.valueOf(ChatColor.BOLD) + "Disenchantment Help page 4/4");
                s.sendMessage(ChatColor.DARK_GRAY + "|");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench sound" + ChatColor.RESET + " - " + ChatColor.GRAY + "Show current configuration of anvil sound.");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench sound [ enable or disable ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Enable/disable the repair cost.");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench sound volume [ float ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Change the value of anvil sound volume.");
                s.sendMessage(ChatColor.DARK_GRAY + "| /disench sound pitch [ float ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Change the value of anvil sound pitch.");
                break;
            }
            default: {
                s.sendMessage(ChatColor.GOLD + String.valueOf(ChatColor.BOLD) + "Disenchantment Help 1/4");
                defaultCommand(s);
            }
        }
    }

    private static void defaultCommand(CommandSender s) {
        s.sendMessage(ChatColor.GRAY + "[ /disenchantment, /disenchant, /disench ]");
        s.sendMessage(ChatColor.DARK_GRAY + "|");
        s.sendMessage(ChatColor.DARK_GRAY + "| /disench help [ page ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Show this help message.");
        s.sendMessage(ChatColor.DARK_GRAY + "| /disench status" + ChatColor.RESET + " - " + ChatColor.GRAY + "Show where the plugin is enabled or disabled.");
        s.sendMessage(ChatColor.DARK_GRAY + "| /disench toggle" + ChatColor.RESET + " - " + ChatColor.GRAY + "Toggle the plugin on/off.");
        s.sendMessage(ChatColor.DARK_GRAY + "| /disench toggle [ world ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Toggle specific world on/off.");
    }
}
