package com.jankominek.disenchantment.plugins;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupportedPluginManagerTest extends DisenchantmentTestBase {

    // Every plugin name supported across any NMS version
    private static final String[] ALL_PLUGIN_NAMES = {
            "EcoEnchants", "AdvancedEnchantments", "EnchantsSquared",
            "UberEnchant", "ExcellentEnchants", "Vane", "Zenchantments"
    };

    // -> activation

    @ParameterizedTest
    @ValueSource(strings = {"EcoEnchants", "AdvancedEnchantments", "EnchantsSquared",
            "UberEnchant", "ExcellentEnchants", "Vane", "Zenchantments"})
    void givenSupportedPlugin_whenActivated_thenIsActivated(String pluginName) {
        mockNMS.addSupportedPlugin(new MockPluginAdapter(pluginName));

        SupportedPluginManager.activatePlugins(List.of(pluginName));

        assertTrue(SupportedPluginManager.getAllActivatedPluginsNames().contains(pluginName));
        assertEquals(1, SupportedPluginManager.getAllActivatedPlugins().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EcoEnchants", "AdvancedEnchantments", "EnchantsSquared",
            "UberEnchant", "ExcellentEnchants", "Vane", "Zenchantments"})
    void givenActivatedPlugin_whenDeactivateAll_thenEmpty(String pluginName) {
        mockNMS.addSupportedPlugin(new MockPluginAdapter(pluginName));
        SupportedPluginManager.activatePlugins(List.of(pluginName));

        SupportedPluginManager.deactivateAllPlugins();

        assertTrue(SupportedPluginManager.getAllActivatedPlugins().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EcoEnchants", "AdvancedEnchantments", "EnchantsSquared",
            "UberEnchant", "ExcellentEnchants", "Vane", "Zenchantments"})
    void givenSupportedPlugin_whenActivated_thenActivateLifecycleHookCalled(String pluginName) {
        MockPluginAdapter adapter = new MockPluginAdapter(pluginName);
        mockNMS.addSupportedPlugin(adapter);

        SupportedPluginManager.activatePlugins(List.of(pluginName));

        assertTrue(adapter.wasActivateCalled());
    }

    @Test
    void givenUnknownPlugin_whenActivate_thenNotActivated() {
        SupportedPluginManager.activatePlugins(List.of("SomeRandomPlugin"));

        assertTrue(SupportedPluginManager.getAllActivatedPlugins().isEmpty());
    }

    @Test
    void givenAllSupportedPlugins_whenActivateAll_thenAllActivated() {
        for (String name : ALL_PLUGIN_NAMES)
            mockNMS.addSupportedPlugin(new MockPluginAdapter(name));

        SupportedPluginManager.activatePlugins(Arrays.asList(ALL_PLUGIN_NAMES));

        assertEquals(ALL_PLUGIN_NAMES.length, SupportedPluginManager.getAllActivatedPlugins().size());
        for (String name : ALL_PLUGIN_NAMES)
            assertTrue(SupportedPluginManager.getAllActivatedPluginsNames().contains(name));
    }

    @Test
    void givenMixedList_whenActivate_thenOnlySupportedPluginsActivated() {
        mockNMS.addSupportedPlugin(new MockPluginAdapter("EcoEnchants"));

        SupportedPluginManager.activatePlugins(List.of("EcoEnchants", "Essentials", "WorldGuard"));

        assertEquals(1, SupportedPluginManager.getAllActivatedPlugins().size());
        assertEquals("EcoEnchants", SupportedPluginManager.getAllActivatedPluginsNames().get(0));
    }

    // -> lookup

    @Test
    void givenSupportedPlugin_getSupportedByName_returnsAdapter() {
        MockPluginAdapter adapter = new MockPluginAdapter("EcoEnchants");
        mockNMS.addSupportedPlugin(adapter);

        ISupportedPlugin found = SupportedPluginManager.getSupportedPluginByName("EcoEnchants");

        assertSame(adapter, found);
    }

    @Test
    void givenUnknownName_getSupportedByName_returnsNull() {
        assertNull(SupportedPluginManager.getSupportedPluginByName("UnknownPlugin"));
    }

    @Test
    void givenNoSupportedPlugins_getAllSupportedNames_returnsEmpty() {
        assertTrue(SupportedPluginManager.getAllSupportedPluginsNames().isEmpty());
    }
}
