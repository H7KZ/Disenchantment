package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import cz.kominekjan.disenchantment.commands.ICommandExecutor;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.config.EnchantmentStatus;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;

import static cz.kominekjan.disenchantment.utils.TextUtils.*;

public abstract class Enchantments implements ICommandExecutor {
    public static final Command disenchantCommand = new Command(
            "enchantments",
            new String[]{"disenchantment.all", "disenchantment.command.enchantments"},
            "You don't have permission to use this command.",
            new String[]{},
            false,
            new Enchantments() {
                @Override
                public Map<Enchantment, EnchantmentStatus> getEnchantmentsStatus() {
                    return Config.getEnchantmentsStatus();
                }

                @Override
                public void setEnchantmentsStatus(Enchantment enchantment, EnchantmentStatus status) {
                    Config.setEnchantmentsStatus(enchantment, status);
                }
            }
    );
    public static final Command bookSplittingCommand = new Command(
            "split-enchantments",
            new String[]{"disenchantment.all", "disenchantment.command.enchantments"},
            "You don't have permission to use this command.",
            new String[]{},
            false,
            new Enchantments() {
                @Override
                public Map<Enchantment, EnchantmentStatus> getEnchantmentsStatus() {
                    return Config.getBookSplittingEnchantmentsStatus();
                }

                @Override
                public void setEnchantmentsStatus(Enchantment enchantment, EnchantmentStatus status) {
                    Config.setBookSplittingEnchantmentsStatus(enchantment, status);
                }
            }
    );

    public void execute(CommandSender s, String[] args) {
        Map<Enchantment, EnchantmentStatus> enchantmentStatus = getEnchantmentsStatus();

        if (args.length == 1) {
            s.sendMessage(textWithPrefix("Disabled enchantments"));
            s.sendMessage("");

            long nonEnabledCount = enchantmentStatus.values().stream().filter(status -> !EnchantmentStatus.ENABLED.equals(status)).count();

            if (nonEnabledCount == 0) {
                s.sendMessage(ChatColor.GRAY + "No enchantments are disabled or set to being kept");
                return;
            }

            for (Map.Entry<Enchantment, EnchantmentStatus> enchantment : enchantmentStatus.entrySet()) {
                EnchantmentStatus status = enchantment.getValue();
                if(EnchantmentStatus.ENABLED.equals(status)) continue;

                StringBuilder builder = new StringBuilder();

                switch (status){
                    case DISABLED -> builder.append(ChatColor.RED).append("[cancel] ");
                    case KEEP -> builder.append(ChatColor.GOLD).append("[ keep ] ");

                }

                builder.append(ChatColor.GRAY).append(enchantment.getKey().getKey().getKey());
                s.sendMessage(builder.toString());
            }
            return;
        }

        Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(args[1].toLowerCase()));

        if (enchantment == null) {
            s.sendMessage(textWithPrefixError("Unknown enchantment!"));
            return;
        }

        if(args.length == 2){
            s.sendMessage(textWithPrefixError("You must specify if you want to enable/keep/cancel this enchantment"));
            return;
        }

        EnchantmentStatus selectedStatus = EnchantmentStatus.getStatusByName(args[2]);

        if (selectedStatus == null) {
            s.sendMessage(textWithPrefixError("You must specify if you want to enable/keep/cancel this enchantment"));
            return;
        }

        s.sendMessage(textWithPrefixSuccess("Enchantment status set to " + selectedStatus.getDisplayName()));

        setEnchantmentsStatus(enchantment, selectedStatus);

    }

    public abstract Map<Enchantment, EnchantmentStatus> getEnchantmentsStatus();

    public abstract void setEnchantmentsStatus(Enchantment enchantment, EnchantmentStatus status);

}
