package com.jankominek.disenchantment.listeners;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.events.AnvilEventGuards;
import com.jankominek.disenchantment.guis.impl.BedrockAnvilGUI;
import com.jankominek.disenchantment.anvils.SpecialAnvilStore;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.BedrockUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Keeps the Bedrock chest-menu anvil honest and opens it from the optional sneak-click trigger.
 * <ul>
 *   <li>Closing the menu (by the player, the server, a teleport, another plugin) and quitting return the items
 *   in its input slots; kicks end in a quit, so they are covered too. Plugin disable is handled in
 *   {@code Disenchantment#disable}.</li>
 *   <li>With {@code bedrock.open-on-sneak-click}, a Bedrock player who sneaks and right-clicks an anvil gets the
 *   menu instead of the vanilla anvil; a normal tap still opens the vanilla anvil.</li>
 * </ul>
 * Registered once from the plugin's enable and reload.
 */
public class BedrockListener implements Listener {

    /**
     * Returns the menu's items when it is closed, however that happens.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof BedrockAnvilGUI gui) {
            DiagnosticUtils.debug("BEDROCK", "Menu closed → inputs returned");
            gui.onClose();
        }
    }

    /**
     * Returns the menu's items when the player leaves (this runs before the player data is saved).
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        BedrockAnvilGUI.onQuit(e.getPlayer());
    }

    /**
     * Opens the menu when a Bedrock player sneak-right-clicks an anvil, if {@code bedrock.open-on-sneak-click} is on.
     */
    // Late, and skipping cancelled events: a claim/region plugin that vetoed the click must be respected.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        try {
            if (BedrockAnvilGUI.isProbing()) return;
            if (e.useInteractedBlock() == Event.Result.DENY) return;
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND) return;
            if (!Config.Bedrock.isFallbackEnabled() || !Config.Bedrock.isOpenOnSneakClick()) return;

            Player p = e.getPlayer();
            if (!p.isSneaking()) return;

            Block block = e.getClickedBlock();
            if (block == null || !SpecialAnvilStore.isAnvilMaterial(block.getType())) return;

            // Only detected Bedrock players: allow-java-players is for the command, a Java player's sneak-click
            // keeps its vanilla meaning.
            if (!BedrockUtils.isBedrockPlayer(p)) return;

            if (!PermissionGroupType.COMMAND_ANVIL.hasPermission(p, false)) return;

            // A non-special anvil under a special-anvils restriction keeps opening the vanilla anvil.
            if (!AnvilEventGuards.isSpecialAnvilBlock(block)) return;

            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
            e.setCancelled(true);

            BedrockAnvilGUI.tryOpen(p, block);
        } catch (RuntimeException ex) {
            DiagnosticUtils.throwReport(ex);
        }
    }
}
