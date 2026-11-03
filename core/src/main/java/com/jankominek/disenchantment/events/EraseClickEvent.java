package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.EraseUtils;
import com.jankominek.disenchantment.utils.FeedbackUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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

    private static void handler(Event event) {
        if (!(event instanceof InventoryClickEvent e)) return;

        Player p = AnvilEventGuards.getPlayer(e);
        if (p == null) return;

        if (!Config.isPluginEnabled() || !EraseUtils.isActive()) return;

        // Delivered manually, so an earlier veto (protection or shop plugin) must be honoured: vanilla will not
        // take a cancelled click and neither may we.
        if (e.isCancelled()) return;

        // The cursor is checked further down, once the result is known to be ours: a full cursor on a plugin
        // result has to be cancelled, not ignored.
        if (!AnvilEventGuards.isAnvilResultSlot(e)) return;

        if (AnvilEventGuards.isWorldBlocked(p, Config.Disenchantment.isWorldRestricted(p.getWorld())))
            return;

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        ItemStack firstItem = anvilInventory.getItem(0);
        ItemStack secondItem = anvilInventory.getItem(1);

        if (firstItem == null) return;
        if (secondItem == null) return;

        List<IPluginEnchantment> enchantments = AnvilOperations.collect(AnvilOperations.Kind.ERASE, p, firstItem, secondItem, false);

        if (enchantments.isEmpty()) {
            DiagnosticUtils.debug("ERASE", "Click: no erasable enchantments → exit");
            return;
        }

        // Fail closed: the gate may have changed since Prepare (anvil unmarked/broken, block below
        // removed, reload). Never let vanilla hand over a result the plugin prepared.
        if (!AnvilEventGuards.isAnvilAllowed(e.getInventory(), p)) {
            ItemStack staleResult = anvilInventory.getItem(2);
            // Only a result the plugin itself prepared is cleared. A vanilla repair result has the same material
            // as the source whenever the catalyst is also a repair material (diamond, iron ingot, ...).
            if (EraseUtils.isPreparedResult(staleResult)) {
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

        // Anything without the Prepare tag is not ours (e.g. a vanilla repair with a catalyst that is also a repair material).
        if (!EraseUtils.isPreparedResult(result)) return;

        DiagnosticUtils.debug("ERASE", () -> "Click: player=" + p.getName() + ", result=" + result.getType() + ", gameMode=" + p.getGameMode());

        // The result may stack (custom-enchant plugins), and vanilla would merge it onto a same-type cursor with
        // no charge. Cancel so it can never be taken that way.
        if (!p.getItemOnCursor().getType().isAir()) {
            DiagnosticUtils.debug("ERASE", "Click: cursor not empty → CANCELLED");
            e.setCancelled(true);
            return;
        }

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

        // Vanilla would refuse the take at 40+ levels; this handler delivers ahead of that check.
        if (AnvilEventGuards.refuseIfTooExpensive(p, AnvilEventType.ERASE, repairCost)) {
            DiagnosticUtils.debug("ERASE", "Click: too expensive → CANCELLED");
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

        // Manual-result pattern: cancel and write the slots and the cursor synchronously. Everything from the
        // Pre event on (economy, XP, the delivered item, cooldown, Post event, sound, particles) is shared
        // with the Bedrock GUI; only the slot writes below are specific to the anvil.
        AnvilOperations.Outcome outcome = AnvilOperations.settle(AnvilOperations.Kind.ERASE, p, firstItem, secondItem, result,
                enchantments, repairCost, anvilInventory, (newFirst, delivered) -> {
                    e.setCancelled(true);

                    anvilInventory.setItem(0, null);
                    anvilInventory.setItem(1, AnvilOperations.consumeCatalyst(secondItem));
                    anvilInventory.setItem(2, null);

                    p.setItemOnCursor(delivered);
                });

        if (outcome.status() == AnvilOperations.Status.CANCELLED) {
            e.setCancelled(true);
            return;
        }

        p.updateInventory();

        DiagnosticUtils.debug("ERASE", "Click: complete ✓");
    }
}
