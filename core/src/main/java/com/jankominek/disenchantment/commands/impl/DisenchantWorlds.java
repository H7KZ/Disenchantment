package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.jankominek.disenchantment.Disenchantment.plugin;
import static com.jankominek.disenchantment.utils.TextUtils.textWithPrefixError;
import static com.jankominek.disenchantment.utils.TextUtils.textWithPrefixSuccess;

public class DisenchantWorlds {
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:worlds",
            PermissionGroupType.COMMAND_DISENCHANT_WORLDS,
            "You don't have permission to use this command.",
            new String[]{},
            false,
            DisenchantWorlds::execute,
            DisenchantWorlds::complete
    );

    public static void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<World> disabledWorlds = Config.Disenchantment.getDisabledWorlds();

            if (disabledWorlds.isEmpty()) {
                sender.sendMessage(ChatColor.GRAY + "No worlds are disabled");
                return;
            }

            sender.sendMessage(ChatColor.GRAY + "Disabled worlds:");
            sender.sendMessage("");

            for (World world : disabledWorlds) {
                String builder = "";
                builder += ChatColor.RED + "[X] ";
                builder += ChatColor.GRAY + "\"" + world.getName() + "\"";

                sender.sendMessage(builder);
            }
        }

        if (args.length != 2) return;

        World world = Bukkit.getWorld(args[1]);

        if (world == null) {
            sender.sendMessage(textWithPrefixError("World \"" + args[1] + "\" does not exist!"));
            return;
        }

        List<World> disabledWorlds = Config.Disenchantment.getDisabledWorlds();

        if (disabledWorlds.contains(world)) {
            disabledWorlds.remove(world);

            sender.sendMessage(textWithPrefixSuccess("Enabled in world \"" + world + "\""));
        } else {
            disabledWorlds.add(world);

            sender.sendMessage(textWithPrefixSuccess("Disabled in world \"" + world + "\""));
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
