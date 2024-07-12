package cz.kominekjan.disenchantment.commands;

import cz.kominekjan.disenchantment.types.DisabledEnchantment;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.List;

import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;
import static cz.kominekjan.disenchantment.config.Config.setDisabledEnchantments;
import static cz.kominekjan.disenchantment.utils.TextUtils.*;

public class Enchantments {
    public static final CommandUnit unit = new CommandUnit("enchantments", "disenchantment.enchantments", "You don't have permission to use this command.", new String[]{}, false, Enchantments::command);

    public static void command(CommandSender s, String[] args) {
        List<DisabledEnchantment> enchantments = getDisabledEnchantments();

        if (args.length == 1) {
            s.sendMessage(textWithPrefix("Disabled enchantments"));
            s.sendMessage("");

            if (enchantments.isEmpty()) {
                s.sendMessage(ChatColor.GRAY + "No enchantments are disabled");
                return;
            }

            for (DisabledEnchantment enchantment : enchantments) {
                String builder = "";
                builder += ChatColor.RED + "[" + (enchantment.doKeep() ? " keep " : "cancel") + "] ";
                builder += ChatColor.GRAY + enchantment.getEnchantmentKey();
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

        DisabledEnchantment disabledEnchantment = new DisabledEnchantment(enchantment.getKey().getKey(), keepOrCancel.equalsIgnoreCase("keep"));

        if (enchantments.contains(disabledEnchantment)) {
            enchantments.remove(disabledEnchantment);

            setDisabledEnchantments(enchantments);

            s.sendMessage(textWithPrefixSuccess("Enchantment removed"));
            return;
        }

        enchantments.add(disabledEnchantment);

        setDisabledEnchantments(enchantments);

        s.sendMessage(textWithPrefixSuccess("Enchantment added"));
    }
}
