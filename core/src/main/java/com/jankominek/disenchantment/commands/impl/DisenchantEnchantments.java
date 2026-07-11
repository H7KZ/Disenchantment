package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

/**
 * Handles the "disenchant:enchantments" subcommand for managing enchantment states
 * in the disenchantment feature. Supports listing current enchantment states and
 * changing individual enchantments to enabled, disabled, kept, or deleted states.
 */
public class DisenchantEnchantments {
    /**
     * The command definition for the disenchant:enchantments subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:enchantments",
            PermissionGroupType.COMMAND_DISENCHANT_ENCHANTMENTS,
            new String[]{},
            false,
            DisenchantEnchantments::execute,
            DisenchantEnchantments::complete
    );

    /**
     * Executes the disenchant:enchantments command. With no extra arguments, lists all
     * enchantment states. With an enchantment key and state, updates the enchantment's
     * configuration (enable, disable, keep, or delete).
     *
     * @param s    the command sender
     * @param args the command arguments: [subcommand, enchantment_key, state]
     */
    public static void execute(CommandSender s, String[] args) {
        Map<String, EnchantmentStateType> enchantmentsStates = Config.Disenchantment.getEnchantmentStates();

        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Enchantments.Disenchantment.title());

            if (enchantmentsStates.isEmpty()) {
                s.sendMessage("§7" + I18n.Commands.Enchantments.Disenchantment.empty());
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

        // A group name expands to every enchantment key it contains — write-time expansion,
        // so each member enchantment is stored individually (no group-aware runtime resolution needed).
        List<String> groupMembers = Config.getEnchantmentGroups().get(key);

        if (groupMembers != null) {
            boolean applied = false;
            for (String member : groupMembers) applied |= applyState(member, state);

            if (!applied) {
                s.sendMessage(I18n.Messages.specifyEnchantmentState());
                return;
            }
        } else if (!applyState(key, state)) {
            s.sendMessage(I18n.Messages.specifyEnchantmentState());
            return;
        }

        if (EnchantmentStateType.ENABLE.getConfigName().equalsIgnoreCase(state))
            s.sendMessage(I18n.Messages.enchantmentIsEnabled(key));
        else if (EnchantmentStateType.KEEP.getConfigName().equalsIgnoreCase(state))
            s.sendMessage(I18n.Messages.enchantmentIsKept(key));
        else if (EnchantmentStateType.DELETE.getConfigName().equalsIgnoreCase(state))
            s.sendMessage(I18n.Messages.enchantmentIsDeleted(key));
        else if (EnchantmentStateType.DISABLE.getConfigName().equalsIgnoreCase(state))
            s.sendMessage(I18n.Messages.enchantmentIsDisabled(key));
    }

    /**
     * Applies the requested state to a single enchantment key and persists it.
     *
     * @param key   the enchantment key
     * @param state the requested state name
     * @return {@code true} if the state was recognized and applied
     */
    private static boolean applyState(String key, String state) {
        HashMap<String, EnchantmentStateType> enchantments = Config.Disenchantment.getEnchantmentStates();

        if (EnchantmentStateType.ENABLE.getConfigName().equalsIgnoreCase(state)) {
            enchantments.remove(key);
        } else if (EnchantmentStateType.KEEP.getConfigName().equalsIgnoreCase(state)) {
            enchantments.put(key, EnchantmentStateType.KEEP);
        } else if (EnchantmentStateType.DELETE.getConfigName().equalsIgnoreCase(state)) {
            enchantments.put(key, EnchantmentStateType.DELETE);
        } else if (EnchantmentStateType.DISABLE.getConfigName().equalsIgnoreCase(state)) {
            enchantments.put(key, EnchantmentStateType.DISABLE);
        } else {
            return false;
        }

        Config.Disenchantment.setEnchantmentStates(enchantments);
        return true;
    }

    /**
     * Provides tab completion suggestions. At position 2, suggests registered enchantment
     * names merged with custom enchantment keys from config; at position 3, suggests enchantment state types.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        if (args.length == 2) {
            Set<String> keys = new LinkedHashSet<>();
            // Add vanilla keys
            for (Enchantment enchantment : EnchantmentUtils.getRegisteredEnchantments()) {
                keys.add(enchantment.getKey().getKey());
            }
            // Add custom keys already in config states
            keys.addAll(Config.Disenchantment.getEnchantmentStates().keySet());
            // Add group names
            keys.addAll(Config.getEnchantmentGroups().keySet());
            // Filter by current input
            for (String key : keys) {
                if (key.toLowerCase().startsWith(args[1].toLowerCase()))
                    result.add(key);
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
