package com.jankominek.disenchantment.events;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AnvilEventGuards#isUnsafeResultClick} — the shared guard that
 * blocks the result-slot duplication exploit (issue #71). Any click that lets vanilla move
 * a second copy of the result must be reported unsafe.
 */
class AnvilEventGuardsTest {

    private InventoryClickEvent clickOf(ClickType type) {
        InventoryClickEvent e = Mockito.mock(InventoryClickEvent.class);
        Mockito.when(e.getClick()).thenReturn(type);
        return e;
    }

    // -> whitelist: every click type EXCEPT a plain left/right pickup must be blocked
    //    (covers shift/number-key/swap/double-click/drops AND middle/creative clone AND any
    //    future ClickType added by the API — a whitelist degrades safely to "block")

    @ParameterizedTest
    @EnumSource(value = ClickType.class, mode = EnumSource.Mode.EXCLUDE, names = {"LEFT", "RIGHT"})
    void givenNonPickupClick_whenGuardChecked_thenUnsafe(ClickType type) {
        assertTrue(AnvilEventGuards.isUnsafeResultClick(clickOf(type)),
                type + " must be treated as an unsafe result-slot click");
    }

    // -> a normal pickup must remain allowed

    @Test
    void givenPlainLeftClick_whenGuardChecked_thenSafe() {
        assertFalse(AnvilEventGuards.isUnsafeResultClick(clickOf(ClickType.LEFT)),
                "A plain left-click pickup must be allowed");
    }

    @Test
    void givenPlainRightClick_whenGuardChecked_thenSafe() {
        assertFalse(AnvilEventGuards.isUnsafeResultClick(clickOf(ClickType.RIGHT)),
                "A plain right-click pickup must be allowed");
    }

    // -> a null click (unstubbed mock) must not be treated as unsafe

    @Test
    void givenNullClick_whenGuardChecked_thenSafe() {
        assertFalse(AnvilEventGuards.isUnsafeResultClick(clickOf(null)),
                "A null click type must be treated as safe (defensive)");
    }
}
