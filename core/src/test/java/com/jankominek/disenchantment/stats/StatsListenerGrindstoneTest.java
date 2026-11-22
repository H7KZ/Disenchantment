package com.jankominek.disenchantment.stats;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.events.api.PostGrindstoneExtractEvent;
import com.jankominek.disenchantment.events.api.PostGrindstoneTransferEvent;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.List;

/**
 * The grindstone operations are recorded under their own operation types, with only the enchantments
 * that actually moved.
 */
class StatsListenerGrindstoneTest extends DisenchantmentTestBase {

    @Test
    void givenExtractCompleted_whenPostEvent_thenRecordedAsGrindExtract() {
        StatsManager manager = Mockito.mock(StatsManager.class);
        PlayerMock player = server.addPlayer();
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentUtils.addStoredEnchantment(book, enchantment("sharpness"), 5);

        new StatsListener(manager).onPostGrindstoneExtract(
                new PostGrindstoneExtractEvent(player, book, new ItemStack(Material.DIAMOND_SWORD), 11, 2.5));

        Mockito.verify(manager).record(OperationType.GRIND_EXTRACT, player, book, "DIAMOND_SWORD", 11, 2.5);
    }

    @Test
    void givenTransferCompleted_whenPostEvent_thenOnlyMovedEnchantmentsRecorded() {
        StatsManager manager = Mockito.mock(StatsManager.class);
        PlayerMock player = server.addPlayer();
        ItemStack shovel = new ItemStack(Material.IRON_SHOVEL);
        shovel.addUnsafeEnchantment(enchantment("efficiency"), 5);
        shovel.addUnsafeEnchantment(enchantment("unbreaking"), 2); // was already on the target

        new StatsListener(manager).onPostGrindstoneTransfer(new PostGrindstoneTransferEvent(
                player, shovel, new ItemStack(Material.DIAMOND_PICKAXE), List.of(mockEnchant("efficiency", 5)), 4, 0.0));

        Mockito.verify(manager).record(OperationType.GRIND_TRANSFER, player, List.of("minecraft:efficiency"), "IRON_SHOVEL", 4, 0.0);
    }
}
