package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.nms.NMSMapper;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.generator.WorldInfo;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.util.*;
import java.util.stream.Collectors;

import static com.jankominek.disenchantment.Disenchantment.logger;
import static com.jankominek.disenchantment.Disenchantment.plugin;

public class DiagnosticUtils {
    public static String getReport(Boolean extended, CommandSender sender) {
        return buildReport(extended, sender);
    }

    public static void throwReport(Throwable e) {
        StringBuilder report = new StringBuilder();

        report.append("Disenchantment encountered an error and had to be disabled.\n");
        report.append("Please report this error to the plugin author.\n");
        report.append("Link to the plugin's issue tracker: https://github.com/H7KZ/Disenchantment/issues\n");
        report.append("Copy the following message.\n\n");

        report.append("Disenchantment Diagnostic Report\n");
        report.append("=================================\n\n");
        report.append(buildReport(true, null));
        report.append("\n");

        report.append("Error Details\n");
        report.append("==============\n\n");

        report.append("The following error occurred: ").append(e.getMessage());
        report.append("\n\n");
        for (StackTraceElement element : e.getStackTrace()) {
            report.append(element.toString()).append("\n");
        }

        logger.severe(report.toString());
    }

    private static String buildReport(Boolean extended, CommandSender sender) {
        StringBuilder report = new StringBuilder();
        try {
            report.append("Disenchantment Diagnostic Report\n");
            report.append("=================================\n\n");

            report.append("Plugin Version: ").append(plugin.getDescription().getVersion()).append("\n");
            report.append("Server Version: ").append(Bukkit.getVersion()).append(" (").append(Bukkit.getName()).append(')').append("\n");
            report.append("Plugin Enabled: ").append(plugin.isEnabled() ? "Yes" : "No").append("\n");
            report.append("Is Using NMS: ").append(NMSMapper.hasNMS() ? "Yes" : "No").append("\n");
            report.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
            report.append("OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("\n");
            report.append("Architecture: ").append(System.getProperty("os.arch")).append("\n\n");

            report.append("Memory Usage\n");
            report.append("=============\n\n");
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory() / 1024 / 1024; // Convert to MB
            long freeMemory = runtime.freeMemory() / 1024 / 1024; // Convert to MB
            long maxMemory = runtime.maxMemory() / 1024 / 1024; // Convert to MB
            report.append("Total Memory: ").append(totalMemory).append(" MB\n");
            report.append("Free Memory: ").append(freeMemory).append(" MB\n");
            report.append("Max Memory: ").append(maxMemory).append(" MB\n\n");

            report.append("System Information\n");
            report.append("===================\n\n");
            report.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
            report.append("Java Vendor: ").append(System.getProperty("java.vendor")).append("\n");
            report.append("OS Name: ").append(System.getProperty("os.name")).append("\n");
            report.append("OS Version: ").append(System.getProperty("os.version")).append("\n");
            report.append("OS Architecture: ").append(System.getProperty("os.arch")).append("\n\n");

            report.append("Current Server Information\n");
            report.append("============================\n\n");

            String currentWorld;
            if (sender instanceof Player player) {
                currentWorld = player.getWorld().getName();
            } else {
                currentWorld = "None (not a player)";
            }

            report.append("Current World: ").append(currentWorld).append("\n");

            List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

            if (activatedPlugins.isEmpty()) {
                report.append("Activated Plugins: None\n");
            } else {
                report.append("Activated Plugins: ")
                        .append(
                                activatedPlugins
                                        .stream()
                                        .map(plugin ->
                                                plugin.getName() +
                                                        " v" +
                                                        Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(plugin.getName()))
                                                                .getDescription()
                                                                .getVersion()
                                        )
                                        .reduce((a, b) -> a + ", " + b)
                                        .orElse("None")
                        )
                        .append("\n");
            }

            List<Plugin> enabledPlugins = new ArrayList<>();
            List<Plugin> disabledPlugins = new ArrayList<>();
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.isEnabled()) {
                    enabledPlugins.add(plugin);
                } else {
                    disabledPlugins.add(plugin);
                }
            }

            report.append("Enabled Plugins: ").append(enabledPlugins.stream().map(
                    plugin -> plugin.getName() + " v" + plugin.getDescription().getVersion()
            ).reduce((a, b) -> a + ", " + b).orElse("None")).append("\n");

            report.append("Disabled Plugins: ").append(disabledPlugins.stream().map(
                    plugin -> plugin.getName() + " v" + plugin.getDescription().getVersion()
            ).reduce((a, b) -> a + ", " + b).orElse("None")).append("\n");


            Set<Plugin> eventListeners = Arrays
                    .stream(
                            PrepareAnvilEvent
                                    .getHandlerList()
                                    .getRegisteredListeners()
                    )
                    .map(RegisteredListener::getPlugin)
                    .collect(Collectors.toSet());
            eventListeners.remove(Disenchantment.plugin);

            report.append("Prepare Anvil Listeners: ").append(eventListeners.isEmpty() ? "None" : eventListeners.stream().map(
                    plugin -> plugin.getName() + " v" + plugin.getDescription().getVersion()
            ).reduce((a, b) -> a + ", " + b).orElse("None")).append("\n\n");

            report.append("Plugin Configuration\n");
            report.append("=====================\n\n");

