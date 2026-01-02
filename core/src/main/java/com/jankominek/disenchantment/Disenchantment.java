package com.jankominek.disenchantment;

import com.jankominek.disenchantment.commands.CommandCompleter;
import com.jankominek.disenchantment.commands.CommandRegister;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.events.GUIClickEvent;
import com.jankominek.disenchantment.listeners.DisenchantClickListener;
import com.jankominek.disenchantment.listeners.DisenchantListener;
import com.jankominek.disenchantment.listeners.ShatterClickListener;
import com.jankominek.disenchantment.listeners.ShatterListener;
import com.jankominek.disenchantment.nms.NMS;
import com.jankominek.disenchantment.nms.NMSMapper;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.utils.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class Disenchantment extends JavaPlugin {
    // Global variables (Should be used with class name, e.g. Disenchantment.enabled)
    public static final String commandName = "disenchantment";
    public static final int spigotmcId = 110741;
    public static final int bstatsId = 19058;
    public static boolean enabled = true;

    // Globally known instances (Does not need to be used with class name)
    public static Disenchantment plugin;
    public static NMS nms;
    public static FileConfiguration config;
    public static FileConfiguration localeConfig;
    public static Logger logger;

    // Tasks
    private final ArrayList<Object> tasks = new ArrayList<>();

    public static void onToggle(boolean enable) {
        enabled = enable;
    }

    public void enable() {
        // Setup instances
        plugin = this;
        logger = getLogger();

        // NMS net.minecraft.server
        NMS mappedNMS = NMSMapper.setup();
        if (mappedNMS == null) {
            logger.severe("This version of Minecraft is not compatible with Disenchantment. Sorry!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        nms = mappedNMS;

        // Config
        ConfigUtils.setupConfig();
        config = getConfig();

        // Locale
        ConfigUtils.setupLocaleConfigs();
        String locale = Config.getLocale();
        File localesFolder = new File(plugin.getDataFolder().getAbsolutePath() + "/locales");
        if (!localesFolder.exists()) {
            logger.severe("Locales folder does not exist. Please reinstall the plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        String finalLocale = locale;
        if (Arrays.stream(Objects.requireNonNull(localesFolder.listFiles())).noneMatch(file -> file.getName().equals(finalLocale + ".yml")))
            locale = "en";
        localeConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(
                        Objects.requireNonNull(
                                plugin.getResource("locales/" + locale + ".yml")
                        ),
                        StandardCharsets.UTF_8
                )
        );

        // Set config values
        Disenchantment.enabled = Config.isPluginEnabled();

        // Find and activate supported plugins
        List<String> activatedPlugins = Arrays.stream(getServer().getPluginManager().getPlugins()).toList().stream().map(Plugin::getName).toList();
        SupportedPluginManager.activatePlugins(activatedPlugins);

        // Events
        new DisenchantListener(Config.EventPriorities.getDisenchantEvent());
        new DisenchantClickListener(Config.EventPriorities.getDisenchantClickEvent());
        new ShatterListener(Config.EventPriorities.getShatterEvent());
        new ShatterClickListener(Config.EventPriorities.getShatterClickEvent());
        getServer().getPluginManager().registerEvents(new GUIClickEvent(), plugin);

        // Commands
        Objects.requireNonNull(getCommand(Disenchantment.commandName)).setExecutor(new CommandRegister());
        Objects.requireNonNull(getCommand(Disenchantment.commandName)).setTabCompleter(new CommandCompleter());

        // BStats
        new BStatsMetrics(plugin, bstatsId);

        // Automatic update check
        tasks.add(new UpdateChecker(spigotmcId).run(plugin, plugin.getDescription().getVersion()));

        logger.info("Disenchantment enabled!");
    }

    public void disable() {
        for (Object task : tasks) {
            SchedulerUtils.cancelTask(task);
        }

        SchedulerUtils.cancelAllTasks(plugin);

        SupportedPluginManager.deactivateAllPlugins();

        logger.info("Disenchantment disabled!");
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
