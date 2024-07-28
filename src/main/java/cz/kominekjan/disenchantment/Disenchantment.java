package cz.kominekjan.disenchantment;

import cz.kominekjan.disenchantment.commands.CommandCompleter;
import cz.kominekjan.disenchantment.commands.CommandRegister;
import cz.kominekjan.disenchantment.events.DisenchantmentClickEvent;
import cz.kominekjan.disenchantment.events.DisenchantmentEvent;
import cz.kominekjan.disenchantment.events.DisenchantmentGUIClickEvent;
import cz.kominekjan.disenchantment.libs.bstats.BStatsMetrics;
import cz.kominekjan.disenchantment.libs.config.ConfigUpdater;
import cz.kominekjan.disenchantment.libs.update.UpdateChecker;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static cz.kominekjan.disenchantment.config.Config.setPluginEnabled;

public final class Disenchantment extends JavaPlugin {

    private static final String[] supportedPlugins = {"AdvancedEnchantments", "EcoEnchants", "ExcellentEnchants", "UberEnchant", "EnchantsSquared"};
    public static Disenchantment plugin;
    public static BukkitScheduler scheduler;
    public static FileConfiguration config;
    public static Logger logger;
    public static List<String> activatedPlugins = new ArrayList<>(supportedPlugins.length);

    public static boolean enabled = true;

    private BukkitTask task;

    public static void toggle() {
        enabled = !enabled;
        setPluginEnabled(enabled);
    }

    @Override
    public void onEnable() {
        plugin = this;

        scheduler = getServer().getScheduler();

        @NotNull Plugin[] pluginList = getServer().getPluginManager().getPlugins();

        for (Plugin plugin : pluginList) {
            for (String supportedPlugin : supportedPlugins) {
                if (plugin.getName().equalsIgnoreCase(supportedPlugin)) {
                    activatedPlugins.add(supportedPlugin);
                }
            }
        }

        plugin.saveDefaultConfig();

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        try {
            ConfigUpdater.update(plugin, "config.yml", configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        plugin.reloadConfig();

        config = getConfig();

        enabled = config.getBoolean("enabled");

        logger = getLogger();

        getServer().getPluginManager().registerEvents(new DisenchantmentEvent(), this);
        getServer().getPluginManager().registerEvents(new DisenchantmentClickEvent(), this);
        getServer().getPluginManager().registerEvents(new DisenchantmentGUIClickEvent(), this);

        Objects.requireNonNull(getCommand("disenchantment")).setExecutor(new CommandRegister());
        Objects.requireNonNull(getCommand("disenchantment")).setTabCompleter(new CommandCompleter());

        new BStatsMetrics(this, 19058);

        // Check for updates every 8 hours
        String version = plugin.getDescription().getVersion();
        this.task = scheduler.runTaskTimerAsynchronously(this, this.checkUpdate(version), 3 * 20, 8 * 60 * 60 * 20);

        logger.info("Disenchantment enabled!");
    }

    @Override
    public void onDisable() {
        this.task.cancel();

        getServer().getScheduler().cancelTasks(this);

        logger.info("Disenchantment disabled!");
    }

    private Runnable checkUpdate(String pluginVersion) {
        return () -> new UpdateChecker(110741).getVersion(version -> {
            if (!pluginVersion.equals(version)) System.out.println("There is a new version available: " + version);
        });
    }
}
