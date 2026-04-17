package com.jankominek.disenchantment;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.nms.MockNMS;
import com.jankominek.disenchantment.nms.NMSMapper;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Base class for all Disenchantment tests. Handles MockBukkit lifecycle and the
 * NMSMapper interception required to load the plugin without a real NMS jar.
 * <p>
 * Subclasses should call super.setUp() / super.tearDown() if they override those methods.
 */
public abstract class DisenchantmentTestBase {

    protected ServerMock server;
    protected Disenchantment plugin;
    private MockedStatic<NMSMapper> nmsMapperMock;

    @BeforeEach
    void setUpBase() {
        server = MockBukkit.mock();

        // Intercept NMSMapper.setup() BEFORE plugin loads — return MockNMS instead of null.
        // Without this, onEnable() gets null from setup() and disables the plugin.
        nmsMapperMock = Mockito.mockStatic(NMSMapper.class);
        nmsMapperMock.when(NMSMapper::setup).thenReturn(new MockNMS());
        nmsMapperMock.when(NMSMapper::hasNMS).thenReturn(true);

        plugin = MockBukkit.load(Disenchantment.class);
    }

    @AfterEach
    void tearDownBase() {
        // Close static mock BEFORE unmock to avoid lingering state contaminating next test
        if (nmsMapperMock != null) nmsMapperMock.close();
        MockBukkit.unmock();

        // Reset static fields that MockBukkit.unmock() does not touch
        Disenchantment.nms = null;
        Disenchantment.config = null;
        Disenchantment.localeConfig = null;
        Disenchantment.enabled = true;

        // Clear enchantment state caches — they are static and survive between tests
        clearEnchantmentStateCaches();
    }

    /**
     * Sets a config value and clears any caches that depend on the config so that
     * the next Config.* call reads fresh values.
     */
    protected void setConfig(String key, Object value) {
        Disenchantment.config.set(key, value);
        clearEnchantmentStateCaches();
    }

    /**
     * Shorthand for the disenchantment enchantments-states list config key.
     * Format: each entry is "key:STATE" e.g. ["sharpness:KEEP", "mending:DISABLE"]
     */
    protected void setDisenchantEnchantmentStates(java.util.List<String> states) {
        setConfig("disenchantment.enchantments-states", states);
    }

    /**
     * Shorthand for the shatterment enchantments-states list config key.
     */
    protected void setShatterEnchantmentStates(java.util.List<String> states) {
        setConfig("shatterment.enchantments-states", states);
    }

    /**
     * Looks up an enchantment by its Minecraft registry key (e.g. "sharpness", "mending").
     * Required because Paper 1.21 removed the static Enchantment.SHARPNESS fields.
     */
    protected static Enchantment enchantment(String key) {
        return Objects.requireNonNull(
                Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key)),
                "Unknown enchantment: " + key);
    }

    private void clearEnchantmentStateCaches() {
        try {
            Field disenchantCache = Config.Disenchantment.class.getDeclaredField("ENCHANTMENT_STATES_CACHE");
            disenchantCache.setAccessible(true);
            disenchantCache.set(null, null);

            Field shatterCache = Config.Shatterment.class.getDeclaredField("ENCHANTMENT_STATES_CACHE");
            shatterCache.setAccessible(true);
            shatterCache.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to clear enchantment state caches", e);
        }
    }
}
