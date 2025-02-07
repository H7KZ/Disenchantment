package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.jankominek.disenchantment.Disenchantment.plugin;

public class DisenchantWorlds {
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:worlds",
            PermissionGroupType.COMMAND_DISENCHANT_WORLDS,
            new String[]{},
            false,
            DisenchantWorlds::execute,
            DisenchantWorlds::complete
    );

    public static void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<World> disabledWorlds = Config.Disenchantment.getDisabledWorlds();

            if (disabledWorlds.isEmpty()) {
                sender.sendMessage(I18n.Commands.Worlds.Disenchantment.empty());
                return;
            }

            sender.sendMessage(I18n.Commands.Worlds.Disenchantment.title());

            for (World world : disabledWorlds) {
                sender.sendMessage(I18n.Commands.Worlds.Disenchantment.world(world.getName(), I18n.Commands.Worlds.Disenchantment.States.disabled()));
            }
        }

        if (args.length != 2) return;

        World world = Bukkit.getWorld(args[1]);

        if (world == null) {
            sender.sendMessage(I18n.Messages.worldNotFound(args[1]));
            return;
        }

        List<World> disabledWorlds = Config.Disenchantment.getDisabledWorlds();

        if (disabledWorlds.contains(world)) {
            disabledWorlds.remove(world);

            sender.sendMessage(I18n.Messages.worldIsEnabled(world.getName()));
        } else {
            disabledWorlds.add(world);

            sender.sendMessage(I18n.Messages.worldIsDisabled(world.getName()));
        }

        Config.Disenchantment.setDisabledWorlds(disabledWorlds);
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        for (String arg : plugin.getServer().getWorlds().stream().map(World::getName).toArray(String[]::new)) {
            if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                result.add(arg);
            }
        }

        return result;
    }
}
