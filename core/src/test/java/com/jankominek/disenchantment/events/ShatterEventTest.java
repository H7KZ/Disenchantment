package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.Material;
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
 * Integration tests for ShatterEvent — fires PrepareAnvilEvent and asserts
 * the handler sets the correct split-book result.
 */
class ShatterEventTest extends DisenchantmentTestBase {

    // ── helpers ──────────────────────────────────────────────────────────────

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

        Mockito.doAnswer(inv -> {
            ItemStack arg = inv.getArgument(0);
            Mockito.when(event.getResult()).thenReturn(arg);
            return null;
        }).when(event).setResult(Mockito.any());

        return event;
    }

    private ItemStack enchantedBook(String... enchantKeys) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        for (String key : enchantKeys) EnchantmentUtils.addStoredEnchantment(item, enchantment(key), 1);
        return item;
    }

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void givenBookWithTwoEnchantments_whenPrepareAnvil_thenResultIsEnchantedBook() {
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result, "Result must be set for a valid shatter");
        assertEquals(Material.ENCHANTED_BOOK, result.getType());
    }

    @Test
    void givenBookWithTwoEnchantments_whenPrepareAnvil_thenResultContainsOneEnchantment() {
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        // 2 enchants → floor(2/2) = 1 split off
        assertEquals(1, meta.getStoredEnchants().size());
    }

    @Test
    void givenBookWithThreeEnchantments_whenPrepareAnvil_thenResultContainsOneEnchantment() {
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(
                player, enchantedBook("sharpness", "mending", "efficiency"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        // 3 enchants → floor(3/2) = 1 split off
        assertEquals(1, meta.getStoredEnchants().size());
    }

    @Test
    void givenBookWithFourEnchantments_whenPrepareAnvil_thenResultContainsTwoEnchantments() {
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(
                player,
                enchantedBook("sharpness", "mending", "efficiency", "unbreaking"),
                new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        // 4 enchants → floor(4/2) = 2 split off
        assertEquals(2, meta.getStoredEnchants().size());
    }

    // ── rejection cases ───────────────────────────────────────────────────────

    @Test
    void givenBookWithOneEnchantment_whenPrepareAnvil_thenResultNotSet() {
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult(), "Single enchantment book cannot be shattered");
    }

    @Test
    void givenSwordInFirstSlot_whenPrepareAnvil_thenResultNotSet() {
        PlayerMock player = server.addPlayer("TestPlayer");
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(enchantment("sharpness"), 5);
        PrepareAnvilEvent event = buildEvent(player, sword, new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult(), "Swords cannot be shattered, only enchanted books");
    }

    @Test
    void givenPluginDisabled_whenPrepareAnvil_thenResultNotSet() {
        setConfig("enabled", false);
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult());
    }

    @Test
    void givenShattermentFeatureDisabled_whenPrepareAnvil_thenResultNotSet() {
        setConfig("shatterment.enabled", false);
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult());
    }

    @Test
    void givenNullFirstSlot_whenPrepareAnvil_thenResultNotSet() {
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, null, new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult());
    }

    @Test
    void givenSecondSlotNotBlankBook_whenPrepareAnvil_thenResultNotSet() {
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(
                player, enchantedBook("sharpness", "mending"), new ItemStack(Material.STICK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult());
    }

    // ── world restrictions ────────────────────────────────────────────────────

    @Test
    void givenPlayerInDisabledWorld_whenPrepareAnvil_thenResultNotSet() {
        server.addSimpleWorld("no_shatter");
        PlayerMock player = server.addPlayer("TestPlayer");
        player.teleport(server.getWorld("no_shatter").getSpawnLocation());
        setConfig("shatterment.disabled-worlds", List.of("no_shatter"));
        PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult());
    }

    // ── enchantment state: KEEP ───────────────────────────────────────────────

    @Test
    void givenEnchantmentInKEEPState_whenPrepareAnvil_thenKEPTEnchantNotInResult() {
        setShatterEnchantmentStates(List.of("sharpness:keep"));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(
                player, enchantedBook("sharpness", "mending", "efficiency"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        assertFalse(meta.hasStoredEnchant(enchantment("sharpness")), "KEEP enchant must not appear in split-off book");
    }

    // ── permission check ──────────────────────────────────────────────────────

    @Test
    void givenPlayerWithoutPermission_whenPrepareAnvil_thenResultNotSet() {
        PlayerMock player = server.addPlayer("NoPermPlayer");
        player.addAttachment(plugin, "disenchantment.anvil.shatter", false);
        player.addAttachment(plugin, "disenchantment.anvil.all", false);
        PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult());
    }
}
