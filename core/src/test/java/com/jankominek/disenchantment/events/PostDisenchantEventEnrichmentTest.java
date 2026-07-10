package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.events.api.PostDisenchantEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostDisenchantEventEnrichmentTest {

    @Test
    void getXpCost_returnsConstructorValue() {
        Player p = Mockito.mock(Player.class);
        ItemStack book = Mockito.mock(ItemStack.class);
        PostDisenchantEvent event = new PostDisenchantEvent(p, book, book, 5, 10.0);
        assertEquals(5, event.getXpCost());
    }

    @Test
    void getEconomyCost_returnsConstructorValue() {
        Player p = Mockito.mock(Player.class);
        ItemStack book = Mockito.mock(ItemStack.class);
        PostDisenchantEvent event = new PostDisenchantEvent(p, book, book, 3, 7.5);
        assertEquals(7.5, event.getEconomyCost());
    }
}
