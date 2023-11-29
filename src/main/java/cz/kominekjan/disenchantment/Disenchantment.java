package cz.kominekjan.disenchantment;

import cz.kominekjan.disenchantment.bstats.Metrics;
import cz.kominekjan.disenchantment.checkers.UpdateChecker;
import cz.kominekjan.disenchantment.commands.CommandCompleter;
import cz.kominekjan.disenchantment.commands.CommandRegister;
import cz.kominekjan.disenchantment.events.DisenchantmentClickEvent;
import cz.kominekjan.disenchantment.events.DisenchantmentEvent;
import cz.kominekjan.disenchantment.updaters.config.ConfigUpdater;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public final class Disenchantment extends JavaPlugin {

    public static Disenchantment plugin;
    public static FileConfiguration config;
    public static Logger logger;

    public static boolean enabled = true;

    /**
     * Toggle the plugin on/off
     */
    public static void toggle() {
        enabled = !enabled;
        config.set("enabled", enabled);
        plugin.saveConfig();
    }

    /**
     * Send a message to a CommandSender
     * with a custom ChatColor
     *
     * @param s CommandSender
     * @param m String
     * @param c ChatColor
     */
    public static void sendMessage(CommandSender s, String m, ChatColor c) {
        s.sendMessage(ChatColor.LIGHT_PURPLE + "Disenchantment: " + c + m);
    }

    /**
     * Send a message to a CommandSender
     * with a default ChatColor of RED
     *
     * @param s CommandSender
     * @param m String
     */
    public static void sendMessage(CommandSender s, String m) {
        s.sendMessage(ChatColor.LIGHT_PURPLE + "Disenchantment: " + ChatColor.RED + m);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Plugin instance
        plugin = this;

        // Config
        plugin.saveDefaultConfig();
        //The config needs to exist before using the updater
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        try {
            ConfigUpdater.update(plugin, "config.yml", configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        plugin.reloadConfig();

        config = getConfig();

        enabled = config.getBoolean("enabled");

        // Logger
        logger = getLogger();

        // Register events
        getServer().getPluginManager().registerEvents(new DisenchantmentEvent(), this);
        getServer().getPluginManager().registerEvents(new DisenchantmentClickEvent(), this);

        // Register commands
        Objects.requireNonNull(getCommand("disenchantment")).setExecutor(new CommandRegister());
        Objects.requireNonNull(getCommand("disenchantment")).setTabCompleter(new CommandCompleter());

        // bStats
        new Metrics(this, 19058);

        // Update checker
        new UpdateChecker(this, 110741).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
                logger.info("There is a new update available.");
            }
        });

        logger.info("Disenchantment enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("Disenchantment disabled!");
    }
}
