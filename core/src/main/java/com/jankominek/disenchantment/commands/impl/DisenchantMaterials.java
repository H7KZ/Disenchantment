package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class DisenchantMaterials {
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:materials",
            PermissionGroupType.COMMAND_DISENCHANT_MATERIALS,
            new String[]{},
            false,
            DisenchantMaterials::execute,
            DisenchantMaterials::complete
    );

    public static void execute(CommandSender s, String[] args) {
        List<Material> materials = Config.Disenchantment.getDisabledMaterials();

        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Materials.title());

            if (materials.isEmpty()) {
                s.sendMessage(I18n.Commands.Materials.empty());
                return;
            }

            for (Material material : materials) {
                s.sendMessage(I18n.Commands.Materials.material(material.getKey().getKey(), I18n.Commands.Materials.States.disabled()));
            }

            return;
        }

        Material material = Material.getMaterial(args[1].toUpperCase());

        if (materials.contains(material)) {
            materials.remove(material);

            Config.Disenchantment.setDisabledMaterials(materials);

            s.sendMessage(I18n.Messages.materialIsEnabled());
        } else {
            materials.add(material);

            Config.Disenchantment.setDisabledMaterials(materials);

            s.sendMessage(I18n.Messages.materialIsDisabled());
        }
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        for (Material material : MaterialUtils.getMaterials()) {
            if (material.getKey().getKey().toLowerCase().startsWith(args[1].toLowerCase()))
                result.add(material.getKey().getKey());
        }

        return result;
    }
}
