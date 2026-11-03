package com.jankominek.disenchantment.listeners;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.anvils.SpecialAnvilStore;
import com.jankominek.disenchantment.guis.impl.BedrockAnvilGUI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The sneak-click trigger and the close/quit hooks of the Bedrock menu.
 */
class BedrockListenerTest extends DisenchantmentTestBase {

    private PlayerMock bedrock;
    private Block anvil;

    @BeforeEach
    void setUp() {
        setConfig("bedrock.fallback-enabled", true);
        setConfig("bedrock.open-on-sneak-click", true);
        bedrock = server.addPlayer(".Steve");
        bedrock.setSneaking(true);
        bedrock.teleport(new org.bukkit.Location(world(), 2.5, 64, 2.5));
        anvil = world().getBlockAt(4, 64, 4);
        anvil.setType(Material.ANVIL);
    }

    @AfterEach
    void tearDown() {
        BedrockAnvilGUI.closeAll();
    }

    private PlayerInteractEvent interact(PlayerMock player, Block block, Action action, EquipmentSlot hand) {
        PlayerInteractEvent event = new PlayerInteractEvent(player, action, new ItemStack(Material.AIR), block, BlockFace.UP, hand);
        server.getPluginManager().callEvent(event);
        return event;
    }

    // -> sneak-click

    @Test
    void givenBedrockPlayerSneakClicksAnvil_thenMenuOpensAndVanillaAnvilIsSuppressed() {
        PlayerInteractEvent event = interact(bedrock, anvil, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND);

        assertNotNull(BedrockAnvilGUI.of(bedrock));
        assertTrue(event.isCancelled());
        assertEquals(org.bukkit.event.Event.Result.DENY, event.useInteractedBlock());
    }

    @Test
    void givenNormalTap_thenVanillaAnvilIsLeftAlone() {
        bedrock.setSneaking(false);

        PlayerInteractEvent event = interact(bedrock, anvil, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND);

        assertNull(BedrockAnvilGUI.of(bedrock));
        assertFalse(event.isCancelled());
    }

    @Test
    void givenOptionOff_thenSneakClickIsVanilla() {
        setConfig("bedrock.open-on-sneak-click", false);

        PlayerInteractEvent event = interact(bedrock, anvil, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND);

        assertNull(BedrockAnvilGUI.of(bedrock));
        assertFalse(event.isCancelled());
    }

    @Test
    void givenFallbackOff_thenSneakClickIsVanilla() {
        setConfig("bedrock.fallback-enabled", false);

        PlayerInteractEvent event = interact(bedrock, anvil, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND);

        assertNull(BedrockAnvilGUI.of(bedrock));
        assertFalse(event.isCancelled());
    }

    @Test
    void givenJavaPlayer_thenSneakClickStaysVanillaEvenWithAllowJavaPlayers() {
        setConfig("bedrock.allow-java-players", true);
        PlayerMock java = server.addPlayer("Alex");
        java.setSneaking(true);

        PlayerInteractEvent event = interact(java, anvil, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND);

        assertNull(BedrockAnvilGUI.of(java));
        assertFalse(event.isCancelled(), "a Java player's sneak-click keeps its vanilla meaning");
    }

