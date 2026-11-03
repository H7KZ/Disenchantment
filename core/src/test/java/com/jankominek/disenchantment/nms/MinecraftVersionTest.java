package com.jankominek.disenchantment.nms;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Version detection must be numeric and exact — a substring match would map 1.21.12 to
 * 1.21.1 (wrong NMS module) and 26.1.10 to 26.1.1.
 */
class MinecraftVersionTest {

    @ParameterizedTest(name = "[{index}] \"{0}\" / \"{1}\" -> {2}")
    @CsvSource(delimiter = '|', nullValues = "NULL", value = {
            // Exact matches from the Paper/Spigot "(MC: x.y.z)" token
            "git-Paper-406 (MC: 1.21.4)            | 1.21.4-R0.1-SNAPSHOT  | MINECRAFT_1_21_4",
            "git-Paper-130 (MC: 1.21.11)           | 1.21.11-R0.1-SNAPSHOT | MINECRAFT_1_21_11",
            "git-Paper-1 (MC: 1.21.1)              | 1.21.1-R0.1-SNAPSHOT  | MINECRAFT_1_21_1",
            "git-Paper-1 (MC: 1.21.10)             | NULL                  | MINECRAFT_1_21_10",
            "git-Paper-1 (MC: 1.21)                | NULL                  | MINECRAFT_1_21",
            "git-Leaf-12 (MC: 1.20.6)              | NULL                  | MINECRAFT_1_20_6",
            "git-Spigot-abc-def (MC: 1.18)         | NULL                  | MINECRAFT_1_18",
            "26.1.2-48-abcdef (MC: 26.1.2)         | 26.1.2.build.48-alpha | MINECRAFT_26_1_2",
            "26.1-12-abcdef (MC: 26.1)             | NULL                  | MINECRAFT_26_1",
            // Paper 26.1.2 builds dropped the (MC: x) token from getVersion() (PaperMC/Paper#13862)
            "26.1.2-60-main@b4682bf (2026-05-04T06:25:04Z) | 26.1.2.build.60-stable | MINECRAFT_26_1_2",
            "26.4-1-main@abcdef (2026-12-01T00:00:00Z)     | 26.4.build.1-alpha     | LATEST",
            "git-Paper-19 (MC: 1.20.1)             | 1.20.1-R0.1-SNAPSHOT  | MINECRAFT_1_20_1",
            // Unknown patch releases must not substring-match a shorter known version
            "git-Paper-1 (MC: 1.21.12)             | NULL                  | MINECRAFT_1_21_11",
            "26.1.10-1-abcdef (MC: 26.1.10)        | NULL                  | LATEST",
            // Newer drops fall back to the newest module
            "26.2-1-abcdef (MC: 26.2)              | NULL                  | LATEST",
            "26.3.1-1-abcdef (MC: 26.3.1)          | NULL                  | LATEST",
            "27.1-1-abcdef (MC: 27.1)              | NULL                  | LATEST",
            // Missing MC token → Bukkit version fallback
            "This server is running CraftBukkit    | 1.21.4-R0.1-SNAPSHOT  | MINECRAFT_1_21_4",
            "custom-fork                           | 26.2-R0.1-SNAPSHOT    | LATEST",
            "1_21_8                                | NULL                  | MINECRAFT_1_21_8",
            // Unsupported / garbage
            "git-Paper-1 (MC: 1.17.1)              | NULL                  | INCOMPATIBLE",
            "git-Paper-1 (MC: 1.12.2)              | 1.12.2-R0.1-SNAPSHOT  | INCOMPATIBLE",
            "unknown                               | unknown               | INCOMPATIBLE",
            "NULL                                  | NULL                  | INCOMPATIBLE",
    })
    void resolve(String serverVersion, String bukkitVersion, MinecraftVersion expected) {
        assertEquals(expected, MinecraftVersion.resolve(serverVersion, bukkitVersion));
    }
}
