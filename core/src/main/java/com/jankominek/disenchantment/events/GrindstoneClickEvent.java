package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.events.api.PostGrindstoneExtractEvent;
import com.jankominek.disenchantment.events.api.PostGrindstoneTransferEvent;
import com.jankominek.disenchantment.events.api.PreGrindstoneExtractEvent;
import com.jankominek.disenchantment.events.api.PreGrindstoneTransferEvent;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.EconomyUtils;
import com.jankominek.disenchantment.utils.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles {@link InventoryClickEvent}s on a grindstone for the two grindstone operations.
 * <ul>
 *   <li><b>Book placement.</b> The vanilla grindstone refuses a plain {@code BOOK} in its input slots
 *   (they only accept damageable or enchanted items), so a click that would put one into slot 1 is
 *   intercepted and exactly one book is placed through the inventory API, which bypasses the slot rule.
 *   On a server that does accept the book this interception is a harmless no-op.</li>
 *   <li><b>Result click.</b> Uses the manual-result pattern: the click is cancelled and everything is done
 *   synchronously — the source goes back into slot 0 stripped of the moved enchantments, the consumed
 *   input leaves slot 1, the result goes to the cursor — so vanilla's own take (which would clear both
 *   inputs and award XP orbs for them) never runs. The result is recomputed from the inputs at click time;
 *   the item sitting in the result slot is only used as a "something is shown" signal.</li>
 * </ul>
 */
public class GrindstoneClickEvent {
    /**
     * Entry point for the grindstone inventory-click event.
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

    private static final int SLOT_SOURCE = 0;
    private static final int SLOT_SECOND = 1;
    private static final int SLOT_RESULT = 2;
    private static final int TOP_SIZE = 3;

    private static void handler(Event event) {
        if (!(event instanceof InventoryClickEvent e)) return;

        Player p = AnvilEventGuards.getPlayer(e);
        if (p == null) return;

        if (e.getInventory().getType() != InventoryType.GRINDSTONE) return;
        if (!(e.getInventory() instanceof GrindstoneInventory inventory)) return;

        if (!Config.isPluginEnabled()) return;
        if (!Config.Grindstone.isEnabled() && !Config.Grindstone.Transfer.isEnabled()) return;

        int rawSlot = e.getRawSlot();
        if (rawSlot == SLOT_SECOND) {
            placeBookFromCursor(e, p, inventory);
        } else if (rawSlot == SLOT_RESULT) {
            takeResult(e, p, inventory);
        } else if (rawSlot >= TOP_SIZE && isShiftClick(e)) {
            placeBookFromInventory(e, p, inventory);
        }
    }

    // A null click only occurs under mocked tests; it is treated like a plain left click.
    private static ClickType clickOf(InventoryClickEvent e) {
        ClickType click = e.getClick();
        return click == null ? ClickType.LEFT : click;
    }

    private static boolean isShiftClick(InventoryClickEvent e) {
        ClickType click = clickOf(e);
        return click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT;
    }

    // ----------------------------------------------------------------------------------------------------
    // Book placement (extract input)

    /**
     * Decides whether a plain book may be put into slot 1 for an extraction: the feature is on, the
     * player may use it, slot 1 is free and slot 0 holds a single item that has something to extract.
     */
    static boolean canPlaceBook(Player p, GrindstoneInventory inventory, ItemStack book) {
        if (!Config.Grindstone.isEnabled()) return false;
        if (!EventUtils.isBlankBook(book)) return false;

        ItemStack second = inventory.getItem(SLOT_SECOND);
        if (second != null && !second.getType().isAir()) return false;

        ItemStack first = inventory.getItem(SLOT_SOURCE);
        if (first == null || first.getType().isAir()) return false;

        if (AnvilEventGuards.isWorldBlocked(p, Config.Disenchantment.isWorldRestricted(p.getWorld()))) return false;
        if (!PermissionGroupType.GRINDSTONE_EXTRACT_EVENT.hasPermission(p, false)) return false;

        ItemStack single = book.clone();
        single.setAmount(1);
        return !GrindstoneOps.collectExtract(first, single, false, p.getWorld(), p).isEmpty();
    }

