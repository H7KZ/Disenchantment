package cz.kominekjan.disenchantment.commands;

import cz.kominekjan.disenchantment.Disenchantment;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cz.kominekjan.disenchantment.Disenchantment.plugin;
import static cz.kominekjan.disenchantment.config.Config.getDisabledWorlds;
import static cz.kominekjan.disenchantment.config.Config.setDisabledWorlds;
import static cz.kominekjan.disenchantment.utils.TextUtils.textWithPrefixError;
import static cz.kominekjan.disenchantment.utils.TextUtils.textWithPrefixSuccess;

public class Toggle {
    public static final CommandUnit unit = new CommandUnit("toggle", "disenchantment.toggle", "You don't have permission to use this command.", new String[]{}, false, Toggle::command);

    public static void command(CommandSender s, String[] args) {
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
