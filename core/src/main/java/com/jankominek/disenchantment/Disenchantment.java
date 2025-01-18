package com.jankominek.disenchantment;

import com.jankominek.disenchantment.commands.CommandCompleter;
import com.jankominek.disenchantment.commands.CommandRegister;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.ConfigMigrations;
import com.jankominek.disenchantment.events.*;
import com.jankominek.disenchantment.libs.bstats.BStatsMetrics;
import com.jankominek.disenchantment.libs.update.UpdateChecker;
import com.jankominek.disenchantment.nms.MinecraftVersion;
import com.jankominek.disenchantment.nms.NMS;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
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
import java.util.logging.Logger;

public final class Disenchantment extends JavaPlugin {
    public static Disenchantment plugin;
    public static NMS nms;
    public static BukkitScheduler scheduler;
    public static FileConfiguration config;
    public static Logger logger;
    public static Boolean enabled = true;

    private BukkitTask checkUpdateTask;

    public static void toggle(boolean enable) {
        enabled = enable;
    }

    @Override
    public void onEnable() {
        plugin = this;

        logger = getLogger();

        if (!setupNMS()) {
            logger.severe("This version of Minecraft is not compatible with Disenchantment. Sorry!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        scheduler = getServer().getScheduler();

        List<String> activatedPlugins = Arrays.stream(getServer().getPluginManager().getPlugins()).toList().stream().map(Plugin::getName).toList();
        SupportedPluginManager.activatePlugins(activatedPlugins);

        plugin.saveDefaultConfig();

        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("config.yml")), StandardCharsets.UTF_8));

        FileConfiguration updatedConfig = ConfigMigrations.apply(plugin, oldConfig, newConfig);

        try {
            updatedConfig.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        plugin.reloadConfig();

        config = getConfig();

        enabled = Config.isPluginEnabled();

        getServer().getPluginManager().registerEvents(new DisenchantEvent(), this);
        getServer().getPluginManager().registerEvents(new DisenchantClickEvent(), this);
        getServer().getPluginManager().registerEvents(new ShatterEvent(), this);
        getServer().getPluginManager().registerEvents(new ShatterClickEvent(), this);
        getServer().getPluginManager().registerEvents(new GUIClickEvent(), this);

        Objects.requireNonNull(getCommand("disenchantment")).setExecutor(new CommandRegister());
        Objects.requireNonNull(getCommand("disenchantment")).setTabCompleter(new CommandCompleter());

        new BStatsMetrics(this, 19058);

        String version = plugin.getDescription().getVersion();
        this.checkUpdateTask = scheduler.runTaskTimerAsynchronously(this, this.checkForUpdate(version), 3 * 20, 8 * 60 * 60 * 20); // 8 Hours

        logger.info("Disenchantment enabled!");
    }

    @Override
    public void onDisable() {
        this.checkUpdateTask.cancel();

        getServer().getScheduler().cancelTasks(this);

        SupportedPluginManager.deactivateAllPlugins();

        logger.info("Disenchantment disabled!");
    }

    private boolean setupNMS() {
        try {
            String nmsVersion = MinecraftVersion.getServerVersion().getNmsVersion();

            if (nmsVersion == null) return false;

            Class<?> clazz = Class.forName("com.jankominek.disenchantment.nms.NMS_" + nmsVersion);

            if (NMS.class.isAssignableFrom(clazz)) {
                nms = (NMS) clazz.getDeclaredConstructor().newInstance();
            }

            return nms != null;
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    private Runnable checkForUpdate(String pluginVersion) {
        return () -> new UpdateChecker(110741).getVersion(version -> {
            if (!pluginVersion.equals(version)) logger.info("There is a new version available: " + version);
        });
    }
}
