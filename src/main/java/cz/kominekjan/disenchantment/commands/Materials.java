package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cz.kominekjan.disenchantment.config.Config.getDisabledMaterials;
import static cz.kominekjan.disenchantment.config.Config.setDisabledMaterials;
import static cz.kominekjan.disenchantment.utils.TextUtils.textWithPrefix;
import static cz.kominekjan.disenchantment.utils.TextUtils.textWithPrefixSuccess;

public class Materials {
    public static final CommandUnit unit = new CommandUnit("materials", "disenchantment.materials", "You don't have permission to use this command.", new String[]{}, false, Materials::command);

    public static void command(CommandSender s, String[] args) {
        List<Material> materials = getDisabledMaterials();

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

            setDisabledMaterials(materials);

            s.sendMessage(textWithPrefixSuccess("Material enabled"));
            return;
        }

        materials.add(material);

        setDisabledMaterials(materials);

        s.sendMessage(textWithPrefixSuccess("Material disabled"));
    }
}
