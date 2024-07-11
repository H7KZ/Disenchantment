package cz.kominekjan.disenchantment.utils;

import org.bukkit.ChatColor;

public class TextUtil {
    public static String TextWithPrefix(String text, ChatColor color) {
        String builder = "";
        builder += ChatColor.DARK_GRAY + "[Disenchantment] ";
        builder += color + text;

        return builder;
    }

    public static String TextWithPrefix(String text) {
        return TextWithPrefix(text, ChatColor.GRAY);
    }

    public static String TextWithPrefixError(String text) {
        return TextWithPrefix(text, ChatColor.RED);
    }

    public static String TextWithPrefixSuccess(String text) {
        return TextWithPrefix(text, ChatColor.GREEN);
    }
}
