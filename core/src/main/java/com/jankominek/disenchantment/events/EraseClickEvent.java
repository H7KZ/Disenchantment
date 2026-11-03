package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.events.api.PostEraseEvent;
import com.jankominek.disenchantment.events.api.PreEraseEvent;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.*;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the {@link InventoryClickEvent} when a player clicks the result slot of an anvil during an
 * erase operation.
 * <p>
 * Unlike the book operations this uses the <strong>manual-result pattern</strong>: the click is
 * cancelled and the slots and the cursor are mutated synchronously. The disenchant/shatter flow only
 * works because an enchanted book cannot stack, so vanilla's own pick-up of the result is a no-op once
 * the plugin has put the book on the cursor. An erase result is an ordinary (possibly stackable) item,
 * where vanilla would merge a second copy onto the cursor — an item dupe. Cancelling removes any
 * dependence on vanilla's take, and the catalyst is decremented in the same tick, so no timing window
 * remains.
 */
public class EraseClickEvent {
    /**
     * Entry point for the erase inventory-click event.
     * Delegates to the internal handler and reports any exceptions via diagnostics.
     *
     * @param event the Bukkit event to process
     */
    public static void onEvent(Event event) {
        try {
            handler(event);
        } catch (Exception e) {
            DiagnosticUtils.throwReport(e);
        }
    }

    private static final AnvilEventGuards.EconomyConfig ECONOMY_CONFIG = new AnvilEventGuards.EconomyConfig() {
        @Override
        public boolean isEnabled() {
            return Config.Erase.Economy.isEnabled();
        }

        @Override
        public double getCost() {
            return Config.Erase.Economy.getCost();
        }

        @Override
        public boolean isChargeMessageEnabled() {
            return Config.Erase.Economy.isChargeMessageEnabled();
        }
    };

    private static void handler(Event event) {
        if (!(event instanceof InventoryClickEvent e)) return;

        Player p = AnvilEventGuards.getPlayer(e);
        if (p == null) return;

        if (!Config.isPluginEnabled() || !EraseUtils.isActive()) return;

        if (!AnvilEventGuards.isAnvilResultSlotClick(e, p)) return;

        if (AnvilEventGuards.isWorldBlocked(p, Config.Disenchantment.isWorldRestricted(p.getWorld())))
            return;

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        ItemStack firstItem = anvilInventory.getItem(0);
        ItemStack secondItem = anvilInventory.getItem(1);

        if (firstItem == null) return;
        if (secondItem == null) return;

        List<IPluginEnchantment> enchantments = AnvilEventGuards.collectEnchantments(
                firstItem, secondItem, false,
                EraseUtils::getErasableEnchantments,
                EraseUtils::getErasableEnchantments,
                p.getWorld(), p);

        if (enchantments.isEmpty()) {
            DiagnosticUtils.debug("ERASE", "Click: no erasable enchantments → exit");
            return;
        }

        // Fail closed: the gate may have changed since Prepare (anvil unmarked/broken, block below
        // removed, reload). Never let vanilla hand over a result the plugin prepared.
        if (!AnvilEventGuards.isAnvilAllowed(e.getInventory(), p)) {
            ItemStack staleResult = anvilInventory.getItem(2);
            if (staleResult != null && staleResult.getType() == firstItem.getType()) {
                DiagnosticUtils.debug("ERASE", "Click: not a special anvil but a plugin result is present → CANCELLED, result cleared");
                e.setCancelled(true);
                anvilInventory.setItem(2, null);
            }
            return;
        }

        // Genuine plugin operation. Maintenance mode nulls the prepared result, so this must run
        // before the result-slot checks: the player clicks an empty slot and is told why.
        if (AnvilEventGuards.isMaintenanceBlocked(p)) {
            e.setCancelled(true);
            return;
        }

        ItemStack result = anvilInventory.getItem(2);

        if (result == null) return;

        // The prepared result is a copy of the source item; anything else in the slot is not ours.
        if (result.getType() != firstItem.getType()) return;

        DiagnosticUtils.debug("ERASE", () -> "Click: player=" + p.getName() + ", result=" + result.getType() + ", gameMode=" + p.getGameMode());

        // Genuine erase operation confirmed. Block non-standard result-slot clicks
        // (shift/number-key/swap/double-click/drop/etc.); nothing has been touched yet, so cancelling is enough.
        if (AnvilEventGuards.isUnsafeResultClick(e)) {
            DiagnosticUtils.debug("ERASE", () -> "Click: unsafe click type " + e.getClick() + " → CANCELLED");
            e.setCancelled(true);
            FeedbackUtils.refuse(p, I18n.Messages.useNormalClick());
            return;
        }

        // A previous operation on this anvil has not finished consuming its second item yet.
        if (AnvilEventGuards.isOperationPending(anvilInventory)) {
            DiagnosticUtils.debug("ERASE", "Click: operation still pending on this anvil → CANCELLED");
            e.setCancelled(true);
            return;
        }

        if (AnvilEventGuards.isOnCooldown(p)) {
            DiagnosticUtils.debug("ERASE", "Click: player on cooldown → CANCELLED");
            e.setCancelled(true);
            return;
        }

        int repairCost = AnvilEventGuards.peekBypassCost(p, AnvilCostUtils.getRepairCost(anvilInventory, e.getView()));
        DiagnosticUtils.debug("ERASE", () -> "Click: xp check — repairCost=" + repairCost + ", playerLevel=" + p.getLevel());
        if (!AnvilEventGuards.hasEnoughXp(p, repairCost)) {
            DiagnosticUtils.debug("ERASE", "Click: insufficient XP → CANCELLED");
            FeedbackUtils.refuseNotEnoughXp(p, repairCost);
            e.setCancelled(true);
            return;
        }

        // Cancel rather than exit: the result is only in the slot because Prepare ran, so a player who
        // lost the permission since must not be handed the vanilla pick-up of it.
        if (!PermissionGroupType.ERASE_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("ERASE", "Click: permission denied → CANCELLED");
            e.setCancelled(true);
            return;
        }

        PreEraseEvent preEvent = new PreEraseEvent(p, firstItem.clone(), new ArrayList<>(enchantments), secondItem.clone());
        org.bukkit.Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            e.setCancelled(true);
            return;
        }

