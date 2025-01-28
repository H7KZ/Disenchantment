package com.jankominek.disenchantment;

import com.jankominek.disenchantment.commands.CommandCompleter;
import com.jankominek.disenchantment.commands.CommandRegister;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.migration.ConfigMigrations;
import com.jankominek.disenchantment.events.*;
import com.jankominek.disenchantment.nms.NMS;
import com.jankominek.disenchantment.nms.NMSMapper;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.utils.BStatsMetrics;
import com.jankominek.disenchantment.utils.UpdateChecker;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
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
    public static BukkitScheduler scheduler;
    public static FileConfiguration config;
    public static Logger logger;

    // Tasks
    private final BukkitTask[] tasks = new BukkitTask[2];

    public static void onToggle(boolean enable) {
        enabled = enable;
    }

    @Override
    public void onEnable() {
        // Setup instances
        plugin = this;
        logger = getLogger();
        scheduler = getServer().getScheduler();

        // NMS net.minecraft.server
        NMS mappedNMS = NMSMapper.setup();
        if (mappedNMS == null) {
            logger.severe("This version of Minecraft is not compatible with Disenchantment. Sorry!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        nms = mappedNMS;

        // Config
        setupConfig();
        config = getConfig();

        // Set config values
        Disenchantment.enabled = Config.isPluginEnabled();

        // Find and activate supported plugins
        List<String> activatedPlugins = Arrays.stream(getServer().getPluginManager().getPlugins()).toList().stream().map(Plugin::getName).toList();
        SupportedPluginManager.activatePlugins(activatedPlugins);

        // Events
        getServer().getPluginManager().registerEvents(new DisenchantEvent(), plugin);
        getServer().getPluginManager().registerEvents(new DisenchantClickEvent(), plugin);
        getServer().getPluginManager().registerEvents(new ShatterEvent(), plugin);
        getServer().getPluginManager().registerEvents(new ShatterClickEvent(), plugin);
        getServer().getPluginManager().registerEvents(new GUIClickEvent(), plugin);

        // Commands
        Objects.requireNonNull(getCommand(Disenchantment.commandName)).setExecutor(new CommandRegister());
        Objects.requireNonNull(getCommand(Disenchantment.commandName)).setTabCompleter(new CommandCompleter());

        // BStats
        new BStatsMetrics(plugin, bstatsId);

        // Automatic update check
        tasks[0] = new UpdateChecker(spigotmcId).run(plugin, plugin.getDescription().getVersion());

        logger.info("Disenchantment enabled!");
    }

    @Override
    public void onDisable() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }

        getServer().getScheduler().cancelTasks(plugin);

        SupportedPluginManager.deactivateAllPlugins();

        logger.info("Disenchantment disabled!");
    }

    private void setupConfig() {
        plugin.saveDefaultConfig();

        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("config.yml")), StandardCharsets.UTF_8));

        FileConfiguration updatedConfig = ConfigMigrations.apply(plugin, oldConfig, newConfig);

        try {
            updatedConfig.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not save config.yml", e);
        }

        plugin.reloadConfig();
    }
}
