package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.anvils.SpecialAnvilItem;
import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handles the "give" subcommand: {@code /disenchantment give anvil <player> [amount]} hands out the
 * special anvil item. Whatever does not fit in the receiver's inventory is dropped at their feet.
 */
public class Give {
    /**
     * Largest amount a single command may give (one stack).
     */
    public static final int MAX_AMOUNT = 64;

    /**
     * The command definition for the give subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "give",
            PermissionGroupType.COMMAND_GIVE,
            new String[]{"anvil"},
            true,
            Give::execute,
            Give::complete
    );

    /**
     * Gives the special anvil item to an online player.
     *
     * @param s    the command sender
     * @param args the command arguments; args[1] is "anvil", args[2] the player, args[3] the optional amount
     */
    public static void execute(CommandSender s, String[] args) {
        if (args.length < 3) {
            s.sendMessage(I18n.getPrefix() + " " + I18n.Messages.giveAnvilUsage());
            return;
        }

        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            s.sendMessage(I18n.getPrefix() + " " + I18n.Messages.givePlayerNotFound(args[2]));
            return;
        }

        int amount = 1;
        if (args.length > 3) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                amount = -1;
            }

            if (amount < 1 || amount > MAX_AMOUNT) {
                s.sendMessage(I18n.getPrefix() + " " + I18n.Messages.giveAmountInvalid(String.valueOf(MAX_AMOUNT)));
                return;
            }
        }

        HashMap<Integer, ItemStack> leftover = target.getInventory().addItem(SpecialAnvilItem.create(amount));
        for (ItemStack stack : leftover.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), stack);
        }

        s.sendMessage(I18n.getPrefix() + " " + I18n.Messages.giveAnvilGiven(String.valueOf(amount), target.getName()));
        if (!target.equals(s)) {
            target.sendMessage(I18n.getPrefix() + " " + I18n.Messages.giveAnvilReceived(String.valueOf(amount)));
        }
    }

    /**
     * Suggests "anvil", then online player names, then common amounts.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length == 2) {
            for (String arg : Give.command.args()) {
                if (arg.toLowerCase().startsWith(args[1].toLowerCase())) result.add(arg);
            }
        } else if (args.length == 3 && args[1].equalsIgnoreCase("anvil")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) result.add(player.getName());
            }
        } else if (args.length == 4 && args[1].equalsIgnoreCase("anvil")) {
            for (String amount : new String[]{"1", "8", "16", "32", "64"}) {
                if (amount.startsWith(args[3])) result.add(amount);
            }
        }

        return result;
    }
}
