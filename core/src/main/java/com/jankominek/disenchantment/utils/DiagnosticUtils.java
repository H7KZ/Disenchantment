package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.nms.NMSMapper;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.types.LogLevelType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.generator.WorldInfo;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.jankominek.disenchantment.Disenchantment.logger;
import static com.jankominek.disenchantment.Disenchantment.plugin;

/**
 * Utility class for generating diagnostic reports about the plugin's state,
 * server environment, configuration, and player permissions.
 */
public class DiagnosticUtils {
    /**
     * Generates a diagnostic report string containing plugin, server, and configuration information.
     *
     * @param extended whether to include extended details (sound, worlds, enchantment states, permissions)
     * @param sender   the command sender requesting the report (used for player-specific info), or null
     * @return the formatted diagnostic report
     */
    public static String getReport(Boolean extended, CommandSender sender) {
        return buildReport(extended, sender);
    }

    /**
     * Logs a severe diagnostic report to the console when the plugin encounters a critical error.
     * Includes the full diagnostic report plus the full exception cause chain.
     * If {@code logging.save-reports} is enabled (or config is not yet loaded), the report is
     * also written to {@code plugins/Disenchantment/logs/crash-<timestamp>.txt}.
     *
     * @param e the throwable that caused the error
     */
    public static void throwReport(Throwable e) {
        StringBuilder report = new StringBuilder();

        report.append("Disenchantment encountered an error and had to be disabled.\n");
        report.append("Please report this error at: https://github.com/H7KZ/Disenchantment/issues\n");
        report.append("Attach the report below (or the saved file if present).\n\n");

        report.append("=== Disenchantment Crash Report ===\n\n");
        report.append(buildReport(true, null));
        report.append("\n");

        report.append("=== Error Details ===\n\n");
        appendThrowable(report, e, 0);

        String reportStr = report.toString();
        logger.severe(reportStr);

        // Default to saving if config is not yet available (e.g. error during onEnable)
        boolean saveToFile = true;
        try {
            saveToFile = Config.Logging.isSaveReportsEnabled();
        } catch (Exception ignored) {
        }

        if (saveToFile) {
            String path = saveReportToFile(reportStr, "crash");
            if (path != null) {
                logger.severe("Crash report saved to: " + path);
            }
        }
    }

