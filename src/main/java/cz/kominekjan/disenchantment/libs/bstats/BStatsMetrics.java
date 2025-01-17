/*
 * This Metrics class was auto-generated and can be copied into your project if you are
 * not using a build tool like Gradle or Maven for dependency management.
 *
 * IMPORTANT: You are not allowed to modify this class, except changing the package.
 *
 * Disallowed modifications include but are not limited to:
 *  - Remove the option for users to opt-out
 *  - Change the frequency for data submission
 *  - Obfuscate the code (every obfuscator should allow you to make an exception for specific files)
 *  - Reformat the code (if you use a linter, add an exception)
 *
 * Violations will result in a ban of your plugin and account from bStats.
 */
package cz.kominekjan.disenchantment.libs.bstats;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class BStatsMetrics {

    /**
     * Creates a new Metrics instance.
     *
     * @param plugin    Your plugin instance.
     */
    public BStatsMetrics(JavaPlugin plugin) {
        // Get the config file
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", true);
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            config.addDefault("logFailedRequests", false);
            config.addDefault("logSentData", false);
            config.addDefault("logResponseStatusText", false);
            // Inform the server owners about bStats
            config
                    .options()
                    .setHeader(
                            List.of("""
                                    bStats (https://bStats.org) collects some basic information for plugin authors, like how
                                    many people use their plugin and their total player count. It's recommended to keep bStats
                                    enabled, but if you're not comfortable with this, you can turn this setting off. There is no
                                    performance penalty associated with having metrics enabled, and data sent to bStats is fully
                                    anonymous."""))
                    .copyDefaults(true);
            try {
                config.save(configFile);
            } catch (IOException ignored) {
            }
        }
    }

}
