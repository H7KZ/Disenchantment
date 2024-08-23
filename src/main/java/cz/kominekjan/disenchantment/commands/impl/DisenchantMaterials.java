package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import cz.kominekjan.disenchantment.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cz.kominekjan.disenchantment.utils.TextUtils.textWithPrefix;
import static cz.kominekjan.disenchantment.utils.TextUtils.textWithPrefixSuccess;

public class DisenchantMaterials {
    public static final Command command = new Command(
            "disenchant_materials",
            new String[]{"disenchantment.all", "disenchantment.command.disenchant_materials"},
            "You don't have permission to use this command.",
            new String[]{},
            false,
            DisenchantMaterials::execute
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
}
