package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.utils.EconomyTestSupport;
import com.jankominek.disenchantment.utils.EraseUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

/**
 * The settlement order: everything is built before the player is charged (an adapter that throws costs nothing),
 * and a failure while writing the result gives the payment back.
 */
class SettleOrderingTest extends DisenchantmentTestBase {

    /**
     * A third-party enchantment whose adapter blows up when it is removed from an item.
     */
    static class BoomEnchantment implements IPluginEnchantment {
        @Override
        public String getKey() {
            return "boom";
        }

        @Override
        public int getLevel() {
            return 1;
        }

        @Override
        public ItemStack removeFromItem(ItemStack item) {
            throw new IllegalStateException("adapter failure");
        }
    }

    private ItemStack sword() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        item.addUnsafeEnchantment(enchantment("sharpness"), 5);
        return item;
    }

    private ItemStack shard() {
        return new ItemStack(Material.ECHO_SHARD);
    }

    private void enableEraseEconomy() {
        setConfig("erase.enabled", true);
        setConfig("erase.economy.enabled", true);
        setConfig("erase.economy.cost", 75.0);
    }

    // -> the item is built before anything is charged

    @Test
    void givenAdapterThrowsWhileBuildingEraseResult_whenSettle_thenNothingWasCharged() {
        enableEraseEconomy();
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(20);

            assertThrows(IllegalStateException.class, () -> AnvilOperations.settle(AnvilOperations.Kind.ERASE, player,
                    sword(), shard(), sword(), List.of(new BoomEnchantment()), 12, null,
                    (first, delivered) -> fail("nothing may be written when the result could not be built")));

            assertEquals(20, player.getLevel(), "XP must not be taken");
            Mockito.verify(economy, Mockito.never()).withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble());
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    // -> a failure while writing refunds both payments

    @Test
    void givenWriteFailsAfterPayment_whenSettleErase_thenXpAndMoneyAreGivenBack() {
        enableEraseEconomy();
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(20);
            ItemStack source = sword();
            List<IPluginEnchantment> erasable = EraseUtils.getErasableEnchantments(source, shard());

            assertThrows(AnvilOperations.WriteFailedException.class, () -> AnvilOperations.settle(AnvilOperations.Kind.ERASE, player,
                    source, shard(), source, erasable, 12, null, (first, delivered) -> {
                        throw new IllegalStateException("slot write failed");
                    }));

            assertEquals(20, player.getLevel(), "XP is restored");
            Mockito.verify(economy).withdrawPlayer(Mockito.any(OfflinePlayer.class), eq(75.0));
            Mockito.verify(economy).depositPlayer(Mockito.any(OfflinePlayer.class), eq(75.0));
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    @Test
    void givenWriteFailsAfterPayment_whenSettleDisenchant_thenXpIsGivenBack() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);
        ItemStack source = sword();
        ItemStack book = new ItemStack(Material.BOOK);
        List<IPluginEnchantment> prepared = AnvilOperations.collect(AnvilOperations.Kind.DISENCHANT, player, source, book, true);
        ItemStack result = AnvilOperations.buildBook(prepared);
        List<IPluginEnchantment> atClick = AnvilOperations.collect(AnvilOperations.Kind.DISENCHANT, player, source, book, false);

        assertThrows(AnvilOperations.WriteFailedException.class, () -> AnvilOperations.settle(AnvilOperations.Kind.DISENCHANT, player,
                source, book, result, atClick, 7, null, (first, delivered) -> {
                    throw new IllegalStateException("slot write failed");
                }));

        assertEquals(20, player.getLevel());
    }

    @Test
    void givenSuccessfulSettle_thenXpIsTakenExactlyOnce() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);
        ItemStack source = sword();
        ItemStack book = new ItemStack(Material.BOOK);
        List<IPluginEnchantment> prepared = AnvilOperations.collect(AnvilOperations.Kind.DISENCHANT, player, source, book, true);
        ItemStack result = AnvilOperations.buildBook(prepared);
        List<IPluginEnchantment> atClick = AnvilOperations.collect(AnvilOperations.Kind.DISENCHANT, player, source, book, false);

        AnvilOperations.Outcome outcome = AnvilOperations.settle(AnvilOperations.Kind.DISENCHANT, player,
                source, book, result, atClick, 7, null, (first, delivered) -> {
                });

        assertEquals(AnvilOperations.Status.DONE, outcome.status());
        assertEquals(13, player.getLevel());
    }

    // -> the XP is re-checked once the economy has been charged

    @Test
    void givenLevelsVanishDuringEconomyWithdrawal_whenSettle_thenMoneyRefundedAndNothingWritten() {
        enableEraseEconomy();
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(20);
            // An economy plugin that also zeroes the player's levels while withdrawing.
            Mockito.when(economy.withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble())).thenAnswer(inv -> {
                player.setLevel(0);
                return new net.milkbowl.vault.economy.EconomyResponse(inv.getArgument(1), 0,
                        net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS, null);
            });
            ItemStack source = sword();
            List<IPluginEnchantment> erasable = EraseUtils.getErasableEnchantments(source, shard());

            AnvilOperations.Outcome outcome = AnvilOperations.settle(AnvilOperations.Kind.ERASE, player,
                    source, shard(), source, erasable, 12, null, (first, delivered) -> fail("must not write"));

            assertEquals(AnvilOperations.Status.CANCELLED, outcome.status());
            Mockito.verify(economy).depositPlayer(Mockito.any(OfflinePlayer.class), eq(75.0));
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    @Test
    void givenCreativePlayer_whenWriteFails_thenNoRefundIsAttempted() {
        enableEraseEconomy();
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("Creative");
            player.setGameMode(GameMode.CREATIVE);
            ItemStack source = sword();
            List<IPluginEnchantment> erasable = EraseUtils.getErasableEnchantments(source, shard());

            assertThrows(AnvilOperations.WriteFailedException.class, () -> AnvilOperations.settle(AnvilOperations.Kind.ERASE, player,
                    source, shard(), source, erasable, 12, null, (first, delivered) -> {
                        throw new IllegalStateException("slot write failed");
                    }));

            Mockito.verify(economy, Mockito.never()).depositPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble());
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    // -> a failed write restores the three input slots, the cursor and the pending removal, not only the payment

    /** An anvil whose slots 0-2 live in an array, so a restore is observable. */
    private static AnvilInventory statefulAnvil(ItemStack... slots) {
        ItemStack[] backing = new ItemStack[3];
        for (int i = 0; i < slots.length; i++) backing[i] = slots[i];
        AnvilInventory anvil = Mockito.mock(AnvilInventory.class);
        Mockito.when(anvil.getSize()).thenReturn(3);
        Mockito.when(anvil.getItem(Mockito.anyInt())).thenAnswer(inv -> backing[inv.<Integer>getArgument(0)]);
        Mockito.doAnswer(inv -> {
            backing[inv.<Integer>getArgument(0)] = inv.getArgument(1);
            return null;
        }).when(anvil).setItem(Mockito.anyInt(), Mockito.any());
        return anvil;
    }

    private static void assertSlot(ItemStack expected, ItemStack actual, String what) {
        if (expected == null) assertTrue(actual == null || actual.getType().isAir(), what + " must be empty");
        else assertTrue(actual != null && expected.isSimilar(actual) && expected.getAmount() == actual.getAmount(), what + " must be as before");
    }

    private void failAfterAllWrites(AnvilOperations.Kind kind, ItemStack source, ItemStack second, ItemStack result, boolean erase) {
        enableEraseEconomy();
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(20);
            ItemStack sourceBefore = source.clone();
            ItemStack secondBefore = second.clone();
            ItemStack resultBefore = result.clone();
            AnvilInventory anvil = statefulAnvil(source.clone(), second.clone(), result.clone());
            List<IPluginEnchantment> atClick = AnvilOperations.collect(kind, player, source, second, false);

            assertThrows(AnvilOperations.WriteFailedException.class, () -> AnvilOperations.settle(kind, player,
                    source, second, result, atClick, 7, anvil, (first, delivered) -> {
                        if (erase) {
                            anvil.setItem(0, null);
                            anvil.setItem(1, AnvilOperations.consumeCatalyst(second));
                            anvil.setItem(2, null);
                        } else {
                            anvil.setItem(0, first);
                            AnvilEventGuards.scheduleSecondItemRemoval(player, anvil, second, null);
                        }
                        player.setItemOnCursor(delivered);
                        throw new IllegalStateException("the last write failed");
                    }));

            assertSlot(sourceBefore, anvil.getItem(0), "slot 0");
            assertSlot(secondBefore, anvil.getItem(1), "slot 1");
            assertSlot(resultBefore, anvil.getItem(2), "slot 2");
            assertTrue(player.getItemOnCursor().getType().isAir(), "nothing on the cursor");
            assertFalse(AnvilEventGuards.isOperationPending(anvil), "no stale pending removal may lock the anvil");
            assertEquals(20, player.getLevel(), "levels refunded");
            if (kind == AnvilOperations.Kind.ERASE) {
                Mockito.verify(economy).withdrawPlayer(Mockito.any(OfflinePlayer.class), eq(75.0));
                Mockito.verify(economy).depositPlayer(Mockito.any(OfflinePlayer.class), eq(75.0));
            }
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    @Test
    void givenLaterWriteFails_whenSettleDisenchant_thenSlotsCursorAndPendingAreRestored() {
        ItemStack source = sword();
        ItemStack book = new ItemStack(Material.BOOK);
        PlayerMock prep = server.addPlayer("Prep");
        ItemStack result = AnvilOperations.buildBook(AnvilOperations.collect(AnvilOperations.Kind.DISENCHANT, prep, source, book, true));
        failAfterAllWrites(AnvilOperations.Kind.DISENCHANT, source, book, result, false);
    }

    @Test
    void givenLaterWriteFails_whenSettleShatter_thenSlotsCursorAndPendingAreRestored() {
        ItemStack source = new ItemStack(Material.ENCHANTED_BOOK);
        com.jankominek.disenchantment.utils.EnchantmentUtils.addStoredEnchantment(source, enchantment("sharpness"), 5);
        com.jankominek.disenchantment.utils.EnchantmentUtils.addStoredEnchantment(source, enchantment("unbreaking"), 3);
        ItemStack book = new ItemStack(Material.BOOK);
        PlayerMock prep = server.addPlayer("Prep");
        ItemStack result = AnvilOperations.buildBook(AnvilOperations.collect(AnvilOperations.Kind.SHATTER, prep, source, book, true));
        failAfterAllWrites(AnvilOperations.Kind.SHATTER, source, book, result, false);
    }

    @Test
    void givenLaterWriteFails_whenSettleErase_thenSlotsCursorAndMoneyAreRestored() {
        failAfterAllWrites(AnvilOperations.Kind.ERASE, sword(), shard(), sword(), true);
    }

    @Test
    void givenSnapshotCannotBeTaken_whenWriteFails_thenPaymentIsStillRefunded() {
        enableEraseEconomy();
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(20);
            AnvilInventory anvil = Mockito.mock(AnvilInventory.class);
            Mockito.when(anvil.getSize()).thenReturn(3);
            Mockito.when(anvil.getItem(Mockito.anyInt())).thenThrow(new IllegalStateException("cannot read slots"));
            ItemStack source = sword();
            List<IPluginEnchantment> erasable = EraseUtils.getErasableEnchantments(source, shard());

            assertThrows(AnvilOperations.WriteFailedException.class, () -> AnvilOperations.settle(AnvilOperations.Kind.ERASE, player,
                    source, shard(), source, erasable, 12, anvil, (first, delivered) -> {
                        throw new IllegalStateException("slot write failed");
                    }));

            assertEquals(20, player.getLevel(), "levels refunded");
            Mockito.verify(economy, Mockito.times(1)).depositPlayer(Mockito.any(OfflinePlayer.class), eq(75.0));
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    @Test
    void givenRestoreStepThrows_whenWriteFails_thenRefundedOnceAndOriginalFailureIsTheCause() {
        enableEraseEconomy();
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(20);
            AnvilInventory anvil = statefulAnvil(sword(), shard(), sword());
            ItemStack source = sword();
            List<IPluginEnchantment> erasable = EraseUtils.getErasableEnchantments(source, shard());
            IllegalStateException original = new IllegalStateException("the original write failure");

            AnvilOperations.WriteFailedException thrown = assertThrows(AnvilOperations.WriteFailedException.class,
                    () -> AnvilOperations.settle(AnvilOperations.Kind.ERASE, player, source, shard(), source, erasable, 12, anvil,
                            (first, delivered) -> {
                                // From here every slot write (the rollback's too) fails.
                                Mockito.doThrow(new IllegalStateException("restore failure")).when(anvil).setItem(Mockito.anyInt(), Mockito.any());
                                throw original;
                            }));

            assertSame(original, thrown.getCause(), "the original failure is the one reported");
            assertEquals(20, player.getLevel(), "levels refunded");
            Mockito.verify(economy, Mockito.times(1)).depositPlayer(Mockito.any(OfflinePlayer.class), eq(75.0));
        } finally {
            EconomyTestSupport.uninstall();
        }
    }
}
