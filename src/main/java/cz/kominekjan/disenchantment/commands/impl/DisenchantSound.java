package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.permission.PermissionGoal;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.utils.TextUtils.*;

public class DisenchantSound {
    public static final Command command = new Command(
            "disenchant_sound",
            PermissionGoal.SHATTER_SOUND_CMD,
            "You don't have permission to use this command.",
            new String[]{"enable", "disable", "volume", "pitch"},
            false,
            DisenchantSound::execute
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
            }
            default: {
                s.sendMessage(textWithPrefixError("Unknown argument!"));
            }
        }
    }
}
