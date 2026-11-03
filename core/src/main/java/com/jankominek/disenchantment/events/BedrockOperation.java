package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.EraseUtils;
import com.jankominek.disenchantment.utils.EventUtils;
import com.jankominek.disenchantment.utils.FeedbackUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * The operation logic behind the Bedrock chest menu: works out what a pair of input items would do (the
 * preview) and runs it (the confirmation). It owns no state and touches no inventory; the menu passes the
 * items in and an {@link AnvilOperations.Applier} to write them out. Everything that decides <em>whether</em>
 * and <em>how</em> an operation runs goes through the same guards and {@link AnvilOperations#settle} as the
 * anvil click handlers.
 */
public final class BedrockOperation {

    private BedrockOperation() {
    }

    /**
     * Whether and why an operation can run.
     */
    public enum Status {
        /**
         * Not both inputs are present yet.
         */
        WAITING,
        /**
         * The inputs are present but are no operation (or nothing is eligible).
         */
        NOTHING,
        /**
         * The operation can run.
         */
        READY,
        /**
         * The feature is off, or off in this world, or the plugin is disabled.
         */
        DISABLED,
        /**
         * Not near a special anvil while {@code special-anvils.mode} is not {@code ALL}.
         */
        SPECIAL_ANVIL,
        /**
         * Maintenance mode is on and the player has no bypass.
         */
        MAINTENANCE,
        /**
         * The player is on cooldown.
         */
        COOLDOWN,
        /**
         * The player lacks the permission for the operation.
         */
        PERMISSION,
        /**
         * The cost is at or above 40 levels and {@code bypass-too-expensive} is off.
         */
        TOO_EXPENSIVE
    }

    /**
     * What a pair of inputs would do for a player right now.
     *
     * @param kind         the detected operation, {@code null} when the inputs are no operation
     * @param status       whether it can run
     * @param enchantments the enchantments that would be transferred or erased (empty unless {@link Status#READY})
     * @param result       the item the player would get (a fresh, un-annotated copy; {@code null} unless {@link Status#READY})
     * @param xpCost       the XP levels it costs
     * @param economyCost  the money it costs
     */
    public record Plan(AnvilOperations.Kind kind, Status status, List<IPluginEnchantment> enchantments, ItemStack result,
                       int xpCost, double economyCost) {
        static Plan of(AnvilOperations.Kind kind, Status status) {
            return new Plan(kind, status, List.of(), null, 0, 0.0);
        }

        /**
         * @return whether the operation can run
         */
        public boolean isReady() {
            return status == Status.READY;
        }
    }

    /**
     * Detects the operation from the two inputs, the way the anvil handlers tell them apart: a plain book with
     * an enchanted book disenchants nothing but splits it (shatter), a plain book with any other item
     * disenchants it, and a catalyst with an item erases it.
     *
     * @param first  the main item (may be {@code null})
     * @param second the plain book / catalyst (may be {@code null})
     * @return the operation, or {@code null} when the pair is none
     */
    public static AnvilOperations.Kind detectKind(ItemStack first, ItemStack second) {
        if (first == null || second == null || first.getType().isAir() || second.getType().isAir()) return null;

        if (EventUtils.isBlankBook(second)) {
            return first.getType() == Material.ENCHANTED_BOOK ? AnvilOperations.Kind.SHATTER : AnvilOperations.Kind.DISENCHANT;
        }

        if (EraseUtils.isActive() && EraseUtils.isCatalyst(second)) return AnvilOperations.Kind.ERASE;

        return null;
    }

    /**
     * Works out what the inputs would do, without changing anything. This is the menu's equivalent of
     * {@code PrepareAnvilEvent}: same guards in the same order, same collection, same cost.
     *
     * @param p                 the player
     * @param first             the main item
     * @param second            the plain book / catalyst
     * @param specialAnvilReady whether the player is close enough to a special anvil (only relevant when
     *                          {@code special-anvils.mode} is not {@code ALL})
     * @return the plan
     */
    public static Plan evaluate(Player p, ItemStack first, ItemStack second, boolean specialAnvilReady) {
        boolean firstPresent = first != null && !first.getType().isAir();
        boolean secondPresent = second != null && !second.getType().isAir();
        if (!firstPresent || !secondPresent) return Plan.of(null, Status.WAITING);

        AnvilOperations.Kind kind = detectKind(first, second);
        if (kind == null) return Plan.of(null, Status.NOTHING);

        if (!Config.isPluginEnabled() || !kind.isActive() || kind.isWorldBlocked(p)) return Plan.of(kind, Status.DISABLED);

        if (!specialAnvilReady) return Plan.of(kind, Status.SPECIAL_ANVIL);

        if (AnvilEventGuards.isMaintenanceActiveFor(p)) return Plan.of(kind, Status.MAINTENANCE);

        if (AnvilEventGuards.isOnCooldownQuiet(p)) return Plan.of(kind, Status.COOLDOWN);

        if (!kind.permission().hasPermission(p, false)) return Plan.of(kind, Status.PERMISSION);

        List<IPluginEnchantment> collected = AnvilOperations.collect(kind, p, first, second, true);
        if (collected.isEmpty()) {
            DiagnosticUtils.debug("BEDROCK", "Preview: no eligible enchantments → nothing to do");
            return Plan.of(kind, Status.NOTHING);
        }

        // The split subset is cached per player and inputs, so the preview shown is the subset a confirmation applies.
        List<IPluginEnchantment> selected = kind == AnvilOperations.Kind.SHATTER
                ? ShatterEvent.selectForInputs(p, collected, first, second)
                : collected;
        if (selected.isEmpty()) return Plan.of(kind, Status.NOTHING);

        ItemStack result;
        if (kind == AnvilOperations.Kind.ERASE) {
            result = EraseUtils.buildResult(first, selected);
            if (Config.Erase.Anvil.Repair.isResetEnabled()) AnvilCostUtils.setItemRepairCost(result, 0);
        } else {
            result = AnvilOperations.buildBook(selected);
        }

        int xpCost = AnvilCostUtils.countAnvilCost(selected, kind.eventType(), p);
        double economyCost = AnvilOperations.economyCost(kind, selected);

        if (p.getGameMode() != GameMode.CREATIVE && AnvilEventGuards.isTooExpensive(kind.eventType(), xpCost)) {
            return Plan.of(kind, Status.TOO_EXPENSIVE);
        }

        return new Plan(kind, Status.READY, selected, result, xpCost, economyCost);
    }

    /**
     * Returns the localized text that explains a status to the player.
     *
     * @param status the status
     * @return the text
     */
    public static String describe(Status status) {
        return switch (status) {
            case WAITING -> I18n.Messages.bedrockStatusWaiting();
            case READY -> I18n.Messages.bedrockStatusReady();
            case NOTHING -> I18n.Messages.bedrockStatusNothing();
            case DISABLED -> I18n.Messages.bedrockStatusDisabled();
            case SPECIAL_ANVIL -> I18n.Messages.bedrockStatusSpecial();
            case MAINTENANCE -> I18n.Messages.maintenanceActive();
            case COOLDOWN -> I18n.Messages.bedrockStatusCooldown();
            case PERMISSION -> I18n.Messages.bedrockStatusPermission();
            case TOO_EXPENSIVE -> I18n.Messages.bedrockStatusExpensive();
        };
    }

    /**
     * Runs the operation for the given inputs: re-evaluates them, applies the click-time guards in the order
     * the anvil click handlers do (cooldown, XP, permission), collects the click-time enchantments, then
     * settles through {@link AnvilOperations#settle}. Nothing is changed or charged unless the outcome is
     * {@link AnvilOperations.Status#DONE}; refusals tell the player why.
     *
     * @param p                 the player
     * @param first             the live main item
     * @param second            the live plain book / catalyst
     * @param specialAnvilReady whether a special anvil is close enough right now
     * @param applier           writes the slots and hands the result to the player (called only on success)
     * @return the outcome ({@link AnvilOperations.Status#IGNORED} when the inputs cannot run)
     */
    public static AnvilOperations.Outcome confirm(Player p, ItemStack first, ItemStack second, boolean specialAnvilReady,
                                                  Applier applier) {
        Plan plan = evaluate(p, first, second, specialAnvilReady);
        AnvilOperations.Outcome ignored = new AnvilOperations.Outcome(AnvilOperations.Status.IGNORED, null, null, 0, 0.0);

        if (!plan.isReady()) {
            refuse(p, plan);
            return ignored;
        }

        AnvilOperations.Kind kind = plan.kind();

        // Same order as the click handlers: maintenance, cooldown, XP, permission.
        if (AnvilEventGuards.isMaintenanceBlocked(p)) return ignored;

        if (AnvilEventGuards.isOnCooldown(p)) return ignored;

        if (!AnvilEventGuards.hasEnoughXp(p, plan.xpCost())) {
            FeedbackUtils.refuseNotEnoughXp(p, plan.xpCost());
            return ignored;
        }

        if (!kind.permission().hasPermission(p)) return ignored;

        List<IPluginEnchantment> enchantments = AnvilOperations.collect(kind, p, first, second, false);
        if (enchantments.isEmpty()) {
            FeedbackUtils.refuse(p, describe(Status.NOTHING));
            return ignored;
        }

        // The menu holds the second item itself, so it is consumed here, exactly like the anvil handlers do:
        // one book (disenchant/shatter) or one catalyst (erase, unless consume-catalyst is off).
        ItemStack remainingSecond = kind == AnvilOperations.Kind.ERASE
                ? AnvilOperations.consumeCatalyst(second)
                : AnvilOperations.decrement(second);

        return AnvilOperations.settle(kind, p, first, second, plan.result(), enchantments, plan.xpCost(), null,
                (newFirst, delivered) -> applier.apply(newFirst, remainingSecond, delivered));
    }

    /**
     * Writes the outcome of a confirmed menu operation.
     */
    @FunctionalInterface
    public interface Applier {
        /**
         * @param newFirst     what the main slot holds afterwards ({@code null} = emptied)
         * @param newSecond    what the book / catalyst slot holds afterwards ({@code null} = emptied)
         * @param delivered    the result the player receives
         */
        void apply(ItemStack newFirst, ItemStack newSecond, ItemStack delivered);
    }

    private static void refuse(Player p, Plan plan) {
        // Waiting is not a refusal: the player simply has not put both items in yet.
        if (plan.status() == Status.WAITING) {
            FeedbackUtils.refuse(p, describe(Status.WAITING));
            return;
        }

        if (plan.status() == Status.COOLDOWN) {
            AnvilEventGuards.isOnCooldown(p);
            return;
        }

        if (plan.status() == Status.MAINTENANCE) {
            AnvilEventGuards.isMaintenanceBlocked(p);
            return;
        }

        FeedbackUtils.refuse(p, describe(plan.status()));
    }
}
