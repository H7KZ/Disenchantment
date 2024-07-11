package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.Disenchantment.config;
import static cz.kominekjan.disenchantment.Disenchantment.plugin;
import static cz.kominekjan.disenchantment.utils.TextUtil.*;

public class Sound {
    public static final CommandUnit unit = new CommandUnit("sound", "disenchantment.sound", "You don't have permission to use this command.", new String[]{"enable", "disable", "volume", "pitch"}, false, Sound::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(TextWithPrefix("Anvil sound configuration"));
            s.sendMessage("");
            String builder = "";
            builder += ChatColor.GRAY + "Anvil sound is ";
            builder += config.getBoolean("enable-anvil-sound") ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            s.sendMessage(builder);
            s.sendMessage(ChatColor.GRAY + "Anvil volume: " + config.getInt("anvil-volume"));
            s.sendMessage(ChatColor.GRAY + "Anvil pitch: " + config.getDouble("anvil-pitch"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enable": {
                config.set("enable-anvil-sound", true);
                plugin.saveConfig();
                s.sendMessage(TextWithPrefixSuccess("Anvil sound enabled"));
                break;
            }
            case "disable": {
                config.set("enable-anvil-sound", false);
                plugin.saveConfig();
                s.sendMessage(TextWithPrefixSuccess("Anvil sound disabled"));
                break;
            }
            case "volume": {
                if (args.length == 2) {
                    s.sendMessage(TextWithPrefixError("You must specify a value"));
                    break;
                }

                try {
                    config.set("anvil-volume", Double.parseDouble(args[2]));
                    plugin.saveConfig();
                    s.sendMessage(TextWithPrefixSuccess("Anvil volume set to " + args[2]));
                } catch (NumberFormatException e) {
                    s.sendMessage(TextWithPrefixError("You must specify a valid number"));
                }
            }
            case "pitch": {
                if (args.length == 2) {
                    s.sendMessage(TextWithPrefixError("You must specify a value"));
                    break;
                }

                try {
                    config.set("anvil-pitch", Double.parseDouble(args[2]));
                    plugin.saveConfig();
                    s.sendMessage(TextWithPrefixSuccess("Anvil pitch set to " + args[2]));
                } catch (NumberFormatException e) {
                    s.sendMessage(TextWithPrefixError("You must specify a valid number"));
                }
            }
            default: {
                s.sendMessage(TextWithPrefixError("Unknown argument!"));
            }
        }
    }
}
