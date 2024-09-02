package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.permission.PermissionGoal;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cz.kominekjan.disenchantment.utils.TextUtils.textWithPrefixError;
import static cz.kominekjan.disenchantment.utils.TextUtils.textWithPrefixSuccess;

public class ShatterWorlds {
    public static final Command command = new Command(
            "shatter_worlds",
            PermissionGoal.SHATTER_WORLDS_CMD,
            "You don't have permission to use this command.",
            new String[]{},
            false,
            ShatterWorlds::execute
    );

    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            List<World> disabledWorlds = Config.Shatterment.getDisabledWorlds();

            if (disabledWorlds.isEmpty()) {
                s.sendMessage(ChatColor.GRAY + "No worlds are disabled");
                return;
            }

            s.sendMessage(ChatColor.GRAY + "Disabled worlds:");
            s.sendMessage("");

            for (World world : disabledWorlds) {
                String builder = "";
                builder += ChatColor.RED + "[X] ";
                builder += ChatColor.GRAY + "\"" + world.getName() + "\"";

                s.sendMessage(builder);
            }
        }

        if (args.length != 2) return;

        String wrl = args[1];

        World world = Bukkit.getWorld(wrl);

        if (world == null) {
            s.sendMessage(textWithPrefixError("World \"" + wrl + "\" does not exist!"));
            return;
        }

        List<World> disabledWorlds = Config.Shatterment.getDisabledWorlds();

        if (disabledWorlds.contains(world)) {
            disabledWorlds.remove(world);

            s.sendMessage(textWithPrefixSuccess("Enabled in world \"" + world + "\""));
        } else {
            disabledWorlds.add(world);

            s.sendMessage(textWithPrefixSuccess("Disabled in world \"" + world + "\""));
        }

        Config.Shatterment.setDisabledWorlds(disabledWorlds);
    }
}
