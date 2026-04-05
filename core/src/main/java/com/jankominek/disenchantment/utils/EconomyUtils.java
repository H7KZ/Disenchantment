package com.jankominek.disenchantment.utils;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.stream.Collectors;

/**
 * Utility class that manages the optional Vault economy hook.
 * All economy operations are routed through this class so that the rest of the
 * codebase never references Vault directly and can safely call these methods
 * regardless of whether Vault is installed.
 */
public class EconomyUtils {
    private static Economy economy = null;

    /**
     * Attempts to hook into Vault and locate an active economy provider.
     * Should be called once during plugin startup after all plugins have loaded.
     *
     * @return {@code true} if Vault and an economy plugin were found and hooked successfully
     */
    public static boolean setup() {
        boolean vaultFound = Bukkit.getPluginManager().getPlugin("Vault") != null;
        boolean vaultUnlockedFound = Bukkit.getPluginManager().getPlugin("VaultUnlocked") != null;

        DiagnosticUtils.debug("ECONOMY", "Vault found: " + vaultFound + ", VaultUnlocked found: " + vaultUnlockedFound);

        if (!vaultFound && !vaultUnlockedFound) {
            DiagnosticUtils.debug("ECONOMY", "Setup: FAILED (no Vault or VaultUnlocked plugin present)");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
                Bukkit.getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            DiagnosticUtils.debug("ECONOMY", "Setup: FAILED (RSP<Economy> lookup returned null — no economy plugin registered under Vault API)");
            if (DiagnosticUtils.isDebugEnabled()) {
                String known = Bukkit.getServicesManager().getKnownServices().stream()
                        .map(Class::getName)
                        .collect(Collectors.joining(", "));
                DiagnosticUtils.debug("ECONOMY", "Registered services at time of lookup: [" + known + "]");
            }
            return false;
        }

        economy = rsp.getProvider();

        if (economy == null) {
            DiagnosticUtils.debug("ECONOMY", "Setup: FAILED (provider is null)");
            return false;
        }

        DiagnosticUtils.debug("ECONOMY", "Setup: SUCCESS (provider=" + economy.getClass().getSimpleName() + ", class=" + economy.getClass().getName() + ")");
        return true;
    }

    /**
     * Returns whether a Vault economy provider is currently available.
     *
     * @return {@code true} if economy operations can be performed
     */
    public static boolean isAvailable() {
        return economy != null;
    }

    /**
     * Checks whether the player has at least the given amount in their economy balance.
     *
     * @param player the player to check
     * @param amount the required amount
     * @return {@code true} if the player can afford the amount
     */
    public static boolean has(Player player, double amount) {
        boolean result = economy.has(player, amount);
        DiagnosticUtils.debug("ECONOMY", "has(" + player.getName() + ", " + amount + ") → " + result);
        return result;
    }

    /**
     * Withdraws the given amount from the player's economy balance.
     *
     * @param player the player to charge
     * @param amount the amount to withdraw
     * @return the {@link EconomyResponse} from the economy plugin
     */
    public static EconomyResponse withdraw(Player player, double amount) {
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        String detail = response.errorMessage != null && !response.errorMessage.isEmpty() ? " (" + response.errorMessage + ")" : "";
        DiagnosticUtils.debug("ECONOMY", "withdraw(" + player.getName() + ", " + amount + ") → " + response.type.name() + detail);
        return response;
    }

    /**
     * Formats the given amount as a currency string using the economy plugin's
     * own formatting (e.g. {@code "$100.00"} or {@code "100 coins"}).
     *
     * @param amount the amount to format
     * @return the formatted currency string
     */
    public static String format(double amount) {
        if (economy == null) return String.valueOf(amount);
        return economy.format(amount);
    }

    /**
     * Clears the cached economy provider reference.
     * Should be called during plugin shutdown.
     */
    public static void reset() {
        economy = null;
    }
}
