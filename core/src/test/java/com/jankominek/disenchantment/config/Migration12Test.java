package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.config.migration.steps.Migration12;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Verifies migration step 12 restructures the flat {@code disabled-worlds}/{@code disabled-materials}
 * denylists into the nested {@code worlds}/{@code materials} {@code {mode, list}} blocks without
 * changing behaviour (upgrade-safety — acceptance criterion #2 of the issue-71 spec).
 */
class Migration12Test extends DisenchantmentTestBase {

    private YamlConfiguration templateV12() {
        YamlConfiguration template = new YamlConfiguration();
        template.set("migration", 12);
        template.set("disenchantment.worlds.mode", "DENYLIST");
        template.set("disenchantment.worlds.list", List.of());
        template.set("disenchantment.materials.mode", "DENYLIST");
        template.set("disenchantment.materials.list", List.of());
        template.set("shatterment.worlds.mode", "DENYLIST");
        template.set("shatterment.worlds.list", List.of());
        template.set("shatterment.materials.mode", "DENYLIST");
        template.set("shatterment.materials.list", List.of());
        return template;
    }

    // -> populated old lists move into nested list with DENYLIST mode; stale flat keys dropped

    @Test
    void givenPopulatedFlatLists_whenMigrated_thenMovedToNestedWithDenylist() {
        YamlConfiguration old = new YamlConfiguration();
        old.set("migration", 11);
        old.set("disenchantment.disabled-worlds", List.of("world_nether"));
        old.set("disenchantment.disabled-materials", List.of("DIAMOND_SWORD"));
        old.set("shatterment.disabled-worlds", List.of("no_shatter"));
        old.set("shatterment.disabled-materials", List.of("BOOK"));

        FileConfiguration result = new Migration12().migrate(old, templateV12());

        assertEquals("DENYLIST", result.getString("disenchantment.worlds.mode"));
        assertEquals("DENYLIST", result.getString("disenchantment.materials.mode"));
        assertEquals("DENYLIST", result.getString("shatterment.worlds.mode"));
        assertEquals("DENYLIST", result.getString("shatterment.materials.mode"));

        assertEquals(List.of("world_nether"), result.getStringList("disenchantment.worlds.list"));
        assertEquals(List.of("DIAMOND_SWORD"), result.getStringList("disenchantment.materials.list"));
        assertEquals(List.of("no_shatter"), result.getStringList("shatterment.worlds.list"));
        assertEquals(List.of("BOOK"), result.getStringList("shatterment.materials.list"));

        assertFalse(result.contains("disenchantment.disabled-worlds"), "stale flat key must be removed");
        assertFalse(result.contains("disenchantment.disabled-materials"), "stale flat key must be removed");
        assertFalse(result.contains("shatterment.disabled-worlds"), "stale flat key must be removed");
        assertFalse(result.contains("shatterment.disabled-materials"), "stale flat key must be removed");
    }

    // -> empty/absent old lists still yield empty nested lists with DENYLIST mode (behaviour unchanged)

    @Test
    void givenEmptyFlatLists_whenMigrated_thenEmptyNestedListsWithDenylist() {
        YamlConfiguration old = new YamlConfiguration();
        old.set("migration", 11);
        old.set("disenchantment.disabled-worlds", List.of());
        old.set("disenchantment.disabled-materials", List.of());

        FileConfiguration result = new Migration12().migrate(old, templateV12());

        assertEquals("DENYLIST", result.getString("disenchantment.worlds.mode"));
        assertEquals(List.of(), result.getStringList("disenchantment.worlds.list"));
        assertEquals(List.of(), result.getStringList("disenchantment.materials.list"));
        assertFalse(result.contains("disenchantment.disabled-worlds"));
    }
}
