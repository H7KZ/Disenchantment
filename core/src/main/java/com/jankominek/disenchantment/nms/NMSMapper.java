package com.jankominek.disenchantment.nms;

import java.util.logging.Level;

import static com.jankominek.disenchantment.Disenchantment.logger;

/**
 * Reflectively loads the correct {@link NMS} implementation class based on the detected
 * Minecraft server version.
 *
 * <p>The implementation class is resolved by naming convention:
 * {@code com.jankominek.disenchantment.nms.NMS_<nmsVersion>}.</p>
 */
public class NMSMapper {
    /**
     * Instantiates and returns the {@link NMS} implementation matching the current server version.
     *
     * @return the NMS implementation instance, or {@code null} if the version is unsupported
     * or instantiation fails
     */
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

    /**
     * Checks whether a valid {@link NMS} implementation class exists for the current server version.
     *
     * @return {@code true} if a compatible NMS class is available, {@code false} otherwise
     */
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
