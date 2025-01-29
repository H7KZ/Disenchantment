package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class DisenchantSound {
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:sound",
            PermissionGroupType.COMMAND_DISENCHANT_SOUND,
            new String[]{"enable", "disable", "volume", "pitch"},
            false,
            DisenchantSound::execute,
            DisenchantSound::complete
    );

    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(textWithPrefix("Anvil sound configuration"));
            s.sendMessage("");

            String builder = "";

            builder += ChatColor.GRAY + "Anvil sound is ";
            builder += Config.Disenchantment.Anvil.Sound.isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";

            s.sendMessage(builder);
            s.sendMessage(ChatColor.GRAY + "Anvil volume: " + Config.Disenchantment.Anvil.Sound.getVolume());
            s.sendMessage(ChatColor.GRAY + "Anvil pitch: " + Config.Disenchantment.Anvil.Sound.getPitch());
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enable": {
                Config.Disenchantment.Anvil.Sound.setEnabled(true);
                s.sendMessage(textWithPrefixSuccess("Anvil sound enabled"));
                break;
            }
            case "disable": {
                Config.Disenchantment.Anvil.Sound.setEnabled(false);
                s.sendMessage(textWithPrefixSuccess("Anvil sound disabled"));
                break;
            }
            case "volume": {
                if (args.length == 2) {
                    s.sendMessage(textWithPrefixError("You must specify a value"));
                    break;
                }

                try {
                    Config.Disenchantment.Anvil.Sound.setVolume(Double.parseDouble(args[2]));
                    s.sendMessage(textWithPrefixSuccess("Anvil volume set to " + args[2]));
                } catch (NumberFormatException e) {
                    s.sendMessage(textWithPrefixError("You must specify a valid number"));
                }

                break;
            }
            case "pitch": {
                if (args.length == 2) {
                    s.sendMessage(textWithPrefixError("You must specify a value"));
                    break;
                }

                try {
                    Config.Disenchantment.Anvil.Sound.setPitch(Double.parseDouble(args[2]));
                    s.sendMessage(textWithPrefixSuccess("Anvil pitch set to " + args[2]));
                } catch (NumberFormatException e) {
                    s.sendMessage(textWithPrefixError("You must specify a valid number"));
                }

                break;
            }
            default: {
                s.sendMessage(textWithPrefixError("Unknown argument!"));
            }
        }
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        for (String arg : DisenchantSound.command.args) {
            if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                result.add(arg);
            }
        }

        return result;
    }
}
