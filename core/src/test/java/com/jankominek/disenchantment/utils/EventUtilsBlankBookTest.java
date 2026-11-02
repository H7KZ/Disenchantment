package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.MockPluginAdapter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Only a plain {@link Material#BOOK} may act as the blank book. A book another plugin has
 * tagged (display name, lore, custom model data, persistent data) must be rejected so it
 * cannot be consumed by a disenchant / shatter operation.
 */
class EventUtilsBlankBookTest extends DisenchantmentTestBase {

    private ItemStack book(Consumer<ItemMeta> customizer) {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        customizer.accept(meta);
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack sword() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        item.addUnsafeEnchantment(enchantment("sharpness"), 3);
        return item;
    }

    private ItemStack twoEnchantBook() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentUtils.addStoredEnchantment(item, enchantment("sharpness"), 1);
        EnchantmentUtils.addStoredEnchantment(item, enchantment("mending"), 1);
        return item;
    }

    private ItemStack namedBook() {
        return book(m -> m.setDisplayName("Quest Tome"));
    }

    private ItemStack loreBook() {
        return book(m -> m.setLore(List.of("Currency")));
    }

    private ItemStack modelDataBook() {
        return book(m -> m.setCustomModelData(7));
    }

    private ItemStack taggedBook() {
        return book(m -> m.getPersistentDataContainer().set(new NamespacedKey("otherplugin", "voucher"), PersistentDataType.STRING, "x"));
    }

    // -> isBlankBook

    @Test
    void givenPlainBook_whenIsBlankBook_thenTrue() {
        assertTrue(EventUtils.isBlankBook(new ItemStack(Material.BOOK)));
    }

    @Test
    void givenBookWithEmptyMeta_whenIsBlankBook_thenTrue() {
        assertTrue(EventUtils.isBlankBook(book(m -> {
        })));
    }

    @Test
    void givenNonBookOrNull_whenIsBlankBook_thenFalse() {
        assertFalse(EventUtils.isBlankBook(null));
        assertFalse(EventUtils.isBlankBook(new ItemStack(Material.PAPER)));
        assertFalse(EventUtils.isBlankBook(new ItemStack(Material.ENCHANTED_BOOK)));
    }

    @Test
    void givenSpecialBooks_whenIsBlankBook_thenFalse() {
        assertFalse(EventUtils.isBlankBook(namedBook()), "display name");
        assertFalse(EventUtils.isBlankBook(loreBook()), "lore");
        assertFalse(EventUtils.isBlankBook(modelDataBook()), "custom model data");
        assertFalse(EventUtils.isBlankBook(taggedBook()), "persistent data");
    }

    // -> every collection overload must apply the check

    @Test
    void givenSpecialBook_whenCollectDisenchant_thenRejected() {
        for (ItemStack special : List.of(namedBook(), loreBook(), modelDataBook(), taggedBook())) {
            assertTrue(EventUtils.Disenchantment.getDisenchantedEnchantments(sword(), special, false).isEmpty());
        }
        assertEquals(1, EventUtils.Disenchantment.getDisenchantedEnchantments(sword(), new ItemStack(Material.BOOK), false).size());
    }

    @Test
    void givenSpecialBook_whenCollectShatter_thenRejected() {
        for (ItemStack special : List.of(namedBook(), loreBook(), modelDataBook(), taggedBook())) {
            assertTrue(EventUtils.Shatterment.getShattermentEnchantments(twoEnchantBook(), special, false).isEmpty());
        }
        assertEquals(2, EventUtils.Shatterment.getShattermentEnchantments(twoEnchantBook(), new ItemStack(Material.BOOK), false).size());
    }

    @Test
    void givenSpecialBook_whenCollectViaAdapter_thenRejected() {
        MockPluginAdapter adapter = new MockPluginAdapter("Vane", mockEnchant("sharpness", 1), mockEnchant("mending", 1));
        ItemStack tagged = taggedBook();

        assertTrue(EventUtils.Disenchantment.getDisenchantedEnchantments(sword(), tagged, false, adapter, world()).isEmpty());
        assertTrue(EventUtils.Shatterment.getShattermentEnchantments(twoEnchantBook(), tagged, false, adapter, world()).isEmpty());
    }
}