        List<IPluginEnchantment> erased = new ArrayList<>(preEvent.getEnchantments());
        if (erased.isEmpty()) {
            DiagnosticUtils.debug("ERASE", "Click: listeners removed every enchantment → CANCELLED");
            e.setCancelled(true);
            return;
        }

        // Economy check — runs after PreEraseEvent so cancellation doesn't charge the player
        DiagnosticUtils.debug("ERASE", () -> "Click: economy check — enabled=" + Config.Erase.Economy.isEnabled() + ", gameMode=" + p.getGameMode());
        double economyCost = AnvilCostUtils.economyCostForEnchantments(
                erased, Config.Erase.Economy.getCost(), Config.Erase.Anvil.Repair.getEnchantmentEconomyCosts());
        AnvilEventGuards.EconomyResult economyResult = AnvilEventGuards.processEconomyCost(p, ECONOMY_CONFIG, economyCost);
        if (economyResult == AnvilEventGuards.EconomyResult.NOT_AVAILABLE) {
            DiagnosticUtils.debug("ERASE", "Click: economy not available → CANCELLED");
            FeedbackUtils.refuse(p, I18n.Messages.economyNotAvailable());
            e.setCancelled(true);
            return;
        }
        if (economyResult == AnvilEventGuards.EconomyResult.INSUFFICIENT_FUNDS) {
            DiagnosticUtils.debug("ERASE", "Click: insufficient funds → CANCELLED");
            FeedbackUtils.refuse(p, I18n.Messages.economyInsufficientFunds(EconomyUtils.format(economyCost)));
            e.setCancelled(true);
            return;
        }
        if (economyResult == AnvilEventGuards.EconomyResult.WITHDRAW_FAILED) {
            DiagnosticUtils.debug("ERASE", "Click: economy withdrawal failed → CANCELLED");
            FeedbackUtils.refuse(p, I18n.Messages.economyWithdrawFailed());
            e.setCancelled(true);
            return;
        }

        AnvilEventGuards.clearBypassCost(p);

        int exp = p.getLevel() - repairCost;
        DiagnosticUtils.debug("ERASE", () -> "Click: xp → " + p.getLevel() + " - " + repairCost + " = " + exp);

        // ----------------------------------------------------------------------------------------------------
        // Manual result: nothing below may return before the whole mutation is done.

        // Rebuilt from the live source item instead of trusting the result slot, so the delivered item is
        // exactly the source minus what the listeners agreed to erase.
        ItemStack delivered = EraseUtils.buildResult(firstItem, erased);
        if (Config.Erase.Anvil.Repair.isResetEnabled()) AnvilCostUtils.setItemRepairCost(delivered, 0);

        e.setCancelled(true);

        anvilInventory.setItem(0, null);
        anvilInventory.setItem(1, consumeCatalyst(secondItem));
        anvilInventory.setItem(2, null);

        p.setItemOnCursor(delivered);

        boolean creative = p.getGameMode() == GameMode.CREATIVE;
        if (!creative) p.setLevel(exp);

        int xpCost = creative ? 0 : repairCost;
        double finalEconomyCost = (Config.Erase.Economy.isEnabled() && !creative) ? economyCost : 0.0;
        AnvilEventGuards.recordCooldownOperation(p);
        org.bukkit.Bukkit.getPluginManager().callEvent(new PostEraseEvent(p, delivered.clone(), List.copyOf(erased), xpCost, finalEconomyCost));

        if (Config.Erase.Anvil.Sound.isEnabled())
            p.playSound(
                    p.getLocation(),
                    Sound.BLOCK_ANVIL_USE,
                    Config.Erase.Anvil.Sound.getVolume().floatValue(),
                    Config.Erase.Anvil.Sound.getPitch().floatValue()
            );

        ParticleUtils.playErase(p, anvilInventory);

        p.updateInventory();

        if (DiagnosticUtils.isDebugEnabled()) {
            String names = erased.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("ERASE", "Click: erased=[" + names + "]");
        }
        DiagnosticUtils.debug("ERASE", "Click: complete ✓");
    }

    /**
     * Returns what slot 1 holds after one catalyst was used: one fewer, or {@code null} when it was the
     * last. The stack is returned unchanged when {@code erase.consume-catalyst} is off.
     */
    private static ItemStack consumeCatalyst(ItemStack catalyst) {
        if (!Config.Erase.isConsumeCatalyst()) return catalyst;
        if (catalyst.getAmount() <= 1) return null;

        ItemStack remaining = catalyst.clone();
        remaining.setAmount(catalyst.getAmount() - 1);
        return remaining;
    }
}
