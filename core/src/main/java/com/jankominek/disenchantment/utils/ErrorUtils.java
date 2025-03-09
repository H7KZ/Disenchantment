package com.jankominek.disenchantment.utils;

import static com.jankominek.disenchantment.Disenchantment.logger;
import static com.jankominek.disenchantment.Disenchantment.plugin;

public class ErrorUtils {
    public static void fullReportError(Throwable e) {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory() / 1024 / 1024; // Convert to MB
        long freeMemory = runtime.freeMemory() / 1024 / 1024; // Convert to MB
        long maxMemory = runtime.maxMemory() / 1024 / 1024; // Convert to MB

        String java = System.getProperty("java.version");
        String vendor = System.getProperty("java.vendor");
        String os = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");

        String version = plugin.getDescription().getVersion();

        StringBuilder message = new StringBuilder();

        message.append("Disenchantment encountered an error and had to be disabled.\n");
        message.append("Please report this error to the plugin author.\n");
        message.append("Link to the plugin's issue tracker: https://github.com/H7KZ/Disenchantment/issues\n");
        message.append("Copy the following message and paste it in the issue section 'Screenshots & System'.\n\n");
        message.append(String.format("Disenchantment v%s\n", version));
        message.append(String.format("Java: %s (%s)\n", java, vendor));
        message.append(String.format("OS: %s %s (%s)\n", os, osVersion, osArch));
        message.append(String.format("Memory: f%d / t%d / m%d MB\n", freeMemory, totalMemory, maxMemory));
        message.append("\n");
        message.append("The following error occurred:\n");
        message.append(e.getMessage());
        message.append("\n\n");
        message.append("Stack trace:\n");
        for (StackTraceElement element : e.getStackTrace()) {
            message.append(element.toString()).append("\n");
        }

        logger.severe(message.toString());
    }
}
