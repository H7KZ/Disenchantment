package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.events.api.PostDisenchantEvent;
import com.jankominek.disenchantment.utils.EconomyTestSupport;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.jankominek.disenchantment.utils.PreviewLore;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests of the preview lore: Prepare appends marked lines to the result book, the click
 * handlers strip them before the book reaches the player and before the Post event is fired.
 */
class PreviewLoreFlowTest extends DisenchantmentTestBase {

    private ItemStack sword(String enchantKey, int level) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        item.addUnsafeEnchantment(enchantment(enchantKey), level);
        return item;
    }

    private ItemStack twoEnchantBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentUtils.addStoredEnchantment(book, enchantment("sharpness"), 5);
        EnchantmentUtils.addStoredEnchantment(book, enchantment("unbreaking"), 3);
        return book;
    }

    private ItemStack prepareDisenchant(PlayerMock player, ItemStack source) {
        PrepareAnvilEvent prepare = buildAnvilEvent(player, source, new ItemStack(Material.BOOK));
        DisenchantEvent.onEvent(prepare);
        return prepare.getResult();
    }

    private AnvilInventory lastAnvil;

    private InventoryClickEvent buildClick(PlayerMock player, ItemStack slot0, ItemStack slot1, ItemStack slot2) {
        AnvilInventory anvil = Mockito.mock(AnvilInventory.class);
        lastAnvil = anvil;
        Mockito.when(anvil.getType()).thenReturn(InventoryType.ANVIL);
        Mockito.when(anvil.getItem(0)).thenReturn(slot0);
        Mockito.when(anvil.getItem(1)).thenReturn(slot1);
        Mockito.when(anvil.getItem(2)).thenReturn(slot2);
        Mockito.when(anvil.getRepairCost()).thenReturn(0);
        Mockito.when(anvil.getViewers()).thenReturn(List.of(player));

        List<Class<?>> interfaces = new ArrayList<>();
        interfaces.add(InventoryView.class);
        try {
            interfaces.add(Class.forName("org.bukkit.inventory.view.AnvilView"));
        } catch (ClassNotFoundException ignored) {
        }
        Object view = Proxy.newProxyInstance(getClass().getClassLoader(), interfaces.toArray(new Class[0]), (proxy, method, args) -> {
            if ("getPlayer".equals(method.getName())) return player;
            if (method.getReturnType() == boolean.class) return false;
            if (method.getReturnType().isPrimitive()) return 0;
            return null;
        });

        InventoryClickEvent event = Mockito.mock(InventoryClickEvent.class);
        Mockito.when(event.getWhoClicked()).thenReturn(player);
        Mockito.when(event.getInventory()).thenReturn(anvil);
        Mockito.when(event.getSlot()).thenReturn(2);
        Mockito.when(event.getRawSlot()).thenReturn(2);
        Mockito.when(event.getView()).thenReturn((InventoryView) view);
        return event;
    }

    private static List<String> lore(ItemStack item) {
        List<String> lore = item.getItemMeta().getLore();
        return lore == null ? List.of() : lore;
    }

    // -> Prepare (disenchant)

    @Test
    void givenDefaults_whenPrepareDisenchant_thenResultListsTransfersAndCostAsMarkedPreview() {
        PlayerMock player = server.addPlayer("TestPlayer");

        ItemStack result = prepareDisenchant(player, sword("sharpness", 5));

        assertTrue(PreviewLore.hasPreview(result));
        List<String> lore = lore(result);
        assertEquals(2, lore.size(), "Transfers line + cost line: " + lore);
        assertTrue(lore.get(0).contains("Sharpness V"), lore.get(0));
        assertTrue(lore.get(1).contains("levels"), lore.get(1));
    }

    @Test
    void givenPreviewDisabled_whenPrepareDisenchant_thenNoLoreAndNoMarker() {
        setConfig("feedback.preview-lore", false);
        PlayerMock player = server.addPlayer("TestPlayer");

        ItemStack result = prepareDisenchant(player, sword("sharpness", 5));

        assertFalse(PreviewLore.hasPreview(result));
        assertTrue(lore(result).isEmpty());
    }

    @Test
    void givenChanceBelowOneAndPreviewDisabled_whenPrepareDisenchant_thenOnlyMarkedWarningLine() {
        setConfig("feedback.preview-lore", false);
        setConfig("disenchantment.enchantment-chances.sharpness", 0.5);
        PlayerMock player = server.addPlayer("TestPlayer");

        ItemStack result = prepareDisenchant(player, sword("sharpness", 5));

        assertTrue(PreviewLore.hasPreview(result), "The warning line is part of the stripped preview lines");
        assertEquals(1, lore(result).size());
    }

    @Test
    void givenEconomyShowCost_whenPrepareDisenchant_thenCostLineIncludesMoney() {
        setConfig("disenchantment.economy.enabled", true);
        setConfig("disenchantment.economy.cost", 100.0);
        EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");

            ItemStack result = prepareDisenchant(player, sword("sharpness", 5));

            assertTrue(lore(result).stream().anyMatch(line -> line.contains("100.0")), lore(result).toString());
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    @Test
    void givenEconomyHiddenCost_whenPrepareDisenchant_thenNoMoneyInLore() {
        setConfig("disenchantment.economy.enabled", true);
        setConfig("disenchantment.economy.show-cost", false);
        setConfig("disenchantment.economy.cost", 100.0);
        EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");

            ItemStack result = prepareDisenchant(player, sword("sharpness", 5));

            assertTrue(lore(result).stream().noneMatch(line -> line.contains("100.0")), lore(result).toString());
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    // -> Prepare (shatter)

    @Test
    void givenDefaults_whenPrepareShatter_thenResultIsMarkedPreview() {
        PlayerMock player = server.addPlayer("TestPlayer");
        PrepareAnvilEvent prepare = buildAnvilEvent(player, twoEnchantBook(), new ItemStack(Material.BOOK));

        ShatterEvent.onEvent(prepare);

        ItemStack result = prepare.getResult();
        assertNotNull(result);
        assertTrue(PreviewLore.hasPreview(result));
        assertTrue(lore(result).get(0).contains("Transfers"), lore(result).get(0));
    }

    // -> Click (disenchant): the delivered book is clean

    @Test
    void givenPreparedBook_whenClickDisenchant_thenDeliveredBookHasNoPreviewLore() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);
        ItemStack source = sword("sharpness", 5);
        ItemStack prepared = prepareDisenchant(player, source);
        assertTrue(PreviewLore.hasPreview(prepared));

        DisenchantClickEvent.onEvent(buildClick(player, source, new ItemStack(Material.BOOK), prepared));

        ItemStack cursor = player.getItemOnCursor();
        assertEquals(Material.ENCHANTED_BOOK, cursor.getType());
        assertFalse(PreviewLore.hasPreview(cursor));
        assertTrue(lore(cursor).isEmpty(), "Delivered book must carry no lore: " + lore(cursor));
    }

    @Test
    void givenChanceWarningAndPreviewDisabled_whenClickDisenchant_thenWarningNotLeftOnBook() {
        setConfig("feedback.preview-lore", false);
        setConfig("disenchantment.enchantment-chances.sharpness", 1.0);
        setConfig("disenchantment.enchantment-chances.unbreaking", 0.5);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);
        ItemStack source = sword("sharpness", 5);
        source.addUnsafeEnchantment(enchantment("unbreaking"), 3);
        ItemStack prepared = prepareDisenchant(player, source);
        assertEquals(1, lore(prepared).size(), "The chance warning is on the prepared book");

        DisenchantClickEvent.onEvent(buildClick(player, source, new ItemStack(Material.BOOK), prepared));

        ItemStack cursor = player.getItemOnCursor();
        assertEquals(Material.ENCHANTED_BOOK, cursor.getType());
        assertTrue(lore(cursor).isEmpty(), "The warning must be stripped: " + lore(cursor));
    }

    @Test
    void givenPreparedBook_whenClickDisenchant_thenPostEventBookIsClean() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);
        AtomicReference<PostDisenchantEvent> captured = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PostDisenchantEvent e) {
                captured.set(e);
            }
        }, plugin);
        ItemStack source = sword("sharpness", 5);
        ItemStack prepared = prepareDisenchant(player, source);

        DisenchantClickEvent.onEvent(buildClick(player, source, new ItemStack(Material.BOOK), prepared));

        assertNotNull(captured.get());
        assertFalse(PreviewLore.hasPreview(captured.get().getResultBook()));
        assertTrue(lore(captured.get().getResultBook()).isEmpty());
    }

    @Test
    void givenPreparedBookAndRefusedClick_whenNotEnoughLevels_thenResultStillCarriesPreview() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(0);
        ItemStack source = sword("sharpness", 5);
        ItemStack prepared = prepareDisenchant(player, source);
        InventoryClickEvent click = buildClick(player, source, new ItemStack(Material.BOOK), prepared);
        Mockito.when(lastAnvil.getRepairCost()).thenReturn(30);

        DisenchantClickEvent.onEvent(click);

        assertTrue(PreviewLore.hasPreview(prepared), "A refused click must not mutate the result slot");
        assertTrue(player.getItemOnCursor().getType().isAir());
    }

    // -> Click (shatter)

    @Test
    void givenPreparedShatterBook_whenClickShatter_thenDeliveredBookHasNoPreviewLore() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);
        ItemStack source = twoEnchantBook();
        PrepareAnvilEvent prepare = buildAnvilEvent(player, source, new ItemStack(Material.BOOK));
        ShatterEvent.onEvent(prepare);
        ItemStack prepared = prepare.getResult();
        assertTrue(PreviewLore.hasPreview(prepared));

        ShatterClickEvent.onEvent(buildClick(player, source, new ItemStack(Material.BOOK), prepared));

        ItemStack cursor = player.getItemOnCursor();
        assertEquals(Material.ENCHANTED_BOOK, cursor.getType());
        assertFalse(PreviewLore.hasPreview(cursor));
        assertTrue(lore(cursor).isEmpty(), "Delivered book must carry no lore: " + lore(cursor));
    }
}
