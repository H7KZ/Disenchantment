package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.commands.impl.*;
import com.jankominek.disenchantment.utils.ErrorUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Map;

public class CommandRegister implements CommandExecutor {
    public final static Map<String, CommandBuilder> commands = new HashMap<>() {
        {
            put(Help.command.name, Help.command);
            put(GUI.command.name, GUI.command);
            put(Status.command.name, Status.command);
            put(Toggle.command.name, Toggle.command);
            put(DisenchantEnchantments.command.name, DisenchantEnchantments.command);
            put(DisenchantWorlds.command.name, DisenchantWorlds.command);
            put(DisenchantMaterials.command.name, DisenchantMaterials.command);
            put(DisenchantRepair.command.name, DisenchantRepair.command);
            put(DisenchantSound.command.name, DisenchantSound.command);
            put(ShatterEnchantments.command.name, ShatterEnchantments.command);
            put(ShatterWorlds.command.name, ShatterWorlds.command);
            put(ShatterRepair.command.name, ShatterRepair.command);
            put(ShatterSound.command.name, ShatterSound.command);
        }
    };

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        try {
            if (args.length == 0) return executeCommand("help", sender, args);

            return executeCommand(args[0].toLowerCase(), sender, args);
        } catch (Exception e) {
            ErrorUtils.fullReportError(e);

            return false;
        }
    }

    private boolean executeCommand(String name, CommandSender sender, String[] args) {
        CommandBuilder command = commands.get(name.toLowerCase()) == null ? commands.get("help") : commands.get(name.toLowerCase());

        return command.execute(sender, args);
    }
}
