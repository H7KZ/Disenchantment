package com.jankominek.disenchantment.nms;

import java.util.logging.Level;

import static com.jankominek.disenchantment.Disenchantment.logger;

public class NMSMapper {
    public static NMS setup() {
        NMS nms = null;

        try {
            String nmsVersion = MinecraftVersion.getServerVersion().getNmsVersion();

            if (nmsVersion == null) return null;

            Class<?> clazz = Class.forName("com.jankominek.disenchantment.nms.NMS_" + nmsVersion);

            if (NMS.class.isAssignableFrom(clazz)) nms = (NMS) clazz.getDeclaredConstructor().newInstance();

            return nms;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to setup NMS for version " + MinecraftVersion.getServerVersion().getVersionString(), e);

            return null;
        }
    }

    public static boolean hasNMS() {
        try {
            String nmsVersion = MinecraftVersion.getServerVersion().getNmsVersion();

            if (nmsVersion == null) return false;

            Class<?> clazz = Class.forName("com.jankominek.disenchantment.nms.NMS_" + nmsVersion);

            return NMS.class.isAssignableFrom(clazz);
        } catch (Exception e) {
            return false;
        }
    }

}
