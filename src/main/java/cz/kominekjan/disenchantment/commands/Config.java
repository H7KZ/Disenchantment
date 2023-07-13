package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

import static cz.kominekjan.disenchantment.Disenchantment.*;

public class Config {
    public static final CommandUnit unit = new CommandUnit("config", "disenchantment.config", "You don't have permission to use this command.", new String[]{"base", "multiply", "items", "enchantments"}, false, Config::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 1) {
            sendMessage(s, "Base value: " + config.getInt("base"), ChatColor.DARK_GREEN);
            sendMessage(s, "Multiply value: " + config.getDouble("multiply"), ChatColor.DARK_GREEN);
            return;
        }

        switch (args[1]) {
            case "base": {
                if (args.length == 2) {
                    sendMessage(s, "You must specify a value");
                    break;
                }

                try {
                    config.set("base", Integer.parseInt(args[2]));
                    plugin.saveConfig();
                    sendMessage(s, "Base value set to " + args[2], ChatColor.GREEN);
                } catch (NumberFormatException e) {
                    sendMessage(s, "You must specify a valid integer");
                }
            }
            case "multiply": {
                if (args.length == 2) {
                    sendMessage(s, "You must specify a value");
                    break;
                }

                try {
                    config.set("multiply", Double.parseDouble(args[2]));
                    plugin.saveConfig();
                    sendMessage(s, "Multiply value set to " + args[2], ChatColor.GREEN);
                } catch (NumberFormatException e) {
                    sendMessage(s, "You must specify a valid double");
                }
            }
            case "enchantments": {
                if (args.length == 2) {
                    for (Map<?, ?> entry : config.getMapList("disabled-enchantments")) {
                        String enchantment = (String) entry.get("enchantment");
                        if (enchantment == null) continue;
                        Boolean keep = (Boolean) entry.get("keep");
                        if (keep == null) continue;
                        Integer level = (Integer) entry.get("level");
                        if (level == null) continue;

                        sendMessage(s, enchantment + " " + level + "+ " + (keep ? "keep" : "cancel"), ChatColor.DARK_GREEN);
                    }
                    break;
                }

                Enchantment enchantment = Enchantment.getByName(args[2]);
                if (enchantment == null) {
                    sendMessage(s, "Unknown enchantment");
                    break;
                }

                int level;
                try {
                    level = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sendMessage(s, "You must specify a valid integer");
                    break;
                }

                boolean keep = args[4].equalsIgnoreCase("keep");
                if (!keep && !args[4].equalsIgnoreCase("cancel")) {
                    sendMessage(s, "You must specify a keep/cancel");
                    break;
                }

                Map<String, Object> entry = new java.util.HashMap<>();
                entry.put("enchantment", enchantment.getName());
                entry.put("level", level);
                entry.put("keep", keep);

                List<Map<?, ?>> list = config.getMapList("disabled-enchantments");

                if (config.getMapList("disabled-enchantments").contains(entry)) {
                    list.remove(entry);

                    config.set("disabled-enchantments", list);
                    plugin.saveConfig();

                    sendMessage(s, "Enchantment removed", ChatColor.GREEN);
                    break;
                }

                list.add(entry);

                config.set("disabled-enchantments", list);
                plugin.saveConfig();

                sendMessage(s, "Enchantment added", ChatColor.GREEN);
                break;
            }
            case "items": {
                if (args.length == 2) {
                    config.getStringList("disabled-items").forEach(item -> sendMessage(s, item, ChatColor.DARK_GREEN));
                    break;
                }

                String item = args[2];

                List<String> list = config.getStringList("disabled-items");

                if (list.contains(item)) {
                    list.remove(item);

                    config.set("disabled-items", list);
                    plugin.saveConfig();

                    sendMessage(s, "Item removed", ChatColor.GREEN);
                    break;
                }

                list.add(item);

                config.set("disabled-items", list);
                plugin.saveConfig();

                sendMessage(s, "Item added", ChatColor.GREEN);
                break;
            }
            default: {
                sendMessage(s, "Unknown config option");
            }
        }
    }
}
