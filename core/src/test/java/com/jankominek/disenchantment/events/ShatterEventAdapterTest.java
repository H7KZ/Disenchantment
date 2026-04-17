package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.MockPluginAdapter;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
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
 * Integration tests for ShatterEvent when third-party plugin adapters are active.
 * Parameterized across all supported plugin names to verify each one flows end-to-end.
 */
class ShatterEventAdapterTest extends DisenchantmentTestBase {

    private ItemStack enchantedBook() { return new ItemStack(Material.ENCHANTED_BOOK); }

    // -> happy path — one test per supported plugin

    @ParameterizedTest
    @ValueSource(strings = {"EcoEnchants", "AdvancedEnchantments", "EnchantsSquared",
            "UberEnchant", "ExcellentEnchants", "Vane", "Zenchantments"})
    void givenAdapterActive_whenPrepareAnvil_thenResultIsEnchantedBook(String pluginName) {
        activateMockPlugin(new MockPluginAdapter(pluginName,
                mockEnchant("sharpness", 1), mockEnchant("mending", 1)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(player, enchantedBook(), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result, "Result must be set when adapter provides ≥2 enchantments");
        assertEquals(Material.ENCHANTED_BOOK, result.getType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EcoEnchants", "AdvancedEnchantments", "EnchantsSquared",
            "UberEnchant", "ExcellentEnchants", "Vane", "Zenchantments"})
    void givenAdapterActiveWithTwoEnchantments_whenPrepareAnvil_thenResultContainsOne(String pluginName) {
        activateMockPlugin(new MockPluginAdapter(pluginName,
                mockEnchant("sharpness", 1), mockEnchant("mending", 1)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(player, enchantedBook(), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        // 2 enchants → floor(2/2) = 1 split off
        assertEquals(1, meta.getStoredEnchants().size());
    }

    // -> config filtering via adapter

    @Test
    void givenAdapterActive_withKEEPEnchant_thenKEPTEnchantNotInResult() {
        setShatterEnchantmentStates(List.of("sharpness:keep"));
        // 3 enchants; sharpness kept → 2 eligible → 1 split off
        activateMockPlugin(new MockPluginAdapter("EcoEnchants",
                mockEnchant("sharpness", 1), mockEnchant("mending", 1), mockEnchant("efficiency", 1)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(player, enchantedBook(), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        assertFalse(meta.hasStoredEnchant(enchantment("sharpness")), "KEEP enchant must not appear in split-off book");
    }

    // -> global gates still apply

    @Test
    void givenAdapterActive_pluginDisabled_thenResultNotSet() {
        setConfig("enabled", false);
        activateMockPlugin(new MockPluginAdapter("EcoEnchants",
                mockEnchant("sharpness", 1), mockEnchant("mending", 1)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(player, enchantedBook(), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult());
    }

    @Test
    void givenAdapterActive_shatterFeatureDisabled_thenResultNotSet() {
        setConfig("shatterment.enabled", false);
        activateMockPlugin(new MockPluginAdapter("EcoEnchants",
                mockEnchant("sharpness", 1), mockEnchant("mending", 1)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(player, enchantedBook(), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult());
    }

    @Test
    void givenAdapterActive_playerWithoutPermission_thenResultNotSet() {
        activateMockPlugin(new MockPluginAdapter("EcoEnchants",
                mockEnchant("sharpness", 1), mockEnchant("mending", 1)));
        PlayerMock player = server.addPlayer("NoPermPlayer");
        player.addAttachment(plugin, "disenchantment.anvil.shatter", false);
        player.addAttachment(plugin, "disenchantment.anvil.all", false);
        PrepareAnvilEvent event = buildAnvilEvent(player, enchantedBook(), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult());
    }

    @Test
    void givenAdapterActive_withOnlyOneEnchantment_thenResultNotSet() {
        activateMockPlugin(new MockPluginAdapter("EcoEnchants", mockEnchant("sharpness", 1)));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildAnvilEvent(player, enchantedBook(), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult(), "Cannot shatter book with only one enchantment");
    }
}
