package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

import static cz.kominekjan.disenchantment.Disenchantment.*;

public class Enchantments {
    public static final CommandUnit unit = new CommandUnit("enchantments", "disenchantment.enchantments", "You don't have permission to use this command.", new String[]{}, false, Enchantments::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 1) {
            sendMessage(s, "Disabled enchantments", ChatColor.GRAY);
            s.sendMessage(ChatColor.GRAY + "|");

            List<Map<?, ?>> list = config.getMapList("disabled-enchantments");

            if (list.isEmpty()) {
                s.sendMessage(ChatColor.GRAY + "| No enchantments disabled");
                return;
            }

            for (Map<?, ?> entry : list) {
                String enchantment = (String) entry.get("enchantment");
                if (enchantment == null) continue;
                Boolean keep = (Boolean) entry.get("keep");
                if (keep == null) continue;
                Integer level = (Integer) entry.get("level");
                if (level == null) continue;

                s.sendMessage(ChatColor.RED + "[" + (keep ? " keep " : "cancel") + "]" + ChatColor.GRAY + "\"" + enchantment + " - LVL " + level + "\"");
            }
            return;
        }

        Enchantment enchantment = Enchantment.getByName(args[1]);

        if (enchantment == null) {
            sendMessage(s, "Unknown enchantment!");
            return;
        }

        int level;

        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sendMessage(s, "You must specify a valid integer");
            return;
        }

        String keepOrCancel = args[3];

        if (!keepOrCancel.equalsIgnoreCase("keep") && !keepOrCancel.equalsIgnoreCase("cancel")) {
            sendMessage(s, "You must specify a keep/cancel");
            return;
        }

        Map<String, Object> entry = new java.util.HashMap<>();

        entry.put("enchantment", enchantment.getName());
        entry.put("level", level);
        entry.put("keep", keepOrCancel.equalsIgnoreCase("keep"));

        List<Map<?, ?>> list = config.getMapList("disabled-enchantments");

        if (config.getMapList("disabled-enchantments").contains(entry)) {
            list.remove(entry);

            config.set("disabled-enchantments", list);
            plugin.saveConfig();

            sendMessage(s, "Enchantment removed", ChatColor.GREEN);
            return;
        }

        list.add(entry);

        config.set("disabled-enchantments", list);
        plugin.saveConfig();

        sendMessage(s, "Enchantment added", ChatColor.GREEN);
    }
}
