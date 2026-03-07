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

import static com.jankominek.disenchantment.Disenchantment.logger;

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
            new String[]{"all", "save", "log"},
            false,
            Diagnostic::execute,
            Diagnostic::complete
    );

    /**
     * Executes the diagnostic command.
     * <ul>
     *   <li>{@code /disenchantment diagnostic}     – players get a GitHub-ready click-to-copy message;
     *                                                console receives the plain-text report</li>
     *   <li>{@code /disenchantment diagnostic all}  – same as above but with extended details</li>
     *   <li>{@code /disenchantment diagnostic save} – writes the full report to
     *                                                 {@code plugins/Disenchantment/logs/} and
     *                                                 reports the file path to the sender</li>
     *   <li>{@code /disenchantment diagnostic log}  – prints the full report to the server console;
     *                                                 useful for RCON users who cannot click in-game</li>
     * </ul>
     *
     * @param sender the command sender
     * @param args   the command arguments
     */
    public static void execute(CommandSender sender, String[] args) {
        if (args.length > 1) {
            String sub = args[1].toLowerCase();

            if (sub.equals("save")) {
                String report = DiagnosticUtils.getReport(true, sender);
                String path = DiagnosticUtils.saveReportToFile(report, "diagnostic");
                if (path != null) {
                    sender.sendMessage(ChatColor.GREEN + "Diagnostic report saved to: " + ChatColor.WHITE + path);
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to save diagnostic report. Check the server console for details.");
                }
                return;
            }

            if (sub.equals("log")) {
                String report = DiagnosticUtils.getReport(true, sender);
                logger.info("\n" + report);
                sender.sendMessage(ChatColor.GREEN + "Full diagnostic report printed to the server console.");
                return;
            }
        }

        boolean extendedInfo = args.length > 1 && args[1].equalsIgnoreCase("all");
        String report = DiagnosticUtils.getReport(extendedInfo, sender);

        if (sender instanceof Player player) {
            // Wrap in a GitHub code block so the player can paste it directly into an issue
            String githubReport = "```\n" + report + "```";

            TextComponent message = new TextComponent(ChatColor.GREEN + "Click to copy diagnostic data (GitHub-ready)");
            message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, githubReport));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Copies a GitHub-formatted code block.\n§7Use §f/disenchantment diagnostic save§7 to write to a file.")));

            player.spigot().sendMessage(message);
        } else {
            sender.sendMessage(report);
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
