package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.commands.impl.*;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers all plugin subcommands and acts as the main command executor.
 * Routes incoming commands to the appropriate {@link CommandBuilder} based on
 * the first argument, falling back to the help command when no match is found.
 */
public class CommandRegister implements CommandExecutor {
    /**
     * Map of subcommand names to their {@link CommandBuilder} definitions.
     */
    public final static Map<String, CommandBuilder> commands = new HashMap<>() {
        {
            put(Help.command.name(), Help.command);
            put(GUI.command.name(), GUI.command);
            put(Status.command.name(), Status.command);
            put(Toggle.command.name(), Toggle.command);
            put(DisenchantEnchantments.command.name(), DisenchantEnchantments.command);
            put(DisenchantWorlds.command.name(), DisenchantWorlds.command);
            put(DisenchantMaterials.command.name(), DisenchantMaterials.command);
            put(DisenchantRepair.command.name(), DisenchantRepair.command);
            put(DisenchantSound.command.name(), DisenchantSound.command);
            put(ShatterEnchantments.command.name(), ShatterEnchantments.command);
            put(ShatterWorlds.command.name(), ShatterWorlds.command);
            put(ShatterRepair.command.name(), ShatterRepair.command);
            put(ShatterSound.command.name(), ShatterSound.command);
            put(Diagnostic.command.name(), Diagnostic.command);
        }
    };

    /**
     * {@inheritDoc}
     * Routes the command to the matching subcommand or defaults to the help command.
     *
     * @param sender  the command sender
     * @param command the executed command
     * @param label   the alias used
     * @param args    the command arguments
     * @return true if the command was handled, false if an exception occurred
     */
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        try {
            if (args.length == 0) return executeCommand("help", sender, args);

            return executeCommand(args[0].toLowerCase(), sender, args);
        } catch (Exception e) {
            DiagnosticUtils.throwReport(e);

            return false;
        }
    }

    private boolean executeCommand(String name, CommandSender sender, String[] args) {
        CommandBuilder command = commands.get(name.toLowerCase()) == null ? commands.get("help") : commands.get(name.toLowerCase());

        return command.execute(sender, args);
    }
}
