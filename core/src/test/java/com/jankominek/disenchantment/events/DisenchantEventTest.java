package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DisenchantEvent — fires PrepareAnvilEvent and asserts
 * the handler sets the correct enchanted-book result.
 * <p>
 * Strategy: mock PrepareAnvilEvent + AnvilInventory via Mockito so we don't depend
 * on MockBukkit's (potentially unimplemented) AnvilInventoryMock. PlayerMock is real
 * so permission and world checks work correctly.
 */
class DisenchantEventTest extends DisenchantmentTestBase {

    // -> helpers

    private PrepareAnvilEvent buildEvent(PlayerMock player, ItemStack slot0, ItemStack slot1) {
        AnvilInventory mockAnvil = Mockito.mock(AnvilInventory.class);
        Mockito.when(mockAnvil.getItem(0)).thenReturn(slot0);
        Mockito.when(mockAnvil.getItem(1)).thenReturn(slot1);
        Mockito.when(mockAnvil.getRepairCost()).thenReturn(0);
        Mockito.doNothing().when(mockAnvil).setRepairCost(Mockito.anyInt());
        Mockito.when(mockAnvil.getViewers()).thenReturn(List.of(player));

        // InventoryView changed from class → interface in Paper 1.21; AnvilView is a new
        // sub-interface that PrepareAnvilEvent.getView() returns. Use JDK Proxy so the test
        // code never emits invokevirtual InventoryView.getPlayer (would throw ICCE at runtime).
        List<Class<?>> proxyInterfaces = new ArrayList<>();
        proxyInterfaces.add(InventoryView.class);
        try {
            proxyInterfaces.add(Class.forName("org.bukkit.inventory.view.AnvilView"));
        } catch (ClassNotFoundException ignored) {
        }
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

        // Capture setResult so assertions can inspect the result
        Mockito.doAnswer(inv -> {
            ItemStack arg = inv.getArgument(0);
            Mockito.when(event.getResult()).thenReturn(arg);
            return null;
        }).when(event).setResult(Mockito.any());

        return event;
    }

    private ItemStack sword(String enchantKey, int level) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        item.addUnsafeEnchantment(enchantment(enchantKey), level);
        return item;
    }

    // -> happy path

    @Test
    void givenEnchantedSwordAndBlankBook_whenPrepareAnvil_thenResultIsEnchantedBook() {
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result, "Result must be set for a valid disenchant");
        assertEquals(Material.ENCHANTED_BOOK, result.getType());
    }

    @Test
    void givenEnchantedSword_whenPrepareAnvil_thenResultBookContainsCorrectEnchantment() {
        PlayerMock player = server.addPlayer("TestPlayer");
        Enchantment sharpness = enchantment("sharpness");
        PrepareAnvilEvent event = buildEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        assertTrue(meta.hasStoredEnchant(sharpness));
        assertEquals(5, meta.getStoredEnchantLevel(sharpness));
    }

    @Test
    void givenMultipleEnchantments_whenPrepareAnvil_thenAllTransferredToBook() {
        PlayerMock player = server.addPlayer("TestPlayer");
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(enchantment("sharpness"), 5);
        sword.addUnsafeEnchantment(enchantment("unbreaking"), 3);
        sword.addUnsafeEnchantment(enchantment("looting"), 3);
        PrepareAnvilEvent event = buildEvent(player, sword, new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        assertTrue(meta.hasStoredEnchant(enchantment("sharpness")));
        assertTrue(meta.hasStoredEnchant(enchantment("unbreaking")));
        assertTrue(meta.hasStoredEnchant(enchantment("looting")));
    }

    // -> plugin disabled

    @Test
    void givenPluginDisabled_whenPrepareAnvil_thenResultNotSet() {
        setConfig("enabled", false);
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, sword("sharpness", 1), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        assertNull(event.getResult(), "Result must not be set when plugin is disabled");
    }

    @Test
    void givenDisenchantmentFeatureDisabled_whenPrepareAnvil_thenResultNotSet() {
        setConfig("disenchantment.enabled", false);
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, sword("sharpness", 1), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        assertNull(event.getResult());
    }

    // -> invalid slot contents

    @Test
    void givenEnchantedBookInFirstSlot_whenPrepareAnvil_thenResultNotSet() {
        PlayerMock player = server.addPlayer("TestPlayer");
        ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentUtils.addStoredEnchantment(enchantedBook, enchantment("sharpness"), 5);
        PrepareAnvilEvent event = buildEvent(player, enchantedBook, new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        assertNull(event.getResult(), "Enchanted books can only be shattered, not disenchanted");
    }

    @Test
    void givenNullInFirstSlot_whenPrepareAnvil_thenResultNotSet() {
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, null, new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        assertNull(event.getResult());
    }

    @Test
    void givenStickInSecondSlot_whenPrepareAnvil_thenResultNotSet() {
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, sword("sharpness", 5), new ItemStack(Material.STICK));

        DisenchantEvent.onEvent(event);

        assertNull(event.getResult());
    }

    // -> world restrictions

    @Test
    void givenPlayerInDisabledWorld_whenPrepareAnvil_thenResultNotSet() {
        server.addSimpleWorld("disabled_world");
        PlayerMock player = server.addPlayer("TestPlayer");
        player.teleport(server.getWorld("disabled_world").getSpawnLocation());
        setConfig("disenchantment.disabled-worlds", List.of("disabled_world"));
        PrepareAnvilEvent event = buildEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        assertNull(event.getResult());
    }

    // -> material restrictions

    @Test
    void givenDisabledMaterial_whenPrepareAnvil_thenResultNotSet() {
        setConfig("disenchantment.disabled-materials", List.of("DIAMOND_SWORD"));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        assertNull(event.getResult());
    }

    // -> enchantment state: KEEP

    @Test
    void givenEnchantmentInKEEPState_whenPrepareAnvil_thenKEPTEnchantNotInResult() {
        setDisenchantEnchantmentStates(List.of("sharpness:keep"));
        PlayerMock player = server.addPlayer("TestPlayer");
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(enchantment("sharpness"), 5);
        sword.addUnsafeEnchantment(enchantment("efficiency"), 3);
        PrepareAnvilEvent event = buildEvent(player, sword, new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        assertFalse(meta.hasStoredEnchant(enchantment("sharpness")), "KEEP enchant must not be in result book");
        assertTrue(meta.hasStoredEnchant(enchantment("efficiency")));
    }

    // -> permission check

    @Test
    void givenPlayerWithoutPermission_whenPrepareAnvil_thenResultNotSet() {
        PlayerMock player = server.addPlayer("NoPermPlayer");
        // Explicitly revoke the anvil disenchant permission
        player.addAttachment(plugin, "disenchantment.anvil.disenchant", false);
        player.addAttachment(plugin, "disenchantment.anvil.all", false);
        PrepareAnvilEvent event = buildEvent(player, sword("sharpness", 1), new ItemStack(Material.BOOK));

        DisenchantEvent.onEvent(event);

        assertNull(event.getResult());
    }
}
