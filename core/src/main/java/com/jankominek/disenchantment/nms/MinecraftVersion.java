package com.jankominek.disenchantment.nms;

import com.jankominek.disenchantment.Disenchantment;

/**
 * Enum mapping known Minecraft release versions to their corresponding NMS module identifiers.
 *
 * <p>On first access, the server version is detected automatically from the Bukkit server
 * version string. Unknown versions 1.21+ fall back to {@link #LATEST}; versions below 1.18
 * are marked {@link #INCOMPATIBLE}. Provides utility methods for version comparison.</p>
 */
public enum MinecraftVersion {
    LATEST((byte) 127, null, null, "v1_21_R5"),


    // 26_1 (mapped to v1_21_R5 via LATEST fallback)
    MINECRAFT_26_1_3((byte) 29, "26_1_3", "26.1.3", "v1_21_R5"),
    MINECRAFT_26_1_2((byte) 28, "26_1_2", "26.1.2", "v1_21_R5"),
    MINECRAFT_26_1_1((byte) 27, "26_1_1", "26.1.1", "v1_21_R5"),
    MINECRAFT_26_1((byte) 26, "26_1", "26.1", "v1_21_R5"),

    // 1_21_R5
    MINECRAFT_1_21_11((byte) 25, "1_21_11", "1.21.11", "v1_21_R5"),
    MINECRAFT_1_21_10((byte) 25, "1_21_10", "1.21.10", "v1_21_R5"),
    MINECRAFT_1_21_9((byte) 25, "1_21_9", "1.21.9", "v1_21_R5"),
    MINECRAFT_1_21_8((byte) 24, "1_21_8", "1.21.8", "v1_21_R5"),

    // 1_21_R4
    MINECRAFT_1_21_7((byte) 23, "1_21_7", "1.21.7", "v1_21_R4"),
    MINECRAFT_1_21_6((byte) 22, "1_21_6", "1.21.6", "v1_21_R4"),
    MINECRAFT_1_21_5((byte) 21, "1_21_5", "1.21.5", "v1_21_R4"),

    // 1_21_R1
    MINECRAFT_1_21_4((byte) 20, "1_21_4", "1.21.4", "v1_21_R1"),
    MINECRAFT_1_21_3((byte) 19, "1_21_3", "1.21.3", "v1_21_R1"),
    MINECRAFT_1_21_2((byte) 18, "1_21_2", "1.21.2", "v1_21_R1"),
    MINECRAFT_1_21_1((byte) 17, "1_21_1", "1.21.1", "v1_21_R1"),
    MINECRAFT_1_21((byte) 16, "1_21", "1.21", "v1_21_R1"),

    // 1_20_R4
    MINECRAFT_1_20_6((byte) 15, "1_20_6", "1.20.6", "v1_20_R4"),
    MINECRAFT_1_20_5((byte) 14, "1_20_5", "1.20.5", "v1_20_R4"),

    // 1_18_R1
    MINECRAFT_1_20_4((byte) 13, "1_20_4", "1.20.4", "v1_18_R1"),
    MINECRAFT_1_20_3((byte) 12, "1_20_3", "1.20.3", "v1_18_R1"),
    MINECRAFT_1_20_2((byte) 11, "1_20_2", "1.20.2", "v1_18_R1"),
    MINECRAFT_1_20_1((byte) 10, "1_20_1", "1.20.1", "v1_18_R1"),
    MINECRAFT_1_20((byte) 9, "1_20", "1.20", "v1_18_R1"),
    MINECRAFT_1_19_4((byte) 8, "1_19_4", "1.19.4", "v1_18_R1"),
    MINECRAFT_1_19_3((byte) 7, "1_19_3", "1.19.3", "v1_18_R1"),
    MINECRAFT_1_19_2((byte) 6, "1_19_2", "1.19.2", "v1_18_R1"),
    MINECRAFT_1_19_1((byte) 5, "1_19_1", "1.19.1", "v1_18_R1"),
    MINECRAFT_1_19((byte) 4, "1_19", "1.19", "v1_18_R1"),
    MINECRAFT_1_18_2((byte) 3, "1_18_2", "1.18.2", "v1_18_R1"),
    MINECRAFT_1_18_1((byte) 2, "1_18_1", "1.18.1", "v1_18_R1"),
    MINECRAFT_1_18((byte) 1, "1_18", "1.18", "v1_18_R1"),

    INCOMPATIBLE((byte) -1, null, null, null),
    ;

    private final Byte value;
    private final String versionUnderlined;
    private final String versionDotted;
    private final String nmsVersion;

    MinecraftVersion(Byte value, String versionUnderlined, String versionDotted, String nmsVersion) {
        this.value = value;
        this.versionUnderlined = versionUnderlined;
        this.versionDotted = versionDotted;
        this.nmsVersion = nmsVersion;
    }

    private static final java.util.regex.Pattern MC_TOKEN = java.util.regex.Pattern.compile("\\(MC: (\\d+)\\.(\\d+)(?:\\.(\\d+))?");
    private static final java.util.regex.Pattern LEADING_VERSION = java.util.regex.Pattern.compile("^(\\d+)[._](\\d+)(?:[._](\\d+))?");

    /**
     * Detects the version of the given server via {@link #resolve(String, String)}.
     *
     * @param server the running server
     * @return the matching version, never {@code null}
     */
    static MinecraftVersion detect(org.bukkit.Server server) {
        return resolve(server.getVersion(), server.getBukkitVersion());
    }

