package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.nms.NMSMapper;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Diagnostic {
    public static final CommandBuilder command = new CommandBuilder(
            "diagnostic",
            PermissionGroupType.COMMAND_DIAGNOSTIC,
            new String[]{"all"},
            false,
            Diagnostic::execute,
            Diagnostic::complete
    );

    private static final String SPACER = "\n" + ChatColor.YELLOW + "-------------------------------" + ChatColor.RESET;

    public static void execute(CommandSender sender, String[] args) {
        boolean allInfo = args.length > 1 && args[1].equalsIgnoreCase("all");

        StringBuilder result = new StringBuilder();
        try {
            result.append("\nDisenchantment diagnostic information:");
            String currentWorld;
            if (sender instanceof Player player) {
                currentWorld = player.getWorld().getName();
            } else {
                currentWorld = "None (not a player)";
            }

            // Generic debug section
            result.append(SPACER);

            result.append("\nServer Version: ")
                    .append(NMSMapper.hasNMS() ? ChatColor.GREEN : ChatColor.RED) // Color start
                    .append(Bukkit.getVersion()).append(" (").append(Bukkit.getName()).append(')')
                    .append(ChatColor.RESET); // Color end
            result.append("\nPlugin Version: ")
                    .append(ChatColor.YELLOW) // Color start
                    .append(Disenchantment.plugin.getDescription().getVersion())
                    .append(ChatColor.RESET); // Color end
            result.append("\nPlugin Enabled: ").append(stylizedBool(Config.isPluginEnabled()));

            // Plugins section
            result.append(SPACER);
            List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();
            result.append("\nActivated Plugins: ")
                    .append(ChatColor.YELLOW) // Color start
                    .append(extractValueFromList(activatedPlugins, ISupportedPlugin::getName))
                    .append(ChatColor.RESET); // Color end

            List<Plugin> enabledPlugins = new ArrayList<>();
            List<Plugin> disabledPlugins = new ArrayList<>();
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.isEnabled()) {
                    enabledPlugins.add(plugin);
                } else {
                    disabledPlugins.add(plugin);
                }
            }

            result.append("\nEnabled Plugins: ")
                    .append(ChatColor.GREEN) // Color start
                    .append(extractValueFromList(enabledPlugins, Plugin::getName))
                    .append(ChatColor.RESET); // Color end
            result.append("\nDisabled Plugins: ")
                    .append(ChatColor.RED) // Color start
                    .append(extractValueFromList(disabledPlugins, Plugin::getName))
                    .append(ChatColor.RESET); // Color end

            Set<Plugin> eventListeners = Arrays.stream(PrepareAnvilEvent.getHandlerList().getRegisteredListeners())
                    .map(RegisteredListener::getPlugin).collect(Collectors.toSet());
            eventListeners.remove(Disenchantment.plugin);

            result.append("\nPrepare Anvil Listeners: ")
                    .append(ChatColor.YELLOW) // Color start
                    .append(extractValueFromList(disabledPlugins, Plugin::getName))
                    .append(ChatColor.RESET); // Color end

            // Disenchantment debug section
            result.append(SPACER);

            result.append("\nDisenchantment Enabled: ")
                    .append(stylizedBool(Config.Disenchantment.isEnabled()));

            result.append("\nDisenchantment Cost Enabled: ")
                    .append(stylizedBool(Config.Disenchantment.Anvil.Repair.isCostEnabled()));
            result.append("\nDisenchantment Reset Enabled: ")
                    .append(stylizedBool(Config.Disenchantment.Anvil.Repair.isResetEnabled()));
            result.append("\nDisenchantment Base cost: ")
                    .append(ChatColor.YELLOW) // Color start
                    .append(Config.Disenchantment.Anvil.Repair.getBaseCost())
                    .append(ChatColor.RESET); // Color end
            result.append("\nDisenchantment Cost multiplier: ")
                    .append(ChatColor.YELLOW) // Color start
                    .append(Config.Disenchantment.Anvil.Repair.getCostMultiplier())
                    .append(ChatColor.RESET); // Color end

            if (allInfo) {
                // Disenchantment sound section
                result.append(SPACER);
                result.append("\nDisenchantment Sound Enabled: ")
                        .append(stylizedBool(Config.Disenchantment.Anvil.Sound.isEnabled()));
                result.append("\nDisenchantment Sound Volume: ")
                        .append(ChatColor.YELLOW) // Color start
                        .append(Config.Disenchantment.Anvil.Sound.getVolume())
                        .append(ChatColor.RESET); // Color end
                result.append("\nDisenchantment Sound Pitch: ")
                        .append(ChatColor.YELLOW) // Color start
                        .append(Config.Disenchantment.Anvil.Sound.getPitch())
                        .append(ChatColor.RESET); // Color end

                // Disenchantment Disabled Worlds, Materials & enchantment states
                result.append(SPACER);

                result.append("\nCurrent World: ")
                        .append(ChatColor.YELLOW) // Color start
                        .append(currentWorld)
                        .append(ChatColor.RESET); // Color end
                result.append("\nDisenchantment Disabled Worlds: ")
                        .append(ChatColor.YELLOW) // Color start
                        .append(readDisabledWorld(Config.Disenchantment.getDisabledWorlds()))
                        .append(ChatColor.RESET); // Color end

                result.append("\nDisenchantment Disabled Materials: ")
                        .append(ChatColor.YELLOW) // Color start
                        .append(readDisabledMaterials(Config.Disenchantment.getDisabledMaterials()))
                        .append(ChatColor.RESET); // Color end

                result.append("\nDisenchantment Enchantment States:");
                writeEnchantmentStates(result, Config.Disenchantment.getEnchantmentStates());
            }

            // Shatterment debug section
            result.append(SPACER);

            result.append("\nShatterment Enabled: ")
                    .append(stylizedBool(Config.Shatterment.isEnabled()));

            result.append("\nShatterment Cost Enabled: ")
                    .append(stylizedBool(Config.Shatterment.Anvil.Repair.isCostEnabled()));
            result.append("\nShatterment Reset Enabled: ")
                    .append(stylizedBool(Config.Shatterment.Anvil.Repair.isResetEnabled()));
            result.append("\nShatterment Base cost: ")
                    .append(ChatColor.YELLOW) // Color start
                    .append(Config.Shatterment.Anvil.Repair.getBaseCost())
                    .append(ChatColor.RESET); // Color end
            result.append("\nShatterment Cost multiplier: ")
                    .append(ChatColor.YELLOW) // Color start
                    .append(Config.Shatterment.Anvil.Repair.getCostMultiplier())
                    .append(ChatColor.RESET); // Color end

            if (allInfo) {
                // Shatterment sound section
                result.append(SPACER);
                result.append("\nShatterment Sound Enabled: ")
                        .append(stylizedBool(Config.Shatterment.Anvil.Sound.isEnabled()));
                result.append("\nShatterment Sound Volume: ")
                        .append(ChatColor.YELLOW) // Color start
                        .append(Config.Shatterment.Anvil.Sound.getVolume())
                        .append(ChatColor.RESET); // Color end
                result.append("\nShatterment Sound Pitch: ")
                        .append(ChatColor.YELLOW) // Color start
                        .append(Config.Shatterment.Anvil.Sound.getPitch())
                        .append(ChatColor.RESET); // Color end

                // Shatterment Disabled Worlds, Materials & enchantment states
                result.append(SPACER);

                result.append("\nCurrent World: ")
                        .append(ChatColor.YELLOW) // Color start
                        .append(currentWorld)
                        .append(ChatColor.RESET); // Color end
                result.append("\nShatterment Disabled Worlds: ")
                        .append(ChatColor.YELLOW) // Color start
                        .append(readDisabledWorld(Config.Shatterment.getDisabledWorlds()))
                        .append(ChatColor.RESET); // Color end

                result.append("\nShatterment Enchantment States:");
                writeEnchantmentStates(result, Config.Shatterment.getEnchantmentStates());
            }

            // Permission test section
            result.append(SPACER);
            boolean hasDisenchantmentPerm = PermissionGroupType.DISENCHANT_EVENT.hasPermission(sender);
            boolean hasShattermentPerm = PermissionGroupType.DISENCHANT_EVENT.hasPermission(sender);
            result.append("\nHas Disenchantment Permission: ").append(stylizedBool(hasDisenchantmentPerm));
            result.append("\nHas Shatterment Permission: ").append(stylizedBool(hasShattermentPerm));

        } catch (Exception e) {
            result.append(SPACER);
            result.append("\nAn error occurred while trying to get some diagnostic info:\n");
            result.append(e.getMessage());
        }

        result.append(SPACER);

        if (sender instanceof Player player) {
            // Strip color & send button to copy the diag
            String strippedResult = ChatColor.stripColor(result.toString());

            TextComponent message = new TextComponent(ChatColor.GREEN + "Click to copy diagnostic data");
            message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, strippedResult));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to copy")));

            player.spigot().sendMessage(message);
        } else {
            sender.sendMessage(result.toString());
        }
    }

    private static String readDisabledWorld(List<World> disabledWorlds) {
        return extractValueFromList(disabledWorlds, WorldInfo::getName);
    }

    private static String readDisabledMaterials(List<Material> disabledMaterials) {
        return extractValueFromList(disabledMaterials, Enum::toString);
    }

    private static <T> String extractValueFromList(List<T> list, Function<T, String> convert) {
        StringBuilder stb = new StringBuilder();
        if (list.isEmpty()) {
            stb.append("None");
        } else {
            for (T element : list) {
                stb.append(convert.apply(element)).append(", ");
            }
            stb.delete(stb.length() - 2, stb.length());
        }

        return stb.toString();
    }

    private static void writeEnchantmentStates(StringBuilder stb, Map<Enchantment, EnchantmentStateType> enchantmentStates) {
        enchantmentStates.forEach((key, val) -> {
            stb.append("\n-").append(key.getKey())
                    .append('=').append(val.getDisplayName()).append('\n');
        });
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 2 && "all".startsWith(args[1].toLowerCase())) {
            return List.of("all");
        }
        return Collections.emptyList();
    }

    private static String stylizedBool(boolean val) {
        if (val)
            return ChatColor.GREEN + "True" + ChatColor.RESET;
        else
            return ChatColor.RED + "False" + ChatColor.RESET;
    }

}
