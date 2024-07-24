package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.Disenchantment;
import cz.kominekjan.disenchantment.commands.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cz.kominekjan.disenchantment.Disenchantment.plugin;
import static cz.kominekjan.disenchantment.config.Config.getDisabledWorlds;
import static cz.kominekjan.disenchantment.config.Config.setDisabledWorlds;
import static cz.kominekjan.disenchantment.utils.TextUtil.textWithPrefixError;
import static cz.kominekjan.disenchantment.utils.TextUtil.textWithPrefixSuccess;

public class Toggle {
    public static final Command command = new Command("toggle", "disenchantment.toggle", "You don't have permission to use this command.", new String[]{}, false, Toggle::execute);

    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            Disenchantment.toggle();
            String builder = "";
            builder += ChatColor.GRAY + "Plugin is ";
            builder += Disenchantment.enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            s.sendMessage(builder);
            return;
        }

        String world = args[1];

        List<String> disabledWorlds = getDisabledWorlds();

        if (disabledWorlds.contains(world)) {
            disabledWorlds.remove(world);

            setDisabledWorlds(disabledWorlds);

            s.sendMessage(textWithPrefixSuccess("Enabled in world \"" + world + "\""));
        } else {
            if (!plugin.getServer().getWorlds().contains(plugin.getServer().getWorld(world))) {
                s.sendMessage(textWithPrefixError("World \"" + world + "\" does not exist!"));
                return;
            }

            disabledWorlds.add(world);

            setDisabledWorlds(disabledWorlds);

            s.sendMessage(textWithPrefixSuccess("Disabled in world \"" + world + "\""));
        }
    }
}