    private static MinecraftVersion init() {
        try {
            return detect(Disenchantment.plugin.getServer());
        } catch (Exception e) {
            return INCOMPATIBLE;
        }
    }

    /**
     * Resolves the {@link MinecraftVersion} for a server from its version strings.
     *
     * <p>The numeric version is read from the {@code (MC: x.y[.z])} token of
     * {@code Server#getVersion()} (e.g. {@code "git-Paper-406 (MC: 1.21.4)"},
     * {@code "26.1.2-48-abcdef (MC: 26.1.2)"}), falling back to the leading
     * {@code x.y[.z]} of {@code Server#getBukkitVersion()} (e.g. {@code "1.21.4-R0.1-SNAPSHOT"}).
     * Versions are compared numerically — never by substring, so {@code 1.21.12} cannot be
     * mistaken for {@code 1.21.1}. A version without an exact entry maps to the newest known
     * version below it; anything newer than every known 1.21+ version maps to {@link #LATEST},
     * and anything below 1.18 (or unparseable) is {@link #INCOMPATIBLE}.</p>
     *
     * @param serverVersion value of {@code Server#getVersion()} (may be {@code null})
     * @param bukkitVersion value of {@code Server#getBukkitVersion()} (may be {@code null})
     * @return the matching version, never {@code null}
     */
    static MinecraftVersion resolve(String serverVersion, String bukkitVersion) {
        int[] parsed = parse(serverVersion, MC_TOKEN);
        if (parsed == null) parsed = parse(bukkitVersion, LEADING_VERSION);
        if (parsed == null) parsed = parse(serverVersion, LEADING_VERSION);
        if (parsed == null) return INCOMPATIBLE;

        MinecraftVersion best = null;
        int[] bestParsed = null;
        int[] newestKnown = null;
        for (MinecraftVersion version : values()) {
            if (version.versionDotted == null) continue;
            int[] known = parse(version.versionDotted, LEADING_VERSION);
            if (newestKnown == null || compare(known, newestKnown) > 0) newestKnown = known;
            int cmp = compare(known, parsed);
            if (cmp == 0) return version;
            if (cmp < 0 && (bestParsed == null || compare(known, bestParsed) > 0)) {
                best = version;
                bestParsed = known;
            }
        }

        // Newer than every known version → newest NMS module.
        if (newestKnown != null && compare(parsed, newestKnown) > 0) return LATEST;

        if (best == null) return INCOMPATIBLE;

        // A patch release missing from the table (e.g. 1.21.12) shares the newest known version
        // of its line; an unknown 1.21+ line between known ones also uses the newest module.
        if (bestParsed[0] != parsed[0] || bestParsed[1] != parsed[1])
            return (parsed[0] > 1 || parsed[1] >= 21) ? LATEST : best;

        return best;
    }

    private static int[] parse(String text, java.util.regex.Pattern pattern) {
        if (text == null) return null;
        java.util.regex.Matcher m = pattern.matcher(text.trim());
        if (!m.find()) return null;
        try {
            return new int[]{
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    m.group(3) != null ? Integer.parseInt(m.group(3)) : 0
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int compare(int[] a, int[] b) {
        for (int i = 0; i < 3; i++) {
            if (a[i] != b[i]) return Integer.compare(a[i], b[i]);
        }
        return 0;
    }

    /**
     * Checks whether the current server version is older than or equal to the given version.
     *
     * @param version the version to compare against
     * @return {@code true} if the server version is older than or equal to {@code version},
     * or {@code false} if the server version is incompatible
     */
    public static boolean currentVersionOlderThan(MinecraftVersion version) {
        if (Holder.SERVER_VERSION == MinecraftVersion.INCOMPATIBLE) return false;
        return Holder.SERVER_VERSION.value <= version.value;
    }

    /**
     * Checks whether the current server version is newer than or equal to the given version.
     *
     * @param version the version to compare against
     * @return {@code true} if the server version is newer than or equal to {@code version},
     * or {@code false} if the server version is incompatible
     */
    public static boolean currentVersionNewerThan(MinecraftVersion version) {
        if (Holder.SERVER_VERSION == MinecraftVersion.INCOMPATIBLE) return false;
        return Holder.SERVER_VERSION.value >= version.value;
    }

    /**
     * Returns the detected Minecraft version of the running server.
     *
     * @return the server's {@link MinecraftVersion}, or {@link #INCOMPATIBLE} if unrecognised
     */
    public static MinecraftVersion getServerVersion() {
        return Holder.SERVER_VERSION;
    }

    /**
     * Returns the underscored version string (e.g. {@code "1_21_4"}).
     *
     * @return the version string, or {@code null} for {@link #LATEST} and {@link #INCOMPATIBLE}
     */
    public String getVersionString() {
        return versionUnderlined;
    }

    /**
     * Returns the NMS module identifier used to resolve the version-specific implementation class
     * (e.g. {@code "v1_21_R5"}).
     *
     * @return the NMS module identifier, or {@code null} if the version is incompatible
     */
    public String getNmsVersion() {
        return nmsVersion;
    }

    /**
     * Lazily detects the server version on first use. A holder class (instead of a static field
     * on the enum) guarantees detection runs only after the enum — including its regex patterns —
     * is fully initialised. A static field declared before the patterns ran {@code init()} while
     * they were still {@code null}, so every detection failed with a swallowed NPE.
     */
    private static final class Holder {
        private static final MinecraftVersion SERVER_VERSION = init();
    }
}
