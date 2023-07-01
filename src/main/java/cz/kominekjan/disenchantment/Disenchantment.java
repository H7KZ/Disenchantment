package cz.kominekjan.disenchantment;

import cz.kominekjan.disenchantment.commands.CommandCompleter;
import cz.kominekjan.disenchantment.commands.CommandRegister;
import cz.kominekjan.disenchantment.events.DisenchantmentClickEvent;
import cz.kominekjan.disenchantment.events.DisenchantmentEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class Disenchantment extends JavaPlugin {

    public static Disenchantment plugin;
    public static FileConfiguration config;
    public static Logger logger;

    public static boolean enabled = true;

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Plugin instance
        plugin = this;

        // Config
        config = getConfig();
        config.options().copyDefaults(true);
        saveDefaultConfig();

        enabled = config.getBoolean("enabled");

        // Logger
        logger = getLogger();

        // Register events
        getServer().getPluginManager().registerEvents(new DisenchantmentEvent(), this);
        getServer().getPluginManager().registerEvents(new DisenchantmentClickEvent(), this);

        // Register commands
        Objects.requireNonNull(getCommand("disenchantment")).setExecutor(new CommandRegister());
        Objects.requireNonNull(getCommand("disenchantment")).setTabCompleter(new CommandCompleter());

        logger.info("Disenchantment enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("Disenchantment disabled!");
    }

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
}
