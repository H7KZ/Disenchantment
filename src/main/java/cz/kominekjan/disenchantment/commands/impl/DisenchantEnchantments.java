package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.types.EnchantmentState;
import cz.kominekjan.disenchantment.utils.DisenchantUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

import static cz.kominekjan.disenchantment.utils.TextUtils.*;

public class DisenchantEnchantments {
    public static final Command command = new Command(
            "disenchant_enchantments",
            new String[]{"disenchantment.all", "disenchantment.command.disenchant_enchantments"},
            "You don't have permission to use this command.",
            new String[]{},
            false,
            DisenchantEnchantments::execute
    );

    public static void execute(CommandSender s, String[] args) {
        Map<Enchantment, EnchantmentState> enchantmentsStates = Config.Disenchantment.getEnchantmentStates();

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

        Enchantment enchantment = DisenchantUtils.everyEnchantments().stream()
                .filter(enchantment1 -> enchantment1.getKey().getKey().equalsIgnoreCase(args[1]))
                .findFirst().orElse(null);

        if (enchantment == null) {
            s.sendMessage(textWithPrefixError("Unknown enchantment!"));
            return;
        }

        if (args.length == 2) {
            s.sendMessage(textWithPrefixError("You must specify if you want to enable/keep/cancel this enchantment"));
            return;
        }

        HashMap<Enchantment, EnchantmentState> enchantments = Config.Disenchantment.getEnchantmentStates();

        switch (args[2].toLowerCase()) {
            case "enable":
                enchantments.remove(enchantment);
                Config.Disenchantment.setEnchantmentStates(enchantments);

                s.sendMessage(textWithPrefixSuccess("Enchantment enabled"));
                break;
            case "keep":
                if (enchantments.containsKey(enchantment)) {
                    enchantments.replace(enchantment, EnchantmentState.KEEP);
                    Config.Disenchantment.setEnchantmentStates(enchantments);

                    s.sendMessage(textWithPrefixSuccess("Enchantment state updated"));
                    break;
                }

                enchantments.put(enchantment, EnchantmentState.KEEP);
                Config.Disenchantment.setEnchantmentStates(enchantments);

                s.sendMessage(textWithPrefixSuccess("Enchantment kept"));

                break;
            case "cancel":
                if (enchantments.containsKey(enchantment)) {
                    enchantments.replace(enchantment, EnchantmentState.DISABLED);
                    Config.Disenchantment.setEnchantmentStates(enchantments);

                    s.sendMessage(textWithPrefixSuccess("Enchantment state updated"));
                    break;
                }

                enchantments.put(enchantment, EnchantmentState.DISABLED);
                Config.Disenchantment.setEnchantmentStates(enchantments);

                s.sendMessage(textWithPrefixSuccess("Enchantment disabled"));
                break;
            default:
                s.sendMessage(textWithPrefixError("Unknown state!"));
        }
    }
}
