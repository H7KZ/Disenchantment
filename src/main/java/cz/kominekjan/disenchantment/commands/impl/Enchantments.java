package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.config.EnchantmentStatus;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

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
        Map<Enchantment, EnchantmentStatus> enchantmentStatus = Config.getEnchantmentsStatus();

        if (args.length == 1) {
            s.sendMessage(textWithPrefix("Disabled enchantments for disenchanting"));
            s.sendMessage("");

            List<Enchantment> nonEnabledEnchantments = enchantmentStatus.entrySet().stream()
                    .filter(entry -> !EnchantmentStatus.ENABLED.equals(entry.getValue()))
                    .map(Map.Entry::getKey).toList();

            if (nonEnabledEnchantments.isEmpty()) {
                s.sendMessage(ChatColor.GRAY + "No enchantments are disabled or set to being kept for disenchanting");
                return;
            }

            for (Enchantment enchantment : nonEnabledEnchantments) {
                EnchantmentStatus status = enchantmentStatus.get(enchantment);

                StringBuilder builder = new StringBuilder();
                switch (status){
                    case DISABLED -> builder.append(ChatColor.RED).append("[cancel] ");
                    case KEEP -> builder.append(ChatColor.GOLD).append("[ keep ] ");

                }

                builder.append(ChatColor.GRAY).append(enchantment.getKey().getKey());
                s.sendMessage(builder.toString());
            }
            return;
        }

        Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(args[1].toLowerCase()));

        if (enchantment == null) {
            s.sendMessage(textWithPrefixError("Unknown enchantment!"));
            return;
        }

        if(args.length == 2) {
            s.sendMessage(textWithPrefixError("You must specify if you want to enable/keep/cancel this enchantment"));
            return;
        }

        EnchantmentStatus selectedStatus = switch (args[2].toLowerCase()) {
            case "enable" -> EnchantmentStatus.ENABLED;
            case "keep" -> EnchantmentStatus.KEEP;
            case "cancel" -> EnchantmentStatus.DISABLED;
            default -> null;
        };

        if (selectedStatus == null) {
            s.sendMessage(textWithPrefixError("You must specify if you want to enable/keep/cancel this enchantment"));
            return;
        }

        s.sendMessage(textWithPrefixSuccess("Enchantment status set to " + selectedStatus.getDisplayName() + " for disenchantment"));

        Config.setEnchantmentsStatus(enchantment, selectedStatus);

    }

}
