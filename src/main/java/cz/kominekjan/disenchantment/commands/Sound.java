package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.Disenchantment.*;

public class Sound {
    public static final CommandUnit unit = new CommandUnit("sound", "disenchantment.sound", "You don't have permission to use this command.", new String[]{"enable", "disable", "volume", "pitch"}, false, Sound::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 1) {
            sendMessage(s, "Anvil sound configuration", ChatColor.GRAY);
            s.sendMessage(ChatColor.GRAY + "|");
            s.sendMessage(ChatColor.GRAY + "| Anvil sound is " + (config.getBoolean("enable-anvil-sound") ? ChatColor.GREEN : ChatColor.RED) + (config.getBoolean("enable-anvil-sound") ? "Enabled" : "Disabled"));
            s.sendMessage(ChatColor.GRAY + "| Anvil volume: " + config.getInt("anvil-volume"));
            s.sendMessage(ChatColor.GRAY + "| Anvil pitch: " + config.getDouble("anvil-pitch"));
            return;
        }

        switch (args[1]) {
            case "enable": {
                config.set("enable-anvil-sound", true);
                plugin.saveConfig();
                sendMessage(s, "Anvil sound enabled", ChatColor.GREEN);
                break;
            }
            case "disable": {
                config.set("enable-anvil-sound", false);
                plugin.saveConfig();
                sendMessage(s, "Anvil sound disabled", ChatColor.GREEN);
                break;
            }
            case "volume": {
                if (args.length == 2) {
                    sendMessage(s, "You must specify a value");
                    break;
                }

                try {
                    config.set("anvil-volume", Double.parseDouble(args[2]));
                    plugin.saveConfig();
                    sendMessage(s, "Anvil volume set to " + args[2], ChatColor.GREEN);
                } catch (NumberFormatException e) {
                    sendMessage(s, "You must specify a valid double");
                }
            }
            case "pitch": {
                if (args.length == 2) {
                    sendMessage(s, "You must specify a value");
                    break;
                }

                try {
                    config.set("anvil-pitch", Double.parseDouble(args[2]));
                    plugin.saveConfig();
                    sendMessage(s, "Anvil pitch set to " + args[2], ChatColor.GREEN);
                } catch (NumberFormatException e) {
                    sendMessage(s, "You must specify a valid double");
                }
            }
            default: {
                sendMessage(s, "Unknown argument!");
            }
        }
    }
}
