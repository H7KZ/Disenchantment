package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.utils.CooldownManager;
import org.bukkit.Material;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the erase Prepare handler: the result is the source item without its erasable enchantments.
 */
class EraseEventTest extends DisenchantmentTestBase {

    private ItemStack sword(Object... keyLevelPairs) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        for (int i = 0; i < keyLevelPairs.length; i += 2) {
            item.addUnsafeEnchantment(enchantment((String) keyLevelPairs[i]), (Integer) keyLevelPairs[i + 1]);
        }
        return item;
    }

    private ItemStack shard() {
        return new ItemStack(Material.ECHO_SHARD);
    }

    private PrepareAnvilEvent prepare(PlayerMock player, ItemStack first, ItemStack second) {
        PrepareAnvilEvent event = buildAnvilEvent(player, first, second);
        EraseEvent.onEvent(event);
        return event;
    }

    // -> disabled by default

    @Test
    void givenDefaultConfig_whenPrepare_thenEraseIsOffAndNoResultSet() {
        PlayerMock player = server.addPlayer("TestPlayer");

        PrepareAnvilEvent event = prepare(player, sword("sharpness", 5), shard());

        assertNull(event.getResult());
    }

    // -> happy path

    @Test
    void givenEnabled_whenPrepareSwordAndShard_thenResultIsSwordWithoutEnchantments() {
        setConfig("erase.enabled", true);
        PlayerMock player = server.addPlayer("TestPlayer");

        PrepareAnvilEvent event = prepare(player, sword("sharpness", 5, "unbreaking", 3), shard());

        ItemStack result = event.getResult();
        assertNotNull(result);
        assertEquals(Material.DIAMOND_SWORD, result.getType());
        assertTrue(result.getEnchantments().isEmpty());
    }

    @Test
    void givenEnabled_whenPrepare_thenSourceItemUntouched() {
        setConfig("erase.enabled", true);
        PlayerMock player = server.addPlayer("TestPlayer");
        ItemStack source = sword("sharpness", 5);

        prepare(player, source, shard());

        assertTrue(source.containsEnchantment(enchantment("sharpness")));
    }

    @Test
    void givenRenamedItem_whenPrepare_thenNameKept() {
        setConfig("erase.enabled", true);
        PlayerMock player = server.addPlayer("TestPlayer");
        ItemStack source = sword("sharpness", 5);
        ItemMeta meta = source.getItemMeta();
        meta.setDisplayName("Excalibur");
        source.setItemMeta(meta);

        PrepareAnvilEvent event = prepare(player, source, shard());

        assertEquals("Excalibur", event.getResult().getItemMeta().getDisplayName());
    }

    @Test
    void givenCurseAndEnchantment_whenPrepare_thenCurseKeptOnResult() {
        setConfig("erase.enabled", true);
        PlayerMock player = server.addPlayer("TestPlayer");

        PrepareAnvilEvent event = prepare(player, sword("sharpness", 5, "vanishing_curse", 1), shard());

        assertFalse(event.getResult().containsEnchantment(enchantment("sharpness")));
        assertTrue(event.getResult().containsEnchantment(enchantment("vanishing_curse")));
    }

    // -> cost

    @Test
    void givenEnabled_whenPrepare_thenEraseCostFormulaIsApplied() {
        setConfig("erase.enabled", true);
        setConfig("erase.anvil.repair.base", 20);
        setConfig("erase.anvil.repair.multiply", 1.0);
        PlayerMock player = server.addPlayer("TestPlayer");

        PrepareAnvilEvent event = prepare(player, sword("sharpness", 5), shard());

        // 20 + 5 * 1.0, from the erase block — not the disenchantment one (10 + 5 * 0.25).
        Mockito.verify(event.getInventory(), Mockito.atLeastOnce()).setRepairCost(25);
    }

    @Test
    void givenCostDisabled_whenPrepare_thenFree() {
        setConfig("erase.enabled", true);
        setConfig("erase.anvil.repair.cost", false);
        PlayerMock player = server.addPlayer("TestPlayer");

        PrepareAnvilEvent event = prepare(player, sword("sharpness", 5), shard());

        Mockito.verify(event.getInventory(), Mockito.atLeastOnce()).setRepairCost(0);
    }

    // -> rejections

    @Test
    void givenWrongCatalyst_whenPrepare_thenNoResult() {
        setConfig("erase.enabled", true);
        PlayerMock player = server.addPlayer("TestPlayer");

        assertNull(prepare(player, sword("sharpness", 5), new ItemStack(Material.DIAMOND)).getResult());
    }

    @Test
    void givenBlankBook_whenPrepare_thenEraseStaysOutOfTheWay() {
        setConfig("erase.enabled", true);
        PlayerMock player = server.addPlayer("TestPlayer");

        assertNull(prepare(player, sword("sharpness", 5), new ItemStack(Material.BOOK)).getResult());
    }

    @Test
    void givenDisenchantWithBook_whenPrepareBothHandlers_thenEraseDoesNotReplaceTheBook() {
        setConfig("erase.enabled", true);
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);
        EraseEvent.onEvent(event);

        assertEquals(Material.ENCHANTED_BOOK, event.getResult().getType());
    }

    @Test
    void givenDisableState_whenPrepare_thenNoResult() {
        setConfig("erase.enabled", true);
        setDisenchantEnchantmentStates(List.of("unbreaking:disable"));
        PlayerMock player = server.addPlayer("TestPlayer");

        assertNull(prepare(player, sword("sharpness", 5, "unbreaking", 3), shard()).getResult());
    }

    @Test
    void givenOnlyKeptEnchantments_whenPrepare_thenNoResult() {
        setConfig("erase.enabled", true);
        setDisenchantEnchantmentStates(List.of("sharpness:keep"));
        PlayerMock player = server.addPlayer("TestPlayer");

        assertNull(prepare(player, sword("sharpness", 5), shard()).getResult());
    }

    @Test
    void givenBlockedWorld_whenPrepare_thenNoResult() {
        setConfig("erase.enabled", true);
        PlayerMock player = server.addPlayer("TestPlayer");
        setConfig("disenchantment.worlds.mode", "DENYLIST");
        setConfig("disenchantment.worlds.list", List.of(player.getWorld().getName()));

        assertNull(prepare(player, sword("sharpness", 5), shard()).getResult());
    }

    @Test
    void givenPluginDisabled_whenPrepare_thenNoResult() {
        setConfig("erase.enabled", true);
        setConfig("enabled", false);
        PlayerMock player = server.addPlayer("TestPlayer");

        assertNull(prepare(player, sword("sharpness", 5), shard()).getResult());
    }

    @Test
    void givenNoPermission_whenPrepare_thenNoResult() {
        setConfig("erase.enabled", true);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.addAttachment(plugin, "disenchantment.anvil.erase", false);
        player.addAttachment(plugin, "disenchantment.anvil.all", false);

        assertNull(prepare(player, sword("sharpness", 5), shard()).getResult());
    }

    @Test
    void givenAnvilAllPermissionOnly_whenPrepare_thenResultSet() {
        setConfig("erase.enabled", true);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.addAttachment(plugin, "disenchantment.anvil.erase", false);
        player.addAttachment(plugin, "disenchantment.anvil.all", true);

        assertNotNull(prepare(player, sword("sharpness", 5), shard()).getResult());
    }

    @Test
    void givenMaintenance_whenPrepare_thenResultCleared() {
        setConfig("erase.enabled", true);
        Disenchantment.maintenanceEnabled = true;
        try {
            PlayerMock player = server.addPlayer("TestPlayer");

            assertNull(prepare(player, sword("sharpness", 5), shard()).getResult());
        } finally {
            Disenchantment.maintenanceEnabled = false;
        }
    }

    @Test
    void givenCooldown_whenPrepare_thenResultCleared() {
        setConfig("erase.enabled", true);
        setConfig("disenchantment.cooldown.seconds", 30);
        PlayerMock player = server.addPlayer("TestPlayer");
        CooldownManager.recordOperation(player);
        try {
            assertNull(prepare(player, sword("sharpness", 5), shard()).getResult());
        } finally {
            CooldownManager.reset();
        }
    }
}
