package cz.kominekjan.disenchantment;

import cz.kominekjan.disenchantment.bstats.Metrics;
import cz.kominekjan.disenchantment.checkers.UpdateChecker;
import cz.kominekjan.disenchantment.commands.CommandCompleter;
import cz.kominekjan.disenchantment.commands.CommandRegister;
import cz.kominekjan.disenchantment.config.ConfigUpdater;
import cz.kominekjan.disenchantment.events.DisenchantmentClickEvent;
import cz.kominekjan.disenchantment.events.DisenchantmentEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import static cz.kominekjan.disenchantment.config.Config.setPluginEnabled;

public final class Disenchantment extends JavaPlugin {

    public static Disenchantment plugin;
    public static FileConfiguration config;
    public static Logger logger;

    public static boolean enabled = true;

    public static void toggle() {
        enabled = !enabled;
        setPluginEnabled(enabled);
    }

    @Override
    public void onEnable() {
        plugin = this;

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

        Objects.requireNonNull(getCommand("disenchantment")).setExecutor(new CommandRegister());
        Objects.requireNonNull(getCommand("disenchantment")).setTabCompleter(new CommandCompleter());

        // bStats
        new Metrics(this, 19058);

        new UpdateChecker(this, 110741).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
                logger.info("There is a new update available.");
            }
        });

        logger.info("Disenchantment enabled!");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);

        logger.info("Disenchantment disabled!");
    }
}