            report.append("Disenchantment\n");
            report.append("Is Enabled: ").append(Config.Disenchantment.isEnabled() ? "Yes" : "No").append("\n");
            report.append("Is Cost Enabled: ").append(Config.Disenchantment.Anvil.Repair.isCostEnabled() ? "Yes" : "No").append("\n");
            report.append("Is Reset Enabled: ").append(Config.Disenchantment.Anvil.Repair.isResetEnabled() ? "Yes" : "No").append("\n");
            report.append("Base Repair Cost: ").append(Config.Disenchantment.Anvil.Repair.getBaseCost()).append("\n");
            report.append("Cost Multiplier: ").append(Config.Disenchantment.Anvil.Repair.getCostMultiplier()).append("\n");

            if (extended) {
                report.append("Is Sound Enabled: ").append(Config.Disenchantment.Anvil.Sound.isEnabled() ? "Yes" : "No").append("\n");
                report.append("Sound Volume: ").append(Config.Disenchantment.Anvil.Sound.getVolume()).append("\n");
                report.append("Sound Pitch: ").append(Config.Disenchantment.Anvil.Sound.getPitch()).append("\n");

                report.append("Disabled Worlds: ")
                        .append(
                                Config.Disenchantment.getDisabledWorlds().isEmpty() ?
                                        "None" :
                                        Config.Disenchantment.getDisabledWorlds()
                                                .stream()
                                                .map(WorldInfo::getName)
                                                .reduce((a, b) -> a + ", " + b)
                                                .orElse("None")
                        )
                        .append("\n");

                report.append("Disabled Materials: ")
                        .append(
                                Config.Disenchantment.getDisabledMaterials().isEmpty() ?
                                        "None" :
                                        Config.Disenchantment.getDisabledMaterials()
                                                .stream()
                                                .map(Enum::name)
                                                .reduce((a, b) -> a + ", " + b)
                                                .orElse("None")
                        )
                        .append("\n");

                report.append("Enchantment States:\n");
                for (Map.Entry<Enchantment, EnchantmentStateType> entry : Config.Disenchantment.getEnchantmentStates().entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    EnchantmentStateType state = entry.getValue();
                    report.append(" - ").append(enchantment.getKey().getKey()).append(" = ").append(state.name()).append("\n");
                }
            }

            report.append("\n");

            report.append("Shatterment\n");
            report.append("Is Enabled: ").append(Config.Shatterment.isEnabled() ? "Yes" : "No").append("\n");
            report.append("Is Cost Enabled: ").append(Config.Shatterment.Anvil.Repair.isCostEnabled() ? "Yes" : "No").append("\n");
            report.append("Is Reset Enabled: ").append(Config.Shatterment.Anvil.Repair.isResetEnabled() ? "Yes" : "No").append("\n");
            report.append("Base Repair Cost: ").append(Config.Shatterment.Anvil.Repair.getBaseCost()).append("\n");
            report.append("Cost Multiplier: ").append(Config.Shatterment.Anvil.Repair.getCostMultiplier()).append("\n");

            if (extended) {
                report.append("Is Sound Enabled: ").append(Config.Shatterment.Anvil.Sound.isEnabled() ? "Yes" : "No").append("\n");
                report.append("Sound Volume: ").append(Config.Shatterment.Anvil.Sound.getVolume()).append("\n");
                report.append("Sound Pitch: ").append(Config.Shatterment.Anvil.Sound.getPitch()).append("\n");

                report.append("Disabled Worlds: ")
                        .append(
                                Config.Shatterment.getDisabledWorlds().isEmpty() ?
                                        "None" :
                                        Config.Shatterment.getDisabledWorlds()
                                                .stream()
                                                .map(WorldInfo::getName)
                                                .reduce((a, b) -> a + ", " + b)
                                                .orElse("None")
                        )
                        .append("\n");

                report.append("Enchantment States:\n");
                for (Map.Entry<Enchantment, EnchantmentStateType> entry : Config.Shatterment.getEnchantmentStates().entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    EnchantmentStateType state = entry.getValue();
                    report.append(" - ").append(enchantment.getKey().getKey()).append(" = ").append(state.name()).append("\n");
                }
            }

            report.append("\n");

            if (sender instanceof Player player) {
                report.append("Player Permissions\n");
                report.append("===================\n\n");

                report.append("Player: ").append(player.getName()).append("\n");
                report.append("Is Operator: ").append(player.isOp() ? "Yes" : "No").append("\n");

                if (extended) {
                    report.append("Effective Permissions:\n");
                    List<PermissionAttachmentInfo> permissions = player.getEffectivePermissions()
                            .stream()
                            .filter(p -> p.getPermission().startsWith("disenchantment."))
                            .toList();
                    for (PermissionAttachmentInfo permission : permissions) {
                        report.append(" - ").append(permission.getPermission()).append("\n");
                    }
                }
            }

            report.append("\n");

        } catch (Exception e) {
            report.append("\n\nAn error occurred while generating the report!\n");

            report.append("Error Details\n");
            report.append("==============\n\n");

            report.append("The following error occurred: ").append(e.getMessage());
            report.append("\n\n");
            for (StackTraceElement element : e.getStackTrace()) {
                report.append(element.toString()).append("\n");
            }

            logger.severe(report.toString());
        }

        return report.toString();
    }
}
