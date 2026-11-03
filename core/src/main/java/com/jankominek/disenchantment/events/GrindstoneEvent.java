package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.FeedbackUtils;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.EconomyUtils;
import com.jankominek.disenchantment.utils.EventUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles the grindstone prepare event for the two grindstone operations:
 * <ul>
 *   <li><b>Extract</b> — slot 0 enchanted item + slot 1 plain book: the result is an enchanted book.</li>
 *   <li><b>Transfer</b> — slot 0 enchanted item + slot 1 a different kind of item: the result is the
 *   slot 1 item carrying the compatible enchantments.</li>
 * </ul>
 * {@code PrepareGrindstoneEvent} does not exist in the Spigot 1.18 API the core is compiled against,
 * so the event is handled as a plain {@link Event}: the inventory is read through
 * {@link InventoryEvent#getInventory()} and the result is set through {@code setResult(ItemStack)}
 * via reflection. The same {@link #prepare} logic also runs from the tick-based fallback on servers
 * that have no prepare event at all.
 */
public class GrindstoneEvent {
    /**
     * Entry point for the grindstone prepare event.
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

    private static final AnvilEventGuards.EconomyConfig EXTRACT_ECONOMY = new AnvilEventGuards.EconomyConfig() {
        @Override
        public boolean isEnabled() {
            return Config.Grindstone.Economy.isEnabled();
        }

        @Override
        public double getCost() {
            return Config.Grindstone.Economy.getCost();
        }

        @Override
        public boolean isChargeMessageEnabled() {
            return Config.Grindstone.Economy.isChargeMessageEnabled();
        }

        @Override
        public boolean isShowCostEnabled() {
            return Config.Grindstone.Economy.isShowCostEnabled();
        }
    };

    private static final AnvilEventGuards.EconomyConfig TRANSFER_ECONOMY = new AnvilEventGuards.EconomyConfig() {
        @Override
        public boolean isEnabled() {
            return Config.Grindstone.Transfer.Economy.isEnabled();
        }

        @Override
        public double getCost() {
            return Config.Grindstone.Transfer.Economy.getCost();
        }

        @Override
        public boolean isChargeMessageEnabled() {
            return Config.Grindstone.Transfer.Economy.isChargeMessageEnabled();
        }

        @Override
        public boolean isShowCostEnabled() {
            return Config.Grindstone.Transfer.Economy.isShowCostEnabled();
        }
    };

    /**
     * Economy config of the extraction operation.
     */
    static AnvilEventGuards.EconomyConfig extractEconomy() {
        return EXTRACT_ECONOMY;
    }

    /**
     * Economy config of the transfer operation.
     */
    static AnvilEventGuards.EconomyConfig transferEconomy() {
        return TRANSFER_ECONOMY;
    }

    // Resolved lazily from the concrete event class; it differs between Spigot, Paper and Paper's legacy package.
    private static volatile Class<?> setResultOwner;
    private static volatile Method setResultMethod;

    private static void handler(Event event) {
        if (!(event instanceof InventoryEvent inventoryEvent)) return;

        Inventory inventory = inventoryEvent.getInventory();
        if (!(inventory instanceof GrindstoneInventory grindstone)) return;

        // getViewers() instead of getView().getPlayer(): InventoryView changed from class to interface in 1.21.
        if (grindstone.getViewers().isEmpty() || !(grindstone.getViewers().get(0) instanceof Player p)) return;

        prepare(p, grindstone, result -> setResult(event, result));
    }

    /**
     * Sets the result of a prepare event through reflection.
     */
    private static void setResult(Event event, ItemStack result) {
        try {
            Method method = setResultMethod;
            if (method == null || setResultOwner != event.getClass()) {
                method = event.getClass().getMethod("setResult", ItemStack.class);
                setResultMethod = method;
                setResultOwner = event.getClass();
            }
            method.invoke(event, result);
        } catch (ReflectiveOperationException ex) {
            DiagnosticUtils.throwReport(ex);
        }
    }

    /**
     * Computes the grindstone result for the current slot contents of {@code p}'s grindstone and hands it
     * to {@code setResult}. Does nothing for combinations that are not a plugin operation, so the vanilla
     * result (repair / disenchant) is left alone.
     *
     * @param p         the player using the grindstone
     * @param inventory the grindstone inventory
     * @param setResult receives the result item, or {@code null} to clear the result slot
     */
    public static void prepare(Player p, GrindstoneInventory inventory, Consumer<ItemStack> setResult) {
        if (!Config.isPluginEnabled()) return;

        boolean extractEnabled = Config.Grindstone.isEnabled();
        boolean transferEnabled = Config.Grindstone.Transfer.isEnabled();
        if (!extractEnabled && !transferEnabled) return;

        ItemStack first = inventory.getItem(0);
        ItemStack second = inventory.getItem(1);
        if (first == null || second == null || first.getType().isAir() || second.getType().isAir()) return;

        if (EventUtils.isBlankBook(second)) {
            if (extractEnabled) prepareExtract(p, first, second, setResult);
        } else if (transferEnabled) {
            prepareTransfer(p, first, second, setResult);
        }
    }

    private static void prepareExtract(Player p, ItemStack first, ItemStack book, Consumer<ItemStack> setResult) {
        if (AnvilEventGuards.isWorldBlocked(p, Config.Disenchantment.isWorldRestricted(p.getWorld()))) return;

        List<IPluginEnchantment> enchantments = GrindstoneOps.collectExtract(first, book, true, p.getWorld(), p);
        if (enchantments.isEmpty()) {
            DiagnosticUtils.debug("GRINDSTONE", "Prepare: no extractable enchantments → exit");
            return;
        }

        if (!isAllowed(p)) {
            setResult.accept(null);
            return;
        }

        if (!PermissionGroupType.GRINDSTONE_EXTRACT_EVENT.hasPermission(p, false)) {
            DiagnosticUtils.debug("GRINDSTONE", "Prepare: extract permission denied → exit");
            return;
        }

        setResult.accept(GrindstoneOps.buildExtractBook(enchantments));

        int xp = AnvilCostUtils.countAnvilCost(enchantments, AnvilEventType.GRINDSTONE_EXTRACT, p);
        double economyCost = AnvilCostUtils.economyCostForEnchantments(
                enchantments, Config.Grindstone.Economy.getCost(), Config.Grindstone.Repair.getEnchantmentEconomyCosts());
        showCost(p, xp, EXTRACT_ECONOMY, economyCost);
    }

    private static void prepareTransfer(Player p, ItemStack first, ItemStack target, Consumer<ItemStack> setResult) {
        if (AnvilEventGuards.isWorldBlocked(p, Config.Disenchantment.isWorldRestricted(p.getWorld()))) return;

        List<IPluginEnchantment> transfers = GrindstoneOps.computeTransfer(first, target, p);
        if (transfers.isEmpty()) {
            DiagnosticUtils.debug("GRINDSTONE", "Prepare: nothing transferable → exit");
            return;
        }

        if (!isAllowed(p)) {
            setResult.accept(null);
            return;
        }

        if (!PermissionGroupType.GRINDSTONE_TRANSFER_EVENT.hasPermission(p, false)) {
            DiagnosticUtils.debug("GRINDSTONE", "Prepare: transfer permission denied → exit");
            return;
        }

        setResult.accept(GrindstoneOps.buildTransferResult(target, transfers));

        int xp = AnvilCostUtils.countAnvilCost(transfers, AnvilEventType.GRINDSTONE_TRANSFER, p);
        double economyCost = AnvilCostUtils.economyCostForEnchantments(
                transfers, Config.Grindstone.Transfer.Economy.getCost(), Config.Grindstone.Transfer.Repair.getEnchantmentEconomyCosts());
        showCost(p, xp, TRANSFER_ECONOMY, economyCost);
    }

    /**
     * Maintenance and cooldown only block a genuine plugin operation, silently — the click path tells the player.
     */
    private static boolean isAllowed(Player p) {
        if (AnvilEventGuards.isMaintenanceActiveFor(p)) {
            DiagnosticUtils.debug("GRINDSTONE", "Prepare: maintenance active → result cleared");
            return false;
        }
        if (AnvilEventGuards.isOnCooldownQuiet(p)) {
            DiagnosticUtils.debug("GRINDSTONE", "Prepare: player on cooldown → result cleared");
            return false;
        }
        return true;
    }

    /**
     * The grindstone has no cost label, so the price is shown in the action bar.
     */
    private static void showCost(Player p, int xp, AnvilEventGuards.EconomyConfig economy, double economyCost) {
        if (p.getGameMode() == GameMode.CREATIVE) return;

        String text = null;
        if (economy.isEnabled() && economy.isShowCostEnabled() && EconomyUtils.isAvailable()) {
            text = I18n.Messages.grindstoneCostEconomy(String.valueOf(xp), EconomyUtils.format(economyCost));
        } else if (xp > 0) {
            text = I18n.Messages.grindstoneCost(String.valueOf(xp));
        }

        if (text != null) FeedbackUtils.sendActionBar(p, text);
    }

    /**
     * Tick-based fallback for servers without a grindstone prepare event: recomputes the result for
     * {@code p}'s open grindstone and writes it straight into the result slot. Only touches the slot when
     * the plugin has something to show, so vanilla results stay as they are.
     *
     * @param p         the player using the grindstone
     * @param inventory the grindstone inventory
     */
    public static void refresh(Player p, GrindstoneInventory inventory) {
        try {
            prepare(p, inventory, result -> inventory.setItem(2, result));
        } catch (Exception e) {
            DiagnosticUtils.throwReport(e);
        }
    }

    /**
     * Schedules {@link #refresh} for the next tick on the player's own thread.
     */
    public static void scheduleRefresh(Player p, GrindstoneInventory inventory) {
        com.jankominek.disenchantment.utils.SchedulerUtils.runForEntityLater(Disenchantment.plugin, p, () -> refresh(p, inventory), 1L);
    }
}
