package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
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

public class DisenchantEnchantments {
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:enchantments",
            PermissionGroupType.COMMAND_DISENCHANT_ENCHANTMENTS,
            new String[]{},
            false,
            DisenchantEnchantments::execute,
            DisenchantEnchantments::complete
    );

    public static void execute(CommandSender s, String[] args) {
        Map<String, EnchantmentStateType> enchantmentsStates = Config.Disenchantment.getEnchantmentStates();

        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Enchantments.Disenchantment.title());

            if (enchantmentsStates.isEmpty()) {
                s.sendMessage(ChatColor.GRAY + I18n.Commands.Enchantments.Disenchantment.empty());
                return;
            }

            enchantmentsStates.forEach((key, state) -> {
                String stateI18n = switch (state) {
                    case DISABLE -> I18n.States.disable();
                    case KEEP -> I18n.States.keep();
                    case DELETE -> I18n.States.delete();
                    default -> I18n.States.enable();
                };

                s.sendMessage(I18n.Commands.Enchantments.Disenchantment.enchantment(key, stateI18n));
            });

            return;
        }

        if (args.length == 2) {
            s.sendMessage(I18n.Messages.specifyEnchantmentState());
            return;
        }

        String key = args[1].toLowerCase();
        String state = args[2].toLowerCase();
        HashMap<String, EnchantmentStateType> enchantments = Config.Disenchantment.getEnchantmentStates();

        if (EnchantmentStateType.ENABLE.getConfigName().equalsIgnoreCase(state)) {
            enchantments.remove(key);

            Config.Disenchantment.setEnchantmentStates(enchantments);

            s.sendMessage(I18n.Messages.enchantmentIsEnabled(key));

            return;
        } else if (EnchantmentStateType.KEEP.getConfigName().equalsIgnoreCase(state)) {
            if (enchantments.containsKey(key)) enchantments.replace(key, EnchantmentStateType.KEEP);
            else enchantments.put(key, EnchantmentStateType.KEEP);

            Config.Disenchantment.setEnchantmentStates(enchantments);

            s.sendMessage(I18n.Messages.enchantmentIsKept(key));

            return;
        } else if (EnchantmentStateType.DELETE.getConfigName().equalsIgnoreCase(state)) {
            if (enchantments.containsKey(key)) enchantments.replace(key, EnchantmentStateType.DELETE);
            else enchantments.put(key, EnchantmentStateType.DELETE);

            Config.Disenchantment.setEnchantmentStates(enchantments);

            s.sendMessage(I18n.Messages.enchantmentIsDeleted(key));

            return;
        } else if (EnchantmentStateType.DISABLE.getConfigName().equalsIgnoreCase(state)) {
            if (enchantments.containsKey(key)) enchantments.replace(key, EnchantmentStateType.DISABLE);
            else enchantments.put(key, EnchantmentStateType.DISABLE);

            Config.Disenchantment.setEnchantmentStates(enchantments);

            s.sendMessage(I18n.Messages.enchantmentIsDisabled(key));

            return;
        }

        s.sendMessage(I18n.Messages.specifyEnchantmentState());
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        if (args.length == 2) {
            for (Enchantment enchantment : EnchantmentUtils.getRegisteredEnchantments()) {
                if (enchantment.getKey().getKey().toLowerCase().startsWith(args[1].toLowerCase()))
                    result.add(enchantment.getKey().getKey());
            }
        }

        if (args.length == 3) {
            if (EnchantmentStateType.ENABLE.getConfigName().startsWith(args[2].toLowerCase()))
                result.add(EnchantmentStateType.ENABLE.getConfigName());

            if (EnchantmentStateType.KEEP.getConfigName().startsWith(args[2].toLowerCase()))
                result.add(EnchantmentStateType.KEEP.getConfigName());

            if (EnchantmentStateType.DELETE.getConfigName().startsWith(args[2].toLowerCase()))
                result.add(EnchantmentStateType.DELETE.getConfigName());

            if (EnchantmentStateType.DISABLE.getConfigName().startsWith(args[2].toLowerCase()))
                result.add(EnchantmentStateType.DISABLE.getConfigName());
        }

        return result;
    }
}
