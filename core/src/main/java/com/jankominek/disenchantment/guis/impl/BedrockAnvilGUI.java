package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.events.AnvilEventGuards;
import com.jankominek.disenchantment.events.AnvilOperations;
import com.jankominek.disenchantment.events.BedrockOperation;
import com.jankominek.disenchantment.events.ShatterEvent;
import com.jankominek.disenchantment.guis.GUIBorderComponent;
import com.jankominek.disenchantment.guis.ItemBuilder;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.utils.BedrockUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.EconomyUtils;
import com.jankominek.disenchantment.utils.EraseUtils;
import com.jankominek.disenchantment.utils.PreviewLore;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import com.jankominek.disenchantment.types.SpecialAnvilMode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * The Bedrock fallback: a three-row chest menu that does what the anvil does, for clients (Geyser/Bedrock)
 * that cannot see custom anvil results.
 *
 * <pre>
 *  . . . . . . . . .
 *  . [item] . [book/catalyst] (mode) [result] . [confirm] .
 *  . label . label . label . . . [cancel] .
 * </pre>
 *
 * <p><b>Items.</b> The two input slots hold the player's <em>real</em> items, taken out of their inventory
 * when put in. Plugin GUIs have every click cancelled by the click router, so moving is explicit: tapping an
 * item in the player's own inventory puts <strong>one</strong> of it into the matching input slot (a plain
 * book or a catalyst into the second slot, anything else into the first), tapping an input slot gives its
 * content back. Whatever is in the input slots is handed back on close, quit, kick, plugin disable and
 * reload ({@link #finish()} is idempotent), to the inventory or, when full, dropped at the player's feet.
 * The result slot, the mode label and the buttons are display-only and never given to anyone.</p>
 *
 * <p><b>Operations.</b> Confirming goes through {@link BedrockOperation#confirm}, which uses the same guards
 * and {@code AnvilOperations.settle} as the anvil click handlers.</p>
 */
public class BedrockAnvilGUI implements InventoryHolder {
    /**
     * Inventory size (three rows).
     */
    public static final int SIZE = 27;
    /**
     * Slot of the main item.
     */
    public static final int SLOT_ITEM = 10;
    /**
     * Slot of the plain book / catalyst.
     */
    public static final int SLOT_SECOND = 12;
    /**
     * Slot of the mode label.
     */
    public static final int SLOT_MODE = 13;
    /**
     * Slot of the result preview.
     */
    public static final int SLOT_RESULT = 14;
    /**
     * Slot of the confirm button.
     */
    public static final int SLOT_CONFIRM = 16;
    /**
     * Slot of the cancel button.
     */
    public static final int SLOT_CANCEL = 25;

    private static final int SLOT_LABEL_ITEM = 19;
    private static final int SLOT_LABEL_SECOND = 21;
    private static final int SLOT_LABEL_RESULT = 23;

    /**
     * How far away the player may look at the special anvil to open the menu by command.
     */
    public static final int REACH = 5;

    /**
     * How far from the remembered special anvil the player may be while the menu is open.
     */
    public static final int LEAVE_DISTANCE = 6;

    /**
     * Minimum time between two successful confirmations, so a double tap cannot run (and charge) twice.
     */
    public static final long CONFIRM_DEBOUNCE_MS = 500L;

    // ConcurrentHashMap for Folia region-thread safety. One menu per player.
    private static final Map<UUID, BedrockAnvilGUI> OPEN = new ConcurrentHashMap<>();

    private static volatile LongSupplier clock = System::currentTimeMillis;

    private static volatile Function<Player, Block> targetResolver = BedrockAnvilGUI::lookedAtBlock;

    // True while a synthetic interact event is being fired, so our own listener does not react to it.
    private static final ThreadLocal<Boolean> PROBING = ThreadLocal.withInitial(() -> false);

    private static Block lookedAtBlock(Player p) {
        try {
            return p.getTargetBlockExact(REACH);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Replaces how the block the player looks at is found. Intended for tests.
     *
     * @param resolver finds the block within reach, or {@code null} to restore the default ray trace
     */
    public static void setTargetResolver(Function<Player, Block> resolver) {
        targetResolver = resolver == null ? BedrockAnvilGUI::lookedAtBlock : resolver;
    }

    /**
     * Returns whether our own synthetic access probe is being dispatched right now.
     *
     * @return {@code true} while {@link #tryOpen} fires its probe event
     */
    public static boolean isProbing() {
        return PROBING.get();
    }

    private final Player player;
    private final Inventory inventory;
    private boolean finished;
    private boolean busy;
    // The special anvil this menu is tied to; null when special-anvils.mode is ALL (the menu then acts like a virtual anvil).
    private final Block anvil;
    private long lastConfirmAt = Long.MIN_VALUE / 2;

    // ----------------------------------------------------------------------------------------------------
    // Opening

    /**
     * Replaces the time source of the confirmation debounce. Intended for tests.
     *
     * @param newClock supplies the current time in milliseconds, or {@code null} to restore the system clock
     */
    public static void setClock(LongSupplier newClock) {
        clock = newClock == null ? System::currentTimeMillis : newClock;
    }

    /**
     * Opens the menu for {@code p} if the fallback may be used: it is enabled, the player is a Bedrock player
     * (or {@code allow-java-players} is on), and with {@code special-anvils.mode} other than {@code ALL} the player
     * is tied to one special anvil they can actually use. Tells the player why not otherwise.
     * <p>
     * By command ({@code clicked == null}) the player must be <em>looking at</em> a special anvil within
     * {@value #REACH} blocks, and a synthetic right-click {@link PlayerInteractEvent} on that anvil must not be
     * vetoed, so claim and region plugins decide whether this player may use that block. A sneak-click already is
     * a real interact event, so the clicked anvil is used as is. The anvil is remembered and checked again on every
     * click. In {@code ALL} mode nothing is tied to a block: the command works anywhere, like a virtual anvil.
     *
     * @param p       the player
     * @param clicked the anvil block the player sneak-clicked, or {@code null} when opened by command
     * @return {@code true} if the menu was opened
     */
    public static boolean tryOpen(Player p, Block clicked) {
        if (!Config.isPluginEnabled() || !Config.Bedrock.isFallbackEnabled()) {
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.bedrockUnavailable());
            return false;
        }

        if (!Config.Bedrock.isAllowJavaPlayers() && !BedrockUtils.isBedrockPlayer(p)) {
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.bedrockOnly());
            return false;
        }

        Block anvil = null;
        if (Config.SpecialAnvils.getMode() != SpecialAnvilMode.ALL) {
            anvil = clicked != null ? clicked : targetResolver.apply(p);

            if (anvil == null || !AnvilEventGuards.isSpecialAnvilBlock(anvil)
                    || !isWithinReach(p, anvil, clicked != null ? LEAVE_DISTANCE : REACH + 1)) {
                p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.specialAnvilRequired());
                return false;
            }

            if (clicked == null && !mayUse(p, anvil)) {
                p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.requiresPermission());
                return false;
            }
        }

        open(p, anvil);
        return true;
    }

    private static boolean isWithinReach(Player p, Block block, int distance) {
        if (block.getWorld() == null || !block.getWorld().equals(p.getWorld())) return false;

        Location at = p.getLocation();
        double dx = at.getX() - (block.getX() + 0.5);
        double dy = at.getY() - (block.getY() + 0.5);
        double dz = at.getZ() - (block.getZ() + 0.5);
        return dx * dx + dy * dy + dz * dz <= (double) distance * distance;
    }

    /**
     * Asks whoever protects the block whether this player may right-click it, by firing the event a real
     * right-click would. A cancelled event, or one whose block use is denied, is a veto.
     */
    private static boolean mayUse(Player p, Block block) {
        PlayerInteractEvent probe = new PlayerInteractEvent(p, Action.RIGHT_CLICK_BLOCK,
                p.getInventory().getItemInMainHand(), block, BlockFace.UP, EquipmentSlot.HAND);

        PROBING.set(true);
        try {
            Bukkit.getPluginManager().callEvent(probe);
        } finally {
            PROBING.set(false);
        }

        return !probe.isCancelled() && probe.useInteractedBlock() != Event.Result.DENY;
    }

    /**
     * Whether the special-anvil requirement still holds: nothing is required in {@code ALL} mode; otherwise the
     * remembered block must still be a special anvil, in the player's world and within {@value #LEAVE_DISTANCE} blocks.
     */
    public boolean specialAnvilHolds() {
        if (Config.SpecialAnvils.getMode() == SpecialAnvilMode.ALL) return true;

        return anvil != null && isWithinReach(player, anvil, LEAVE_DISTANCE) && AnvilEventGuards.isSpecialAnvilBlock(anvil);
    }

    /**
     * Opens a fresh menu for {@code p} that is not tied to an anvil (callers gate with {@link #tryOpen}).
     *
     * @param p the player
     * @return the opened menu
     */
    public static BedrockAnvilGUI open(Player p) {
        return open(p, null);
    }

    /**
     * Opens a fresh menu for {@code p} without any permission or setting check (callers gate with
     * {@link #tryOpen}). A menu the player already has open is closed first and its items returned.
     *
     * @param p     the player
     * @param anvil the special anvil the menu is tied to, or {@code null}
     * @return the opened menu
     */
    public static BedrockAnvilGUI open(Player p, Block anvil) {
        BedrockAnvilGUI existing = OPEN.get(p.getUniqueId());
        if (existing != null) existing.finish();

        BedrockAnvilGUI gui = new BedrockAnvilGUI(p, anvil);
        OPEN.put(p.getUniqueId(), gui);
        gui.refresh();

        p.openInventory(gui.inventory);
        return gui;
    }

    /**
     * Returns the menu {@code p} currently has registered, if any.
     *
     * @param p the player
     * @return the menu or {@code null}
     */
    public static BedrockAnvilGUI of(Player p) {
        return OPEN.get(p.getUniqueId());
    }

    /**
     * Gives every open menu's items back and closes the menus. Called when the plugin is disabled, because
     * Bukkit does not close inventories when a plugin goes away.
     */
    public static void closeAll() {
        for (BedrockAnvilGUI gui : new ArrayList<>(OPEN.values())) {
            try {
                gui.finish();
                gui.player.closeInventory();
            } catch (RuntimeException e) {
                // One broken menu (for example a player on another region thread) must not stop the others
                // or the shutdown.
                DiagnosticUtils.debug("BEDROCK", () -> "closeAll: could not close the menu of " + gui.player.getName() + ": " + e);
            }
        }
    }

    private BedrockAnvilGUI(Player player, Block anvil) {
        this.player = player;
        this.anvil = anvil;
        this.inventory = Bukkit.createInventory(this, SIZE,
                LegacyComponentSerializer.legacySection().deserialize(I18n.Messages.bedrockTitle()));
    }

    // ----------------------------------------------------------------------------------------------------
    // Items

    /**
     * Gives a stack to the player; whatever does not fit is dropped at their feet.
     */
    private void giveOrDrop(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return;

        // Whatever goes wrong while adding, the stack must end up somewhere: on the floor at worst.
        Collection<ItemStack> leftovers;
        try {
            leftovers = player.getInventory().addItem(stack).values();
        } catch (RuntimeException e) {
            DiagnosticUtils.debug("BEDROCK", () -> "addItem failed, dropping the stack instead: " + e);
            leftovers = List.of(stack);
        }

        boolean dropped = false;
        for (ItemStack leftover : leftovers) {
            player.getWorld().dropItem(player.getLocation(), leftover);
            dropped = true;
        }

        if (dropped) player.sendMessage(I18n.getPrefix() + " " + I18n.Messages.bedrockItemsDropped());

        try {
            player.updateInventory();
        } catch (RuntimeException e) {
            DiagnosticUtils.debug("BEDROCK", () -> "updateInventory failed: " + e);
        }
    }

    private ItemStack take(int slot) {
        ItemStack stack = inventory.getItem(slot);
        inventory.setItem(slot, null);
        return stack;
    }

    /**
     * Hands both input slots back to the player. Idempotent: the slots are emptied before anything is given,
     * so a second call (close event after quit, plugin disable after close, ...) finds nothing to return.
     */
    public void returnInputs() {
        ItemStack first = take(SLOT_ITEM);
        ItemStack second = take(SLOT_SECOND);

        giveOrDrop(first);
        giveOrDrop(second);
    }

    /**
     * Ends the session: returns the inputs, forgets the menu and the cached split selection. Safe to call
     * any number of times, from the close, quit and disable paths alike.
     */
    public void finish() {
        returnInputs();
        finished = true;

        OPEN.remove(player.getUniqueId(), this);
        ShatterEvent.clearSelection(player);
    }

    /**
     * Called when the inventory is closed (by the player or the server).
     */
    public void onClose() {
        finish();
    }

    /**
     * Called when the player leaves the server while the menu is open.
     *
     * @param p the player who left
     */
    public static void onQuit(Player p) {
        BedrockAnvilGUI gui = OPEN.get(p.getUniqueId());
        if (gui != null) gui.finish();
    }

    // ----------------------------------------------------------------------------------------------------
    // Clicks

    /**
     * Handles a click anywhere in the menu's window (the menu itself or the player's own inventory below it).
     * The router has already cancelled the event.
     *
     * @param event the click
     */
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (finished) return;

        // Walked away, or the anvil was broken or unmarked: the menu may no longer be used. Everything comes back.
        if (!specialAnvilHolds()) {
            finish();
            player.closeInventory();
            player.sendMessage(I18n.getPrefix() + " " + I18n.Messages.specialAnvilRequired());
            return;
        }

        ClickType click = event.getClick();
        if (click != ClickType.LEFT && click != ClickType.RIGHT
                && click != ClickType.SHIFT_LEFT && click != ClickType.SHIFT_RIGHT) return;

        int raw = event.getRawSlot();
        if (raw < 0) return;

        if (raw < SIZE) {
            clickMenu(raw);
        } else {
            moveIn(event.getSlot());
        }
    }

    private void clickMenu(int slot) {
        switch (slot) {
            case SLOT_ITEM, SLOT_SECOND -> {
                giveOrDrop(take(slot));
                refresh();
            }
            case SLOT_CONFIRM -> confirm();
            case SLOT_CANCEL -> {
                finish();
                player.closeInventory();
            }
            default -> {
            }
        }
    }

    private static int targetSlot(ItemStack stack) {
        if (stack.getType() == Material.BOOK) return SLOT_SECOND;
        if (EraseUtils.isActive() && EraseUtils.isCatalyst(stack)) return SLOT_SECOND;
        return SLOT_ITEM;
    }

    /**
     * Moves one item from the player's inventory slot into the matching input slot. The item leaves the
     * inventory and enters the menu in the same step; whatever the input slot held before goes back.
     */
    private void moveIn(int playerSlot) {
        PlayerInventory inv = player.getInventory();
        if (playerSlot < 0 || playerSlot >= inv.getSize()) return;

        ItemStack stack = inv.getItem(playerSlot);
        if (stack == null || stack.getType().isAir()) return;

        int target = targetSlot(stack);
        ItemStack current = inventory.getItem(target);
        boolean occupied = current != null && !current.getType().isAir();

        ItemStack one = stack.clone();
        one.setAmount(1);

        if (occupied && target == SLOT_SECOND && current.isSimilar(one)) {
            // One more of what is already there (an operation uses one at a time, so a few are enough).
            if (current.getAmount() >= current.getMaxStackSize()) return;

            inv.setItem(playerSlot, AnvilOperations.decrement(stack));
            ItemStack merged = current.clone();
            merged.setAmount(current.getAmount() + 1);
            inventory.setItem(target, merged);
        } else {
            inv.setItem(playerSlot, AnvilOperations.decrement(stack));
            inventory.setItem(target, one);
            if (occupied) giveOrDrop(current);
        }

        player.updateInventory();
        refresh();
    }

    private void confirm() {
        if (busy) return;

        long now = clock.getAsLong();
        if (now >= lastConfirmAt && now - lastConfirmAt < CONFIRM_DEBOUNCE_MS) return;

        busy = true;
        try {
            AnvilOperations.Outcome outcome = BedrockOperation.confirm(
                    player, inventory.getItem(SLOT_ITEM), inventory.getItem(SLOT_SECOND), specialAnvilHolds(),
                    (newFirst, newSecond, delivered) -> {
                        inventory.setItem(SLOT_ITEM, newFirst);
                        inventory.setItem(SLOT_SECOND, newSecond);
                        giveOrDrop(delivered);
                    });

            if (outcome.status() == AnvilOperations.Status.DONE) lastConfirmAt = now;
        } finally {
            busy = false;
        }

        refresh();
    }

    // ----------------------------------------------------------------------------------------------------
    // Rendering

    /**
     * Re-evaluates the inputs and redraws the mode label, the result preview and the confirm button.
     */
    public void refresh() {
        BedrockOperation.Plan plan = BedrockOperation.evaluate(
                player, inventory.getItem(SLOT_ITEM), inventory.getItem(SLOT_SECOND), specialAnvilHolds());

        for (int slot = 0; slot < SIZE; slot++) {
            if (isInputOrDisplay(slot)) continue;
            inventory.setItem(slot, GUIBorderComponent.borderItem());
        }

        inventory.setItem(SLOT_LABEL_ITEM, label(I18n.Messages.bedrockItemLabel(), I18n.Messages.bedrockItemLabelLore()));
        inventory.setItem(SLOT_LABEL_SECOND, label(I18n.Messages.bedrockSecondLabel(), I18n.Messages.bedrockSecondLabelLore()));
        inventory.setItem(SLOT_LABEL_RESULT, label(I18n.Messages.bedrockResultLabel(), I18n.Messages.bedrockResultEmpty()));

        inventory.setItem(SLOT_MODE, new ItemBuilder(Material.ANVIL)
                .setDisplayName(modeTitle(plan.kind()))
                .setLore(BedrockOperation.describe(plan.status()))
                .addAllFlags()
                .build());

        inventory.setItem(SLOT_RESULT, resultItem(plan));

        ItemBuilder confirm = new ItemBuilder(plan.isReady() ? Material.EMERALD : Material.GRAY_DYE)
                .setDisplayName(I18n.Messages.bedrockConfirm());
        confirm.setLore(plan.isReady() ? I18n.Messages.bedrockConfirmLore() : List.of(BedrockOperation.describe(plan.status())));
        inventory.setItem(SLOT_CONFIRM, confirm.addAllFlags().build());

        inventory.setItem(SLOT_CANCEL, new ItemBuilder(Material.BARRIER)
                .setDisplayName(I18n.Messages.bedrockCancel())
                .setLore(I18n.Messages.bedrockCancelLore())
                .addAllFlags()
                .build());
    }

    private static boolean isInputOrDisplay(int slot) {
        return slot == SLOT_ITEM || slot == SLOT_SECOND;
    }

    private static ItemStack label(String title, List<String> lore) {
        return new ItemBuilder(Material.PAPER).setDisplayName(title).setLore(lore).addAllFlags().build();
    }

    private static String modeTitle(AnvilOperations.Kind kind) {
        if (kind == null) return I18n.Messages.bedrockModeNone();

        return switch (kind) {
            case DISENCHANT -> I18n.Messages.bedrockModeDisenchant();
            case SHATTER -> I18n.Messages.bedrockModeShatter();
            case ERASE -> I18n.Messages.bedrockModeErase();
        };
    }

    private ItemStack resultItem(BedrockOperation.Plan plan) {
        if (!plan.isReady()) {
            boolean blocked = plan.status() != BedrockOperation.Status.WAITING && plan.status() != BedrockOperation.Status.NOTHING;
            return new ItemBuilder(blocked ? Material.RED_STAINED_GLASS_PANE : Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                    .setDisplayName(I18n.Messages.bedrockResultLabel())
                    .setLore(List.of(BedrockOperation.describe(plan.status())))
                    .addAllFlags()
                    .build();
        }

        // A display copy: the delivered item is built again at confirmation, so none of these lines can leak.
        ItemStack preview = plan.result().clone();
        return PreviewLore.append(preview, previewLines(plan));
    }

    private List<String> previewLines(BedrockOperation.Plan plan) {
        AnvilOperations.Kind kind = plan.kind();
        List<String> lines = new ArrayList<>();

        List<String> names = new ArrayList<>();
        boolean chance = false;
        for (IPluginEnchantment enchantment : plan.enchantments()) {
            names.add(PreviewLore.describe(enchantment));
            if (kind == AnvilOperations.Kind.DISENCHANT && Config.Disenchantment.getEnchantmentChance(enchantment.getKey()) < 1.0) chance = true;
            if (kind == AnvilOperations.Kind.SHATTER && Config.Shatterment.getEnchantmentChance(enchantment.getKey()) < 1.0) chance = true;
        }
        String joined = String.join(", ", names);
        lines.add(kind == AnvilOperations.Kind.ERASE ? I18n.Messages.bedrockErases(joined) : I18n.Messages.previewTransfers(joined));

        boolean creative = player.getGameMode() == GameMode.CREATIVE;
        int levels = creative ? 0 : plan.xpCost();
        boolean showMoney = !creative && kind.economy().isEnabled() && kind.economy().isShowCostEnabled()
                && EconomyUtils.isAvailable() && plan.economyCost() > 0;

        if (levels > 0 && showMoney) {
            lines.add(I18n.Messages.previewCostLevelsMoney(String.valueOf(levels), EconomyUtils.format(plan.economyCost())));
        } else if (levels > 0) {
            lines.add(I18n.Messages.previewCostLevels(String.valueOf(levels)));
        } else if (showMoney) {
            lines.add(I18n.Messages.previewCostMoney(EconomyUtils.format(plan.economyCost())));
        }

        if (chance) lines.add(I18n.Messages.someEnchantmentsMayNotTransfer());

        return lines;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
