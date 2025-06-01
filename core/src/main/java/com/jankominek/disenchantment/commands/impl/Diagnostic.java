package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.nms.NMSMapper;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class Diagnostic {
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:diagnostic",
            PermissionGroupType.COMMAND_DIAGNOSTIC,
            new String[]{"all"},
            false,
            Diagnostic::execute,
            Diagnostic::complete
    );

    private static final String SPACER = "\n----------------";

    public static void execute(CommandSender s, String[] args) {
        boolean allInfo = args.length > 0 && args[0].equalsIgnoreCase("all");

        StringBuilder result = new StringBuilder();
        result.append("Disenchantment diagnostic information:");
        try {
            // Generic debug section
            result.append(SPACER);

            result.append("\n-Server Version: ")
                    .append(ChatColor.COLOR_CHAR).append(NMSMapper.hasNMS() ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(Bukkit.getVersion())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
            result.append("\n-Plugin Version: ")
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                    .append(Disenchantment.plugin.getDescription().getVersion())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
            result.append("\n-Plugin Enabled: ")
                    .append(ChatColor.COLOR_CHAR).append(Config.isPluginEnabled() ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(Config.isPluginEnabled())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end

            // Get currently activated plugin(s) or None if none enabled
            List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();
            StringBuilder activatedPluginStr = new StringBuilder();
            if (activatedPlugins.isEmpty()) {
                activatedPluginStr.append("None");
            } else {
                for (ISupportedPlugin plugin : activatedPlugins) {
                    activatedPluginStr.append(plugin.getName()).append(", ");
                }
                activatedPluginStr.delete(activatedPluginStr.length() - 2, activatedPluginStr.length());
            }

            result.append("\n-Activated Plugins: ")
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                    .append(activatedPluginStr)
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end

            // Disenchantment debug section
            result.append(SPACER);

            result.append("\n-Disenchantment Enabled: ")
                    .append(ChatColor.COLOR_CHAR).append(Config.Disenchantment.isEnabled() ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(Config.Disenchantment.isEnabled())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end

            result.append("\n-Disenchantment Cost Enabled: ")
                    .append(ChatColor.COLOR_CHAR).append(Config.Disenchantment.Anvil.Repair.isCostEnabled() ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(Config.Disenchantment.Anvil.Repair.isCostEnabled())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
            result.append("\n-Disenchantment Reset Enabled: ")
                    .append(ChatColor.COLOR_CHAR).append(Config.Disenchantment.Anvil.Repair.isCostEnabled() ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(Config.Disenchantment.Anvil.Repair.isResetEnabled())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
            result.append("\n-Disenchantment Base cost: ")
                    .append(ChatColor.COLOR_CHAR).append(Config.Disenchantment.Anvil.Repair.isCostEnabled() ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(Config.Disenchantment.Anvil.Repair.getBaseCost())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
            result.append("\n-Disenchantment Cost multiplier: ")
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                    .append(Config.Disenchantment.Anvil.Repair.getBaseCost())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end

            if (allInfo) {
                // Disenchantment sound section
                result.append(SPACER);
                result.append("\n-Disenchantment Sound Enabled: ")
                        .append(ChatColor.COLOR_CHAR).append(Config.Disenchantment.Anvil.Sound.isEnabled() ? ChatColor.GREEN : ChatColor.RED) // Color start
                        .append(Config.Disenchantment.Anvil.Sound.isEnabled())
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
                result.append("\n-Disenchantment Sound Volume: ")
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                        .append(Config.Disenchantment.Anvil.Sound.getVolume())
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
                result.append("\n-Disenchantment Sound Pitch: ")
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                        .append(Config.Disenchantment.Anvil.Sound.getPitch())
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end

                // Disenchantment Disabled Worlds & Materials TODO
            }

            // Shatterment debug section
            result.append(SPACER);

            result.append("\n-Shatterment Enabled: ")
                    .append(ChatColor.COLOR_CHAR).append(Config.Shatterment.isEnabled() ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(Config.Shatterment.isEnabled())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end

            result.append("\n-Shatterment Cost Enabled: ")
                    .append(ChatColor.COLOR_CHAR).append(Config.Shatterment.Anvil.Repair.isCostEnabled() ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(Config.Shatterment.Anvil.Repair.isCostEnabled())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
            result.append("\n-Shatterment Reset Enabled: ")
                    .append(ChatColor.COLOR_CHAR).append(Config.Shatterment.Anvil.Repair.isCostEnabled() ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(Config.Shatterment.Anvil.Repair.isResetEnabled())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
            result.append("\n-Shatterment Base cost: ")
                    .append(ChatColor.COLOR_CHAR).append(Config.Shatterment.Anvil.Repair.isCostEnabled() ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(Config.Shatterment.Anvil.Repair.getBaseCost())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
            result.append("\n-Shatterment Cost multiplier: ")
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                    .append(Config.Shatterment.Anvil.Repair.getBaseCost())
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end

            if (allInfo) {
                // Shatterment sound section
                result.append(SPACER);
                result.append("\n-Shatterment Sound Enabled: ")
                        .append(ChatColor.COLOR_CHAR).append(Config.Shatterment.Anvil.Sound.isEnabled() ? ChatColor.GREEN : ChatColor.RED) // Color start
                        .append(Config.Shatterment.Anvil.Sound.isEnabled())
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
                result.append("\n-Shatterment Sound Volume: ")
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                        .append(Config.Shatterment.Anvil.Sound.getVolume())
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
                result.append("\n-Shatterment Sound Pitch: ")
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                        .append(Config.Shatterment.Anvil.Sound.getPitch())
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end

                // Shatterment Disabled Worlds & Materials TODO
            }

            // Permission test section
            result.append(SPACER);
            boolean hasDisenchantmentPerm = PermissionGroupType.DISENCHANT_EVENT.hasPermission(s);
            boolean hasShattermentPerm = PermissionGroupType.DISENCHANT_EVENT.hasPermission(s);
            result.append("\nHas Disenchantment Permission: ")
                    .append(ChatColor.COLOR_CHAR).append(hasDisenchantmentPerm ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(hasDisenchantmentPerm)
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
            result.append("\nHas Shatterment Permission: ")
                    .append(ChatColor.COLOR_CHAR).append(hasShattermentPerm ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(hasShattermentPerm)
                    .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end

        } catch (Exception e) {
            result.append(SPACER);
            result.append("\nAn error occurred while trying to get some diagnostic info:\n");
            result.append(e.getMessage());
        }

        result.append(SPACER);

        s.sendMessage(result.toString());
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        return null;
    }


}
