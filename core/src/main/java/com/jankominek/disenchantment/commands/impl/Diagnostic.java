package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.nms.NMSMapper;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

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

    public static void execute(CommandSender sender, String[] args) {
        boolean allInfo = args.length > 0 && args[0].equalsIgnoreCase("all");

        StringBuilder result = new StringBuilder();
        result.append("Disenchantment diagnostic information:");
        try {
            String currentWorld;
            if (sender instanceof Player player) {
                currentWorld = player.getWorld().getName();
            } else {
                currentWorld = "None (not a player)";
            }

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

                // Disenchantment Disabled Worlds, Materials & enchantment states
                result.append(SPACER);

                result.append("\n-Disenchantment Disabled Worlds: ")
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                        .append(readDisabledWorld(Config.Disenchantment.getDisabledWorlds()))
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
                result.append("\n-Current World: ")
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                        .append(currentWorld)
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end

                result.append("\n-Disenchantment Disabled Materials: ")
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                        .append(readDisabledMaterials(Config.Disenchantment.getDisabledMaterials()))
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end

                result.append("\n-Disenchantment Enchantment States:");
                writeEnchantmentSates(result, "disenchantment", Config.Disenchantment.getEnchantmentStates());
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

                // Shatterment Disabled Worlds, Materials & enchantment states
                result.append(SPACER);

                result.append("\n-Shatterment Disabled Worlds: ")
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                        .append(readDisabledWorld(Config.Shatterment.getDisabledWorlds()))
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end
                result.append("\n-Current World: ")
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.YELLOW) // Color start
                        .append(currentWorld)
                        .append(ChatColor.COLOR_CHAR).append(ChatColor.RESET); // Color end

                result.append("\n-Shatterment Enchantment States:");
                writeEnchantmentSates(result, "shatterment", Config.Shatterment.getEnchantmentStates());
            }

            // Permission test section
            result.append(SPACER);
            boolean hasDisenchantmentPerm = PermissionGroupType.DISENCHANT_EVENT.hasPermission(sender);
            boolean hasShattermentPerm = PermissionGroupType.DISENCHANT_EVENT.hasPermission(sender);
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

        sender.sendMessage(result.toString());
    }

    public static String readDisabledWorld(List<World> disabledWorlds) {
        StringBuilder dsbWorlds = new StringBuilder();
        if (disabledWorlds.isEmpty()) {
            dsbWorlds.append("None");
        } else {
            for (World world : disabledWorlds) {
                dsbWorlds.append(world.getName()).append(", ");
            }
            dsbWorlds.delete(dsbWorlds.length() - 2, dsbWorlds.length());
        }

        return dsbWorlds.toString();
    }

    public static String readDisabledMaterials(List<Material> disabledMaterials) {
        StringBuilder dsbWorlds = new StringBuilder();
        if (disabledMaterials.isEmpty()) {
            dsbWorlds.append("None");
        } else {
            for (Material material : disabledMaterials) {
                dsbWorlds.append(material).append(", ");
            }
            dsbWorlds.delete(dsbWorlds.length() - 2, dsbWorlds.length());
        }

        return dsbWorlds.toString();
    }


    public static void writeEnchantmentSates(StringBuilder stb, String prefix, Map<Enchantment, EnchantmentStateType> enchantmentStates) {
        enchantmentStates.forEach((key, val) -> {
            stb.append('\n').append(prefix).append('.').append(key.getKey())
                    .append('=').append(val.getDisplayName()).append('\n');
        });
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        return null;
    }


}
