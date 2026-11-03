package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.FeedbackUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the {@link InventoryClickEvent} when a player clicks the result slot
 * of an anvil during a shatterment (book-splitting) operation. Removes the
 * transferred enchantments from the source book, deducts experience, plays a
 * sound effect, and delivers the resulting enchanted book to the player's cursor.
 */
public class ShatterClickEvent {
    /**
     * Entry point for the shatter inventory-click event.
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

        if (!Config.isPluginEnabled() || !Config.Shatterment.isEnabled()) return;

        // Vanilla's own take (which this flow relies on) does not run for a cancelled click, so nothing may be
        // charged or changed here either: an earlier veto (protection or shop plugin) is honoured.
        if (e.isCancelled()) return;

        // A non-empty cursor is ignored on purpose: an enchanted book cannot stack, so vanilla cannot merge the
        // result onto the cursor and nothing is lost or duplicated.
        if (!AnvilEventGuards.isAnvilResultSlotClick(e, p)) return;

        if (AnvilEventGuards.isWorldBlocked(p, Config.Shatterment.isWorldRestricted(p.getWorld()))) return;

        boolean anvilAllowed = AnvilEventGuards.isAnvilAllowed(e.getInventory(), p);

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        ItemStack firstItem = anvilInventory.getItem(0);
        ItemStack secondItem = anvilInventory.getItem(1);

        if (firstItem == null) return;
        if (secondItem == null) return;

        List<IPluginEnchantment> enchantments = AnvilOperations.collect(AnvilOperations.Kind.SHATTER, p, firstItem, secondItem, false);

        if (enchantments.isEmpty()) {
            DiagnosticUtils.debug("SHATTER", "Click: no eligible enchantments → exit");
            return;
        }

        // Not a special anvil. Normally Prepare produced no plugin result and this is plain vanilla use. But the
        // gate may have changed since Prepare (anvil unmarked or broken, block below removed, mode reloaded), so a
        // plugin-made book can still sit in the result slot: vanilla must not hand it out uncharged.
        if (!anvilAllowed) {
            ItemStack staleResult = anvilInventory.getItem(2);
            if (staleResult != null && staleResult.getType() == Material.ENCHANTED_BOOK) {
                DiagnosticUtils.debug("SHATTER", "Click: not a special anvil but a plugin result is present → CANCELLED, result cleared");
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

        if (result.getType() != Material.ENCHANTED_BOOK) return;

        // Genuine shatter operation confirmed. Block non-standard result-slot clicks
        // (shift/number-key/swap/double-click/drop/etc.) that would otherwise let vanilla deliver a
        // second copy of the result in addition to the one the plugin puts on the cursor — item-dupe.
        if (AnvilEventGuards.isUnsafeResultClick(e)) {
            DiagnosticUtils.debug("SHATTER", () -> "Click: unsafe click type " + e.getClick() + " → CANCELLED");
            e.setCancelled(true);
            FeedbackUtils.refuse(p, I18n.Messages.useNormalClick());
            return;
        }

        // A previous operation on this anvil has not consumed its blank book yet — a second one now
        // would be paid for with the same book.
        if (AnvilEventGuards.isOperationPending(anvilInventory)) {
            DiagnosticUtils.debug("SHATTER", "Click: operation still pending on this anvil → CANCELLED");
            e.setCancelled(true);
            return;
        }

        // Only gate genuine plugin operations — a vanilla repair/rename must not be blocked by
        // a disenchant/shatter cooldown.
        if (AnvilEventGuards.isOnCooldown(p)) {
            DiagnosticUtils.debug("SHATTER", "Click: player on cooldown → CANCELLED");
            e.setCancelled(true);
            return;
        }

        DiagnosticUtils.debug("SHATTER", () -> "Click: player=" + p.getName() + ", result=" + result.getType() + ", gameMode=" + p.getGameMode());

        if (DiagnosticUtils.isDebugEnabled()) {
            String names = enchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("SHATTER", "Click: enchantments=[" + names + "]");
        }

        int repairCost = AnvilEventGuards.peekBypassCost(p, AnvilCostUtils.getRepairCost(anvilInventory, e.getView()));
        DiagnosticUtils.debug("SHATTER", () -> "Click: xp check — repairCost=" + repairCost + ", playerLevel=" + p.getLevel());
        if (!AnvilEventGuards.hasEnoughXp(p, repairCost)) {
            DiagnosticUtils.debug("SHATTER", "Click: insufficient XP → CANCELLED");
            FeedbackUtils.refuseNotEnoughXp(p, repairCost);
            e.setCancelled(true);
            return;
        }

        // Vanilla would refuse the take at 40+ levels; this handler delivers ahead of that check.
        if (AnvilEventGuards.refuseIfTooExpensive(p, AnvilEventType.SHATTERMENT, repairCost)) {
            DiagnosticUtils.debug("SHATTER", "Click: too expensive → CANCELLED");
            e.setCancelled(true);
            return;
        }

        if (!PermissionGroupType.SHATTER_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("SHATTER", "Click: permission denied → exit");
            return;
        }

        // Everything from the Pre event on (economy, XP, the delivered book, cooldown, Post event, sound,
        // particles) is shared with the Bedrock GUI; only the slot writes below are specific to the anvil.
        if (!(result.getItemMeta() instanceof EnchantmentStorageMeta)) return;

        AnvilOperations.Outcome outcome = AnvilOperations.settle(AnvilOperations.Kind.SHATTER, p, firstItem, secondItem, result,
                enchantments, repairCost, anvilInventory, (newFirst, delivered) -> {
                    anvilInventory.setItem(0, newFirst);
                    AnvilEventGuards.scheduleSecondItemRemoval(p, anvilInventory, secondItem, e);
                    p.setItemOnCursor(delivered);
                });

        if (outcome.status() == AnvilOperations.Status.CANCELLED) {
            e.setCancelled(true);
            return;
        }

        DiagnosticUtils.debug("SHATTER", "Click: complete ✓");
    }
}
