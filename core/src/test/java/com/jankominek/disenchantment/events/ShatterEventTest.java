package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.Disenchantment;
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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ShatterEvent — fires PrepareAnvilEvent and asserts
 * the handler sets the correct split-book result.
 */
class ShatterEventTest extends DisenchantmentTestBase {

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

    // -> happy path

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
    void givenBookWithFourEnchantments_whenPrepareAnvil_thenResultContainsOneEnchantment() {
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
        // default split-count=1 → always splits exactly 1 enchantment off regardless of total count
        assertEquals(1, meta.getStoredEnchants().size());
    }

    @Test
    void givenBookWithFourEnchantments_andSplitCountTwo_whenPrepareAnvil_thenResultContainsTwoEnchantments() {
        setConfig("shatterment.split-count", 2);
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
        // split-count=2 → 2 enchantments split off
        assertEquals(2, meta.getStoredEnchants().size());
    }

    // -> rejection cases

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

    // -> world restrictions

    @Test
    void givenPlayerInDisabledWorld_whenPrepareAnvil_thenResultNotSet() {
        server.addSimpleWorld("no_shatter");
        PlayerMock player = server.addPlayer("TestPlayer");
        player.teleport(Objects.requireNonNull(server.getWorld("no_shatter")).getSpawnLocation());
        setConfig("shatterment.worlds.list", List.of("no_shatter"));
        PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult());
    }

    // -> enchantment state: KEEP

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

    // -> KEEP leaves one splittable enchantment: it splits off, KEEP stays on the source

    @Test
    void givenTwoEnchantmentsWithOneKEEP_whenPrepareAnvil_thenSplittableEnchantInResult() {
        setShatterEnchantmentStates(List.of("sharpness:keep"));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        ItemStack result = event.getResult();
        assertNotNull(result, "The non-KEEP enchantment must be splittable");
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertNotNull(meta);
        assertTrue(meta.hasStoredEnchant(enchantment("mending")), "Result must carry the splittable enchantment");
        assertFalse(meta.hasStoredEnchant(enchantment("sharpness")), "KEEP enchantment must stay on the source");
    }

    // -> DELETE leaves one splittable enchantment and nothing stays on the source: no result

    @Test
    void givenTwoEnchantmentsWithOneDELETE_whenPrepareAnvil_thenNoResult() {
        setShatterEnchantmentStates(List.of("sharpness:delete"));
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult(), "Splitting must not produce an empty result book");
    }

    // -> permission check

    @Test
    void givenPlayerWithoutPermission_whenPrepareAnvil_thenResultNotSet() {
        PlayerMock player = server.addPlayer("NoPermPlayer");
        player.addAttachment(plugin, "disenchantment.anvil.shatter", false);
        player.addAttachment(plugin, "disenchantment.anvil.all", false);
        PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(event);

        assertNull(event.getResult());
    }

    // -> maintenance mode only blocks genuine plugin operations, and never messages from Prepare

    @Test
    void givenMaintenance_whenPrepareGenuineShatter_thenResultClearedWithoutMessage() {
        Disenchantment.maintenanceEnabled = true;
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK));

            ShatterEvent.onEvent(event);

            Mockito.verify(event).setResult(null);
            assertNull(player.nextMessage(), "PrepareAnvilEvent fires per keystroke — it must not message the player");
        } finally {
            Disenchantment.maintenanceEnabled = false;
        }
    }

    @Test
    void givenMaintenance_whenPrepareVanillaAnvilUse_thenResultUntouched() {
        Disenchantment.maintenanceEnabled = true;
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            PrepareAnvilEvent event = buildEvent(player, enchantedBook("sharpness", "mending"), new ItemStack(Material.DIAMOND));

            ShatterEvent.onEvent(event);

            Mockito.verify(event, Mockito.never()).setResult(Mockito.any());
            assertNull(player.nextMessage());
        } finally {
            Disenchantment.maintenanceEnabled = false;
        }
    }

    // -> the chosen subset must be stable while the anvil inputs are unchanged (rename keystrokes re-fire Prepare)

    private String selectedKeys(PrepareAnvilEvent event) {
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) Objects.requireNonNull(event.getResult()).getItemMeta();
        return Objects.requireNonNull(meta).getStoredEnchants().keySet().stream()
                .map(e -> e.getKey().getKey()).sorted().collect(java.util.stream.Collectors.joining(","));
    }

    @Test
    void givenUnchangedInputs_whenPrepareFiresRepeatedly_thenSameSubsetEveryTime() {
        PlayerMock player = server.addPlayer("TestPlayer");
        ItemStack source = enchantedBook("sharpness", "mending", "efficiency", "unbreaking", "looting", "fortune");

        String first = null;
        for (int i = 0; i < 25; i++) {
            PrepareAnvilEvent event = buildEvent(player, source.clone(), new ItemStack(Material.BOOK));
            ShatterEvent.onEvent(event);
            String selected = selectedKeys(event);
            if (first == null) first = selected;
            assertEquals(first, selected, "Re-firing Prepare with unchanged inputs must not re-roll the subset");
        }
    }

    @Test
    void givenInputsChangeBetweenPrepares_whenPrepareFires_thenSubsetIsRolledAgain() {
        PlayerMock player = server.addPlayer("TestPlayer");
        ItemStack source = enchantedBook("sharpness", "mending", "efficiency", "unbreaking", "looting", "fortune");

        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = 0; i < 40; i++) {
            // Taking the blank book out and putting it back changes the inputs in between.
            ShatterEvent.onEvent(buildEvent(player, source.clone(), null));
            PrepareAnvilEvent event = buildEvent(player, source.clone(), new ItemStack(Material.BOOK));
            ShatterEvent.onEvent(event);
            seen.add(selectedKeys(event));
        }
        assertTrue(seen.size() > 1, "Changed inputs must invalidate the cache and re-roll (6 candidates, 40 rolls)");
    }

    @Test
    void givenBlankBookAmountChanges_whenPrepareFires_thenCachedSubsetDropped() {
        PlayerMock player = server.addPlayer("TestPlayer");
        ItemStack source = enchantedBook("sharpness", "mending", "efficiency", "unbreaking", "looting", "fortune");

        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = 0; i < 40; i++) {
            ShatterEvent.onEvent(buildEvent(player, source.clone(), new ItemStack(Material.BOOK, 1)));
            PrepareAnvilEvent event = buildEvent(player, source.clone(), new ItemStack(Material.BOOK, 2));
            ShatterEvent.onEvent(event);
            seen.add(selectedKeys(event));
        }
        assertTrue(seen.size() > 1, "A different blank-book amount is a different input");
    }

    @Test
    void givenOperationCompleted_whenSelectionCleared_thenNextPrepareRollsAgain() {
        PlayerMock player = server.addPlayer("TestPlayer");
        ItemStack source = enchantedBook("sharpness", "mending", "efficiency", "unbreaking", "looting", "fortune");

        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = 0; i < 40; i++) {
            PrepareAnvilEvent event = buildEvent(player, source.clone(), new ItemStack(Material.BOOK));
            ShatterEvent.onEvent(event);
            seen.add(selectedKeys(event));
            ShatterEvent.clearSelection(player);
        }
        assertTrue(seen.size() > 1, "clearSelection (operation completed) must force a fresh roll");
    }
}
