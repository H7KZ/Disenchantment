package cz.kominekjan.disenchantment.commands;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cz.kominekjan.disenchantment.Disenchantment.plugin;
import static cz.kominekjan.disenchantment.commands.CommandRegister.commands;

public class CommandCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            for (String c : commands.keySet()) {
                if (c.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(c);
                }
            }

            return result;
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "config":
                    for (String arg : commands.get("config").args) {
                        if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                            result.add(arg);
                        }
                    }
                    return result;
                case "toggle":
                    for (String arg : plugin.getServer().getWorlds().stream().map(World::getName).toArray(String[]::new)) {
                        if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                            result.add(arg);
                        }
                    }
                    return result;
            }
        }

        if (args.length == 3) {
            if (!args[0].equalsIgnoreCase("config")) return null;

            switch (args[1].toLowerCase()) {
                case "enchantments":
                    for (String arg : Arrays.stream(Enchantment.values()).map(Enchantment::getName).toArray(String[]::new)) {
                        if (arg.toLowerCase().startsWith(args[2].toLowerCase())) {
                            result.add(arg);
                        }
                    }
                    return result;
                case "items":
                    for (String arg : Arrays.stream(Material.values()).map(Material::name).toArray(String[]::new)) {
                        if (arg.toLowerCase().startsWith(args[2].toLowerCase())) {
                            result.add(arg);
                        }
                    }
                    return result;
            }
        }

        return null;
    }
}
