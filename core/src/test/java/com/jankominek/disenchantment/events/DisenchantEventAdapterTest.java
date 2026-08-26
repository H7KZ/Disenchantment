package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.MockPluginAdapter;
import org.bukkit.Material;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DisenchantEvent when third-party plugin adapters are active.
 * Parameterized across all supported plugin names to verify each one flows end-to-end.
 */
class DisenchantEventAdapterTest extends DisenchantmentTestBase {

    // -> happy path — one test per supported plugin

    @ParameterizedTest
    @ValueSource(strings = {"EcoEnchants", "AdvancedEnchantments", "EnchantsSquared",
            "UberEnchant", "ExcellentEnchants", "Vane", "Zenchantments"})
    void givenAdapterActive_whenPrepareAnvil_thenResultIsEnchantedBook(String pluginName) {
        activateMockPlugin(new MockPluginAdapter(pluginName, mockEnchant("sharpness", 5)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(
                player, new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result, "Result must be set when adapter provides enchantments");
        assertEquals(Material.ENCHANTED_BOOK, result.getType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EcoEnchants", "AdvancedEnchantments", "EnchantsSquared",
            "UberEnchant", "ExcellentEnchants", "Vane", "Zenchantments"})
    void givenAdapterActive_whenPrepareAnvil_thenResultContainsAdapterEnchantment(String pluginName) {
        activateMockPlugin(new MockPluginAdapter(pluginName, mockEnchant("sharpness", 5)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(
                player, new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        assertTrue(meta.hasStoredEnchant(enchantment("sharpness")));
        assertEquals(5, meta.getStoredEnchantLevel(enchantment("sharpness")));
    }

    // -> config filtering via adapter

    @Test
    void givenAdapterActive_withKEEPEnchant_thenKEPTEnchantNotInResult() {
        setDisenchantEnchantmentStates(List.of("sharpness:keep"));
        activateMockPlugin(new MockPluginAdapter("EcoEnchants",
                mockEnchant("sharpness", 5), mockEnchant("efficiency", 3)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(
                player, new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        assertFalse(meta.hasStoredEnchant(enchantment("sharpness")), "KEEP enchant must not appear in result");
        assertTrue(meta.hasStoredEnchant(enchantment("efficiency")));
    }

    // -> issue #73: a namespace-scoped adapter (e.g. Vane) that reports no vanilla
    //    enchantments must NOT suppress collection of the item's real vanilla enchantments.
    //    A MockPluginAdapter with no enchantments returns an empty list for any item,
    //    reproducing how Vane's adapter ignores minecraft:* enchantments.

    @Test
    void givenNamespaceScopedAdapterActive_withVanillaEnchantedItem_thenResultStillProduced() {
        activateMockPlugin(new MockPluginAdapter("Vane"));
        PlayerMock player = server.addPlayer("TestPlayer");
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(enchantment("sharpness"), 5);
        PrepareAnvilEvent event = buildAnvilEvent(player, sword, new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result, "Vanilla enchantments must still be collected when a namespace-scoped adapter is active (issue #73)");
        assertEquals(Material.ENCHANTED_BOOK, result.getType());
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        assertTrue(meta.hasStoredEnchant(enchantment("sharpness")));
    }

    // -> issue #73: when an adapter is active AND the item also carries a real vanilla
    //    enchantment, BOTH the vanilla and the adapter enchantments must appear in the result.
    //    (Vanilla is now collected unconditionally, in addition to the adapter's own keys.)

    @Test
    void givenAdapterActive_withItemAlsoVanillaEnchanted_thenResultContainsBoth() {
        activateMockPlugin(new MockPluginAdapter("EcoEnchants", mockEnchant("efficiency", 3)));
        PlayerMock player = server.addPlayer("TestPlayer");
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(enchantment("sharpness"), 5);
        PrepareAnvilEvent event = buildAnvilEvent(player, sword, new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        assertTrue(meta.hasStoredEnchant(enchantment("sharpness")), "Vanilla enchantment must be collected alongside the adapter's");
        assertTrue(meta.hasStoredEnchant(enchantment("efficiency")), "Adapter enchantment must also be present");
    }

    @Test
    void givenAdapterActive_withDISABLEEnchant_thenResultNotSet() {
        setDisenchantEnchantmentStates(List.of("mending:disable"));
        activateMockPlugin(new MockPluginAdapter("EcoEnchants",
                mockEnchant("sharpness", 5), mockEnchant("mending", 1)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(
                player, new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        assertNull(event.getResult());
    }

    // -> global gates still apply

    @Test
    void givenAdapterActive_pluginDisabled_thenResultNotSet() {
        setConfig("enabled", false);
        activateMockPlugin(new MockPluginAdapter("EcoEnchants", mockEnchant("sharpness", 5)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(
                player, new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        assertNull(event.getResult());
    }

    @Test
    void givenAdapterActive_disenchantFeatureDisabled_thenResultNotSet() {
        setConfig("disenchantment.enabled", false);
        activateMockPlugin(new MockPluginAdapter("EcoEnchants", mockEnchant("sharpness", 5)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(
                player, new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        assertNull(event.getResult());
    }

    @Test
    void givenAdapterActive_playerWithoutPermission_thenResultNotSet() {
        activateMockPlugin(new MockPluginAdapter("EcoEnchants", mockEnchant("sharpness", 5)));
        PlayerMock player = server.addPlayer("NoPermPlayer");
        player.addAttachment(plugin, "disenchantment.anvil.disenchant", false);
        player.addAttachment(plugin, "disenchantment.anvil.all", false);
        PrepareAnvilEvent event = buildAnvilEvent(
                player, new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        assertNull(event.getResult());
    }

    // -> multiple adapters

    @Test
    void givenMultipleAdaptersActive_whenPrepareAnvil_thenAllEnchantmentsMergedInResult() {
        activateMockPlugin(new MockPluginAdapter("EcoEnchants", mockEnchant("sharpness", 5)));
        activateMockPlugin(new MockPluginAdapter("AdvancedEnchantments", mockEnchant("unbreaking", 3)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(
                player, new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        assertTrue(meta.hasStoredEnchant(enchantment("sharpness")));
        assertTrue(meta.hasStoredEnchant(enchantment("unbreaking")));
    }
}
