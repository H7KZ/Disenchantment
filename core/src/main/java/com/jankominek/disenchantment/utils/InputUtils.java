package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.I18n;
import org.bukkit.command.CommandSender;

/**
 * Validation helpers for numeric command arguments.
 */
public class InputUtils {
    /**
     * Parses a finite decimal number. {@link Double#parseDouble(String)} also accepts {@code "NaN"} and
     * {@code "Infinity"}, which must never reach the config.
     *
     * @param raw the raw argument text
     * @return the parsed value, or {@code null} if it is not a valid finite number
     */
    public static Double parseFinite(String raw) {
        if (raw == null) return null;

        try {
            double value = Double.parseDouble(raw.trim());
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses a finite number argument and, if it is invalid, tells the sender why.
     *
     * @param sender      the command sender to notify on failure
     * @param raw         the raw argument text
     * @param nonNegative whether values below zero are rejected
     * @return the parsed value, or {@code null} after an error message was sent
     */
    public static Double parseOrReport(CommandSender sender, String raw, boolean nonNegative) {
        Double value = parseFinite(raw);

        if (value == null) {
            sender.sendMessage(I18n.Messages.specifyValidDouble());
            return null;
        }

        if (nonNegative && value < 0) {
            sender.sendMessage(I18n.Messages.specifyNonNegativeDouble());
            return null;
        }

        return value;
    }
}
