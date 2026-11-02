package com.jankominek.disenchantment.utils;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.mockito.Mockito;

/**
 * Test helper that hooks a Mockito {@link Economy} into {@link EconomyUtils}. Every call
 * to {@link #install()} replaces the provider; tests must call {@link #uninstall()} in a
 * {@code finally} block so the static hook does not leak between tests.
 */
public final class EconomyTestSupport {
    private EconomyTestSupport() {
    }

    /**
     * Installs a mock economy that always has funds and whose withdrawals succeed.
     *
     * @return the installed mock, for stubbing and verification
     */
    public static Economy install() {
        Economy economy = Mockito.mock(Economy.class);
        Mockito.when(economy.has(Mockito.any(OfflinePlayer.class), Mockito.anyDouble())).thenReturn(true);
        Mockito.when(economy.withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble()))
                .thenAnswer(inv -> new EconomyResponse(inv.getArgument(1), 0, EconomyResponse.ResponseType.SUCCESS, null));
        Mockito.when(economy.depositPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble()))
                .thenAnswer(inv -> new EconomyResponse(inv.getArgument(1), 0, EconomyResponse.ResponseType.SUCCESS, null));
        Mockito.when(economy.format(Mockito.anyDouble())).thenAnswer(inv -> String.valueOf((double) inv.getArgument(0)));
        EconomyUtils.setProvider(economy);
        return economy;
    }

    /**
     * Removes the installed economy hook.
     */
    public static void uninstall() {
        EconomyUtils.reset();
    }
}
