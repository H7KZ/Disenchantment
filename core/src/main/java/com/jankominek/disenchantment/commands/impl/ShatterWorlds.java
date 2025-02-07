package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ShatterWorlds {
    public static final CommandBuilder command = new CommandBuilder(
            "shatter:worlds",
            PermissionGroupType.COMMAND_SHATTER_WORLDS,
            new String[]{},
            false,
            ShatterWorlds::execute,
            ShatterWorlds::complete
    );

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

    public static List<String> complete(CommandSender sender, String[] args) {
        return DisenchantWorlds.complete(sender, args);
    }
}
