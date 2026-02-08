package com.jankominek.disenchantment.utils;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.function.Consumer;

import static com.jankominek.disenchantment.Disenchantment.logger;

/**
 * Checks for plugin updates by querying the SpigotMC API.
 * Based on <a href="https://www.spigotmc.org/wiki/creating-an-update-checker-that-checks-for-updates">SpigotMC wiki</a>.
 */
// From: https://www.spigotmc.org/wiki/creating-an-update-checker-that-checks-for-updates
public class UpdateChecker {
    private final int resourceId;

    /**
     * Constructs a new UpdateChecker for the given SpigotMC resource.
     *
     * @param resourceId the SpigotMC resource ID
     */
    public UpdateChecker(int resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * Fetches the latest version string from the SpigotMC API and passes it to the consumer.
     *
     * @param consumer the consumer to receive the latest version string
     */
    public void getVersion(final Consumer<String> consumer) {
        try (
                InputStream is = new URI("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).toURL().openStream();
                Scanner scan = new Scanner(is)
        ) {
            if (scan.hasNext()) consumer.accept(scan.next());
        } catch (IOException e) {
            logger.warning("Cannot look for updates: " + e.getMessage());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a runnable that checks for updates and logs a message if a new version is available.
     *
     * @param version the current plugin version to compare against
     * @return a runnable update check task
     */
    public Runnable runnableUpdateTask(String version) {
        return () -> this.getVersion(newVersion -> {
            if (!version.equals(newVersion)) logger.info("There is a new version available: v" + newVersion);
        });
    }

    /**
     * Starts a periodic async update check task that runs every 8 hours.
     *
     * @param plugin  the owning plugin instance
     * @param version the current plugin version to compare against
     * @return the scheduled task object
     */
    public Object run(Plugin plugin, String version) {
        return SchedulerUtils.runAsyncTimer(plugin, this.runnableUpdateTask(version), 3 * 20, 8 * 60 * 60 * 20); // 8 Hours
    }
}
