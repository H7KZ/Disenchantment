package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.plugins.SupportedPluginCustomEnchantment;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShatterEnchantments {
    public static final CommandBuilder command = new CommandBuilder(
            "shatter:enchantments",
            PermissionGroupType.COMMAND_SHATTER_ENCHANTMENTS,
            new String[]{},
            false,
            ShatterEnchantments::execute,
            ShatterEnchantments::complete
    );

    public static void execute(CommandSender s, String[] args) {
        Map<Enchantment, EnchantmentStateType> enchantmentsStates = Config.Shatterment.getEnchantmentStates();

        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Enchantments.Shatterment.title());

            if (enchantmentsStates.isEmpty()) {
                s.sendMessage(ChatColor.GRAY + I18n.Commands.Enchantments.Shatterment.empty());
                return;
            }

            enchantmentsStates.forEach((enchantment, state) -> {
                String stateI18n = switch (state) {
                    case DISABLE -> I18n.States.disable();
                    case KEEP -> I18n.States.keep();
                    case DELETE -> I18n.States.delete();
                    default -> I18n.States.enable();
                };

                s.sendMessage(I18n.Commands.Enchantments.Shatterment.enchantment(enchantment.getKey().getKey(), stateI18n));
            });

            return;
        }

        Enchantment enchantment = EnchantmentUtils.getRegisteredEnchantments().stream()
                .filter(enchantment1 -> enchantment1.getKey().getKey().equalsIgnoreCase(args[1]))
                .findFirst().orElse(null);

        if (enchantment == null) {
            // Not every enchantment is registered in Bukkit, e.g. custom enchantments.
            enchantment = new SupportedPluginCustomEnchantment(NamespacedKey.minecraft(args[1].toLowerCase()));
        }

        if (args.length == 2) {
            s.sendMessage(I18n.Messages.specifyEnchantmentState());
            return;
        }

        String state = args[2].toLowerCase();
        HashMap<Enchantment, EnchantmentStateType> enchantments = Config.Shatterment.getEnchantmentStates();

        if (EnchantmentStateType.ENABLE.getConfigName().equalsIgnoreCase(state)) {
            enchantments.remove(enchantment);

            Config.Shatterment.setEnchantmentStates(enchantments);

            s.sendMessage(I18n.Messages.enchantmentIsEnabled(enchantment.getKey().getKey()));

            return;
        } else if (EnchantmentStateType.KEEP.getConfigName().equalsIgnoreCase(state)) {
            if (enchantments.containsKey(enchantment)) enchantments.replace(enchantment, EnchantmentStateType.KEEP);
            else enchantments.put(enchantment, EnchantmentStateType.KEEP);

            Config.Shatterment.setEnchantmentStates(enchantments);

            s.sendMessage(I18n.Messages.enchantmentIsKept(enchantment.getKey().getKey()));

            return;
        } else if (EnchantmentStateType.DELETE.getConfigName().equalsIgnoreCase(state)) {
            if (enchantments.containsKey(enchantment)) enchantments.replace(enchantment, EnchantmentStateType.DELETE);
            else enchantments.put(enchantment, EnchantmentStateType.DELETE);

            Config.Shatterment.setEnchantmentStates(enchantments);

            s.sendMessage(I18n.Messages.enchantmentIsDeleted(enchantment.getKey().getKey()));

            return;
        } else if (EnchantmentStateType.DISABLE.getConfigName().equalsIgnoreCase(state)) {
            if (enchantments.containsKey(enchantment)) enchantments.replace(enchantment, EnchantmentStateType.DISABLE);
            else enchantments.put(enchantment, EnchantmentStateType.DISABLE);

            Config.Shatterment.setEnchantmentStates(enchantments);

            s.sendMessage(I18n.Messages.enchantmentIsDisabled(enchantment.getKey().getKey()));

            return;
        }

        s.sendMessage(I18n.Messages.specifyEnchantmentState());
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        return DisenchantEnchantments.complete(sender, args);
    }
}
