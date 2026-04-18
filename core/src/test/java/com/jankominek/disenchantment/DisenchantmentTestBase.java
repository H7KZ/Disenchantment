package com.jankominek.disenchantment;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.nms.MockNMS;
import com.jankominek.disenchantment.nms.NMSMapper;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.MockPluginAdapter;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class DisenchantmentTestBase {

    protected ServerMock server;
    protected Disenchantment plugin;
    protected MockNMS mockNMS;
    private MockedStatic<NMSMapper> nmsMapperMock;

    @BeforeEach
    void setUpBase() {
        server = MockBukkit.mock();
        mockNMS = new MockNMS();

        nmsMapperMock = Mockito.mockStatic(NMSMapper.class);
        nmsMapperMock.when(NMSMapper::setup).thenReturn(mockNMS);
        nmsMapperMock.when(NMSMapper::hasNMS).thenReturn(true);

        plugin = MockBukkit.load(Disenchantment.class);
    }

    @AfterEach
    void tearDownBase() {
        SupportedPluginManager.deactivateAllPlugins();
        mockNMS.clearSupportedPlugins();

        if (nmsMapperMock != null) nmsMapperMock.close();
        MockBukkit.unmock();

        Disenchantment.nms = null;
        Disenchantment.config = null;
        Disenchantment.localeConfig = null;
        Disenchantment.enabled = true;

        clearEnchantmentStateCaches();
    }

    // -> config helpers

    protected void setConfig(String key, Object value) {
        Disenchantment.config.set(key, value);
        clearEnchantmentStateCaches();
    }

    protected void setDisenchantEnchantmentStates(java.util.List<String> states) {
        setConfig("disenchantment.enchantments-states", states);
    }

    protected void setShatterEnchantmentStates(java.util.List<String> states) {
        setConfig("shatterment.enchantments-states", states);
    }

    // -> enchantment helpers

    protected static Enchantment enchantment(String key) {
        return Objects.requireNonNull(
                Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key)),
                "Unknown enchantment: " + key);
    }

    protected IPluginEnchantment mockEnchant(String key, int level) {
        return EnchantmentUtils.remapEnchantment(enchantment(key), level);
    }

    // -> world helper

    protected World world() {
        List<World> worlds = server.getWorlds();
        return worlds.isEmpty() ? server.addSimpleWorld("world") : worlds.get(0);
    }

    // -> adapter helpers

    /** Registers adapter in MockNMS and activates it via SupportedPluginManager. */
    protected void activateMockPlugin(MockPluginAdapter adapter) {
        mockNMS.addSupportedPlugin(adapter);
        SupportedPluginManager.activatePlugins(List.of(adapter.getName()));
    }

    // -> anvil event builder

    /**
     * Builds a mocked PrepareAnvilEvent with the given player and slot items.
     * Uses a JDK Proxy for InventoryView/AnvilView to avoid IncompatibleClassChangeError
     * on servers where InventoryView changed from class to interface (Paper 1.21).
     */
    protected PrepareAnvilEvent buildAnvilEvent(PlayerMock player, ItemStack slot0, ItemStack slot1) {
        AnvilInventory mockAnvil = Mockito.mock(AnvilInventory.class);
        Mockito.when(mockAnvil.getItem(0)).thenReturn(slot0);
        Mockito.when(mockAnvil.getItem(1)).thenReturn(slot1);
        Mockito.when(mockAnvil.getRepairCost()).thenReturn(0);
        Mockito.doNothing().when(mockAnvil).setRepairCost(Mockito.anyInt());
        Mockito.when(mockAnvil.getViewers()).thenReturn(List.of(player));

        List<Class<?>> proxyInterfaces = new ArrayList<>();
        proxyInterfaces.add(InventoryView.class);
        try { proxyInterfaces.add(Class.forName("org.bukkit.inventory.view.AnvilView")); }
        catch (ClassNotFoundException ignored) {}
        Object viewProxy = Proxy.newProxyInstance(
                getClass().getClassLoader(),
                proxyInterfaces.toArray(new Class[0]),
                (proxy, method, args) -> {
                    if ("getPlayer".equals(method.getName())) return player;
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType().isPrimitive()) return 0;
                    return null;
                });

        PrepareAnvilEvent event = Mockito.mock(PrepareAnvilEvent.class);
        Mockito.doReturn(viewProxy).when(event).getView();
        Mockito.when(event.getInventory()).thenReturn(mockAnvil);

        Mockito.doAnswer(inv -> {
            ItemStack arg = inv.getArgument(0);
            Mockito.when(event.getResult()).thenReturn(arg);
            return null;
        }).when(event).setResult(Mockito.any());

        return event;
    }

    // -> internal

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
