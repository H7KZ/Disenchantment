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

/**
 * Handles the "disenchant:materials" subcommand for managing disabled materials
 * in the disenchantment feature. Supports listing currently disabled materials
 * and toggling individual materials on or off.
 */
public class DisenchantMaterials {
    /**
     * The command definition for the disenchant:materials subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:materials",
            PermissionGroupType.COMMAND_DISENCHANT_MATERIALS,
            new String[]{},
            false,
            DisenchantMaterials::execute,
            DisenchantMaterials::complete
    );

    /**
     * Executes the disenchant:materials command. With no extra arguments, lists all
     * disabled materials. With a material name, toggles it between enabled and disabled.
     *
     * @param s    the command sender
     * @param args the command arguments: [subcommand, material_name]
     */
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

            assert material != null;
            s.sendMessage(I18n.Messages.materialIsEnabled(material.getKey().getKey()));
        } else {
            materials.add(material);

            Config.Disenchantment.setDisabledMaterials(materials);

            assert material != null;
            s.sendMessage(I18n.Messages.materialIsDisabled(material.getKey().getKey()));
        }
    }

    /**
     * Provides tab completion suggestions for material names.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching material name suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        for (Material material : MaterialUtils.getMaterials()) {
            if (material.getKey().getKey().toLowerCase().startsWith(args[1].toLowerCase()))
                result.add(material.getKey().getKey());
        }

        return result;
    }
}
