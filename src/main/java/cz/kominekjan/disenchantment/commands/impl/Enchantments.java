package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;

import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;
import static cz.kominekjan.disenchantment.config.Config.setDisabledEnchantments;
import static cz.kominekjan.disenchantment.utils.TextUtils.*;

public class Enchantments {
    public static final Command command = new Command(
            "enchantments",
            new String[]{"disenchantment.all", "disenchantment.command.enchantments"},
            "You don't have permission to use this command.",
            new String[]{},
            false,
            Enchantments::execute
    );

    public static void execute(CommandSender s, String[] args) {
        Map<Enchantment, Boolean> disabledEnchantments = getDisabledEnchantments();

        if (args.length == 1) {
            s.sendMessage(textWithPrefix("Disabled enchantments"));
            s.sendMessage("");

            if (disabledEnchantments.isEmpty()) {
                s.sendMessage(ChatColor.GRAY + "No enchantments are disabled");
                return;
            }

            for (Map.Entry<Enchantment, Boolean> enchantment : disabledEnchantments.entrySet()) {
                String builder = "";
                builder += ChatColor.RED + "[" + (enchantment.getValue() ? " keep " : "cancel") + "] ";
                builder += ChatColor.GRAY + enchantment.getKey().getKey().getKey();
                s.sendMessage(builder);
            }
            return;
        }

        Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(args[1]));

        if (enchantment == null) {
            s.sendMessage(textWithPrefixError("Unknown enchantment!"));
            return;
        }

        String keepOrCancel = args[2];

        if (!keepOrCancel.equalsIgnoreCase("keep") && !keepOrCancel.equalsIgnoreCase("cancel")) {
            s.sendMessage(textWithPrefixError("You must specify a keep/cancel"));
            return;
        }

        Map.Entry<Enchantment, Boolean> disabledEnchantment = Map.entry(enchantment, keepOrCancel.equalsIgnoreCase("keep"));

        if (disabledEnchantments.containsKey(disabledEnchantment.getKey())) {
            disabledEnchantments.remove(disabledEnchantment.getKey());

            setDisabledEnchantments(disabledEnchantments);

            s.sendMessage(textWithPrefixSuccess("Enchantment removed"));
            return;
        }

        disabledEnchantments.put(disabledEnchantment.getKey(), disabledEnchantment.getValue());

        setDisabledEnchantments(disabledEnchantments);

        s.sendMessage(textWithPrefixSuccess("Enchantment added"));
    }
}
