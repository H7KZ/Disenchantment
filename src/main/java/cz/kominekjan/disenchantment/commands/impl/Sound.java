package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.config.Config.*;
import static cz.kominekjan.disenchantment.utils.TextUtil.*;

public class Sound {
    public static final Command command = new Command("sound", "disenchantment.sound", "You don't have permission to use this command.", new String[]{"enable", "disable", "volume", "pitch"}, false, Sound::execute);

    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(textWithPrefix("Anvil sound configuration"));
            s.sendMessage("");
            String builder = "";
            builder += ChatColor.GRAY + "Anvil sound is ";
            builder += getEnableAnvilSound() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            s.sendMessage(builder);
            s.sendMessage(ChatColor.GRAY + "Anvil volume: " + getAnvilSoundVolume());
            s.sendMessage(ChatColor.GRAY + "Anvil pitch: " + getAnvilSoundPitch());
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enable": {
                setEnableAnvilSound(true);
                s.sendMessage(textWithPrefixSuccess("Anvil sound enabled"));
                break;
            }
            case "disable": {
                setEnableAnvilSound(false);
                s.sendMessage(textWithPrefixSuccess("Anvil sound disabled"));
                break;
            }
            case "volume": {
                if (args.length == 2) {
                    s.sendMessage(textWithPrefixError("You must specify a value"));
                    break;
                }

                try {
                    setAnvilSoundVolume(Integer.parseInt(args[2]));
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
                    setAnvilSoundPitch(Float.parseFloat(args[2]));
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
