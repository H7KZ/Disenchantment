package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.utils.EconomyTestSupport;
import com.jankominek.disenchantment.utils.EraseUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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

            assertThrows(IllegalStateException.class, () -> AnvilOperations.settle(AnvilOperations.Kind.ERASE, player,
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

        assertThrows(IllegalStateException.class, () -> AnvilOperations.settle(AnvilOperations.Kind.DISENCHANT, player,
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

            assertThrows(IllegalStateException.class, () -> AnvilOperations.settle(AnvilOperations.Kind.ERASE, player,
                    source, shard(), source, erasable, 12, null, (first, delivered) -> {
                        throw new IllegalStateException("slot write failed");
                    }));

            Mockito.verify(economy, Mockito.never()).depositPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble());
        } finally {
            EconomyTestSupport.uninstall();
        }
    }
}
