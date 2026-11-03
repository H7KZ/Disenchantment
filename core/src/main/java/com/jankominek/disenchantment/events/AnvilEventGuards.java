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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static utility class holding shared guard and helper logic used by the four
 * anvil event handlers: {@link DisenchantEvent}, {@link ShatterEvent},
 * {@link DisenchantClickEvent}, and {@link ShatterClickEvent}.
 * <p>
 * Almost all methods are pure functions — callers supply the data and decide what to do with
 * the result. The only class state is per-anvil / per-player bookkeeping (pending blank-book
 * removals, cached "Too Expensive!" bypass costs).
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
        // Raw slot 2 is the anvil's result slot. getSlot() is relative to the clicked inventory,
        // so it also returns 2 for the third hotbar/inventory slot of the player's own inventory.
        if (e.getRawSlot() != 2) return false;
        // We do not want to continue if the player has an item in cursor as it would delete it.
        return p.getItemOnCursor().getType().isAir();
    }

    /**
     * Returns {@code true} when the click on the anvil result slot is a type that could
     * <em>duplicate</em> the result item, and therefore must be blocked.
     * <p>
     * The click handlers deliver the result by calling {@code p.setItemOnCursor(result)}
     * without cancelling the event on the normal-pickup path. Any click that also makes
     * vanilla move a <em>second</em> copy of the result elsewhere (into the inventory, a
     * hotbar slot, the off-hand, or the world) yields two books from one operation — an
     * item-duplication exploit. Rather than enumerate the dangerous click types, this uses a
     * <strong>whitelist</strong>: only a plain left- or right-click pickup is permitted, so any
     * present or future non-pickup action (shift-click, number-key swap, off-hand swap,
     * double-click collect, drops, middle/creative clone, …) is treated as unsafe. A {@code null}
     * click only occurs under mocked tests; it is treated as safe so it does not block normal
     * handling there. Callers must {@code setCancelled(true)} and return when this returns
     * {@code true}, and only after confirming the click is a genuine plugin operation (otherwise
     * unrelated vanilla anvil result-slot interactions would be blocked).
     */
    public static boolean isUnsafeResultClick(InventoryClickEvent e) {
        ClickType click = e.getClick();
        if (click == null) return false;
        return click != ClickType.LEFT && click != ClickType.RIGHT;
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

        // Always collect the vanilla enchantments first, THEN overlay any activated adapters.
        // A namespace-scoped adapter (e.g. Vane) only reports its own enchantments and returns
        // an empty list for a normal item's minecraft:* enchantments — so gating the vanilla
        // collector on "no plugins active" dropped those enchantments entirely whenever Vane was
        // present, leaving the anvil output empty (issue #73). Deduplicate by key; vanilla seeds
        // each key first (putIfAbsent) and plugin adapters overlay on collision (put) so an
        // adapter that owns a key keeps its custom add-to-book/lore handling.
        Map<String, IPluginEnchantment> seen = new LinkedHashMap<>();

        for (IPluginEnchantment enc : baseCollector.collect(firstItem, secondItem, isPrepare)) {
            seen.putIfAbsent(enc.getKey(), enc);
        }

        for (ISupportedPlugin activatedPlugin : activatedPlugins) {
            for (IPluginEnchantment enc : pluginCollector.collect(firstItem, secondItem, isPrepare, activatedPlugin, world)) {
                seen.put(enc.getKey(), enc);
            }
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
     * permission. Silent: never messages, so it is safe to call from {@code PrepareAnvilEvent}
     * handlers (which fire on every anvil keystroke). Prepare handlers must only call it
     * <em>after</em> confirming a genuine plugin operation, then null the result.
     */
    public static boolean isMaintenanceActiveFor(Player p) {
        if (!Disenchantment.maintenanceEnabled) return false;
        return !PermissionType.MAINTENANCE_BYPASS.hasPermission(p);
    }

    /**
     * Same check as {@link #isMaintenanceActiveFor(Player)} but also sends the maintenance
     * message when blocking. Intended for the click path only, once per click, and only after a
     * genuine plugin operation was confirmed. Callers are responsible for exiting the handler.
     */
    public static boolean isMaintenanceBlocked(Player p) {
        if (!isMaintenanceActiveFor(p)) return false;

        FeedbackUtils.refuse(p, I18n.Messages.maintenanceActive());
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
        if (!isOnCooldownQuiet(p)) return false;

        long remaining = CooldownManager.getRemainingSeconds(p);
        FeedbackUtils.refuse(p, I18n.Messages.cooldownActive(String.valueOf(remaining)));
        return true;
    }

    /**
     * Same check as {@link #isOnCooldown(Player)} but never messages. Used by the
     * {@code PrepareAnvilEvent} handlers, which fire on every anvil keystroke and only need to
     * clear the result; the click handlers inform the player once per attempt.
     */
    public static boolean isOnCooldownQuiet(Player p) {
        return CooldownManager.isOnCooldown(p);
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
        return processEconomyCost(p, config, config.getCost());
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
        INSUFFICIENT_FUNDS,
        /**
         * The economy provider refused or failed the withdrawal even though the balance check
         * passed (e.g. a bank limit or a provider error). The player was not charged.
         */
        WITHDRAW_FAILED
    }

    /**
     * Same as {@link #processEconomy(Player, EconomyConfig)} but charges an explicit
     * {@code cost} instead of {@link EconomyConfig#getCost()} — used when the effective
     * cost has been recalculated from per-enchantment economy overrides.
     * <p>
     * The provider's {@link net.milkbowl.vault.economy.EconomyResponse} is honoured: an
     * unsuccessful (or missing) response yields {@link EconomyResult#WITHDRAW_FAILED} so the
     * caller never completes an operation the player did not pay for.
     */
    public static EconomyResult processEconomyCost(Player p, EconomyConfig config, double cost) {
        if (!config.isEnabled() || p.getGameMode() == GameMode.CREATIVE) return EconomyResult.OK;

        if (!EconomyUtils.isAvailable()) return EconomyResult.NOT_AVAILABLE;

        if (!EconomyUtils.has(p, cost)) return EconomyResult.INSUFFICIENT_FUNDS;

        net.milkbowl.vault.economy.EconomyResponse response = EconomyUtils.withdraw(p, cost);
        if (response == null || !response.transactionSuccess()) {
            DiagnosticUtils.debug("ECONOMY", () -> "processEconomy: withdraw of " + cost + " failed for " + p.getName()
                    + (response != null ? " (" + response.errorMessage + ")" : " (null response)"));
            return EconomyResult.WITHDRAW_FAILED;
        }

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
        return switch (eventType) {
            case DISENCHANTMENT -> Config.Disenchantment.Anvil.isBypassTooExpensiveEnabled();
            case SHATTERMENT -> Config.Shatterment.Anvil.isBypassTooExpensiveEnabled();
            case ERASE -> Config.Erase.Anvil.isBypassTooExpensiveEnabled();
        };
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
     * A blank-book consumption that has been scheduled but not yet applied to an anvil.
     *
     * @param book        snapshot of the slot-1 stack at click time (amount = amount before the operation)
     * @param originClick the click event that started the operation, so the lock listener never cancels it
     */
    private record PendingRemoval(ItemStack book, Object originClick) {
    }

    // Weak keys: an anvil inventory nobody references any more must not be kept alive by this map.
    // Synchronized rather than concurrent because WeakHashMap permits no concurrent variant; Folia
    // region threads may touch it.
    private static final Map<Inventory, PendingRemoval> pendingRemovals = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Returns {@code true} while a blank-book consumption is still pending for {@code inventory}.
     * A second plugin operation must be refused during that window: the first one has not finished
     * paying for itself yet (the book is still in slot 1).
     */
    public static boolean isOperationPending(Inventory inventory) {
        return inventory != null && pendingRemovals.containsKey(inventory);
    }

    /**
     * Returns {@code true} when {@code inventory} has a pending consumption that was NOT started by
     * {@code click}. The lock listener cancels such clicks so the player cannot move the books or the
     * source item out of the anvil between the operation and its deferred book removal.
     */
    public static boolean isLockedFor(Inventory inventory, Object click) {
        if (inventory == null) return false;
        PendingRemoval pending = pendingRemovals.get(inventory);
        return pending != null && pending.originClick() != click;
    }

    /**
     * Schedules the deferred consumption of one blank book from anvil slot 1.
     *
     * @see #scheduleSecondItemRemoval(Player, AnvilInventory, ItemStack, Object)
     */
    public static void scheduleSecondItemRemoval(Player p, AnvilInventory anvilInventory, ItemStack secondItem) {
        scheduleSecondItemRemoval(p, anvilInventory, secondItem, null);
    }

    /**
     * Schedules the deferred consumption of one blank book from anvil slot 1 and marks the anvil as
     * "operation pending" until it ran.
     * <p>
     * <strong>Why deferred.</strong> The click handlers run before vanilla finishes the take, which
     * itself clears slot 1, and EnchantsSquared additionally nulls slot 1 one tick later (commit
     * e86f4a2). A synchronous write is therefore overwritten; the stack is re-written 2 ticks later
     * with one book fewer. It cannot be done synchronously without losing the remaining books.
     * <p>
     * <strong>Closing the window.</strong> Until it ran, the anvil is registered as pending: new
     * operations on it are refused ({@link #isOperationPending}), the lock listener cancels other
     * clicks/drags on it ({@link #isLockedFor}), and closing the anvil settles the removal
     * immediately ({@link #settlePendingRemoval}) before the contents are handed back to the player.
     * At settle time slot 1 is only touched if it is empty or still holds the same book type, so
     * nothing foreign is overwritten and no stale copy is resurrected.
     *
     * @param p           the player (owns the scheduled task on Folia)
     * @param secondItem  the slot-1 stack at click time
     * @param originClick the click that started the operation (exempt from the lock), may be null
     */
    public static void scheduleSecondItemRemoval(Player p, AnvilInventory anvilInventory, ItemStack secondItem, Object originClick) {
        pendingRemovals.put(anvilInventory, new PendingRemoval(secondItem.clone(), originClick));
        // Schedule task to run 2 ticks after the event
        // It is because of EnchantsSquared (they replace second slot to null after 1 tick)
        SchedulerUtils.runForEntityLater(Disenchantment.plugin, p, () -> settlePendingRemoval(anvilInventory), 2L);
    }

    /**
     * Applies the pending blank-book consumption for {@code inventory} right now, if there is one.
     * Idempotent: the deferred task and the inventory-close listener both call it and only the first
     * does anything.
     *
     * @return {@code true} if a pending removal was applied
     */
    public static boolean settlePendingRemoval(Inventory inventory) {
        if (inventory == null) return false;
        PendingRemoval pending = pendingRemovals.remove(inventory);
        if (pending == null) return false;

        int remaining = pending.book().getAmount() - 1;
        ItemStack current = inventory.getItem(1);
        boolean empty = current == null || current.getType().isAir();

        if (!empty && !current.isSimilar(pending.book())) {
            DiagnosticUtils.debug("ANVIL", () -> "Second-item removal: slot 1 now holds " + current.getType() + " → left untouched");
            return true;
        }

        // Never write back more than what is there or what remains after consuming one.
        int target = empty ? remaining : Math.min(current.getAmount(), remaining);
        if (target > 0) {
            ItemStack restored = pending.book().clone();
            restored.setAmount(target);
            inventory.setItem(1, restored);
        } else {
            inventory.setItem(1, null);
        }
        return true;
    }
}