    @Test
    void givenProtectionPluginCancelledTheClickEarlier_thenNoMenuOpens() {
        server.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGH)
            public void onInteract(PlayerInteractEvent e) {
                e.setCancelled(true);
            }
        }, plugin);

        interact(bedrock, anvil, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND);

        assertNull(BedrockAnvilGUI.of(bedrock), "a vetoed click must not open the menu");
    }

    @Test
    void givenProtectionPluginDeniedBlockUseEarlier_thenNoMenuOpens() {
        server.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGH)
            public void onInteract(PlayerInteractEvent e) {
                e.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            }
        }, plugin);

        interact(bedrock, anvil, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND);

        assertNull(BedrockAnvilGUI.of(bedrock));
    }

    @Test
    void givenOtherBlockOrActionOrHand_thenNothingHappens() {
        Block stone = world().getBlockAt(6, 64, 6);
        stone.setType(Material.STONE);

        assertFalse(interact(bedrock, stone, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND).isCancelled());
        assertFalse(interact(bedrock, anvil, Action.LEFT_CLICK_BLOCK, EquipmentSlot.HAND).isCancelled());
        assertFalse(interact(bedrock, anvil, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.OFF_HAND).isCancelled());

        assertNull(BedrockAnvilGUI.of(bedrock));
    }

    @Test
    void givenDamagedAnvil_thenSneakClickOpensTheMenuToo() {
        anvil.setType(Material.DAMAGED_ANVIL);

        interact(bedrock, anvil, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND);

        assertNotNull(BedrockAnvilGUI.of(bedrock));
    }

    @Test
    void givenMarkedMode_whenAnvilIsNotSpecial_thenVanillaAnvilOpens() {
        setConfig("special-anvils.mode", "MARKED");

        PlayerInteractEvent event = interact(bedrock, anvil, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND);

        assertNull(BedrockAnvilGUI.of(bedrock));
        assertFalse(event.isCancelled(), "the vanilla anvil (repair, rename) must keep working");
    }

    @Test
    void givenMarkedMode_whenAnvilIsSpecial_thenMenuOpens() {
        setConfig("special-anvils.mode", "MARKED");
        SpecialAnvilStore.mark(anvil);

        interact(bedrock, anvil, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND);

        assertNotNull(BedrockAnvilGUI.of(bedrock));
    }

    @Test
    void givenNoPermission_thenSneakClickIsVanilla() {
        bedrock.addAttachment(plugin, "disenchantment.command.anvil", false);

        PlayerInteractEvent event = interact(bedrock, anvil, Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND);

        assertNull(BedrockAnvilGUI.of(bedrock));
        assertFalse(event.isCancelled());
    }

    // -> close and quit hooks

    @Test
    void givenInventoryClosedByTheServer_thenInputsReturned() {
        bedrock.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
        BedrockAnvilGUI.open(bedrock);
        BedrockAnvilGUI gui = BedrockAnvilGUI.of(bedrock);
        gui.getInventory().setItem(BedrockAnvilGUI.SLOT_ITEM, new ItemStack(Material.DIAMOND_PICKAXE));

        bedrock.closeInventory();

        assertNull(BedrockAnvilGUI.of(bedrock));
        boolean found = false;
        for (ItemStack stack : bedrock.getInventory().getContents()) {
            if (stack != null && stack.getType() == Material.DIAMOND_PICKAXE) found = true;
        }
        assertTrue(found, "the pickaxe in the input slot is back in the inventory");
    }

    @Test
    void givenPlayerDisconnectsWithMenuOpen_thenInputsAreReturnedBeforeTheyLeave() {
        BedrockAnvilGUI.open(bedrock);
        BedrockAnvilGUI gui = BedrockAnvilGUI.of(bedrock);
        gui.getInventory().setItem(BedrockAnvilGUI.SLOT_ITEM, new ItemStack(Material.DIAMOND_PICKAXE));
        gui.getInventory().setItem(BedrockAnvilGUI.SLOT_SECOND, new ItemStack(Material.BOOK, 3));

        org.bukkit.event.player.PlayerQuitEvent quit = new org.bukkit.event.player.PlayerQuitEvent(bedrock, (net.kyori.adventure.text.Component) null);
        server.getPluginManager().callEvent(quit);

        assertNull(BedrockAnvilGUI.of(bedrock));
        int pickaxes = 0;
        int books = 0;
        for (ItemStack stack : bedrock.getInventory().getContents()) {
            if (stack == null) continue;
            if (stack.getType() == Material.DIAMOND_PICKAXE) pickaxes += stack.getAmount();
            if (stack.getType() == Material.BOOK) books += stack.getAmount();
        }
        assertEquals(1, pickaxes);
        assertEquals(3, books);
    }
}
