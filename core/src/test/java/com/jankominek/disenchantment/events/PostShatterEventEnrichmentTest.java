package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.events.api.PostShatterEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostShatterEventEnrichmentTest {

    @Test
    void getXpCost_returnsConstructorValue() {
        Player p = Mockito.mock(Player.class);
        ItemStack book = Mockito.mock(ItemStack.class);
        PostShatterEvent event = new PostShatterEvent(p, book, book, 4, 8.0);
        assertEquals(4, event.getXpCost());
    }

    @Test
    void getEconomyCost_returnsConstructorValue() {
        Player p = Mockito.mock(Player.class);
        ItemStack book = Mockito.mock(ItemStack.class);
        PostShatterEvent event = new PostShatterEvent(p, book, book, 2, 6.5);
        assertEquals(6.5, event.getEconomyCost());
    }

    @Test
    void xpCostZero_whenPassedZero() {
        Player p = Mockito.mock(Player.class);
        ItemStack book = Mockito.mock(ItemStack.class);
        PostShatterEvent event = new PostShatterEvent(p, book, book, 0, 0.0);
        assertEquals(0, event.getXpCost());
        assertEquals(0.0, event.getEconomyCost());
    }
}
