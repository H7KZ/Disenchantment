package cz.kominekjan.disenchantment;

import cz.kominekjan.disenchantment.commands.CommandCompleter;
import cz.kominekjan.disenchantment.commands.CommandRegister;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.config.ConfigMigrations;
import cz.kominekjan.disenchantment.events.*;
import cz.kominekjan.disenchantment.libs.bstats.BStatsMetrics;
import cz.kominekjan.disenchantment.libs.update.UpdateChecker;
import cz.kominekjan.disenchantment.plugins.PluginManager;
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
    public static BukkitScheduler scheduler;
    public static FileConfiguration config;
    public static Logger logger;

    public static boolean enabled = true;

    private BukkitTask checkUpdateTask;

    public static void toggle(boolean enable) {
        enabled = enable;
    }

    @Override
    public void onEnable() {
        plugin = this;

        logger = getLogger();

        scheduler = getServer().getScheduler();

        List<String> activatedPlugins = Arrays.stream(getServer().getPluginManager().getPlugins()).toList().stream().map(Plugin::getName).toList();

        PluginManager.setActivatedPlugins(activatedPlugins);

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

        // Check for updates every 8 hours
        String version = plugin.getDescription().getVersion();
        this.checkUpdateTask = scheduler.runTaskTimerAsynchronously(this, this.checkUpdate(version), 3 * 20, 8 * 60 * 60 * 20);

        logger.info("Disenchantment enabled!");
    }

    @Override
    public void onDisable() {
        this.checkUpdateTask.cancel();

        getServer().getScheduler().cancelTasks(this);

        logger.info("Disenchantment disabled!");
    }

    private Runnable checkUpdate(String pluginVersion) {
        return () -> new UpdateChecker(110741).getVersion(version -> {
            if (!pluginVersion.equals(version)) System.out.println("There is a new version available: " + version);
        });
    }
}
