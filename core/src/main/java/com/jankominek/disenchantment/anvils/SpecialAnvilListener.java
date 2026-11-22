package com.jankominek.disenchantment.anvils;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.events.AnvilEventGuards;
import com.jankominek.disenchantment.types.PermissionType;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.SchedulerUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.jankominek.disenchantment.Disenchantment.plugin;

/**
 * Keeps the special anvil marks in step with the world: marks a placed special anvil item, clears
 * marks of removed anvils, hands the special item back when one is broken, and carries the mark
 * across a falling anvil.
 *
 * <p>Using an anvil can degrade it (position-based marks follow automatically, the state is
 * irrelevant) or destroy it without any event; both are covered by lazy validation in
 * {@link SpecialAnvilStore} plus a prune one tick after an anvil is closed. Placed anvils are immune
 * to explosions (blast resistance 1200) and cannot be moved by pistons, so neither needs handling.</p>
 *
 * <p>Registered unconditionally: whatever the current {@code special-anvils.mode}, an anvil placed
 * from the special item is remembered, so switching the mode later works for existing anvils.</p>
 */
public class SpecialAnvilListener implements Listener {
    private static final String FALL_KEY_NAME = "special_fall";
    private static final long PENDING_DROP_TTL_MS = 2000L;

    // Positions of special anvils broken by a player a moment ago. BlockBreakEvent already removes the
    // mark, but the drop swap happens in the later BlockDropItemEvent of the same break.
    private static final Map<String, Long> PENDING_DROPS = new ConcurrentHashMap<>();

    private static NamespacedKey fallKey() {
        return new NamespacedKey(plugin, FALL_KEY_NAME);
    }

    private static String dropKey(Block block) {
        return block.getWorld().getUID() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }

    /**
     * Clears the pending-drop bookkeeping (plugin disable / reload).
     */
    public static void reset() {
        PENDING_DROPS.clear();
    }

    // ----------------------------------------------------------------------------------------------------
    // Placement and removal

    /**
     * Always clears a stale mark at the placed anvil's position, then marks it when it was placed
     * from the special item by a player who may place special anvils. Without the permission the
     * anvil is simply placed as a normal anvil.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Block block = e.getBlockPlaced();
        if (!SpecialAnvilStore.isAnvilMaterial(block.getType())) return;

        SpecialAnvilStore.unmark(block);

        if (!SpecialAnvilItem.isSpecial(e.getItemInHand())) return;
        if (!PermissionType.ANVIL_SPECIAL.hasPermission(e.getPlayer())) return;

        SpecialAnvilStore.mark(block);
        DiagnosticUtils.debug("ANVILS", () -> "Placed special anvil at " + block.getX() + "," + block.getY() + "," + block.getZ());
    }

    /**
     * Removes the mark of a broken special anvil and remembers the break so its drop is swapped.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (!SpecialAnvilStore.isAnvilMaterial(block.getType())) return;
        if (!SpecialAnvilStore.isMarked(block)) return;

        SpecialAnvilStore.unmark(block);

        if (e.isDropItems() && e.getPlayer().getGameMode() != GameMode.CREATIVE && Config.SpecialAnvils.isDropSpecialItem()) {
            long now = System.currentTimeMillis();
            PENDING_DROPS.values().removeIf(expiry -> expiry < now);
            PENDING_DROPS.put(dropKey(block), now + PENDING_DROP_TTL_MS);
        }
    }

    /**
     * Swaps the anvil stack dropped by a broken special anvil for the special item, keeping the
     * damage state. Tool and enchantment rules were already applied by the server.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(BlockDropItemEvent e) {
        Long expiry = PENDING_DROPS.remove(dropKey(e.getBlock()));
        if (expiry == null || expiry < System.currentTimeMillis()) return;

        for (Item item : e.getItems()) swapDrop(item);
    }

    // ----------------------------------------------------------------------------------------------------
    // Gravity

    /**
     * Carries the mark across a falling anvil: a marked anvil that starts to fall is unmarked and its
     * falling entity is tagged (the tag persists with the entity); a tagged entity that lands marks the
     * landing position. Any other anvil landing clears a stale mark there. Best effort.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFallingBlock(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof FallingBlock falling)) return;

        Block block = e.getBlock();
        boolean fromAnvil = SpecialAnvilStore.isAnvilMaterial(block.getType());
        boolean toAnvil = SpecialAnvilStore.isAnvilMaterial(e.getTo());

        if (fromAnvil && !toAnvil) {
            if (!SpecialAnvilStore.isMarked(block)) return;

            SpecialAnvilStore.unmark(block);
            falling.getPersistentDataContainer().set(fallKey(), PersistentDataType.BYTE, (byte) 1);
        } else if (!fromAnvil && toAnvil) {
            SpecialAnvilStore.unmark(block);

            if (isTagged(falling)) {
                SpecialAnvilStore.mark(block);
                falling.getPersistentDataContainer().remove(fallKey());
            }
        }
    }

    /**
     * A tagged falling anvil that breaks into an item drops the special item.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFallingBlockDrop(EntityDropItemEvent e) {
        if (!(e.getEntity() instanceof FallingBlock falling) || !isTagged(falling)) return;

        if (Config.SpecialAnvils.isDropSpecialItem()) swapDrop(e.getItemDrop());
    }

    // ----------------------------------------------------------------------------------------------------
    // Use

    /**
     * Using an anvil can destroy it without an event. After a closed anvil with a mark, prune the
     * mark if the block is gone, right away and again one tick later (on the location's region thread).
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getType() != InventoryType.ANVIL) return;

        Location location = e.getInventory().getLocation();
        if (location == null || location.getWorld() == null) return;
        if (!SpecialAnvilStore.hasRawMark(location.getBlock())) return;

        SpecialAnvilStore.pruneIfStale(location.getBlock());
        SchedulerUtils.runForLocationLater(plugin, location, () -> SpecialAnvilStore.pruneIfStale(location.getBlock()), 1L);
    }

    /**
     * Drops the player's hint debounce entry.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        AnvilEventGuards.clearSpecialAnvilHint(e.getPlayer());
    }

    // ----------------------------------------------------------------------------------------------------
    // Helpers

    private static boolean isTagged(FallingBlock falling) {
        return falling.getPersistentDataContainer().has(fallKey(), PersistentDataType.BYTE);
    }

    private static void swapDrop(Item item) {
        ItemStack stack = item.getItemStack();
        if (!SpecialAnvilStore.isAnvilMaterial(stack.getType()) || SpecialAnvilItem.isSpecial(stack)) return;

        item.setItemStack(SpecialAnvilItem.create(stack.getType(), stack.getAmount()));
    }
}
