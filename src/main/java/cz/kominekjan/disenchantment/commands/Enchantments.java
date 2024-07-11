package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

import static cz.kominekjan.disenchantment.Disenchantment.config;
import static cz.kominekjan.disenchantment.Disenchantment.plugin;
import static cz.kominekjan.disenchantment.utils.TextUtil.*;

public class Enchantments {
    public static final CommandUnit unit = new CommandUnit("enchantments", "disenchantment.enchantments", "You don't have permission to use this command.", new String[]{}, false, Enchantments::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(TextWithPrefix("Disabled enchantments"));
            s.sendMessage("");

            List<Map<?, ?>> list = config.getMapList("disabled-enchantments");

            if (list.isEmpty()) {
                s.sendMessage(ChatColor.GRAY + "No enchantments are disabled");
                return;
            }

            for (Map<?, ?> entry : list) {
                String enchantment = (String) entry.get("enchantment");
                if (enchantment == null) continue;
                Boolean keep = (Boolean) entry.get("keep");
                if (keep == null) continue;
                Integer level = (Integer) entry.get("level");
                if (level == null) continue;

                String builder = "";
                builder += ChatColor.RED + "[" + (keep ? " keep " : "cancel") + "] ";
                builder += ChatColor.GRAY + enchantment;
                builder += ChatColor.GRAY + " LVL ";
                builder += ChatColor.GRAY + "" + level;
                s.sendMessage(builder);
            }
            return;
        }

        Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(args[1]));

        if (enchantment == null) {
            s.sendMessage(TextWithPrefixError("Unknown enchantment!"));
            return;
        }

        int level;

        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            s.sendMessage(TextWithPrefixError("You must specify a valid integer"));
            return;
        }

        String keepOrCancel = args[3];

        if (!keepOrCancel.equalsIgnoreCase("keep") && !keepOrCancel.equalsIgnoreCase("cancel")) {
            s.sendMessage(TextWithPrefixError("You must specify a keep/cancel"));
            return;
        }

        Map<String, Object> entry = new java.util.HashMap<>();

        entry.put("enchantment", enchantment.getKey().toString());
        entry.put("level", level);
        entry.put("keep", keepOrCancel.equalsIgnoreCase("keep"));

        List<Map<?, ?>> list = config.getMapList("disabled-enchantments");

        if (config.getMapList("disabled-enchantments").contains(entry)) {
            list.remove(entry);

            config.set("disabled-enchantments", list);
            plugin.saveConfig();

            s.sendMessage(TextWithPrefixSuccess("Enchantment removed"));
            return;
        }

        list.add(entry);

        config.set("disabled-enchantments", list);
        plugin.saveConfig();

        s.sendMessage(TextWithPrefixSuccess("Enchantment added"));
    }
}
