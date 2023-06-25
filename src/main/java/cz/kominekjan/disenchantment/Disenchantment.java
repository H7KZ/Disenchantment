package cz.kominekjan.disenchantment;

import cz.kominekjan.disenchantment.events.DisenchantmentClickEvent;
import cz.kominekjan.disenchantment.events.DisenchantmentEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Disenchantment extends JavaPlugin {

    public static Disenchantment plugin;
    public static FileConfiguration config;
    public static Logger logger;

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Plugin instance
        plugin = this;

        // Config
        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();

        // Logger
        logger = getLogger();

        // Register events
        getServer().getPluginManager().registerEvents(new DisenchantmentEvent(), this);
        getServer().getPluginManager().registerEvents(new DisenchantmentClickEvent(), this);

        logger.info("Disenchantment enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("Disenchantment disabled");
    }
}
