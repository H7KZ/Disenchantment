package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.permissions.PermissionGroups;
import com.jankominek.disenchantment.utils.MaterialUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.jankominek.disenchantment.utils.TextUtils.textWithPrefix;
import static com.jankominek.disenchantment.utils.TextUtils.textWithPrefixSuccess;

public class DisenchantMaterials {
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:materials",
            PermissionGroups.COMMAND_DISENCHANT_MATERIALS,
            "You don't have permission to use this command.",
            new String[]{},
            false,
            DisenchantMaterials::execute,
            DisenchantMaterials::complete
    );

    public static void execute(CommandSender s, String[] args) {
        List<Material> materials = Config.Disenchantment.getDisabledMaterials();

        if (args.length == 1) {
            s.sendMessage(textWithPrefix("Disabled materials"));
            s.sendMessage("");

            if (materials.isEmpty()) {
                s.sendMessage(ChatColor.GRAY + "No materials are disabled");
                return;
            }

            for (Material material : materials) {
                String builder = "";
                builder += ChatColor.RED + "[X] ";
                builder += ChatColor.GRAY + material.getKey().getKey();

                s.sendMessage(builder);
            }

            return;
        }

        Material material = Material.getMaterial(args[1].toUpperCase());

        if (materials.contains(material)) {
            materials.remove(material);

            Config.Disenchantment.setDisabledMaterials(materials);

            s.sendMessage(textWithPrefixSuccess("Material enabled"));
            return;
        }

        materials.add(material);

        Config.Disenchantment.setDisabledMaterials(materials);

        s.sendMessage(textWithPrefixSuccess("Material disabled"));
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        for (Material material : MaterialUtils.getMaterials()) {
            if (material.getKey().getKey().toLowerCase().startsWith(args[1].toLowerCase())) {
                result.add(material.getKey().getKey());
            }
        }

        return result;
    }
}
