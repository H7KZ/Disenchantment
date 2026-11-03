package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.jankominek.disenchantment.Disenchantment.logger;

/**
 * Checks for plugin updates by querying the SpigotMC API.
 * Based on <a href="https://www.spigotmc.org/wiki/creating-an-update-checker-that-checks-for-updates">SpigotMC wiki</a>.
 *
 * <p>Hardened relative to the wiki version: connections time out after {@value #TIMEOUT_MS} ms, a response that does
 * not look like a version (for example an HTML challenge page from Cloudflare) is ignored, versions are compared
 * numerically so a local build newer than the published one is not reported as outdated, and the whole check
 * can be switched off with {@code update-checker: false}.</p>
 */
// From: https://www.spigotmc.org/wiki/creating-an-update-checker-that-checks-for-updates
public class UpdateChecker {
    private static final int TIMEOUT_MS = 5000;
    private static final Pattern VERSION = Pattern.compile("^v?\\d+(\\.\\d+){0,3}([-+][0-9A-Za-z.\\-]+)?$");

    private final int resourceId;
    private final Supplier<String> fetcher;

    /**
     * Constructs a new UpdateChecker for the given SpigotMC resource.
     *
     * @param resourceId the SpigotMC resource ID
     */
    public UpdateChecker(int resourceId) {
        this.resourceId = resourceId;
        this.fetcher = this::fetchFromSpigot;
    }

    /**
     * Constructs an UpdateChecker that obtains the raw response from the given supplier instead of the network.
     */
    UpdateChecker(int resourceId, Supplier<String> fetcher) {
        this.resourceId = resourceId;
        this.fetcher = fetcher;
    }

    /**
     * Returns whether the text looks like a plugin version such as {@code 6.5.12}, {@code v6.5} or
     * {@code 6.5.12-SNAPSHOT}. Anything else (HTML, error text, empty) is not a version.
     *
     * @param text the candidate text
     * @return {@code true} if it is a plausible version string
     */
    public static boolean isVersion(String text) {
        return text != null && VERSION.matcher(text).matches();
    }

    /**
     * Compares the numeric parts of two versions and reports whether {@code remote} is strictly newer than
     * {@code local}. A leading {@code v} and any {@code -qualifier} suffix are ignored, so {@code 6.5.12-SNAPSHOT}
     * equals {@code 6.5.12}, and {@code 6.10.0} is newer than {@code 6.9.0}. Missing segments count as zero.
     *
     * @param local  the running plugin's version
     * @param remote the latest published version
     * @return {@code true} only if {@code remote} is numerically greater than {@code local}
     */
    public static boolean isNewer(String local, String remote) {
        long[] a = numericParts(local);
        long[] b = numericParts(remote);
        int length = Math.max(a.length, b.length);

        for (int i = 0; i < length; i++) {
            long x = i < a.length ? a[i] : 0;
            long y = i < b.length ? b[i] : 0;
            if (y != x) return y > x;
        }

        return false;
    }

    private static long[] numericParts(String version) {
        String core = version == null ? "" : version.trim();
        if (core.startsWith("v") || core.startsWith("V")) core = core.substring(1);

        int end = 0;
        while (end < core.length() && (Character.isDigit(core.charAt(end)) || core.charAt(end) == '.')) end++;
        core = core.substring(0, end);
        if (core.isEmpty()) return new long[0];

        String[] segments = core.split("\\.");
        long[] parts = new long[segments.length];

        for (int i = 0; i < segments.length; i++) {
            try {
                parts[i] = segments[i].isEmpty() ? 0 : Long.parseLong(segments[i]);
            } catch (NumberFormatException e) {
                parts[i] = Long.MAX_VALUE; // absurdly long digit run; treat as "very large" rather than failing
            }
        }

        return parts;
    }

    /**
     * Reads the first whitespace-delimited token of the SpigotMC response, or {@code null} on any failure.
     */
    private String fetchFromSpigot() {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URI("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).toURL().openConnection();
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "Disenchantment-UpdateChecker");

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                DiagnosticUtils.debug("UPDATE", "SpigotMC API returned HTTP " + status);
                return null;
            }

            try (Scanner scan = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                return scan.hasNext() ? scan.next() : null;
            }
        } catch (IOException e) {
            logger.warning("Cannot look for updates: " + e.getMessage());
            return null;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    /**
     * Fetches the latest version string from the SpigotMC API and passes it to the consumer. Nothing is passed
     * if the request fails or the response is not a version.
     *
     * @param consumer the consumer to receive the latest version string
     */
    public void getVersion(final Consumer<String> consumer) {
        String latest = fetcher.get();

        if (latest == null) {
            DiagnosticUtils.debug("UPDATE", "SpigotMC API returned no usable response");
        } else if (!isVersion(latest)) {
            DiagnosticUtils.debug("UPDATE", "Ignoring SpigotMC response that is not a version");
        } else {
            DiagnosticUtils.debug("UPDATE", "Latest version from SpigotMC: " + latest);
            consumer.accept(latest);
        }
    }

    /**
     * Creates a runnable that checks for updates and logs a message if a newer version is available.
     * The {@code update-checker} config option is read on every run, and the run does nothing (no network
     * access) while it is {@code false}.
     *
     * @param version the current plugin version to compare against
     * @return a runnable update check task
     */
    public Runnable runnableUpdateTask(String version) {
        return () -> {
            if (!Config.isUpdateCheckerEnabled()) {
                DiagnosticUtils.debug("UPDATE", "Update check skipped (update-checker: false)");
                return;
            }

            DiagnosticUtils.debug("UPDATE", "Running scheduled update check (current=" + version + ")");
            this.getVersion(newVersion -> {
                if (isNewer(version, newVersion)) {
                    logger.info("There is a new version available: v" + newVersion.replaceFirst("^[vV]", ""));
                } else {
                    DiagnosticUtils.debug("UPDATE", "Plugin is up to date (version=" + version + ", latest=" + newVersion + ")");
                }
            });
        };
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
