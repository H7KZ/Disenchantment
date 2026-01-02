package com.jankominek.disenchantment.utils;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.function.Consumer;

import static com.jankominek.disenchantment.Disenchantment.logger;

// From: https://www.spigotmc.org/wiki/creating-an-update-checker-that-checks-for-updates
public class UpdateChecker {
    private final int resourceId;

    public UpdateChecker(int resourceId) {
        this.resourceId = resourceId;
    }

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

    public Runnable runnableUpdateTask(String version) {
        return () -> this.getVersion(newVersion -> {
            if (!version.equals(newVersion)) logger.info("There is a new version available: v" + newVersion);
        });
    }

    public Object run(Plugin plugin, String version) {
        return SchedulerUtils.runAsyncTimer(plugin, this.runnableUpdateTask(version), 3 * 20, 8 * 60 * 60 * 20); // 8 Hours
    }
}
