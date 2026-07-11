package com.jankominek.disenchantment;

import com.jankominek.disenchantment.commands.CommandCompleter;
import com.jankominek.disenchantment.commands.CommandRegister;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.events.GUIClickEvent;
import com.jankominek.disenchantment.listeners.DisenchantClickListener;
import com.jankominek.disenchantment.listeners.DisenchantListener;
import com.jankominek.disenchantment.listeners.ShatterClickListener;
import com.jankominek.disenchantment.listeners.ShatterListener;
import com.jankominek.disenchantment.nms.MinecraftVersion;
import com.jankominek.disenchantment.nms.NMS;
import com.jankominek.disenchantment.nms.NMSMapper;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.plugins.placeholderapi.DisenchantmentPlaceholderExpansion;
import com.jankominek.disenchantment.types.LogLevelType;
import com.jankominek.disenchantment.stats.StatsManager;
import com.jankominek.disenchantment.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main plugin class for Disenchantment, a Bukkit/Spigot plugin that allows players
 * to remove enchantments from items via an anvil-based mechanic.
 *
 * <p>Provides globally accessible static references to the plugin instance, NMS handler,
 * configuration, locale, and logger for use throughout the codebase.</p>
 */
public class Disenchantment extends JavaPlugin {
    // Global variables (Should be used with class name, e.g. Disenchantment.enabled)
    public static final String commandName = "disenchantment";
    public static final int spigotmcId = 110741;
    public static final int bstatsId = 19058;
    public static boolean enabled = true;
    // Session-only — always false after a restart, never persisted to config.
    public static boolean maintenanceEnabled = false;
    public static Instant startedAt;

    // Globally known instances (Does not need to be used with class name)
    public static Disenchantment plugin;
    public static NMS nms;
    public static FileConfiguration config;
    public static FileConfiguration localeConfig;
    public static Logger logger;

    // Tasks
    private final ArrayList<Object> tasks = new ArrayList<>();

    /**
     * Toggles the plugin's enabled state at runtime without unloading it.
     *
     * @param enable {@code true} to enable the plugin, {@code false} to disable it
     */
    public static void onToggle(boolean enable) {
        enabled = enable;
    }

