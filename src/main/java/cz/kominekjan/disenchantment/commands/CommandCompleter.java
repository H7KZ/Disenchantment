package cz.kominekjan.disenchantment.commands;

import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static cz.kominekjan.disenchantment.Disenchantment.plugin;
import static cz.kominekjan.disenchantment.commands.CommandRegister.commands;

public class CommandCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
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
                case "help":
                    for (String arg : commands.get("help").args) {
                        if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                            result.add(arg);
                        }
                    }
                case "repair":
                    for (String arg : commands.get("repair").args) {
                        if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                            result.add(arg);
                        }
                    }
                    return result;
                case "sound":
                    for (String arg : commands.get("sound").args) {
                        if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                            result.add(arg);
                        }
                    }
                    return result;
                case "enchantments":
                case "split-enchantments":
                    for (Enchantment enchantment : Registry.ENCHANTMENT) {
                        if (enchantment.getKey().getKey().toLowerCase().startsWith(args[1].toLowerCase())) {
                            result.add(enchantment.getKey().getKey());
                        }
                    }
                    return result;
                case "materials":
                    for (Material material : Registry.MATERIAL) {
                        if (material.getKey().getKey().toLowerCase().startsWith(args[1].toLowerCase())) {
                            result.add(material.getKey().getKey());
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

        return null;
    }
}
