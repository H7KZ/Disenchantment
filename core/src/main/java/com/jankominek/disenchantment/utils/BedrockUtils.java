package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.types.BedrockDetectMethod;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

/**
 * Detects players who joined from Minecraft: Bedrock Edition through Geyser.
 * <p>
 * Everything is soft: Floodgate and Geyser are reached by reflection, so there is no compile dependency and
 * nothing is shaded, and every method here swallows every failure (a missing class, a changed API, a
 * {@link LinkageError}) and answers "not detected". The detection methods and their order come from
 * {@code bedrock.detect}; the first one that says yes wins.
 */
public final class BedrockUtils {

    private static final String FLOODGATE_API = "org.geysermc.floodgate.api.FloodgateApi";
    private static final String GEYSER_API = "org.geysermc.geyser.api.GeyserApi";

    private BedrockUtils() {
    }

    /**
     * Returns whether {@code p} is a Bedrock player under the configured {@code bedrock.detect} methods.
     * Never throws.
     *
     * @param p the player
     * @return {@code true} if one of the configured methods recognises the player
     */
    public static boolean isBedrockPlayer(Player p) {
        if (p == null) return false;

        try {
            return isBedrockPlayer(p.getUniqueId(), p.getName(), Config.Bedrock.getDetect(), Config.Bedrock.getPrefixes());
        } catch (Throwable t) {
            DiagnosticUtils.debug("BEDROCK", () -> "Detection failed, treating " + p.getName() + " as a Java player: " + t);
            return false;
        }
    }

    /**
     * Pure form of {@link #isBedrockPlayer(Player)} for explicit inputs. Never throws.
     *
     * @param uuid     the player's UUID
     * @param name     the player's name
     * @param methods  the detection methods, in the order they are tried
     * @param prefixes the name prefixes for {@link BedrockDetectMethod#PREFIX}
     * @return {@code true} if the first method that recognises the player says so
     */
    public static boolean isBedrockPlayer(UUID uuid, String name, List<BedrockDetectMethod> methods, List<String> prefixes) {
        if (methods == null) return false;

        for (BedrockDetectMethod method : methods) {
            if (method == null) continue;

            boolean hit = switch (method) {
                case FLOODGATE -> viaFloodgate(uuid);
                case GEYSER -> viaGeyser(uuid);
                case UUID -> viaUuid(uuid);
                case PREFIX -> viaPrefix(name, prefixes);
            };

            if (hit) return true;
        }

        return false;
    }

    /**
     * Floodgate-style UUIDs have all of the most significant 64 bits zero ({@code 00000000-0000-0000-xxxx-...}).
     *
     * @param uuid the player's UUID
     * @return whether the UUID looks like a Floodgate UUID
     */
    public static boolean viaUuid(UUID uuid) {
        return uuid != null && uuid.getMostSignificantBits() == 0L;
    }

    /**
     * Java names cannot contain the Bedrock prefixes in online mode, so a name that starts with one is a Bedrock player.
     *
     * @param name     the player's name
     * @param prefixes the configured prefixes (blank entries are ignored)
     * @return whether the name starts with one of them
     */
    public static boolean viaPrefix(String name, List<String> prefixes) {
        if (name == null || name.isEmpty() || prefixes == null) return false;

        for (String prefix : prefixes) {
            if (prefix != null && !prefix.isEmpty() && name.startsWith(prefix)) return true;
        }

        return false;
    }

    /**
     * Asks {@code FloodgateApi.getInstance().isFloodgatePlayer(uuid)} by reflection.
     *
     * @param uuid the player's UUID
     * @return {@code false} when Floodgate is absent, its API changed, or anything goes wrong
     */
    public static boolean viaFloodgate(UUID uuid) {
        if (uuid == null) return false;

        try {
            Class<?> api = Class.forName(FLOODGATE_API);
            Object instance = api.getMethod("getInstance").invoke(null);
            if (instance == null) return false;

            Method isFloodgatePlayer = api.getMethod("isFloodgatePlayer", UUID.class);
            return Boolean.TRUE.equals(isFloodgatePlayer.invoke(instance, uuid));
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Asks {@code GeyserApi.api().isBedrockPlayer(uuid)} by reflection.
     *
     * @param uuid the player's UUID
     * @return {@code false} when Geyser is absent, its API changed, or anything goes wrong
     */
    public static boolean viaGeyser(UUID uuid) {
        if (uuid == null) return false;

        try {
            Class<?> api = Class.forName(GEYSER_API);
            Object instance = api.getMethod("api").invoke(null);
            if (instance == null) return false;

            Method isBedrockPlayer = api.getMethod("isBedrockPlayer", UUID.class);
            return Boolean.TRUE.equals(isBedrockPlayer.invoke(instance, uuid));
        } catch (Throwable t) {
            return false;
        }
    }
}
