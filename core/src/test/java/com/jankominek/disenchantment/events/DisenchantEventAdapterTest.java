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
