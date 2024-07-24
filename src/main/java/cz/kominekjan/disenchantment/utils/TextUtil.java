package cz.kominekjan.disenchantment.utils;

import org.bukkit.ChatColor;

public class TextUtil {
    public static String textWithPrefix(String text, ChatColor color) {
        String builder = "";
        builder += ChatColor.DARK_GRAY + "[Disenchantment] ";
        builder += color + text;

        return builder;
    }

    public static String textWithPrefix(String text) {
        return textWithPrefix(text, ChatColor.GRAY);
    }

    public static String textWithPrefixError(String text) {
        return textWithPrefix(text, ChatColor.RED);
    }

    public static String textWithPrefixSuccess(String text) {
        return textWithPrefix(text, ChatColor.GREEN);
    }
}
