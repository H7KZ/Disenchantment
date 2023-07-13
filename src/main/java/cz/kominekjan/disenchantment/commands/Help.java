package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Help {
    public static final CommandUnit unit = new CommandUnit("help", "disenchantment.help", "You don't have permission to use this command.", new String[]{}, false, Help::command);

    public static void command(CommandSender s, String[] ignoredArgs) {
        s.sendMessage(ChatColor.GOLD + String.valueOf(ChatColor.BOLD) + "Disenchantment commands:");
        s.sendMessage(ChatColor.GRAY + "[ /disenchantment, /disenchant, /disench ]");
        s.sendMessage("");
        s.sendMessage(ChatColor.LIGHT_PURPLE + "/disench help" + ChatColor.RESET + " - " + ChatColor.GRAY + "Show this help message.");
        s.sendMessage(ChatColor.LIGHT_PURPLE + "/disench status" + ChatColor.RESET + " - " + ChatColor.GRAY + "Show current status of the plugin.");
        s.sendMessage(ChatColor.LIGHT_PURPLE + "/disench toggle" + ChatColor.RESET + " - " + ChatColor.GRAY + "Toggle the plugin on/off.");
        s.sendMessage(ChatColor.LIGHT_PURPLE + "/disench toggle [ world ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Toggle specific world on/off.");
        s.sendMessage(ChatColor.LIGHT_PURPLE + "/disench config" + ChatColor.RESET + " - " + ChatColor.GRAY + "Show current configuration of the plugin.");
        s.sendMessage(ChatColor.LIGHT_PURPLE + "/disench config base [ int ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Change the configuration of the base exp value.");
        s.sendMessage(ChatColor.LIGHT_PURPLE + "/disench config multiply [ float ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Change the configuration of the multiply exp value.");
        s.sendMessage(ChatColor.LIGHT_PURPLE + "/disench config enchantments" + ChatColor.RESET + " - " + ChatColor.GRAY + "Show current configuration of the disabled enchantments.");
        s.sendMessage(ChatColor.LIGHT_PURPLE + "/disench config enchantments [ enchantment name ] [ enchantment level ] [ keep or cancel ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Change the configuration of the disabled enchantments. Keep for keeping the enchantment on the item, cancel for canceling the operation of disenchanting. To again enable the enchantment, use the same command with the same arguments.");
        s.sendMessage(ChatColor.LIGHT_PURPLE + "/disench config items" + ChatColor.RESET + " - " + ChatColor.GRAY + "Show current configuration of the disabled items.");
        s.sendMessage(ChatColor.LIGHT_PURPLE + "/disench config items [ material (item) ]" + ChatColor.RESET + " - " + ChatColor.GRAY + "Change the configuration of the disabled items. To again enable the item, use the same command with the same arguments.");
    }
}
