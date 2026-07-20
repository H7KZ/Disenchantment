package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.EconomyUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.jankominek.disenchantment.Disenchantment.enabled;

/**
 * Handles the "status" subcommand which displays the current enabled/disabled
 * state of the plugin globally, and for the disenchantment and shatterment
 * features individually.
 * Supports "--json" argument for machine-readable output (RCON/console friendly).
 */
public class Status {
    /**
     * The command definition for the status subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "status",
            PermissionGroupType.COMMAND_STATUS,
            new String[]{},
            false,
            Status::execute,
            Status::complete
    );

    /**
     * Displays the current enabled state of all plugin features. Pass {@code --json} as the
     * first argument for a machine-readable JSON response.
     *
     * @param s    the command sender
     * @param args the command arguments; args[0] may be "--json"
     */
    public static void execute(CommandSender s, String[] args) {
        boolean json = args.length > 1 && "--json".equalsIgnoreCase(args[1]);
        if (json) {
            s.sendMessage(buildJson());
            return;
        }
        s.sendMessage(I18n.Commands.Status.title());
        s.sendMessage("§7Version: §f" + Disenchantment.plugin.getDescription().getVersion());
        s.sendMessage(I18n.Commands.Status.global(enabled ? I18n.Commands.Status.States.enabled() : I18n.Commands.Status.States.disabled()));
        s.sendMessage(I18n.Commands.Status.disenchantment(Config.Disenchantment.isEnabled() ? I18n.Commands.Status.States.enabled() : I18n.Commands.Status.States.disabled()));
        s.sendMessage(I18n.Commands.Status.shatterment(Config.Shatterment.isEnabled() ? I18n.Commands.Status.States.enabled() : I18n.Commands.Status.States.disabled()));
    }

    private static String buildJson() {
        String version = Disenchantment.plugin.getDescription().getVersion();
        boolean disenchantEnabled = Config.Disenchantment.isEnabled();
        boolean shatterEnabled = Config.Shatterment.isEnabled();
        boolean economyConnected = EconomyUtils.isAvailable();

        // ponytail: no Vault API call for name — derive from registered provider class name
        String economyPluginName = "null";
        if (economyConnected) {
            try {
                net.milkbowl.vault.economy.Economy e =
                        Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
                economyPluginName = "\"" + e.getName().replace("\"", "\\\"") + "\"";
            } catch (Exception ignored) {
            }
        }

        List<String> adapters = SupportedPluginManager.getAllActivatedPlugins()
                .stream()
                .map(ISupportedPlugin::getName)
                .map(n -> "\"" + n.replace("\"", "\\\"") + "\"")
                .collect(Collectors.toList());
        String adaptersJson = "[" + String.join(",", adapters) + "]";

        long uptimeSeconds = 0;
        if (Disenchantment.startedAt != null) {
            uptimeSeconds = Duration.between(Disenchantment.startedAt, Instant.now()).getSeconds();
        }

        String serverVersion = Bukkit.getVersion().replace("\"", "\\\"");

        return "{" +
                "\"enabled\":" + enabled + "," +
                "\"version\":\"" + version.replace("\"", "\\\"") + "\"," +
                "\"disenchant_enabled\":" + disenchantEnabled + "," +
                "\"shatter_enabled\":" + shatterEnabled + "," +
                "\"economy_connected\":" + economyConnected + "," +
                "\"economy_plugin_name\":" + economyPluginName + "," +
                "\"active_adapters\":" + adaptersJson + "," +
                "\"uptime_seconds\":" + uptimeSeconds + "," +
                "\"server_version\":\"" + serverVersion + "\"" +
                "}";
    }

    /**
     * Suggests {@code --json} as the only tab completion option.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list containing "--json", or an empty list after the first argument
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) return List.of("--json");
        return List.of();
    }
}
