package com.jankominek.disenchantment.guis;

import org.bukkit.inventory.ItemFlag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemBuilderTest {

    @Test
    void givenExistingFlagName_whenResolveItemFlag_thenReturnsFlag() {
        assertEquals(ItemFlag.HIDE_ENCHANTS, ItemBuilder.resolveItemFlag("HIDE_ENCHANTS"));
    }

    @Test
    void givenBogusFlagName_whenResolveItemFlag_thenReturnsNull() {
        assertNull(ItemBuilder.resolveItemFlag("NOT_A_REAL_ITEM_FLAG"));
    }
}
