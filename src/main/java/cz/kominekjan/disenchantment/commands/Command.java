package cz.kominekjan.disenchantment.commands;

import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.utils.TextUtil.textWithPrefixError;

public class Command {
    public final String name;
    public final String permission;
    public final String permissionMessage;
    public final String[] args;
    public final Boolean reqArgs;
    public final ICommandExecutor executor;

    public Command(String name, String permission, String permissionMessage, String[] args, Boolean reqArgs, ICommandExecutor executor) {
        this.name = name;
        this.permission = permission;
        this.permissionMessage = permissionMessage;
        this.args = args;
        this.reqArgs = reqArgs;
        this.executor = executor;
    }

    public boolean execute(CommandSender s, String[] args) {
        if (!s.hasPermission(permission)) {
            s.sendMessage(textWithPrefixError(permissionMessage));
            return true;
        }

        if (this.args.length == 0 || !reqArgs) {
            executor.execute(s, args);
            return true;
        }

        boolean isArg = true;

        for (String arg : this.args)
            if (arg.equalsIgnoreCase(args[1])) {
                isArg = false;
                break;
            }

        if (isArg) {
            s.sendMessage(textWithPrefixError("Invalid argument!"));
            return true;
        }

        executor.execute(s, args);

        return true;
    }
}