    /**
     * Saves a diagnostic report string to a timestamped file under
     * {@code plugins/Disenchantment/logs/<prefix>-<timestamp>.txt}.
     *
     * @param content the report content to write
     * @param prefix  the filename prefix (e.g. {@code "crash"} or {@code "diagnostic"})
     * @return the absolute path of the written file, or {@code null} if writing failed
     */
    public static String saveReportToFile(String content, String prefix) {
        try {
            File logsDir = new File(plugin.getDataFolder(), "logs");
            logsDir.mkdirs();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            File reportFile = new File(logsDir, prefix + "-" + timestamp + ".txt");

            try (FileWriter writer = new FileWriter(reportFile, StandardCharsets.UTF_8)) {
                writer.write(content);
            }

            return reportFile.getAbsolutePath();
        } catch (Exception ex) {
            logger.warning("Failed to save diagnostic report to file: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Returns {@code true} if debug-level logging is currently active.
     * Safe to call at any point in the plugin lifecycle — returns {@code false} if
     * config is not yet loaded.
     *
     * @return {@code true} if {@code logging.level} is set to {@link LogLevelType#DEBUG}
     */
    public static boolean isDebugEnabled() {
        if (Disenchantment.config == null) return false;
        return Config.Logging.getLevel().isAtLeast(LogLevelType.DEBUG);
    }

    /**
     * Logs a categorised debug message when {@link LogLevelType#DEBUG} is active.
     * Output format: {@code [DEBUG][CATEGORY] message}
     * <p>Safe to call before config is fully loaded — silently dropped if config is unavailable.</p>
     *
     * @param category short subsystem tag, e.g. {@code "ECONOMY"}, {@code "NMS"}, {@code "DISENCHANT"}
     * @param msg      the debug message
     */
    public static void debug(String category, String msg) {
        if (Disenchantment.config == null) return;
        if (Config.Logging.getLevel().isAtLeast(LogLevelType.DEBUG))
            logger.info("[DEBUG][" + category + "] " + msg);
    }

    /**
     * Appends a throwable and its full cause chain to the given {@link StringBuilder}.
     *
     * @param sb    the builder to append to
     * @param t     the throwable to append
     * @param depth the current cause depth (0 = root, 1+ = caused by)
     */
    private static void appendThrowable(StringBuilder sb, Throwable t, int depth) {
        if (depth > 0) sb.append("Caused by: ");
        sb.append(t.getClass().getName()).append(": ").append(t.getMessage()).append("\n");
        for (StackTraceElement element : t.getStackTrace()) {
            sb.append("  at ").append(element).append("\n");
        }
        if (t.getCause() != null && t.getCause() != t) {
            appendThrowable(sb, t.getCause(), depth + 1);
        }
    }

    private static String buildReport(Boolean extended, CommandSender sender) {
        StringBuilder report = new StringBuilder();
        try {
            report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            if (Disenchantment.startedAt != null) {
                Duration uptime = Duration.between(Disenchantment.startedAt, Instant.now());
                long hours = uptime.toHours();
                long minutes = uptime.toMinutesPart();
                long seconds = uptime.toSecondsPart();
                report.append("Uptime: ")
                        .append(hours > 0 ? hours + "h " : "")
                        .append(minutes).append("m ")
                        .append(seconds).append("s\n");
            }
            report.append("\n");

            report.append("Plugin Version: ").append(plugin.getDescription().getVersion()).append("\n");
            report.append("Server Version: ").append(Bukkit.getVersion()).append(" (").append(Bukkit.getName()).append(')').append("\n");
            report.append("Plugin Enabled: ").append(plugin.isEnabled() ? "Yes" : "No").append("\n");
            report.append("Using NMS: ").append(NMSMapper.hasNMS() ? "Yes" : "No").append("\n");
            report.append("Java: ").append(System.getProperty("java.version")).append(" (").append(System.getProperty("java.vendor")).append(")\n");
            report.append("OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append(" (").append(System.getProperty("os.arch")).append(")\n\n");

            report.append("Memory Usage\n");
            report.append("=============\n\n");
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory() / 1024 / 1024; // Convert to MB
            long freeMemory = runtime.freeMemory() / 1024 / 1024; // Convert to MB
            long maxMemory = runtime.maxMemory() / 1024 / 1024; // Convert to MB
            long usedMemory = totalMemory - freeMemory;
            report.append("Used Memory: ").append(usedMemory).append(" MB / ").append(totalMemory).append(" MB (max: ").append(maxMemory).append(" MB)\n\n");

            report.append("Server State\n");
            report.append("=============\n\n");

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

            report.append("Available locales: ").append(String.join(", ", Config.getLocales())).append("\n");
            report.append("Locale: ").append(Config.getLocale()).append("\n");
            report.append("Log Level: ").append(Config.Logging.getLevel().name()).append("\n");
            report.append("Save Reports: ").append(Config.Logging.isSaveReportsEnabled() ? "Yes" : "No").append("\n");

            report.append("\n");

            report.append("Event Priorities:\n");
            report.append(" - Disenchantment: ").append(Config.EventPriorities.getDisenchantEvent().name()).append("\n");
            report.append(" - Disenchantment Click: ").append(Config.EventPriorities.getDisenchantClickEvent().name()).append("\n");
            report.append(" - Shatterment: ").append(Config.EventPriorities.getShatterEvent().name()).append("\n");
            report.append(" - Shatterment Click: ").append(Config.EventPriorities.getShatterClickEvent().name()).append("\n");

            report.append("\n");

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
                for (Map.Entry<String, EnchantmentStateType> entry : Config.Disenchantment.getEnchantmentStates().entrySet()) {
                    String key = entry.getKey();
                    EnchantmentStateType state = entry.getValue();
                    report.append(" - ").append(key).append(" = ").append(state.name()).append("\n");
                }
            }

            report.append("\n");

            report.append("Shatterment\n");
            report.append("Is Enabled: ").append(Config.Shatterment.isEnabled() ? "Yes" : "No").append("\n");
            report.append("Is Cost Enabled: ").append(Config.Shatterment.Anvil.Repair.isCostEnabled() ? "Yes" : "No").append("\n");
            report.append("Is Reset Enabled: ").append(Config.Shatterment.Anvil.Repair.isResetEnabled() ? "Yes" : "No").append("\n");
            report.append("Base Repair Cost: ").append(Config.Shatterment.Anvil.Repair.getBaseCost()).append("\n");
            report.append("Cost Multiplier: ").append(Config.Shatterment.Anvil.Repair.getCostMultiplier()).append("\n");

            report.append("\n");

            report.append("Economy\n");
            report.append("=======\n\n");
            report.append("Vault available: ").append(EconomyUtils.isAvailable() ? "Yes" : "No").append("\n");
            report.append("Disenchantment economy enabled: ").append(Config.Disenchantment.Economy.isEnabled() ? "Yes" : "No").append("\n");
            report.append("Disenchantment economy cost: ").append(Config.Disenchantment.Economy.getCost()).append("\n");
            report.append("Shatterment economy enabled: ").append(Config.Shatterment.Economy.isEnabled() ? "Yes" : "No").append("\n");
            report.append("Shatterment economy cost: ").append(Config.Shatterment.Economy.getCost()).append("\n");

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
                for (Map.Entry<String, EnchantmentStateType> entry : Config.Shatterment.getEnchantmentStates().entrySet()) {
                    String key = entry.getKey();
                    EnchantmentStateType state = entry.getValue();
                    report.append(" - ").append(key).append(" = ").append(state.name()).append("\n");
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
