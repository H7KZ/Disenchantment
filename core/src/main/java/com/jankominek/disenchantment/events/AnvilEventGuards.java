package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.AnvilEventType;
import com.jankominek.disenchantment.types.PermissionType;
import com.jankominek.disenchantment.utils.*;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static utility class holding shared guard and helper logic used by the four
 * anvil event handlers: {@link DisenchantEvent}, {@link ShatterEvent},
 * {@link DisenchantClickEvent}, and {@link ShatterClickEvent}.
 * <p>
 * All methods are pure functions with no side-effects on class state — callers
 * supply the data and decide what to do with the result.
 */
public final class AnvilEventGuards {

    private AnvilEventGuards() {
    }

    // ----------------------------------------------------------------------------------------------------
    // InventoryClickEvent shared guards

    /**
     * Extracts the {@link Player} from an {@link InventoryClickEvent}, or returns
     * {@code null} if the clicker is not a player.
     */
    public static Player getPlayer(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return null;
        return p;
    }

    /**
     * Returns {@code true} when the click is on slot 2 of an anvil inventory and
     * the player's cursor is empty — the conditions that indicate a result pickup.
     */
    public static boolean isAnvilResultSlotClick(InventoryClickEvent e, Player p) {
        if (e.getInventory().getType() != InventoryType.ANVIL) return false;
        if (e.getSlot() != 2) return false;
        // We do not want to continue if the player has an item in cursor as it would delete it.
        return p.getItemOnCursor().getType().isAir();
    }

    // ----------------------------------------------------------------------------------------------------
    // Plugin enchantment collection

    /**
     * Functional interface for the two EventUtils collection methods that accept
     * either a no-plugin or a per-plugin overload.
     */
    @FunctionalInterface
    public interface EnchantmentCollector {
        List<IPluginEnchantment> collect(ItemStack first, ItemStack second, boolean isPrepare);
    }

    /**
     * Functional interface for the per-plugin overload of collection methods.
     */
    @FunctionalInterface
    public interface PluginEnchantmentCollector {
        List<IPluginEnchantment> collect(ItemStack first, ItemStack second, boolean isPrepare, ISupportedPlugin plugin, World world);
    }

    /**
     * Collects all eligible enchantments for a given anvil slot combination, iterating
     * over all activated third-party plugins (or falling back to vanilla logic when none
     * are active). The {@code isPrepare} flag distinguishes PrepareAnvil (true) from
     * InventoryClick (false) calls.
     *
     * @param firstItem       item in anvil slot 0
     * @param secondItem      item in anvil slot 1 (may be null)
     * @param isPrepare       {@code true} for PrepareAnvilEvent, {@code false} for InventoryClickEvent
     * @param baseCollector   vanilla/no-plugin collector
     * @param pluginCollector per-plugin collector
     * @param world           the player's current world
     * @return mutable list of collected enchantments (may be empty)
     */
    public static List<IPluginEnchantment> collectEnchantments(
            ItemStack firstItem,
            ItemStack secondItem,
            boolean isPrepare,
            EnchantmentCollector baseCollector,
            PluginEnchantmentCollector pluginCollector,
            World world) {

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();
        List<IPluginEnchantment> enchantments = new ArrayList<>();

        if (activatedPlugins.isEmpty()) {
            enchantments.addAll(baseCollector.collect(firstItem, secondItem, isPrepare));
        } else {
            for (ISupportedPlugin activatedPlugin : activatedPlugins) {
                enchantments.addAll(pluginCollector.collect(firstItem, secondItem, isPrepare, activatedPlugin, world));
            }
        }

        // Deduplicate by key — first occurrence wins. Prevents double-cost/double-removal
        // when multiple adapters claim the same enchantment key.
        Map<String, IPluginEnchantment> seen = new LinkedHashMap<>();
        for (IPluginEnchantment enc : enchantments) {
            seen.putIfAbsent(enc.getKey(), enc);
        }
        return new ArrayList<>(seen.values());
    }

    /**
     * Same as {@link #collectEnchantments(ItemStack, ItemStack, boolean, EnchantmentCollector, PluginEnchantmentCollector, World)}
     * but honors {@code disenchantment.bypass.material} for {@code p} while collecting.
     */
    public static List<IPluginEnchantment> collectEnchantments(
            ItemStack firstItem,
            ItemStack secondItem,
            boolean isPrepare,
            EnchantmentCollector baseCollector,
            PluginEnchantmentCollector pluginCollector,
            World world,
            Player p) {

        EventUtils.setMaterialBypass(PermissionType.BYPASS_MATERIAL.hasPermission(p));
        try {
            return collectEnchantments(firstItem, secondItem, isPrepare, baseCollector, pluginCollector, world);
        } finally {
            EventUtils.setMaterialBypass(false);
        }
    }

    // ----------------------------------------------------------------------------------------------------
    // Maintenance guard

