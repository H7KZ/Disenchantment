package cz.kominekjan.disenchantment.commands;

import cz.kominekjan.disenchantment.permission.PermissionGoal;
import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.utils.TextUtils.textWithPrefixError;

public record Command(String name, PermissionGoal permission, String permissionMessage, String[] args, Boolean reqArgs,
                      ICommandExecutor executor) {

    public boolean execute(CommandSender s, String[] args) {
        if (permission.checkPermission(s)) {
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
