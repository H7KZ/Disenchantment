package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.types.EnchantmentState;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

import static cz.kominekjan.disenchantment.utils.TextUtils.*;

public class ShatterEnchantments {
    public static final Command command = new Command(
            "shatter_enchantments",
            new String[]{"disenchantment.all", "disenchantment.command.shatter_enchantments"},
            "You don't have permission to use this command.",
            new String[]{},
            false,
            ShatterEnchantments::execute
    );

    public static void execute(CommandSender s, String[] args) {
        Map<Enchantment, EnchantmentState> enchantmentsStates = Config.Shatterment.getEnchantmentStates();

        if (args.length == 1) {
            s.sendMessage(textWithPrefix("Disabled enchantments"));
            s.sendMessage("");

            enchantmentsStates.forEach((enchantment, state) -> {
                String builder = "";

                switch (state) {
                    case EnchantmentState.DISABLED -> builder += ChatColor.RED + "[cancel] ";
                    case EnchantmentState.KEEP -> builder += ChatColor.GOLD + "[ keep ] ";
                }

                builder += ChatColor.GRAY + enchantment.getKey().getKey().toLowerCase();

                s.sendMessage(builder);
            });

            return;
        }

        Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(args[1].toLowerCase()));

        if (enchantment == null) {
            s.sendMessage(textWithPrefixError("Unknown enchantment!"));
            return;
        }

        if (args.length == 2) {
            s.sendMessage(textWithPrefixError("You must specify if you want to enable/keep/cancel this enchantment"));
            return;
        }

        HashMap<Enchantment, EnchantmentState> enchantments = Config.Shatterment.getEnchantmentStates();

        switch (args[2].toLowerCase()) {
            case "enable":
                enchantments.remove(enchantment);
                Config.Shatterment.setEnchantmentStates(enchantments);

                s.sendMessage(textWithPrefixSuccess("Enchantment enabled"));
                break;
            case "keep":
                if (enchantments.containsKey(enchantment)) {
                    enchantments.replace(enchantment, EnchantmentState.KEEP);
                    Config.Shatterment.setEnchantmentStates(enchantments);

                    s.sendMessage(textWithPrefixSuccess("Enchantment state updated"));
                    break;
                }

                enchantments.put(enchantment, EnchantmentState.KEEP);
                Config.Shatterment.setEnchantmentStates(enchantments);

                s.sendMessage(textWithPrefixSuccess("Enchantment kept"));

                break;
            case "cancel":
                if (enchantments.containsKey(enchantment)) {
                    enchantments.replace(enchantment, EnchantmentState.DISABLED);
                    Config.Shatterment.setEnchantmentStates(enchantments);

                    s.sendMessage(textWithPrefixSuccess("Enchantment state updated"));
                    break;
                }

                enchantments.put(enchantment, EnchantmentState.DISABLED);
                Config.Shatterment.setEnchantmentStates(enchantments);

                s.sendMessage(textWithPrefixSuccess("Enchantment disabled"));
                break;
            default:
                s.sendMessage(textWithPrefixError("Unknown state!"));
        }
    }
}
