package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

/**
 * Handles the "shatter:chances" subcommand for managing per-enchantment success
 * chances used by the shatterment feature. Supports listing current chances and
 * setting an individual enchantment's chance (0.0-1.0).
 */
public class ShatterChances {
    /**
     * The command definition for the shatter:chances subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "shatter:chances",
            PermissionGroupType.COMMAND_SHATTER_ENCHANTMENTS,
            new String[]{},
            false,
            ShatterChances::execute,
            ShatterChances::complete
    );

    /**
     * Executes the shatter:chances command. With no extra arguments, lists all
     * configured enchantment chances. With an enchantment key and a chance value,
     * updates that enchantment's success chance.
     *
     * @param s    the command sender
     * @param args the command arguments: [subcommand, enchantment_key, chance]
     */
    public static void execute(CommandSender s, String[] args) {
        Map<String, Double> chances = Config.Shatterment.getEnchantmentChances();

        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Chances.Shatterment.title());

            if (chances.isEmpty()) {
                s.sendMessage("§7" + I18n.Commands.Chances.Shatterment.empty());
                return;
            }

            chances.forEach((key, chance) -> s.sendMessage(I18n.Commands.Chances.Shatterment.enchantment(key, String.valueOf(chance))));

            return;
        }

        if (args.length == 2) {
            s.sendMessage(I18n.Messages.specifyEnchantmentChance());
            return;
        }

        String key = args[1].toLowerCase();

        double chance;
        try {
            chance = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            s.sendMessage(I18n.Messages.specifyValidDouble());
            return;
        }

        if (chance < 0.0 || chance > 1.0) {
            s.sendMessage(I18n.Messages.specifyEnchantmentChance());
            return;
        }

        Config.Shatterment.setEnchantmentChance(key, chance);

        s.sendMessage(I18n.Messages.enchantmentChanceIsSet(key, String.valueOf(chance)));
    }

    /**
     * Provides tab completion suggestions. At position 2, suggests registered enchantment
     * names merged with custom enchantment keys already configured; at position 3, suggests
     * common chance values.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        if (args.length == 2) {
            Set<String> keys = new LinkedHashSet<>();
            for (Enchantment enchantment : EnchantmentUtils.getRegisteredEnchantments()) {
                keys.add(enchantment.getKey().getKey());
            }
            keys.addAll(Config.Shatterment.getEnchantmentChances().keySet());
            for (String key : keys) {
                if (key.toLowerCase().startsWith(args[1].toLowerCase()))
                    result.add(key);
            }
        }

        if (args.length == 3) {
            for (String suggestion : new String[]{"0.0", "0.25", "0.5", "0.75", "1.0"}) {
                if (suggestion.startsWith(args[2]))
                    result.add(suggestion);
            }
        }

        return result;
    }
}
