package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.events.api.PostDisenchantEvent;
import com.jankominek.disenchantment.events.api.PreDisenchantEvent;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.*;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the {@link InventoryClickEvent} when a player clicks the result slot
 * of an anvil during a disenchantment operation. Removes the extracted enchantments
 * from the source item, deducts experience, plays a sound effect, and delivers
 * the enchanted book to the player's cursor.
 */
public class DisenchantClickEvent {
    /**
     * Entry point for the disenchant inventory-click event.
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
            return Config.Disenchantment.Economy.isEnabled();
        }

        @Override
        public double getCost() {
            return Config.Disenchantment.Economy.getCost();
        }

        @Override
        public boolean isChargeMessageEnabled() {
            return Config.Disenchantment.Economy.isChargeMessageEnabled();
        }
    };

    private static void handler(Event event) {
        if (!(event instanceof InventoryClickEvent e)) return;

        Player p = AnvilEventGuards.getPlayer(e);
        if (p == null) return;

        if (!Config.isPluginEnabled() || !Config.Disenchantment.isEnabled()) return;

        if (!AnvilEventGuards.isAnvilResultSlotClick(e, p)) return;

        if (AnvilEventGuards.isWorldBlocked(p, Config.Disenchantment.isWorldRestricted(p.getWorld())))
            return;

        boolean anvilAllowed = AnvilEventGuards.isAnvilAllowed(e.getInventory(), p);

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        ItemStack firstItem = anvilInventory.getItem(0);
        ItemStack secondItem = anvilInventory.getItem(1);

        if (firstItem == null) return;
        if (secondItem == null) return;

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        List<IPluginEnchantment> enchantments = AnvilEventGuards.collectEnchantments(
                firstItem, secondItem, false,
                EventUtils.Disenchantment::getDisenchantedEnchantments,
                EventUtils.Disenchantment::getDisenchantedEnchantments,
                p.getWorld(), p);

        if (enchantments.isEmpty()) {
            DiagnosticUtils.debug("DISENCHANT", "Click: no eligible enchantments → exit");
            return;
        }

        // Not a special anvil. Normally Prepare produced no plugin result and this is plain vanilla use. But the
        // gate may have changed since Prepare (anvil unmarked or broken, block below removed, mode reloaded), so a
        // plugin-made book can still sit in the result slot: vanilla must not hand it out uncharged.
        if (!anvilAllowed) {
            ItemStack staleResult = anvilInventory.getItem(2);
            if (staleResult != null && staleResult.getType() == Material.ENCHANTED_BOOK) {
                DiagnosticUtils.debug("DISENCHANT", "Click: not a special anvil but a plugin result is present → CANCELLED, result cleared");
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

        DiagnosticUtils.debug("DISENCHANT", () -> "Click: player=" + p.getName() + ", result=" + result.getType() + ", gameMode=" + p.getGameMode());

        // Genuine disenchant operation confirmed. Block non-standard result-slot clicks
        // (shift/number-key/swap/double-click/drop/etc.) that would otherwise let vanilla deliver a
        // second copy of the result in addition to the one the plugin puts on the cursor — item-dupe.
        if (AnvilEventGuards.isUnsafeResultClick(e)) {
            DiagnosticUtils.debug("DISENCHANT", () -> "Click: unsafe click type " + e.getClick() + " → CANCELLED");
            e.setCancelled(true);
            FeedbackUtils.refuse(p, I18n.Messages.useNormalClick());
            return;
        }

        // A previous operation on this anvil has not consumed its blank book yet — a second one now
        // would be paid for with the same book.
        if (AnvilEventGuards.isOperationPending(anvilInventory)) {
            DiagnosticUtils.debug("DISENCHANT", "Click: operation still pending on this anvil → CANCELLED");
            e.setCancelled(true);
            return;
        }

        // Only gate genuine plugin operations — a vanilla repair/rename must not be blocked by
        // a disenchant/shatter cooldown.
        if (AnvilEventGuards.isOnCooldown(p)) {
            DiagnosticUtils.debug("DISENCHANT", "Click: player on cooldown → CANCELLED");
            e.setCancelled(true);
            return;
        }

        if (DiagnosticUtils.isDebugEnabled()) {
            String names = enchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("DISENCHANT", "Click: enchantments=[" + names + "]");
        }

        int repairCost = AnvilEventGuards.peekBypassCost(p, AnvilCostUtils.getRepairCost(anvilInventory, e.getView()));
        DiagnosticUtils.debug("DISENCHANT", () -> "Click: xp check — repairCost=" + repairCost + ", playerLevel=" + p.getLevel());
        if (!AnvilEventGuards.hasEnoughXp(p, repairCost)) {
            DiagnosticUtils.debug("DISENCHANT", "Click: insufficient XP → CANCELLED");
            FeedbackUtils.refuseNotEnoughXp(p, repairCost);
            e.setCancelled(true);
            return;
        }

        if (!PermissionGroupType.DISENCHANT_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("DISENCHANT", "Click: permission denied → exit");
            return;
        }

        // Every abortable precondition must be checked BEFORE the player is charged — nothing below the
        // economy step may return without delivering the result.
        EnchantmentStorageMeta resultItemMeta = (EnchantmentStorageMeta) result.getItemMeta();

        if (resultItemMeta == null) return;

        // The enchantments that really end up on the result book. The click-time collection above is
        // wider (it also holds DELETE-state enchantments, and for shatter every split candidate, not
        // just the randomly chosen subset), so the charge — like the Prepare cost display — must be
        // based on this set, not on the full collection.
        List<IPluginEnchantment> transferred = EventUtils.findTransferredEnchantments(enchantments, result, activatedPlugins, p.getWorld());
        List<IPluginEnchantment> destroyed = EventUtils.Disenchantment.findEnchantmentsToDelete(enchantments);

        // Listeners see what is transferred plus what a DELETE state destroys on the source; removing
        // a DELETE entry still vetoes its removal.
        List<IPluginEnchantment> preEnchantments = new ArrayList<>();
        for (IPluginEnchantment enchantment : enchantments) {
            if (transferred.contains(enchantment) || destroyed.contains(enchantment)) preEnchantments.add(enchantment);
        }

        PreDisenchantEvent preEvent = new PreDisenchantEvent(p, firstItem.clone(), preEnchantments);
        org.bukkit.Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            e.setCancelled(true);
            return;
        }

        // Economy check — runs after PreDisenchantEvent so cancellation doesn't charge the player
        DiagnosticUtils.debug("DISENCHANT", () -> "Click: economy check — enabled=" + Config.Disenchantment.Economy.isEnabled() + ", gameMode=" + p.getGameMode());
        double economyCost = AnvilCostUtils.economyCostForEnchantments(
                EventUtils.retainByKey(preEvent.getEnchantments(), transferred), Config.Disenchantment.Economy.getCost(), Config.Disenchantment.Anvil.Repair.getEnchantmentEconomyCosts());
        AnvilEventGuards.EconomyResult economyResult = AnvilEventGuards.processEconomyCost(p, ECONOMY_CONFIG, economyCost);
        if (economyResult == AnvilEventGuards.EconomyResult.NOT_AVAILABLE) {
            DiagnosticUtils.debug("DISENCHANT", "Click: economy not available → CANCELLED");
            FeedbackUtils.refuse(p, I18n.Messages.economyNotAvailable());
            e.setCancelled(true);
            return;
        }
        if (economyResult == AnvilEventGuards.EconomyResult.INSUFFICIENT_FUNDS) {
            DiagnosticUtils.debug("DISENCHANT", "Click: insufficient funds → CANCELLED");
            FeedbackUtils.refuse(p, I18n.Messages.economyInsufficientFunds(EconomyUtils.format(economyCost)));
            e.setCancelled(true);
            return;
        }
        if (economyResult == AnvilEventGuards.EconomyResult.WITHDRAW_FAILED) {
            DiagnosticUtils.debug("DISENCHANT", "Click: economy withdrawal failed → CANCELLED");
            FeedbackUtils.refuse(p, I18n.Messages.economyWithdrawFailed());
            e.setCancelled(true);
            return;
        }

        AnvilEventGuards.clearBypassCost(p);

        int exp = p.getLevel() - repairCost;
        DiagnosticUtils.debug("DISENCHANT", () -> "Click: xp → " + p.getLevel() + " - " + repairCost + " = " + exp);

        // ----------------------------------------------------------------------------------------------------
        // Supported plugins

        ItemStack finalFirstItem = firstItem.clone();
        List<IPluginEnchantment> enchantmentsToDelete = EventUtils.Disenchantment.findEnchantmentsToDelete(preEvent.getEnchantments());

        // Always remove the vanilla/Bukkit enchantments recorded on the result book from the
        // source item — this must NOT depend on whether a custom enchantment plugin is active.
        // A namespace-scoped adapter (e.g. Vane) reports no vanilla enchantments, so gating this
        // on "no plugins active" left them on the source item: the book was delivered while the
        // item kept its enchantments — an item dupe (issue #73).
        finalFirstItem = EnchantmentUtils.removeEnchantments(finalFirstItem, resultItemMeta.getStoredEnchants());

        // Additionally let each active adapter strip its own (e.g. vane:*) enchantments, which are
        // not part of the book's vanilla stored-enchant set.
        for (ISupportedPlugin activatedPlugin : activatedPlugins) {
            List<IPluginEnchantment> pluginEnchantments = activatedPlugin.getItemEnchantments(result, p.getWorld());

            for (IPluginEnchantment enchantment : pluginEnchantments) {
                finalFirstItem = enchantment.removeFromItem(finalFirstItem);
            }
        }

        for (IPluginEnchantment enchantment : enchantmentsToDelete) {
            finalFirstItem = enchantment.removeFromItem(finalFirstItem);
        }

        // Supported plugins
        // ----------------------------------------------------------------------------------------------------

        if (Config.Disenchantment.Anvil.Repair.isResetEnabled()) AnvilCostUtils.setItemRepairCost(finalFirstItem, 0);

        anvilInventory.setItem(0, finalFirstItem);
        AnvilEventGuards.scheduleSecondItemRemoval(p, anvilInventory, secondItem, e);

        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) p.setLevel(exp);

        // Per-enchantment chance roll — failed enchantments are destroyed (already stripped from
        // the source item above via the full stored-enchant set) rather than transferred to the book.
        //
        // In the same pass, restore each surviving enchantment's raw source level. The book pulled
        // from the anvil result slot may have had above-vanilla-max levels clamped down during the
        // PrepareAnvilEvent → result-slot round-trip (e.g. Unbreaking 4 → 3, issue #76). We re-stamp
        // from the freshly collected source levels via addStoredEnchant(..., true) and deliver the
        // book straight to the cursor, so the restored level is not clamped again.
        boolean allTransfersFailed = false;
        if (resultItemMeta != null) {
            Map<String, Integer> sourceLevels = new HashMap<>();
            for (IPluginEnchantment ench : enchantments) sourceLevels.put(EventUtils.normalizeKey(ench.getKey()).toLowerCase(), ench.getLevel());

            Map<org.bukkit.enchantments.Enchantment, Integer> stored = new HashMap<>(resultItemMeta.getStoredEnchants());
            boolean metaChanged = false;
            for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : stored.entrySet()) {
                org.bukkit.enchantments.Enchantment enchantment = entry.getKey();

                double chance = Config.Disenchantment.getEnchantmentChance(enchantment.getKey().getKey());
                if (chance < 1.0 && Math.random() >= chance) {
                    resultItemMeta.removeStoredEnchant(enchantment);
                    metaChanged = true;
                    continue;
                }

                Integer rawLevel = sourceLevels.get(EventUtils.normalizeKey(enchantment.getKey().getKey()).toLowerCase());
                if (rawLevel != null && !rawLevel.equals(entry.getValue())) {
                    resultItemMeta.addStoredEnchant(enchantment, rawLevel, true);
                    metaChanged = true;
                }
            }
            if (metaChanged) result.setItemMeta(resultItemMeta);

            // Every roll failed: an empty enchanted book would be a useless, confusing reward. Hand
            // back a plain book instead (unless an adapter enchantment still made it onto the book).
            allTransfersFailed = !stored.isEmpty() && resultItemMeta.getStoredEnchants().isEmpty()
                    && activatedPlugins.stream().allMatch(plugin -> plugin.getItemEnchantments(result, p.getWorld()).isEmpty());
        }

        // Past every abort: drop the preview lines (and the chance warning) that Prepare added, in place so
        // the result slot cannot hand a second, still-annotated copy to vanilla's own pick-up.
        PreviewLore.strip(result);

        ItemStack delivered = result;
        if (allTransfersFailed) {
            delivered = new ItemStack(Material.BOOK);
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.enchantmentsFailedToTransfer());
            DiagnosticUtils.debug("DISENCHANT", "Click: every enchantment failed its chance roll → plain book delivered");
        }

        p.setItemOnCursor(delivered);
        boolean creative = p.getGameMode() == org.bukkit.GameMode.CREATIVE;
        int xpCost = creative ? 0 : repairCost;
        double finalEconomyCost = (Config.Disenchantment.Economy.isEnabled() && !creative) ? economyCost : 0.0;
        AnvilEventGuards.recordCooldownOperation(p);
        org.bukkit.Bukkit.getPluginManager().callEvent(new PostDisenchantEvent(p, delivered.clone(), finalFirstItem.clone(), xpCost, finalEconomyCost));

        if (Config.Disenchantment.Anvil.Sound.isEnabled())
            p.playSound(
                    p.getLocation(),
                    Sound.BLOCK_ANVIL_USE,
                    Config.Disenchantment.Anvil.Sound.getVolume().floatValue(),
                    Config.Disenchantment.Anvil.Sound.getPitch().floatValue()
            );

        ParticleUtils.playSuccess(p, anvilInventory);

        DiagnosticUtils.debug("DISENCHANT", "Click: complete ✓");
    }
}
