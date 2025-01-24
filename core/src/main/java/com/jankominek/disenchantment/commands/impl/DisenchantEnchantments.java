package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jankominek.disenchantment.utils.TextUtils.*;

public class DisenchantEnchantments {
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:enchantments",
            PermissionGroupType.COMMAND_DISENCHANT_ENCHANTMENTS,
            "You don't have permission to use this command.",
            new String[]{},
            false,
            DisenchantEnchantments::execute,
            DisenchantEnchantments::complete
    );

    public static void execute(CommandSender s, String[] args) {
        Map<Enchantment, EnchantmentStateType> enchantmentsStates = Config.Disenchantment.getEnchantmentStates();

        if (args.length == 1) {
            s.sendMessage(textWithPrefix("Disabled enchantments"));
            s.sendMessage("");

            enchantmentsStates.forEach((enchantment, state) -> {
                String builder = "";

                switch (state) {
                    case DISABLED -> builder += ChatColor.RED + "[cancel] ";
                    case KEEP -> builder += ChatColor.GOLD + "[ keep ] ";
                    case DELETE -> builder += ChatColor.YELLOW + "[delete] ";
                }

                builder += ChatColor.GRAY + enchantment.getKey().getKey().toLowerCase();

                s.sendMessage(builder);
            });

            return;
        }

        Enchantment enchantment = EnchantmentUtils.getRegisteredEnchantments().stream()
                .filter(enchantment1 -> enchantment1.getKey().getKey().equalsIgnoreCase(args[1]))
                .findFirst().orElse(null);

        if (enchantment == null) {
            s.sendMessage(textWithPrefixError("Unknown enchantment!"));
            return;
        }

        if (args.length == 2) {
            s.sendMessage(textWithPrefixError("You must specify a state for this enchantment"));
            return;
        }

        String state = args[2].toLowerCase();
        HashMap<Enchantment, EnchantmentStateType> enchantments = Config.Disenchantment.getEnchantmentStates();

        if (EnchantmentStateType.ENABLED.getConfigName().startsWith(state)) {
            enchantments.remove(enchantment);

            Config.Disenchantment.setEnchantmentStates(enchantments);

            s.sendMessage(textWithPrefixSuccess("Enchantment enabled"));
            return;
        } else if (EnchantmentStateType.KEEP.getConfigName().startsWith(state)) {
            if (enchantments.containsKey(enchantment)) {
                enchantments.replace(enchantment, EnchantmentStateType.KEEP);

                Config.Disenchantment.setEnchantmentStates(enchantments);

                s.sendMessage(textWithPrefixSuccess("Enchantment state updated"));
                return;
            }

            enchantments.put(enchantment, EnchantmentStateType.KEEP);

            Config.Disenchantment.setEnchantmentStates(enchantments);

            s.sendMessage(textWithPrefixSuccess("Enchantment kept"));
            return;
        } else if (EnchantmentStateType.DELETE.getConfigName().startsWith(state)) {
            if (enchantments.containsKey(enchantment)) {
                enchantments.replace(enchantment, EnchantmentStateType.DELETE);

                Config.Disenchantment.setEnchantmentStates(enchantments);

                s.sendMessage(textWithPrefixSuccess("Enchantment state updated"));
                return;
            }

            enchantments.put(enchantment, EnchantmentStateType.DELETE);

            Config.Disenchantment.setEnchantmentStates(enchantments);

            s.sendMessage(textWithPrefixSuccess("Enchantment deleted"));
            return;
        } else if (EnchantmentStateType.DISABLED.getConfigName().startsWith(state)) {
            if (enchantments.containsKey(enchantment)) {
                enchantments.replace(enchantment, EnchantmentStateType.DISABLED);

                Config.Disenchantment.setEnchantmentStates(enchantments);

                s.sendMessage(textWithPrefixSuccess("Enchantment state updated"));
                return;
            }

            enchantments.put(enchantment, EnchantmentStateType.DISABLED);

            Config.Disenchantment.setEnchantmentStates(enchantments);

            s.sendMessage(textWithPrefixSuccess("Enchantment disabled"));
            return;
        }

        s.sendMessage(textWithPrefixError("Unknown state!"));
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        if (args.length == 2) {
            for (Enchantment enchantment : EnchantmentUtils.getRegisteredEnchantments()) {
                if (enchantment.getKey().getKey().toLowerCase().startsWith(args[1].toLowerCase())) {
                    result.add(enchantment.getKey().getKey());
                }
            }
        } else if (args.length == 3) {
            if (EnchantmentStateType.ENABLED.getConfigName().startsWith(args[2].toLowerCase()))
                result.add(EnchantmentStateType.ENABLED.getConfigName());

            if (EnchantmentStateType.KEEP.getConfigName().startsWith(args[2].toLowerCase()))
                result.add(EnchantmentStateType.KEEP.getConfigName());

            if (EnchantmentStateType.DELETE.getConfigName().startsWith(args[2].toLowerCase()))
                result.add(EnchantmentStateType.DELETE.getConfigName());

            if (EnchantmentStateType.DISABLED.getConfigName().startsWith(args[2].toLowerCase()))
                result.add(EnchantmentStateType.DISABLED.getConfigName());
        }

        return result;
    }
}