    /**
     * Initialises all plugin subsystems: NMS mapping, configuration, locale files,
     * third-party plugin integrations, event listeners, commands, bStats metrics,
     * and the automatic update checker.
     */
    @SuppressWarnings("deprecation")
    public void enable() {
        // Setup instances
        plugin = this;
        logger = getLogger();
        startedAt = Instant.now();

        // Config (loaded before NMS so debug logging is available during NMS setup)
        ConfigUtils.setupConfig();
        config = getConfig();
        DiagnosticUtils.setDebugEnabled(Config.Logging.getLevel().isAtLeast(LogLevelType.DEBUG));

        // NMS net.minecraft.server
        NMS mappedNMS = NMSMapper.setup();
        if (mappedNMS == null) {
            logger.severe("This version of Minecraft is not compatible with Disenchantment. Sorry!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        nms = mappedNMS;

        // Locale — extract bundled files to data folder first (best-effort; skips existing files)
        ConfigUtils.setupLocaleConfigs();
        String locale = Config.getLocale();
        // Fall back to "en" if the configured locale file is not available on disk
        File localeFile = new File(plugin.getDataFolder(), "locales/" + locale + ".yml");
        if (!localeFile.exists() && !locale.equals("en")) {
            locale = "en";
            localeFile = new File(plugin.getDataFolder(), "locales/en.yml");
        }
        if (localeFile.exists()) {
            localeConfig = YamlConfiguration.loadConfiguration(localeFile);
        } else {
            // Fall back to JAR resource (always available; also used in test environments)
            var stream = plugin.getResource("locales/" + locale + ".yml");
            if (stream != null) {
                localeConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)
                );
            } else {
                logger.severe("Could not load locale '" + locale + "' from JAR — missing resource.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        // Set config values
        Disenchantment.enabled = Config.isPluginEnabled();

        // Find and activate supported plugins
        List<String> activatedPlugins = Arrays.stream(getServer().getPluginManager().getPlugins()).toList().stream().map(Plugin::getName).toList();
        SupportedPluginManager.activatePlugins(activatedPlugins);

        // Economy (Vault) — hooked via ServerLoadEvent so VaultUnlocked's vault2→vault1 bridge
        // has already fired. VaultUnlocked registers the Bukkit service bridge post-Done, after
        // all onEnable() calls; a one-tick delay is still too early on Paper.
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onServerLoad(ServerLoadEvent event) {
                DiagnosticUtils.debug("ECONOMY", "ServerLoadEvent fired — attempting economy hook");
                boolean economyAvailable = EconomyUtils.setup();
                if (!economyAvailable && (Config.Disenchantment.Economy.isEnabled() || Config.Shatterment.Economy.isEnabled())) {
                    logger.warning("Economy is enabled in config but Vault/economy plugin not found!");
                }
                if (Config.Logging.getLevel().isAtLeast(LogLevelType.INFO)) {
                    logger.info("Economy (Vault): " + (economyAvailable ? "hooked" : "not available"));
                }
                DiagnosticUtils.debug("STARTUP", "Economy hook (ServerLoadEvent): " + (economyAvailable ? "hooked" : "not available"));
            }
        }, plugin);

        // Startup logging
        if (Config.Logging.getLevel().isAtLeast(LogLevelType.INFO)) {
            logger.info("NMS module: " + nms.getClass().getSimpleName());

            List<ISupportedPlugin> activatedAdapters = SupportedPluginManager.getAllActivatedPlugins();
            if (activatedAdapters.isEmpty()) {
                logger.info("Plugin adapters active: none");
            } else {
                logger.info("Plugin adapters active: " + activatedAdapters.stream().map(ISupportedPlugin::getName).collect(Collectors.joining(", ")));
            }

            if (Config.Logging.getLevel().isAtLeast(LogLevelType.DEBUG)) {
                DiagnosticUtils.debug("STARTUP", "NMS: " + nms.getClass().getSimpleName() + " (MC version: " + MinecraftVersion.getServerVersion().name() + ")");
                DiagnosticUtils.debug("STARTUP", "Scheduler: Folia=" + SchedulerUtils.isFolia());
                DiagnosticUtils.debug("STARTUP", "Locale: " + Config.getLocale());
                DiagnosticUtils.debug("STARTUP", "Event priorities — disenchant: " + Config.EventPriorities.getDisenchantEvent()
                        + ", disenchant-click: " + Config.EventPriorities.getDisenchantClickEvent()
                        + ", shatter: " + Config.EventPriorities.getShatterEvent()
                        + ", shatter-click: " + Config.EventPriorities.getShatterClickEvent());
                DiagnosticUtils.debug("STARTUP", "Disenchantment: enabled=" + Config.Disenchantment.isEnabled()
                        + ", economy.enabled=" + Config.Disenchantment.Economy.isEnabled()
                        + ", economy.cost=" + Config.Disenchantment.Economy.getCost()
                        + ", economy.show-cost=" + Config.Disenchantment.Economy.isShowCostEnabled());
                DiagnosticUtils.debug("STARTUP", "Shatterment: enabled=" + Config.Shatterment.isEnabled()
                        + ", economy.enabled=" + Config.Shatterment.Economy.isEnabled()
                        + ", economy.cost=" + Config.Shatterment.Economy.getCost()
                        + ", economy.show-cost=" + Config.Shatterment.Economy.isShowCostEnabled());
            }
        }

        // Events
        new DisenchantListener(Config.EventPriorities.getDisenchantEvent());
        new DisenchantClickListener(Config.EventPriorities.getDisenchantClickEvent());
        new ShatterListener(Config.EventPriorities.getShatterEvent());
        new ShatterClickListener(Config.EventPriorities.getShatterClickEvent());
        getServer().getPluginManager().registerEvents(new GUIClickEvent(), plugin);
        DiagnosticUtils.debug("STARTUP", "All listeners registered");

        // Commands
        Objects.requireNonNull(getCommand(Disenchantment.commandName)).setExecutor(new CommandRegister());
        Objects.requireNonNull(getCommand(Disenchantment.commandName)).setTabCompleter(new CommandCompleter());

        // BStats
        BStatsMetrics metrics = new BStatsMetrics(plugin, bstatsId);

        if (Config.Logging.isOperationsEnabled()) {
            StatsManager.init(plugin.getDataFolder(), metrics);
        }

        // Automatic update check
        tasks.add(new UpdateChecker(spigotmcId).run(plugin, plugin.getDescription().getVersion()));

        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new DisenchantmentPlaceholderExpansion().register();
        }

        logger.info("Disenchantment enabled!");
    }

