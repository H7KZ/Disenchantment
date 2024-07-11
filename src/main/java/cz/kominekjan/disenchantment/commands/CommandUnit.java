package cz.kominekjan.disenchantment.commands;

import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.utils.TextUtil.TextWithPrefixError;

public class CommandUnit {
    public final String name;
    public final String permission;
    public final String permissionMessage;
    public final String[] args;
    public final Boolean reqArgs;
    public final CommandExecutor executor;

    public CommandUnit(String n, String p, String pm, String[] a, Boolean r, CommandExecutor e) {
        this.name = n;
        this.permission = p;
        this.permissionMessage = pm;
        this.args = a;
        this.reqArgs = r;
        this.executor = e;
    }

    public boolean execute(CommandSender s, String[] args) {
        if (!s.hasPermission(permission)) {
            s.sendMessage(TextWithPrefixError(permissionMessage));
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
            s.sendMessage(TextWithPrefixError("Invalid argument!"));
            return true;
        }

        executor.execute(s, args);

        return true;
    }
}
