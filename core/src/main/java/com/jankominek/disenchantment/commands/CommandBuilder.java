package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.permissions.PermissionGroups;
import org.bukkit.command.CommandSender;

import java.util.List;

import static com.jankominek.disenchantment.utils.TextUtils.textWithPrefixError;

public class CommandBuilder {
    public final String name;
    public final PermissionGroups permission;
    public final String permissionMessage;
    public final String[] args;
    public final Boolean reqArgs;
    public final ICommandExecutor executor;
    public final ICommandCompleter completer;

    public CommandBuilder(String n, PermissionGroups p, String pm, String[] a, Boolean r, ICommandExecutor e, ICommandCompleter c) {
        this.name = n;
        this.permission = p;
        this.permissionMessage = pm;
        this.args = a;
        this.reqArgs = r;
        this.executor = e;
        this.completer = c;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!permission.checkPermission(sender)) {
            sender.sendMessage(textWithPrefixError(permissionMessage));
            return true;
        }

        if (this.args.length == 0 || !reqArgs) {
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
            sender.sendMessage(textWithPrefixError("Invalid argument!"));
            return true;
        }

        executor.execute(sender, args);

        return true;
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return completer.onTabComplete(sender, args);
    }
}