    private static void placeBookFromCursor(InventoryClickEvent e, Player p, GrindstoneInventory inventory) {
        ClickType click = clickOf(e);
        if (click != ClickType.LEFT && click != ClickType.RIGHT) return;

        ItemStack cursor = p.getItemOnCursor();
        if (!canPlaceBook(p, inventory, cursor)) return;

        e.setCancelled(true);

        ItemStack single = cursor.clone();
        single.setAmount(1);

        ItemStack remaining = cursor.clone();
        remaining.setAmount(cursor.getAmount() - 1);

        // Both writes happen in this tick: exactly one book leaves the cursor and exactly one enters the slot.
        p.setItemOnCursor(remaining.getAmount() > 0 ? remaining : new ItemStack(Material.AIR));
        inventory.setItem(SLOT_SECOND, single);
        p.updateInventory();

        DiagnosticUtils.debug("GRINDSTONE", () -> "Click: placed one plain book into slot 1 for " + p.getName());
    }

    private static void placeBookFromInventory(InventoryClickEvent e, Player p, GrindstoneInventory inventory) {
        ItemStack clicked = e.getCurrentItem();
        Inventory source = e.getClickedInventory();
        if (clicked == null || source == null || source == inventory) return;
        if (!canPlaceBook(p, inventory, clicked)) return;

        e.setCancelled(true);

        ItemStack single = clicked.clone();
        single.setAmount(1);

        ItemStack remaining = clicked.clone();
        remaining.setAmount(clicked.getAmount() - 1);

        source.setItem(e.getSlot(), remaining.getAmount() > 0 ? remaining : null);
        inventory.setItem(SLOT_SECOND, single);
        p.updateInventory();

        DiagnosticUtils.debug("GRINDSTONE", () -> "Click: shift-placed one plain book into slot 1 for " + p.getName());
    }

    // ----------------------------------------------------------------------------------------------------
    // Result click

    private static void takeResult(InventoryClickEvent e, Player p, GrindstoneInventory inventory) {
        ItemStack first = inventory.getItem(SLOT_SOURCE);
        ItemStack second = inventory.getItem(SLOT_SECOND);
        if (first == null || second == null || first.getType().isAir() || second.getType().isAir()) return;

        if (EventUtils.isBlankBook(second)) {
            if (Config.Grindstone.isEnabled()) takeExtract(e, p, inventory, first, second);
        } else if (Config.Grindstone.Transfer.isEnabled()) {
            takeTransfer(e, p, inventory, first, second);
        }
    }

    /**
     * The checks shared by both result clicks, in the order the anvil click handlers use: from here on the
     * click is ours and stays cancelled on every early exit. Returns {@code false} when the handler must stop.
     */
    private static boolean passesClickGuards(InventoryClickEvent e, Player p, boolean resultMatches, String tag) {
        if (AnvilEventGuards.isMaintenanceBlocked(p)) {
            e.setCancelled(true);
            return false;
        }

        if (!resultMatches) return false;

        // From here on this is a genuine grindstone operation: whatever happens, vanilla must not take the result.
        e.setCancelled(true);

        if (!p.getItemOnCursor().getType().isAir()) {
            DiagnosticUtils.debug(tag, "Click: cursor not empty → CANCELLED");
            return false;
        }

        if (AnvilEventGuards.isUnsafeResultClick(e)) {
            DiagnosticUtils.debug(tag, () -> "Click: unsafe click type " + e.getClick() + " → CANCELLED");
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.useNormalClick());
            return false;
        }

        if (AnvilEventGuards.isOnCooldown(p)) {
            DiagnosticUtils.debug(tag, "Click: player on cooldown → CANCELLED");
            return false;
        }

