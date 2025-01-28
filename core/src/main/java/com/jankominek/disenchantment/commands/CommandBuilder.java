package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandBuilder {
    public final String name;
    public final PermissionGroupType permission;
    public final String[] args;
    public final boolean requireArgs;
    public final ICommandExecutor executor;
    public final ICommandCompleter completer;

    public CommandBuilder(String n, PermissionGroupType p, String[] a, boolean r, ICommandExecutor e, ICommandCompleter c) {
        this.name = n;
        this.permission = p;
        this.args = a;
        this.requireArgs = r;
        this.executor = e;
        this.completer = c;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!permission.hasPermission(sender)) {
            sender.sendMessage(I18n.getPrefix() + " " + I18n.Messages.requiresPermission());
            return true;
        }

        if (this.args.length == 0 || !requireArgs) {
            executor.execute(sender, args);
            return true;
        }

        boolean areArgsValid = true;

        for (String arg : this.args)
            if (arg.equalsIgnoreCase(args[1])) {
                areArgsValid = false;
                break;
            }

        if (areArgsValid) {
            sender.sendMessage(I18n.getPrefix() + " " + I18n.Messages.invalidArgument());
            return true;
        }

        executor.execute(sender, args);

        return true;
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return completer.onTabComplete(sender, args);
    }
}
