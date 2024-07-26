package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.Disenchantment;
import cz.kominekjan.disenchantment.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cz.kominekjan.disenchantment.config.Config.getDisabledWorlds;
import static cz.kominekjan.disenchantment.config.Config.setDisabledWorlds;
import static cz.kominekjan.disenchantment.utils.TextUtil.textWithPrefixError;
import static cz.kominekjan.disenchantment.utils.TextUtil.textWithPrefixSuccess;

public class Toggle {
    public static final Command command = new Command(
            "toggle",
            new String[]{"disenchantment.all", "disenchantment.command.toggle"},
            "You don't have permission to use this command.",
            new String[]{},
            false,
            Toggle::execute
    );

    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            Disenchantment.toggle();
            String builder = "";
            builder += ChatColor.GRAY + "Plugin is ";
            builder += Disenchantment.enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            s.sendMessage(builder);
            return;
        }

        String wrl = args[1];

        World world = Bukkit.getWorld(wrl);

        if (world == null) {
            s.sendMessage(textWithPrefixError("World \"" + wrl + "\" does not exist!"));
            return;
        }

        List<World> disabledWorlds = getDisabledWorlds();

        if (disabledWorlds.contains(world)) {
            disabledWorlds.remove(world);

            setDisabledWorlds(disabledWorlds);

            s.sendMessage(textWithPrefixSuccess("Enabled in world \"" + world + "\""));
        } else {
            disabledWorlds.add(world);

            setDisabledWorlds(disabledWorlds);

            s.sendMessage(textWithPrefixSuccess("Disabled in world \"" + world + "\""));
        }
    }
}
