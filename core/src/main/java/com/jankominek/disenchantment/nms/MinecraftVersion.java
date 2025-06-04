package com.jankominek.disenchantment.nms;

import com.jankominek.disenchantment.Disenchantment;

public enum MinecraftVersion {
    MINECRAFT_1_21_6((byte) 22, "1_21_6", "1.21.6", "v1_21_R4"),
    MINECRAFT_1_21_5((byte) 21, "1_21_5", "1.21.5", "v1_21_R4"),
    MINECRAFT_1_21_4((byte) 20, "1_21_4", "1.21.4", "v1_21_R1"),
    MINECRAFT_1_21_3((byte) 19, "1_21_3", "1.21.3", "v1_21_R1"),
    MINECRAFT_1_21_2((byte) 18, "1_21_2", "1.21.2", "v1_21_R1"),
    MINECRAFT_1_21_1((byte) 17, "1_21_1", "1.21.1", "v1_21_R1"),
    MINECRAFT_1_21((byte) 16, "1_21", "1.21", "v1_21_R1"),
    MINECRAFT_1_20_6((byte) 15, "1_20_6", "1.20.6", "v1_20_R4"),
    MINECRAFT_1_20_5((byte) 14, "1_20_5", "1.20.5", "v1_20_R4"),
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

    private static final MinecraftVersion serverVersion = init();

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

    private static MinecraftVersion init() {
        String v = Disenchantment.plugin.getServer().getVersion();

        for (MinecraftVersion version : MinecraftVersion.values()) {
            if (version.versionUnderlined == null || version.versionDotted == null) continue;
            if (v.contains(version.versionUnderlined) || v.contains(version.versionDotted)) return version;
        }
        return INCOMPATIBLE;
    }

    public static boolean currentVersionOlderThan(MinecraftVersion version) {
        if (serverVersion == MinecraftVersion.INCOMPATIBLE) return false;
        return serverVersion.value <= version.value;
    }

    public static boolean currentVersionNewerThan(MinecraftVersion version) {
        if (serverVersion == MinecraftVersion.INCOMPATIBLE) return false;
        return serverVersion.value >= version.value;
    }

    public static MinecraftVersion getServerVersion() {
        return serverVersion;
    }

    public String getVersionString() {
        return versionUnderlined;
    }

    public String getNmsVersion() {
        return nmsVersion;
    }
}