    /**
     * Shuts down the plugin by cancelling scheduled tasks and deactivating all
     * third-party plugin adapters.
     */
    public void disable() {
        for (Object task : tasks) {
            SchedulerUtils.cancelTask(task);
        }

        SchedulerUtils.cancelAllTasks(plugin);

        SupportedPluginManager.deactivateAllPlugins();
        EconomyUtils.reset();
        LuckPermsUtils.reset();
        CooldownManager.reset();
        StatsManager.shutdown();

        logger.info("Disenchantment disabled!");
    }

    /**
     * Hot-reloads the plugin without a server restart.
     * Re-reads config and locale from disk, syncs the debug flag, unregisters and
     * re-registers all event listeners with updated priorities, re-detects third-party
     * plugin adapters, and re-hooks economy.
     */
    public void reload() {
        // 1. Config
        ConfigUtils.setupConfig();
        config = getConfig();

        // 2. Locale
        ConfigUtils.setupLocaleConfigs();
        String locale = Config.getLocale();
        File localeFile = new File(plugin.getDataFolder(), "locales/" + locale + ".yml");
        if (!localeFile.exists() && !locale.equals("en")) {
            locale = "en";
            localeFile = new File(plugin.getDataFolder(), "locales/en.yml");
        }
        if (localeFile.exists()) {
            localeConfig = YamlConfiguration.loadConfiguration(localeFile);
        } else {
            var stream = plugin.getResource("locales/" + locale + ".yml");
            if (stream != null) {
                localeConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
            }
        }

        // 3. Debug flag
        DiagnosticUtils.setDebugEnabled(Config.Logging.getLevel().isAtLeast(LogLevelType.DEBUG));

        // 4. Unregister all our listeners
        org.bukkit.event.HandlerList.unregisterAll(plugin);

        // 5. Re-register listeners with fresh priorities
        new DisenchantListener(Config.EventPriorities.getDisenchantEvent());
        new DisenchantClickListener(Config.EventPriorities.getDisenchantClickEvent());
        new ShatterListener(Config.EventPriorities.getShatterEvent());
        new ShatterClickListener(Config.EventPriorities.getShatterClickEvent());
        getServer().getPluginManager().registerEvents(new GUIClickEvent(), plugin);

        // 6. Re-detect plugin adapters
        SupportedPluginManager.deactivateAllPlugins();
        List<String> activePluginNames = Arrays.stream(getServer().getPluginManager().getPlugins())
                .map(Plugin::getName)
                .toList();
        SupportedPluginManager.activatePlugins(activePluginNames);

        // 7. Re-hook economy
        EconomyUtils.reset();
        EconomyUtils.setup();
        LuckPermsUtils.reset();

        // 8. Sync enabled flag
        Disenchantment.enabled = Config.isPluginEnabled();

        // 9. Stats manager
        StatsManager.shutdown();
        if (Config.Logging.isOperationsEnabled()) {
            BStatsMetrics reloadMetrics = new BStatsMetrics(plugin, bstatsId);
            StatsManager.init(plugin.getDataFolder(), reloadMetrics);
        }

        DiagnosticUtils.debug("RELOAD", "Full reload complete");
        logger.info("Disenchantment reloaded.");
    }

    @Override
    public void onEnable() {
        try {
            this.enable();
        } catch (Exception e) {
            DiagnosticUtils.throwReport(e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            this.disable();
        } catch (Exception e) {
            DiagnosticUtils.throwReport(e);
        }
    }
}
