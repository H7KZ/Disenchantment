package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Handles the "shatter:worlds" subcommand for managing disabled worlds
 * in the shatterment feature. Supports listing currently disabled worlds
 * and toggling individual worlds on or off.
 */
public class ShatterWorlds {
    /**
     * The command definition for the shatter:worlds subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "shatter:worlds",
            PermissionGroupType.COMMAND_SHATTER_WORLDS,
            new String[]{},
            false,
            ShatterWorlds::execute,
            ShatterWorlds::complete
    );

    /**
     * Executes the shatter:worlds command. With no extra arguments, lists all
     * disabled worlds for the shatterment feature. With a world name, toggles
     * it between enabled and disabled.
     *
     * @param sender the command sender
     * @param args   the command arguments: [subcommand, world_name]
     */
    public static void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<World> disabledWorlds = Config.Shatterment.getDisabledWorlds();

            if (disabledWorlds.isEmpty()) {
                sender.sendMessage(I18n.Commands.Worlds.Shatterment.empty());
                return;
            }

            sender.sendMessage(I18n.Commands.Worlds.Shatterment.title());

            for (World world : disabledWorlds) {
                sender.sendMessage(I18n.Commands.Worlds.Shatterment.world(world.getName(), I18n.Commands.Worlds.Shatterment.States.disabled()));
            }
        }

        if (args.length != 2) return;

        World world = Bukkit.getWorld(args[1]);

        if (world == null) {
            sender.sendMessage(I18n.Messages.worldNotFound(args[1]));
            return;
        }

        List<World> disabledWorlds = Config.Shatterment.getDisabledWorlds();

        if (disabledWorlds.contains(world)) {
            disabledWorlds.remove(world);

            sender.sendMessage(I18n.Messages.worldIsEnabled(world.getName()));
        } else {
            disabledWorlds.add(world);

            sender.sendMessage(I18n.Messages.worldIsDisabled(world.getName()));
        }

        Config.Shatterment.setDisabledWorlds(disabledWorlds);
    }

    /**
     * Provides tab completion suggestions by delegating to
     * {@link DisenchantWorlds#complete(CommandSender, String[])}.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching world name suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        return DisenchantWorlds.complete(sender, args);
    }
}
