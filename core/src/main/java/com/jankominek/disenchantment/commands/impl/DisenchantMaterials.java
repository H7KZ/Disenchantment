package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.types.RestrictionMode;
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
        if (args.length >= 2 && args[1].equalsIgnoreCase("mode")) {
            if (args.length == 2) {
                s.sendMessage(I18n.Messages.modeCurrent("disenchantment", Config.Disenchantment.getMaterialsMode().name()));
                return;
            }

            RestrictionMode mode = RestrictionMode.match(args[2]);

            if (mode == null) {
                s.sendMessage(I18n.Messages.modeInvalid(args[2]));
                return;
            }

            Config.Disenchantment.setMaterialsMode(mode);
            s.sendMessage(I18n.Messages.modeSet("disenchantment", mode.name()));
            return;
        }

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

        // Guard against an unknown material name — otherwise a null slips into the list and
        // setDisabledMaterials() NPEs on Material::name (assertions are off in production).
        if (material == null) return;

        if (materials.contains(material)) {
            materials.remove(material);

            Config.Disenchantment.setDisabledMaterials(materials);

            s.sendMessage(I18n.Messages.materialIsEnabled(material.getKey().getKey()));
        } else {
            materials.add(material);

            Config.Disenchantment.setDisabledMaterials(materials);

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

        if (args.length >= 3 && args[1].equalsIgnoreCase("mode")) {
            for (RestrictionMode mode : RestrictionMode.values()) {
                if (mode.name().toLowerCase().startsWith(args[2].toLowerCase())) result.add(mode.name());
            }

            return result;
        }

        if ("mode".startsWith(args[1].toLowerCase())) result.add("mode");

        for (Material material : MaterialUtils.getMaterials()) {
            if (material.getKey().getKey().toLowerCase().startsWith(args[1].toLowerCase()))
                result.add(material.getKey().getKey());
        }

        return result;
    }
}