        return true;
    }

    /**
     * Sends the matching message for a failed economy step. Returns {@code true} when the operation may go on.
     */
    private static boolean economyAllows(Player p, AnvilEventGuards.EconomyResult result, double economyCost) {
        switch (result) {
            case OK:
                return true;
            case NOT_AVAILABLE:
                p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.economyNotAvailable());
                return false;
            case INSUFFICIENT_FUNDS:
                p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.economyInsufficientFunds(EconomyUtils.format(economyCost)));
                return false;
            default:
                p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.economyWithdrawFailed());
                return false;
        }
    }

    private static void takeExtract(InventoryClickEvent e, Player p, GrindstoneInventory inventory, ItemStack first, ItemStack book) {
        if (AnvilEventGuards.isWorldBlocked(p, Config.Disenchantment.isWorldRestricted(p.getWorld()))) return;

        // Same two collections as the anvil flow: without DELETE-state enchantments (what lands on the book)
        // and with them (what is additionally destroyed on the source).
        List<IPluginEnchantment> transferable = GrindstoneOps.collectExtract(first, book, true, p.getWorld(), p);
        if (transferable.isEmpty()) {
            DiagnosticUtils.debug("GRINDSTONE", "Click: no extractable enchantments → exit");
            return;
        }
        List<IPluginEnchantment> everything = GrindstoneOps.collectExtract(first, book, false, p.getWorld(), p);

        ItemStack shown = inventory.getItem(SLOT_RESULT);
        boolean resultMatches = shown != null && shown.getType() == Material.ENCHANTED_BOOK;
        if (!passesClickGuards(e, p, resultMatches, "GRINDSTONE")) return;

        int xpCost = AnvilCostUtils.countAnvilCost(transferable, AnvilEventType.GRINDSTONE_EXTRACT, p);
        if (!AnvilEventGuards.hasEnoughXp(p, xpCost)) {
            DiagnosticUtils.debug("GRINDSTONE", "Click: insufficient XP → CANCELLED");
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.notEnoughLevels(String.valueOf(xpCost)));
            return;
        }

        if (!PermissionGroupType.GRINDSTONE_EXTRACT_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("GRINDSTONE", "Click: permission denied → CANCELLED");
            return;
        }

        List<IPluginEnchantment> destroyed = EventUtils.Disenchantment.findEnchantmentsToDelete(everything);
        List<IPluginEnchantment> preEnchantments = new ArrayList<>(transferable);
        for (IPluginEnchantment enchantment : destroyed) {
            if (!preEnchantments.contains(enchantment)) preEnchantments.add(enchantment);
        }

        PreGrindstoneExtractEvent preEvent = new PreGrindstoneExtractEvent(p, first.clone(), preEnchantments);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) return;

        // A listener may veto single enchantments: a vetoed one is neither put on the book nor removed.
        List<IPluginEnchantment> moving = EventUtils.retainByKey(transferable, preEvent.getEnchantments());
        List<IPluginEnchantment> deleting = EventUtils.retainByKey(destroyed, preEvent.getEnchantments());
        if (moving.isEmpty()) {
            DiagnosticUtils.debug("GRINDSTONE", "Click: every enchantment vetoed → exit");
            return;
        }

        double economyCost = AnvilCostUtils.economyCostForEnchantments(
                moving, Config.Grindstone.Economy.getCost(), Config.Grindstone.Repair.getEnchantmentEconomyCosts());
        AnvilEventGuards.EconomyResult economyResult = AnvilEventGuards.processEconomyCost(p, GrindstoneEvent.extractEconomy(), economyCost);
        if (!economyAllows(p, economyResult, economyCost)) {
            DiagnosticUtils.debug("GRINDSTONE", () -> "Click: economy " + economyResult + " → CANCELLED");
            return;
        }

        // ----------------------------------------------------------------------------------------------------
        // Everything below is plain assignment: the result is fully built before the first inventory write.

        List<IPluginEnchantment> surviving = new ArrayList<>();
        for (IPluginEnchantment enchantment : moving) {
            double chance = Config.Disenchantment.getEnchantmentChance(enchantment.getKey());
            if (chance < 1.0 && Math.random() >= chance) continue;
            surviving.add(enchantment);
        }

        ItemStack delivered;
        if (surviving.isEmpty()) {
            // Every roll failed: hand back a plain book rather than an empty enchanted one.
            delivered = new ItemStack(Material.BOOK);
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.enchantmentsFailedToTransfer());
        } else {
            delivered = GrindstoneOps.buildExtractBook(surviving);
        }

        List<IPluginEnchantment> toRemove = new ArrayList<>(moving);
        toRemove.addAll(deleting);
        ItemStack finalFirst = GrindstoneOps.stripFromSource(first, toRemove);
        if (Config.Grindstone.Repair.isResetEnabled()) AnvilCostUtils.setItemRepairCost(finalFirst, 0);

        ItemStack remainingBooks = null;
        if (book.getAmount() > 1) {
            remainingBooks = book.clone();
            remainingBooks.setAmount(book.getAmount() - 1);
        }

        boolean creative = p.getGameMode() == GameMode.CREATIVE;
        if (!creative) p.setLevel(p.getLevel() - xpCost);

        // Result first so the intermediate recomputes triggered by the next writes never see a stale result.
        inventory.setItem(SLOT_RESULT, null);
        inventory.setItem(SLOT_SECOND, remainingBooks);
        inventory.setItem(SLOT_SOURCE, finalFirst);
        p.setItemOnCursor(delivered);
        p.updateInventory();

        double charged = (Config.Grindstone.Economy.isEnabled() && !creative) ? economyCost : 0.0;
        AnvilEventGuards.recordCooldownOperation(p);
        Bukkit.getPluginManager().callEvent(new PostGrindstoneExtractEvent(p, delivered.clone(), finalFirst.clone(), creative ? 0 : xpCost, charged));

        playSound(p);
        DiagnosticUtils.debug("GRINDSTONE", "Click: extract complete ✓");
    }

    private static void takeTransfer(InventoryClickEvent e, Player p, GrindstoneInventory inventory, ItemStack first, ItemStack target) {
        if (AnvilEventGuards.isWorldBlocked(p, Config.Disenchantment.isWorldRestricted(p.getWorld()))) return;

        List<IPluginEnchantment> transfers = GrindstoneOps.computeTransfer(first, target, p);
        if (transfers.isEmpty()) {
            DiagnosticUtils.debug("GRINDSTONE", "Click: nothing transferable → exit");
            return;
        }

        ItemStack shown = inventory.getItem(SLOT_RESULT);
        boolean resultMatches = shown != null && shown.getType() == target.getType();
        if (!passesClickGuards(e, p, resultMatches, "GRINDSTONE")) return;

        int xpCost = AnvilCostUtils.countAnvilCost(transfers, AnvilEventType.GRINDSTONE_TRANSFER, p);
        if (!AnvilEventGuards.hasEnoughXp(p, xpCost)) {
            DiagnosticUtils.debug("GRINDSTONE", "Click: insufficient XP → CANCELLED");
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.notEnoughLevels(String.valueOf(xpCost)));
            return;
        }

        if (!PermissionGroupType.GRINDSTONE_TRANSFER_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("GRINDSTONE", "Click: permission denied → CANCELLED");
            return;
        }

        PreGrindstoneTransferEvent preEvent = new PreGrindstoneTransferEvent(p, first.clone(), target.clone(), new ArrayList<>(transfers));
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) return;

        List<IPluginEnchantment> moving = EventUtils.retainByKey(transfers, preEvent.getEnchantments());
        if (moving.isEmpty()) {
            DiagnosticUtils.debug("GRINDSTONE", "Click: every enchantment vetoed → exit");
            return;
        }

        double economyCost = AnvilCostUtils.economyCostForEnchantments(
                moving, Config.Grindstone.Transfer.Economy.getCost(), Config.Grindstone.Transfer.Repair.getEnchantmentEconomyCosts());
        AnvilEventGuards.EconomyResult economyResult = AnvilEventGuards.processEconomyCost(p, GrindstoneEvent.transferEconomy(), economyCost);
        if (!economyAllows(p, economyResult, economyCost)) {
            DiagnosticUtils.debug("GRINDSTONE", () -> "Click: economy " + economyResult + " → CANCELLED");
            return;
        }

        // ----------------------------------------------------------------------------------------------------
        // Enchantments are moved, never copied: both new items are built before the first inventory write,
        // and the source loses exactly what the target gained.

        ItemStack delivered = GrindstoneOps.buildTransferResult(target, moving);
        ItemStack finalFirst = GrindstoneOps.stripFromSource(first, moving);
        if (Config.Grindstone.Transfer.Repair.isResetEnabled()) {
            AnvilCostUtils.setItemRepairCost(finalFirst, 0);
            AnvilCostUtils.setItemRepairCost(delivered, 0);
        }

        boolean creative = p.getGameMode() == GameMode.CREATIVE;
        if (!creative) p.setLevel(p.getLevel() - xpCost);

        inventory.setItem(SLOT_RESULT, null);
        inventory.setItem(SLOT_SECOND, null);
        inventory.setItem(SLOT_SOURCE, finalFirst);
        p.setItemOnCursor(delivered);
        p.updateInventory();

        double charged = (Config.Grindstone.Transfer.Economy.isEnabled() && !creative) ? economyCost : 0.0;
        AnvilEventGuards.recordCooldownOperation(p);
        Bukkit.getPluginManager().callEvent(new PostGrindstoneTransferEvent(p, delivered.clone(), finalFirst.clone(), moving, creative ? 0 : xpCost, charged));

        playSound(p);
        DiagnosticUtils.debug("GRINDSTONE", "Click: transfer complete ✓");
    }

    private static void playSound(Player p) {
        if (!Config.Grindstone.Sound.isEnabled()) return;

        // String key instead of the Sound constant: Sound became an interface in 1.21.3.
        p.playSound(
                p.getLocation(),
                "block.grindstone.use",
                Config.Grindstone.Sound.getVolume().floatValue(),
                Config.Grindstone.Sound.getPitch().floatValue()
        );
    }
}
