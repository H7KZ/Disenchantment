package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "diagnostic" subcommand which generates a diagnostic report
 * of the plugin state. For players, the report is presented as a clickable
 * chat message that copies the data to clipboard. The optional "all" argument
 * includes extended diagnostic information.
 */
public class Diagnostic {
    /**
     * The command definition for the diagnostic subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "diagnostic",
            PermissionGroupType.COMMAND_DIAGNOSTIC,
            new String[]{"all"},
            false,
            Diagnostic::execute,
            Diagnostic::complete
    );

    /**
     * Executes the diagnostic command, generating and sending a diagnostic report.
     * Players receive a clickable message to copy the report; console receives plain text.
     *
     * @param sender the command sender
     * @param args   the command arguments; if args[1] is "all", extended info is included
     */
    public static void execute(CommandSender sender, String[] args) {
        boolean extendedInfo = args.length > 1 && args[1].equalsIgnoreCase("all");

        String result = DiagnosticUtils.getReport(extendedInfo, sender);

        if (sender instanceof Player player) {
            TextComponent message = new TextComponent(ChatColor.GREEN + "Click to copy diagnostic data");
            message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, result));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to copy")));

            player.spigot().sendMessage(message);
        } else {
            sender.sendMessage(result);
        }
    }

    /**
     * Provides tab completion suggestions for the diagnostic command.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching argument suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        for (String arg : Diagnostic.command.args()) {
            if (arg.toLowerCase().startsWith(args[1].toLowerCase())) result.add(arg);
        }

        return result;
    }
}
