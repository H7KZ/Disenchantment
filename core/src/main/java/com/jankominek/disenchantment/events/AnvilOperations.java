package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.events.api.PostDisenchantEvent;
import com.jankominek.disenchantment.events.api.PostEraseEvent;
import com.jankominek.disenchantment.events.api.PostShatterEvent;
import com.jankominek.disenchantment.events.api.PreDisenchantEvent;
import com.jankominek.disenchantment.events.api.PreEraseEvent;
import com.jankominek.disenchantment.events.api.PreShatterEvent;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.AnvilEventType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.EconomyUtils;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.jankominek.disenchantment.utils.EraseUtils;
import com.jankominek.disenchantment.utils.EventUtils;
import com.jankominek.disenchantment.utils.FeedbackUtils;
import com.jankominek.disenchantment.utils.ParticleUtils;
import com.jankominek.disenchantment.utils.PreviewLore;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The one place that knows how an anvil operation (disenchant, shatter, erase) is collected, priced and
 * <em>settled</em>. The anvil click handlers and the Bedrock chest GUI both go through here, so the
 * Pre/Post API events, the economy charge, the XP charge, the chance roll, the cooldown, the sound and
 * the particles cannot drift apart between the two entry points.
 * <p>
 * Where the items physically live is the only thing the callers decide: the anvil handlers write the
 * anvil slots and the cursor, the GUI writes its own input slots and the player's inventory. They
 * hand that over as an {@link Applier}, which {@link #settle} calls once every abortable precondition
 * has passed and the player has been charged.
 */
public final class AnvilOperations {

    private AnvilOperations() {
    }

    // ----------------------------------------------------------------------------------------------------
    // Operation kinds

    /**
     * The three operations the anvil (and the Bedrock GUI) can perform.
     */
    public enum Kind {
        DISENCHANT("DISENCHANT"),
        SHATTER("SHATTER"),
        ERASE("ERASE");

        private final String category;

        Kind(String category) {
            this.category = category;
        }

        /**
         * @return the diagnostics category used for debug output of this operation
         */
        public String category() {
            return category;
        }

        /**
         * @return the cost/permission type of this operation
         */
        public AnvilEventType eventType() {
            return switch (this) {
                case DISENCHANT -> AnvilEventType.DISENCHANTMENT;
                case SHATTER -> AnvilEventType.SHATTERMENT;
                case ERASE -> AnvilEventType.ERASE;
            };
        }

        /**
         * @return the permission group that allows this operation at the anvil
         */
        public PermissionGroupType permission() {
            return switch (this) {
                case DISENCHANT -> PermissionGroupType.DISENCHANT_EVENT;
                case SHATTER -> PermissionGroupType.SHATTER_EVENT;
                case ERASE -> PermissionGroupType.ERASE_EVENT;
            };
        }

        /**
         * @return whether the feature is switched on in the config (erase additionally needs a usable catalyst)
         */
        public boolean isActive() {
            return switch (this) {
                case DISENCHANT -> Config.Disenchantment.isEnabled();
                case SHATTER -> Config.Shatterment.isEnabled();
                case ERASE -> EraseUtils.isActive();
            };
        }

        /**
         * @param p the player
         * @return whether the player's world disables this feature and the player has no bypass
         */
        public boolean isWorldBlocked(Player p) {
            // Erase shares worlds, materials and enchantment states with disenchantment.
            boolean restricted = this == SHATTER
                    ? Config.Shatterment.isWorldRestricted(p.getWorld())
                    : Config.Disenchantment.isWorldRestricted(p.getWorld());
            return AnvilEventGuards.isWorldBlocked(p, restricted);
        }

        /**
         * @return the economy settings of this operation
         */
        public AnvilEventGuards.EconomyConfig economy() {
            return switch (this) {
                case DISENCHANT -> DISENCHANT_ECONOMY;
                case SHATTER -> SHATTER_ECONOMY;
                case ERASE -> ERASE_ECONOMY;
            };
        }

        /**
         * @return whether this operation's result is an enchanted book
         */
        public boolean isBook() {
            return this != ERASE;
        }
    }

    private static final AnvilEventGuards.EconomyConfig DISENCHANT_ECONOMY = new AnvilEventGuards.EconomyConfig() {
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

        @Override
        public boolean isShowCostEnabled() {
            return Config.Disenchantment.Economy.isShowCostEnabled();
        }
    };

    private static final AnvilEventGuards.EconomyConfig SHATTER_ECONOMY = new AnvilEventGuards.EconomyConfig() {
        @Override
        public boolean isEnabled() {
            return Config.Shatterment.Economy.isEnabled();
        }

        @Override
        public double getCost() {
            return Config.Shatterment.Economy.getCost();
        }

        @Override
        public boolean isChargeMessageEnabled() {
            return Config.Shatterment.Economy.isChargeMessageEnabled();
        }

        @Override
        public boolean isShowCostEnabled() {
            return Config.Shatterment.Economy.isShowCostEnabled();
        }
    };

    private static final AnvilEventGuards.EconomyConfig ERASE_ECONOMY = new AnvilEventGuards.EconomyConfig() {
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

        @Override
        public boolean isShowCostEnabled() {
            return Config.Erase.Economy.isShowCostEnabled();
        }
    };

    // ----------------------------------------------------------------------------------------------------
    // Collection, result and price (shared by PrepareAnvil, the click handlers and the Bedrock GUI)

    /**
     * Collects the enchantments an operation would act on for the given inputs (vanilla and every active
     * adapter, with the player's material bypass honoured).
     *
     * @param kind      the operation
     * @param p         the player
     * @param first     the main item
     * @param second    the book / catalyst
     * @param isPrepare {@code true} for a preview, {@code false} at click time (DELETE-state enchantments included)
     * @return the eligible enchantments, possibly empty
     */
    public static List<IPluginEnchantment> collect(Kind kind, Player p, ItemStack first, ItemStack second, boolean isPrepare) {
        return switch (kind) {
            case DISENCHANT -> AnvilEventGuards.collectEnchantments(
                    first, second, isPrepare,
                    EventUtils.Disenchantment::getDisenchantedEnchantments,
                    EventUtils.Disenchantment::getDisenchantedEnchantments,
                    p.getWorld(), p);
            case SHATTER -> AnvilEventGuards.collectEnchantments(
                    first, second, isPrepare,
                    EventUtils.Shatterment::getShattermentEnchantments,
                    EventUtils.Shatterment::getShattermentEnchantments,
                    p.getWorld(), p);
            case ERASE -> AnvilEventGuards.collectEnchantments(
                    first, second, isPrepare,
                    EraseUtils::getErasableEnchantments,
                    EraseUtils::getErasableEnchantments,
                    p.getWorld(), p);
        };
    }

    /**
     * Builds the enchanted book that carries {@code enchantments}.
     *
     * @param enchantments the enchantments to put on the book
     * @return a new enchanted book
     */
    public static ItemStack buildBook(List<IPluginEnchantment> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        for (IPluginEnchantment enchantment : enchantments) {
            book = enchantment.addToBook(book);
        }

        return book;
    }

    /**
     * Prices an operation in money.
     *
     * @param kind         the operation
     * @param enchantments the enchantments the player is charged for
     * @return the economy cost (never negative)
     */
    public static double economyCost(Kind kind, List<IPluginEnchantment> enchantments) {
        return switch (kind) {
            case DISENCHANT -> AnvilCostUtils.economyCostForEnchantments(
                    enchantments, Config.Disenchantment.Economy.getCost(), Config.Disenchantment.Anvil.Repair.getEnchantmentEconomyCosts());
            case SHATTER -> AnvilCostUtils.economyCostForEnchantments(
                    enchantments, Config.Shatterment.Economy.getCost(), Config.Shatterment.Anvil.Repair.getEnchantmentEconomyCosts());
            case ERASE -> AnvilCostUtils.economyCostForEnchantments(
                    enchantments, Config.Erase.Economy.getCost(), Config.Erase.Anvil.Repair.getEnchantmentEconomyCosts());
        };
    }

    /**
     * Returns what a catalyst slot holds after one catalyst was used: one fewer, or {@code null} when it
     * was the last. The stack is returned unchanged when {@code erase.consume-catalyst} is off.
     *
     * @param catalyst the catalyst stack
     * @return the remainder to write back
     */
    public static ItemStack consumeCatalyst(ItemStack catalyst) {
        if (!Config.Erase.isConsumeCatalyst()) return catalyst;
        return decrement(catalyst);
    }

    /**
     * Returns {@code stack} with one fewer item, or {@code null} when it was the last.
     *
     * @param stack the stack
     * @return the remainder
     */
    public static ItemStack decrement(ItemStack stack) {
        if (stack == null || stack.getAmount() <= 1) return null;

        ItemStack remaining = stack.clone();
        remaining.setAmount(stack.getAmount() - 1);
        return remaining;
    }

    // ----------------------------------------------------------------------------------------------------
    // Settlement

    /**
     * Writes the physical outcome of a settled operation. Called by {@link #settle} after every abortable
     * precondition has passed, the delivered items are fully built and the player has been charged. If it
     * throws, the payment is refunded and the exception propagates.
     */
    @FunctionalInterface
    public interface Applier {
        /**
         * @param newFirst  what the main slot holds afterwards ({@code null} = emptied)
         * @param delivered the result the player receives
         */
        void apply(ItemStack newFirst, ItemStack delivered);
    }

    /**
     * How a settlement ended.
     */
    public enum Status {
        /**
         * The operation ran: the player was charged and the {@link Applier} was called.
         */
        DONE,
        /**
         * A listener or the economy refused it; the player was told where needed and nothing was changed or charged.
         * The anvil handlers cancel the click.
         */
        CANCELLED,
        /**
         * The inputs are not an operation at all (nothing was changed). The anvil handlers just return.
         */
        IGNORED
    }

    /**
     * The result of {@link #settle}.
     *
     * @param status       how it ended
     * @param delivered    what the player received ({@code null} unless {@link Status#DONE})
     * @param newFirst     what the main slot holds afterwards
     * @param xpCost       levels charged (0 in creative)
     * @param economyCost  money charged (0 in creative or with economy off)
     */
    public record Outcome(Status status, ItemStack delivered, ItemStack newFirst, int xpCost, double economyCost) {
        static Outcome of(Status status) {
            return new Outcome(status, null, null, 0, 0.0);
        }
    }

    /**
     * Settles an operation whose preconditions (guards, cooldown, XP check, permission) the caller has
     * already checked: fires the Pre event, charges the economy, builds the delivered item, lets the
     * {@link Applier} write the slots, takes the XP, records the cooldown, fires the Post event and plays
     * the sound and particles. This is the single code path for the anvil handlers and the Bedrock GUI.
     *
     * @param kind         the operation
     * @param p            the player
     * @param firstItem    the live main item
     * @param secondItem   the live book / catalyst
     * @param result       the prepared result: the result book (disenchant/shatter) or the source copy (erase); may be modified
     * @param enchantments the click-time collection ({@link #collect} with {@code isPrepare = false})
     * @param repairCost   the XP levels to charge
     * @param effects      the inventory the particles are anchored at, or {@code null} for the player's location
     * @param applier      writes the slots and hands over the result
     * @return the outcome
     */
    public static Outcome settle(Kind kind, Player p, ItemStack firstItem, ItemStack secondItem, ItemStack result,
                                 List<IPluginEnchantment> enchantments, int repairCost, Inventory effects, Applier applier) {
        return kind == Kind.ERASE
                ? settleErase(p, firstItem, secondItem, enchantments, repairCost, effects, applier)
                : settleBook(kind, p, firstItem, secondItem, result, enchantments, repairCost, effects, applier);
    }

    private static boolean charge(Kind kind, Player p, double economyCost) {
        AnvilEventGuards.EconomyResult economyResult = AnvilEventGuards.processEconomyCost(p, kind.economy(), economyCost);
        if (economyResult == AnvilEventGuards.EconomyResult.NOT_AVAILABLE) {
            DiagnosticUtils.debug(kind.category(), "Click: economy not available → CANCELLED");
            FeedbackUtils.refuse(p, I18n.Messages.economyNotAvailable());
            return false;
        }
        if (economyResult == AnvilEventGuards.EconomyResult.INSUFFICIENT_FUNDS) {
            DiagnosticUtils.debug(kind.category(), "Click: insufficient funds → CANCELLED");
            FeedbackUtils.refuse(p, I18n.Messages.economyInsufficientFunds(EconomyUtils.format(economyCost)));
            return false;
        }
        if (economyResult == AnvilEventGuards.EconomyResult.WITHDRAW_FAILED) {
            DiagnosticUtils.debug(kind.category(), "Click: economy withdrawal failed → CANCELLED");
            FeedbackUtils.refuse(p, I18n.Messages.economyWithdrawFailed());
            return false;
        }
        return true;
    }

    /**
     * What a settlement took from the player, so a failure while writing the result can give it back.
     *
     * @param economyCost money withdrawn (0 when the economy is off or the player is in creative)
     * @param xpCost      levels taken (0 in creative)
     * @param levelBefore the player's level before the charge
     */
    private record Payment(double economyCost, int xpCost, int levelBefore) {
    }

    /**
     * Charges the economy and then the XP levels. Returns {@code null} (the player is told, nothing stays
     * charged) when either is refused. The XP is re-checked after the economy step because an economy plugin can
     * change a player's levels while withdrawing.
     */
    private static Payment takePayment(Kind kind, Player p, double economyCost, int repairCost) {
        if (!charge(kind, p, economyCost)) return null;

        boolean creative = p.getGameMode() == GameMode.CREATIVE;
        int levelBefore = p.getLevel();

        if (!creative && levelBefore < repairCost) {
            AnvilEventGuards.refundEconomy(p, kind.economy(), economyCost);
            FeedbackUtils.refuseNotEnoughXp(p, repairCost);
            return null;
        }

        if (!creative) p.setLevel(levelBefore - repairCost);
        AnvilEventGuards.clearBypassCost(p);

        return new Payment(economyCost, creative ? 0 : repairCost, levelBefore);
    }

    private static void refund(Kind kind, Player p, Payment payment) {
        if (payment.xpCost() > 0) p.setLevel(payment.levelBefore());
        AnvilEventGuards.refundEconomy(p, kind.economy(), payment.economyCost());
        DiagnosticUtils.debug(kind.category(), "Click: writing the result failed → payment refunded");
    }

    private static void playSound(Kind kind, Player p) {
        boolean enabled;
        float volume;
        float pitch;
        switch (kind) {
            case DISENCHANT -> {
                enabled = Config.Disenchantment.Anvil.Sound.isEnabled();
                volume = Config.Disenchantment.Anvil.Sound.getVolume().floatValue();
                pitch = Config.Disenchantment.Anvil.Sound.getPitch().floatValue();
            }
            case SHATTER -> {
                enabled = Config.Shatterment.Anvil.Sound.isEnabled();
                volume = Config.Shatterment.Anvil.Sound.getVolume().floatValue();
                pitch = Config.Shatterment.Anvil.Sound.getPitch().floatValue();
            }
            default -> {
                enabled = Config.Erase.Anvil.Sound.isEnabled();
                volume = Config.Erase.Anvil.Sound.getVolume().floatValue();
                pitch = Config.Erase.Anvil.Sound.getPitch().floatValue();
            }
        }

        if (enabled) p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, volume, pitch);
    }

    private static Outcome settleBook(Kind kind, Player p, ItemStack firstItem, ItemStack secondItem, ItemStack result,
                                      List<IPluginEnchantment> enchantments, int repairCost, Inventory effects, Applier applier) {
        String cat = kind.category();
        boolean disenchant = kind == Kind.DISENCHANT;

        // Every abortable precondition must be checked BEFORE the player is charged — nothing below the
        // economy step may return without delivering the result.
        if (!(result.getItemMeta() instanceof EnchantmentStorageMeta resultItemMeta)) return Outcome.of(Status.IGNORED);

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        // The enchantments that really end up on the result book. The click-time collection is wider (it
        // also holds DELETE-state enchantments, and for shatter every split candidate, not just the
        // randomly chosen subset), so the charge — like the Prepare cost display — must be based on this
        // set, not on the full collection.
        List<IPluginEnchantment> transferred = EventUtils.findTransferredEnchantments(enchantments, result, activatedPlugins, p.getWorld());
        List<IPluginEnchantment> destroyed = disenchant
                ? EventUtils.Disenchantment.findEnchantmentsToDelete(enchantments)
                : EventUtils.Shatterment.findEnchantmentsToDelete(enchantments);

        // Listeners see what is transferred plus what a DELETE state destroys on the source; removing
        // a DELETE entry still vetoes its removal.
        List<IPluginEnchantment> preEnchantments = new ArrayList<>();
        for (IPluginEnchantment enchantment : enchantments) {
            if (transferred.contains(enchantment) || destroyed.contains(enchantment)) preEnchantments.add(enchantment);
        }

        List<IPluginEnchantment> approved;
        if (disenchant) {
            PreDisenchantEvent preEvent = new PreDisenchantEvent(p, firstItem.clone(), preEnchantments);
            Bukkit.getPluginManager().callEvent(preEvent);
            if (preEvent.isCancelled()) return Outcome.of(Status.CANCELLED);
            approved = preEvent.getEnchantments();
        } else {
            PreShatterEvent preEvent = new PreShatterEvent(p, firstItem.clone(), preEnchantments);
            Bukkit.getPluginManager().callEvent(preEvent);
            if (preEvent.isCancelled()) return Outcome.of(Status.CANCELLED);
            approved = preEvent.getEnchantments();
        }

        // ----------------------------------------------------------------------------------------------------
        // Build everything first. Adapters can throw here; nothing has been charged or written yet, so such a
        // failure costs the player nothing. The shown result is left untouched until the player has paid.

        double economyCost = economyCost(kind, EventUtils.retainByKey(approved, transferred));

        ItemStack finalFirstItem = firstItem.clone();
        List<IPluginEnchantment> enchantmentsToDelete = disenchant
                ? EventUtils.Disenchantment.findEnchantmentsToDelete(approved)
                : EventUtils.Shatterment.findEnchantmentsToDelete(approved);

        // Always remove the vanilla/Bukkit enchantments recorded on the result book from the source —
        // this must NOT depend on whether a custom enchantment plugin is active. A namespace-scoped
        // adapter (e.g. Vane) reports no vanilla enchantments, so gating this on "no plugins active"
        // left them on the source: the book was delivered while the source kept its enchantments — an
        // item dupe (issue #73).
        finalFirstItem = EnchantmentUtils.removeEnchantments(finalFirstItem, resultItemMeta.getStoredEnchants());

        // Additionally let each active adapter strip its own (e.g. vane:*) enchantments, which are
        // not part of the book's vanilla stored-enchant set. The shatter source is always an enchanted book.
        for (ISupportedPlugin activatedPlugin : activatedPlugins) {
            for (IPluginEnchantment enchantment : activatedPlugin.getItemEnchantments(result, p.getWorld())) {
                finalFirstItem = disenchant ? enchantment.removeFromItem(finalFirstItem) : enchantment.removeFromBook(finalFirstItem);
            }
        }

        for (IPluginEnchantment enchantment : enchantmentsToDelete) {
            finalFirstItem = disenchant ? enchantment.removeFromItem(finalFirstItem) : enchantment.removeFromBook(finalFirstItem);
        }

        boolean reset = disenchant ? Config.Disenchantment.Anvil.Repair.isResetEnabled() : Config.Shatterment.Anvil.Repair.isResetEnabled();
        if (reset) AnvilCostUtils.setItemRepairCost(finalFirstItem, 0);

        // Per-enchantment chance roll — failed enchantments are destroyed (already stripped from the
        // source above via the full stored-enchant set) rather than transferred to the result.
        //
        // In the same pass, restore each surviving enchantment's raw source level. The book pulled
        // from the anvil result slot may have had above-vanilla-max levels clamped down during the
        // PrepareAnvilEvent → result-slot round-trip (e.g. Unbreaking 4 → 3, issue #76). We re-stamp
        // from the freshly collected source levels via addStoredEnchant(..., true) and deliver the
        // book straight to the player, so the restored level is not clamped again.
        Map<String, Integer> sourceLevels = new HashMap<>();
        for (IPluginEnchantment ench : enchantments) sourceLevels.put(EventUtils.normalizeKey(ench.getKey()).toLowerCase(), ench.getLevel());

        ItemStack built = result.clone();
        Map<Enchantment, Integer> stored = new HashMap<>(resultItemMeta.getStoredEnchants());
        boolean metaChanged = false;
        for (Map.Entry<Enchantment, Integer> entry : stored.entrySet()) {
            Enchantment enchantment = entry.getKey();

            double chance = disenchant
                    ? Config.Disenchantment.getEnchantmentChance(enchantment.getKey().getKey())
                    : Config.Shatterment.getEnchantmentChance(enchantment.getKey().getKey());
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
        if (metaChanged) built.setItemMeta(resultItemMeta);

        // Every roll failed: an empty enchanted book would be a useless, confusing reward. Hand
        // back a plain book instead (unless an adapter enchantment still made it onto the book).
        final ItemStack rolled = built;
        boolean allTransfersFailed = !stored.isEmpty() && resultItemMeta.getStoredEnchants().isEmpty()
                && activatedPlugins.stream().allMatch(plugin -> plugin.getItemEnchantments(rolled, p.getWorld()).isEmpty());

        // Drop the preview lines (and the chance warning) that Prepare added from the delivered copy.
        PreviewLore.strip(built);

        ItemStack delivered = allTransfersFailed ? new ItemStack(Material.BOOK) : built;

        // ----------------------------------------------------------------------------------------------------
        // Pay, then write. A refusal changes nothing; a failure while writing refunds both payments.

        DiagnosticUtils.debug(cat, () -> "Click: economy check — enabled=" + kind.economy().isEnabled() + ", gameMode=" + p.getGameMode());
        Payment payment = takePayment(kind, p, economyCost, repairCost);
        if (payment == null) return Outcome.of(Status.CANCELLED);
        DiagnosticUtils.debug(cat, () -> "Click: xp → " + payment.levelBefore() + " - " + repairCost);

        // Past every abort: also drop the preview lines from the shown result, in place, so the result slot
        // cannot hand a second, still-annotated copy to vanilla's own pick-up.
        PreviewLore.strip(result);

        if (allTransfersFailed) {
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.enchantmentsFailedToTransfer());
            DiagnosticUtils.debug(cat, "Click: every enchantment failed its chance roll → plain book delivered");
        }

        try {
            applier.apply(finalFirstItem, delivered);
        } catch (RuntimeException | LinkageError e) {
            refund(kind, p, payment);
            throw e;
        }

        boolean creative = p.getGameMode() == GameMode.CREATIVE;
        int xpCost = creative ? 0 : repairCost;
        double finalEconomyCost = (kind.economy().isEnabled() && !creative) ? economyCost : 0.0;
        AnvilEventGuards.recordCooldownOperation(p);
        if (disenchant) {
            Bukkit.getPluginManager().callEvent(new PostDisenchantEvent(p, delivered.clone(), finalFirstItem.clone(), xpCost, finalEconomyCost));
        } else {
            ShatterEvent.clearSelection(p);
            Bukkit.getPluginManager().callEvent(new PostShatterEvent(p, delivered.clone(), finalFirstItem.clone(), xpCost, finalEconomyCost));
        }

        playSound(kind, p);
        ParticleUtils.playSuccess(p, effects);

        return new Outcome(Status.DONE, delivered, finalFirstItem, xpCost, finalEconomyCost);
    }

    private static Outcome settleErase(Player p, ItemStack firstItem, ItemStack secondItem, List<IPluginEnchantment> enchantments,
                                       int repairCost, Inventory effects, Applier applier) {
        PreEraseEvent preEvent = new PreEraseEvent(p, firstItem.clone(), new ArrayList<>(enchantments), secondItem.clone());
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) return Outcome.of(Status.CANCELLED);

        List<IPluginEnchantment> erased = new ArrayList<>(preEvent.getEnchantments());
        if (erased.isEmpty()) {
            DiagnosticUtils.debug("ERASE", "Click: listeners removed every enchantment → CANCELLED");
            return Outcome.of(Status.CANCELLED);
        }

        // Rebuilt from the live source item instead of trusting the result slot, so the delivered item is
        // exactly the source minus what the listeners agreed to erase. Built before anything is charged: an
        // adapter that throws here costs the player nothing.
        double economyCost = economyCost(Kind.ERASE, erased);
        ItemStack delivered = EraseUtils.buildResult(firstItem, erased);
        if (Config.Erase.Anvil.Repair.isResetEnabled()) AnvilCostUtils.setItemRepairCost(delivered, 0);

        // Pay (after PreEraseEvent, so a cancellation never charges), then write; a failure while writing refunds.
        DiagnosticUtils.debug("ERASE", () -> "Click: economy check — enabled=" + Config.Erase.Economy.isEnabled() + ", gameMode=" + p.getGameMode());
        Payment payment = takePayment(Kind.ERASE, p, economyCost, repairCost);
        if (payment == null) return Outcome.of(Status.CANCELLED);
        DiagnosticUtils.debug("ERASE", () -> "Click: xp → " + payment.levelBefore() + " - " + repairCost);

        try {
            applier.apply(null, delivered);
        } catch (RuntimeException | LinkageError e) {
            refund(Kind.ERASE, p, payment);
            throw e;
        }

        boolean creative = p.getGameMode() == GameMode.CREATIVE;
        int xpCost = creative ? 0 : repairCost;
        double finalEconomyCost = (Config.Erase.Economy.isEnabled() && !creative) ? economyCost : 0.0;
        AnvilEventGuards.recordCooldownOperation(p);
        Bukkit.getPluginManager().callEvent(new PostEraseEvent(p, delivered.clone(), List.copyOf(erased), xpCost, finalEconomyCost));

        playSound(Kind.ERASE, p);
        ParticleUtils.playErase(p, effects);

        if (DiagnosticUtils.isDebugEnabled()) {
            String names = erased.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(java.util.stream.Collectors.joining(", "));
            DiagnosticUtils.debug("ERASE", "Click: erased=[" + names + "]");
        }

        return new Outcome(Status.DONE, delivered, null, xpCost, finalEconomyCost);
    }
}