    /**
     * Returns {@code true} when maintenance mode is active and the player lacks the bypass
     * permission. Sends the maintenance message when blocking. Callers are responsible for
     * exiting the handler (without cancelling — mirrors how the disabled-world/material checks
     * silently no-op rather than cancel).
     */
    public static boolean isMaintenanceBlocked(Player p) {
        if (!Disenchantment.maintenanceEnabled) return false;
        if (PermissionType.MAINTENANCE_BYPASS.hasPermission(p)) return false;

        p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.maintenanceActive());
        return true;
    }

    // ----------------------------------------------------------------------------------------------------
    // World bypass guard

    /**
     * Returns {@code true} when the player's world is disabled for the given feature AND the
     * player lacks {@code disenchantment.bypass.world}.
     */
    public static boolean isWorldBlocked(Player p, boolean disabledForWorld) {
        if (!disabledForWorld) return false;
        return !PermissionType.BYPASS_WORLD.hasPermission(p);
    }

    // ----------------------------------------------------------------------------------------------------
    // Cooldown guard

    /**
     * Checks whether the player is currently on cooldown for anvil operations. When on
     * cooldown, sends the remaining-time message to the player. Callers are responsible
     * for cancelling the event when this returns {@code true}.
     *
     * @param p the player to check
     * @return {@code true} if the player must be blocked from proceeding
     */
    public static boolean isOnCooldown(Player p) {
        if (!CooldownManager.isOnCooldown(p)) return false;

        long remaining = CooldownManager.getRemainingSeconds(p);
        p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.cooldownActive(String.valueOf(remaining)));
        return true;
    }

    /**
     * Records that the player just completed an anvil operation, starting a new cooldown.
     */
    public static void recordCooldownOperation(Player p) {
        CooldownManager.recordOperation(p);
    }

    // ----------------------------------------------------------------------------------------------------
    // XP guard

    /**
     * Returns {@code true} when the player has sufficient XP (or is in creative mode)
     * to pay {@code repairCost} levels.
     */
    public static boolean hasEnoughXp(Player p, int repairCost) {
        return p.getGameMode() == GameMode.CREATIVE || repairCost <= p.getLevel();
    }

    // ----------------------------------------------------------------------------------------------------
    // Economy guard

    /**
     * Represents the economy configuration for one feature (Disenchantment or Shatterment).
     */
    public interface EconomyConfig {
        boolean isEnabled();

        double getCost();

        boolean isChargeMessageEnabled();

        /**
         * Only relevant for PrepareAnvil (action bar display). Defaults to {@code false}.
         */
        default boolean isShowCostEnabled() {
            return false;
        }
    }

    /**
     * Checks whether the player can afford the economy cost and, if so, withdraws it.
     * <p>
     * Returns {@code EconomyResult.OK} on success.  Returns one of the failure variants
     * when the operation should be cancelled; the caller is responsible for cancelling
     * the event and sending any required message.
     *
     * @param p      the player
     * @param config economy config for the active feature
     * @return result indicating what action (if any) the caller must take
     */
    public static EconomyResult processEconomy(Player p, EconomyConfig config) {
        if (!config.isEnabled() || p.getGameMode() == GameMode.CREATIVE) return EconomyResult.OK;

        if (!EconomyUtils.isAvailable()) return EconomyResult.NOT_AVAILABLE;

        double cost = config.getCost();
        if (!EconomyUtils.has(p, cost)) return EconomyResult.INSUFFICIENT_FUNDS;

        EconomyUtils.withdraw(p, cost);
        if (config.isChargeMessageEnabled()) {
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.economyCharged(EconomyUtils.format(cost)));
        }
        return EconomyResult.OK;
    }

    /**
     * Result codes returned by {@link #processEconomy} and {@link #processEconomyCost}.
     */
    public enum EconomyResult {
        /**
         * Economy check passed and the player was charged (or economy is disabled).
         */
        OK,
        /**
         * No Vault/economy provider is hooked up.
         */
        NOT_AVAILABLE,
        /**
         * Player does not have enough money to cover the cost.
         */
        INSUFFICIENT_FUNDS
    }

    /**
     * Same as {@link #processEconomy(Player, EconomyConfig)} but charges an explicit
     * {@code cost} instead of {@link EconomyConfig#getCost()} — used when the effective
     * cost has been recalculated from per-enchantment economy overrides.
     */
    public static EconomyResult processEconomyCost(Player p, EconomyConfig config, double cost) {
        if (!config.isEnabled() || p.getGameMode() == GameMode.CREATIVE) return EconomyResult.OK;

        if (!EconomyUtils.isAvailable()) return EconomyResult.NOT_AVAILABLE;

        if (!EconomyUtils.has(p, cost)) return EconomyResult.INSUFFICIENT_FUNDS;

        EconomyUtils.withdraw(p, cost);
        if (config.isChargeMessageEnabled()) {
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.economyCharged(EconomyUtils.format(cost)));
        }
        return EconomyResult.OK;
    }

    // ----------------------------------------------------------------------------------------------------
    // Economy action bar (PrepareAnvil)

    /**
     * Sends the economy cost action bar to the player when economy display conditions are met.
     */
    public static void showEconomyActionBar(Player p, EconomyConfig config) {
        showEconomyActionBarCost(p, config, config.getCost());
    }

    /**
     * Same as {@link #showEconomyActionBar(Player, EconomyConfig)} but displays an explicit
     * {@code cost} instead of {@link EconomyConfig#getCost()}.
     */
    public static void showEconomyActionBarCost(Player p, EconomyConfig config, double cost) {
        if (config.isEnabled()
                && EconomyUtils.isAvailable()
                && config.isShowCostEnabled()
                && p.getGameMode() != GameMode.CREATIVE) {
            p.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(
                    I18n.Messages.economyCost(EconomyUtils.format(cost))
            ));
        }
    }

    // ----------------------------------------------------------------------------------------------------
    // PrepareAnvil cost + scheduled refresh

    /**
     * Sets the anvil repair cost immediately and schedules a follow-up refresh
     * (required to work around client-side caching).
     *
     * @param e                  the PrepareAnvilEvent
     * @param p                  the viewing player
     * @param pluginEnchantments enchantments whose cost will be recalculated in the scheduled task
     * @param eventType          DISENCHANTMENT or SHATTERMENT
     */
    public static void applyAnvilCostAndSchedule(
            org.bukkit.event.inventory.PrepareAnvilEvent e,
            Player p,
            List<IPluginEnchantment> pluginEnchantments,
            AnvilEventType eventType) {

        int anvilCost = AnvilCostUtils.countAnvilCost(pluginEnchantments, eventType, p);
        applyDisplayedCost(e.getInventory(), e.getView(), p, eventType, anvilCost);

        SchedulerUtils.runForEntity(Disenchantment.plugin, p, () -> {
            int refreshedCost = AnvilCostUtils.countAnvilCost(pluginEnchantments, eventType, p);
            applyDisplayedCost(e.getInventory(), e.getView(), p, eventType, refreshedCost);
            p.updateInventory();
        });
    }

    // ----------------------------------------------------------------------------------------------------
    // "Too Expensive!" bypass

    // ponytail: ConcurrentHashMap for Folia region-thread safety
    private static final Map<UUID, Integer> bypassCostCache = new ConcurrentHashMap<>();

    private static boolean isBypassEnabled(AnvilEventType eventType) {
        return eventType == AnvilEventType.DISENCHANTMENT
                ? Config.Disenchantment.Anvil.isBypassTooExpensiveEnabled()
                : Config.Shatterment.Anvil.isBypassTooExpensiveEnabled();
    }

    // Vanilla blocks the anvil result slot ("Too Expensive!") at repair cost >= 40.
    private static final int VANILLA_TOO_EXPENSIVE_THRESHOLD = 40;
    private static final int BYPASS_DISPLAYED_COST = 1;

    private static void applyDisplayedCost(AnvilInventory anvilInventory, org.bukkit.inventory.InventoryView inventoryView, Player p, AnvilEventType eventType, int realCost) {
        if (isBypassEnabled(eventType) && realCost >= VANILLA_TOO_EXPENSIVE_THRESHOLD) {
            bypassCostCache.put(p.getUniqueId(), realCost);
            AnvilCostUtils.setAnvilRepairCostBypass(anvilInventory, inventoryView, BYPASS_DISPLAYED_COST);
        } else {
            bypassCostCache.remove(p.getUniqueId());
            AnvilCostUtils.setAnvilRepairCost(anvilInventory, inventoryView, realCost);
        }
    }

    /**
     * Returns the cached real cost for {@code p} if a "Too Expensive!" bypass is active,
     * otherwise {@code displayedCost} unchanged. Does not clear the cache — call
     * {@link #clearBypassCost(Player)} once the operation actually completes.
     */
    public static int peekBypassCost(Player p, int displayedCost) {
        Integer cached = bypassCostCache.get(p.getUniqueId());
        return cached != null ? cached : displayedCost;
    }

    /**
     * Clears the cached bypass cost for {@code p}, e.g. after the anvil result has been
     * collected and the real XP cost charged.
     */
    public static void clearBypassCost(Player p) {
        bypassCostCache.remove(p.getUniqueId());
    }

    // ----------------------------------------------------------------------------------------------------
    // Second-item removal (InventoryClickEvent)

    /**
     * Schedules a 2-tick deferred reduction/removal of the item in anvil slot 1.
     * The delay is required because EnchantsSquared replaces slot 1 with null after 1 tick.
     */
    public static void scheduleSecondItemRemoval(Player p, AnvilInventory anvilInventory, ItemStack secondItem) {
        ItemStack finalSecondItem = secondItem.clone();
        // Schedule task to run 2 ticks after the event
        // It is because of EnchantsSquared (they replace second slot to null after 1 tick)
        SchedulerUtils.runForEntityLater(Disenchantment.plugin, p, () -> {
            if (finalSecondItem.getAmount() > 1) {
                finalSecondItem.setAmount(finalSecondItem.getAmount() - 1);
                anvilInventory.setItem(1, finalSecondItem);
            } else {
                anvilInventory.setItem(1, null);
            }
        }, 2L);
    }
}
