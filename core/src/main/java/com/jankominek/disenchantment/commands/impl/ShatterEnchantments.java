package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the "shatter:enchantments" subcommand for managing enchantment states
 * in the shatterment feature. Supports listing current enchantment states and
 * changing individual enchantments to enabled, disabled, kept, or deleted states.
 */
public class ShatterEnchantments {
    /**
     * The command definition for the shatter:enchantments subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "shatter:enchantments",
            PermissionGroupType.COMMAND_SHATTER_ENCHANTMENTS,
            new String[]{},
            false,
            ShatterEnchantments::execute,
            ShatterEnchantments::complete
    );

    /**
     * Executes the shatter:enchantments command. With no extra arguments, lists all
     * enchantment states for the shatterment feature. With an enchantment key and
     * state, updates the enchantment's configuration (enable, disable, keep, or delete).
     *
     * @param s    the command sender
     * @param args the command arguments: [subcommand, enchantment_key, state]
     */
    public static void execute(CommandSender s, String[] args) {
        Map<String, EnchantmentStateType> enchantmentsStates = Config.Shatterment.getEnchantmentStates();

        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Enchantments.Shatterment.title());

            if (enchantmentsStates.isEmpty()) {
                s.sendMessage(ChatColor.GRAY + I18n.Commands.Enchantments.Shatterment.empty());
                return;
            }

            enchantmentsStates.forEach((key, state) -> {
                String stateI18n = switch (state) {
                    case DISABLE -> I18n.States.disable();
                    case KEEP -> I18n.States.keep();
                    case DELETE -> I18n.States.delete();
                    default -> I18n.States.enable();
                };

                s.sendMessage(I18n.Commands.Enchantments.Shatterment.enchantment(key, stateI18n));
            });

            return;
        }

        if (args.length == 2) {
            s.sendMessage(I18n.Messages.specifyEnchantmentState());
            return;
        }

        String key = args[1].toLowerCase();
        String state = args[2].toLowerCase();
        HashMap<String, EnchantmentStateType> enchantments = Config.Shatterment.getEnchantmentStates();

        if (EnchantmentStateType.ENABLE.getConfigName().equalsIgnoreCase(state)) {
            enchantments.remove(key);

            Config.Shatterment.setEnchantmentStates(enchantments);

            s.sendMessage(I18n.Messages.enchantmentIsEnabled(key));

            return;
        } else if (EnchantmentStateType.KEEP.getConfigName().equalsIgnoreCase(state)) {
            if (enchantments.containsKey(key)) enchantments.replace(key, EnchantmentStateType.KEEP);
            else enchantments.put(key, EnchantmentStateType.KEEP);

            Config.Shatterment.setEnchantmentStates(enchantments);

            s.sendMessage(I18n.Messages.enchantmentIsKept(key));

            return;
        } else if (EnchantmentStateType.DELETE.getConfigName().equalsIgnoreCase(state)) {
            if (enchantments.containsKey(key)) enchantments.replace(key, EnchantmentStateType.DELETE);
            else enchantments.put(key, EnchantmentStateType.DELETE);

            Config.Shatterment.setEnchantmentStates(enchantments);

            s.sendMessage(I18n.Messages.enchantmentIsDeleted(key));

            return;
        } else if (EnchantmentStateType.DISABLE.getConfigName().equalsIgnoreCase(state)) {
            if (enchantments.containsKey(key)) enchantments.replace(key, EnchantmentStateType.DISABLE);
            else enchantments.put(key, EnchantmentStateType.DISABLE);

            Config.Shatterment.setEnchantmentStates(enchantments);

            s.sendMessage(I18n.Messages.enchantmentIsDisabled(key));

            return;
        }

        s.sendMessage(I18n.Messages.specifyEnchantmentState());
    }

    /**
     * Provides tab completion suggestions by delegating to
     * {@link DisenchantEnchantments#complete(CommandSender, String[])}.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        return DisenchantEnchantments.complete(sender, args);
    }
}
