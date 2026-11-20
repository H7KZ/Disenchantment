package com.jankominek.disenchantment.e2e.support.vault;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Test double of Vault for the e2e suite: it carries the VaultAPI classes and registers one in-memory economy. Balances
 * are read from and written to this plugin's live config at {@code balances.<lowercase name>}, so a test seeds a balance
 * with {@code PluginHandle.patchConfig} and reads the server's truth back with {@code PluginHandle.config}.
 * The Economy interface is implemented by a dynamic proxy so only the calls the plugin under test uses are spelled out.
 */
public final class E2eVault extends JavaPlugin {

    @Override
    public void onEnable() {
        Economy economy = (Economy) Proxy.newProxyInstance(Economy.class.getClassLoader(), new Class<?>[]{Economy.class},
                new Handler());
        getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.Highest);
    }

    private final class Handler implements InvocationHandler {

        @Override
        public synchronized Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "isEnabled":
                case "hasAccount":
                case "createPlayerAccount":
                    return true;
                case "getName":
                    return "E2eEconomy";
                case "hasBankSupport":
                    return false;
                case "fractionalDigits":
                    return 2;
                case "format":
                    return "$" + String.format(java.util.Locale.ROOT, "%.2f", (Double) args[0]);
                case "currencyNamePlural":
                case "currencyNameSingular":
                    return "dollars";
                case "getBalance":
                    return balance(nameOf(args[0]));
                case "has":
                    return balance(nameOf(args[0])) >= (Double) args[args.length - 1];
                case "withdrawPlayer": {
                    String name = nameOf(args[0]);
                    double amount = (Double) args[args.length - 1];
                    double current = balance(name);
                    if (amount < 0 || current < amount) {
                        return new EconomyResponse(0, current, ResponseType.FAILURE, "insufficient funds");
                    }
                    set(name, current - amount);
                    return new EconomyResponse(amount, current - amount, ResponseType.SUCCESS, null);
                }
                case "depositPlayer": {
                    String name = nameOf(args[0]);
                    double amount = (Double) args[args.length - 1];
                    double current = balance(name);
                    if (amount < 0) {
                        return new EconomyResponse(0, current, ResponseType.FAILURE, "negative deposit");
                    }
                    set(name, current + amount);
                    return new EconomyResponse(amount, current + amount, ResponseType.SUCCESS, null);
                }
                default:
                    throw new UnsupportedOperationException("E2eVault does not implement Economy#" + method.getName());
            }
        }
    }

    private static String nameOf(Object player) {
        String name = player instanceof OfflinePlayer offline ? offline.getName() : String.valueOf(player);
        return name.toLowerCase(java.util.Locale.ROOT);
    }

    private double balance(String name) {
        return getConfig().getDouble("balances." + name, 0.0);
    }

    private void set(String name, double value) {
        getConfig().set("balances." + name, value);
    }
}
