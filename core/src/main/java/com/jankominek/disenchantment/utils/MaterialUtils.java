package com.jankominek.disenchantment.utils;

import org.bukkit.Material;

import java.util.List;

import static com.jankominek.disenchantment.Disenchantment.nms;

/**
 * Utility class for retrieving the list of available materials via the NMS implementation.
 */
public class MaterialUtils {
    /**
     * Returns all materials available on the current server version via NMS.
     *
     * @return a list of all {@link Material} values
     */
    public static List<Material> getMaterials() {
        return nms.getMaterials();
    }
}
