package com.jankominek.disenchantment.nms;

import com.jankominek.disenchantment.utils.DiagnosticUtils;

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
            MinecraftVersion version = MinecraftVersion.getServerVersion();
            String nmsVersion = version.getNmsVersion();

            DiagnosticUtils.debug("NMS", "Resolved MinecraftVersion: " + version.name() + " → nmsVersion=" + nmsVersion);

            if (nmsVersion == null) {
                DiagnosticUtils.debug("NMS", "Setup: FAILED (no NMS module for this Minecraft version)");
                return null;
            }

            String className = "com.jankominek.disenchantment.nms.NMS_" + nmsVersion;
            DiagnosticUtils.debug("NMS", "Loading class: " + className);
            Class<?> clazz = Class.forName(className);

            if (NMS.class.isAssignableFrom(clazz)) {
                nms = (NMS) clazz.getDeclaredConstructor().newInstance();
                DiagnosticUtils.debug("NMS", "NMS instance created: " + nms.getClass().getSimpleName());
            }

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
