package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.Disenchantment;
import org.bukkit.Material;

import java.util.List;

public class MaterialUtils {
    public static List<Material> getMaterials() {
        return Disenchantment.nms.getMaterials();
    }
}
