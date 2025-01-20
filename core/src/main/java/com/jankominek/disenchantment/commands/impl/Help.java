package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.permissions.PermissionGroups;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class Help {
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

                s.sendMessage(CreateHelpTextComponent("/disenchant gui", "Open GUI for disenchantment configuration."));
                s.sendMessage(CreateHelpTextComponent("/disenchant status", "Show if the plugin is enabled or disabled."));
                s.sendMessage(CreateHelpTextComponent("/disenchant toggle", "Toggle the plugin on/off."));
                break;
            }
            case "3": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 3/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:enchantments", "Show current list of the enchantment settings."));
                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:enchantments [enchantment name] ['enable', 'keep', 'delete', 'cancel']", "Change configuration of disabled enchantments."));
                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:worlds", "Show current list of the disabled worlds."));
                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:worlds [world]", "Toggle specific world to enable/disable disenchant."));
                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:materials", "Show current list of the disabled materials."));
                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:materials [material]", "Toggle specific material to enable/disable disenchantment."));
                break;
            }
            case "4": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 4/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:repair", "Show current configuration of repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:repair ['enable', 'disable']", "Enable/disable repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:repair reset ['enable', 'disable']", "Enable/disable the reset of repair cost to 0 after disenchantment."));
                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:repair base [int]", "Change the value of base repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:repair multiply [float]", "Change the value of multiplication of repair cost based on the enchantment level and number of enchantments."));
                break;
            }
            case "5": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 5/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:sound", "Show current configuration of anvil sound."));
                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:sound ['enable', 'disable']", "Enable/disable the repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:sound volume [float]", "Change the value of anvil sound volume."));
                s.sendMessage(CreateHelpTextComponent("/disenchant disenchant:sound pitch [float]", "Change the value of anvil sound pitch."));
                break;
            }
            case "6": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 6/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:enchantments", "Show current list of the disabled enchantments."));
                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:enchantments [enchantment name] ['enable', 'keep', 'delete', 'cancel']", "Change configuration of disabled enchantments."));
                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:worlds", "Show current list of the disabled worlds."));
                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:worlds [world]", "Toggle specific world to enable/disable disenchant. To enable the world, run the same command."));
                break;
            }
            case "7": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 7/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:repair", "Show current configuration of repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:repair ['enable', 'disable']", "Enable/disable repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:repair reset [enable or disable]", "Enable/disable the reset of repair cost to 0 after shatterment."));
                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:repair base [int]", "Change value of the base repair cost."));
                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:repair multiply [float]", "Change value of the multiplication for repair cost."));
                break;
            }
            case "8": {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 8/8");
                s.sendMessage("");

                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:sound", "Show current configuration of anvil sound for shatterment."));
                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:sound ['enable', 'disable']", "Enable/disable anvil sound."));
                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:sound volume [float]", "Change value of the anvil sound volume."));
                s.sendMessage(CreateHelpTextComponent("/disenchant shatter:sound pitch [float]", "Change value of the anvil sound pitch."));
                break;
            }
            default: {
                s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Disenchantment Help page 1/8");
                defaultCommand(s);
            }
        }
    }

    private static void defaultCommand(CommandSender s) {
        s.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[ /disenchantment, /disenchant ]");
        s.sendMessage("");

        s.sendMessage(CreateHelpTextComponent("/disenchant help [page]", "Show command help."));
        s.sendMessage(CreateHelpTextComponent("/disenchant gui", "Open GUI for disenchantment configuration."));
    }

    private static String CreateHelpTextComponent(String command, String description) {
        String builder = "";

        builder += ChatColor.DARK_GRAY + command;
        builder += ChatColor.GRAY + " : ";
        builder += ChatColor.GRAY + description;

        return builder;
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        for (String arg : Help.command.args) {
            if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                result.add(arg);
            }
        }

        return result;
    }

    public static final CommandBuilder command = new CommandBuilder(
            "help",
            PermissionGroups.COMMAND_HELP,
            "You don't have permission to use this command.",
            new String[]{"1", "2", "3", "4", "5", "6", "7", "8"},
            false,
            Help::execute,
            Help::complete
    );


}
