package com.jankominek.disenchantment.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.h7kz.proofstone.api.Agent;
import io.github.h7kz.proofstone.api.BlockPos;
import io.github.h7kz.proofstone.api.Click;
import io.github.h7kz.proofstone.api.Client;
import io.github.h7kz.proofstone.api.ItemView;
import io.github.h7kz.proofstone.api.PlayerHandle;
import io.github.h7kz.proofstone.api.Position;
import io.github.h7kz.proofstone.api.Window;
import java.util.List;

/** Shared setup for the Disenchantment suite. Not a test class (no E2E suffix). */
final class Fixture {

    static final String POST_DISENCHANT = "com.jankominek.disenchantment.events.api.PostDisenchantEvent";
    static final String POST_SHATTER = "com.jankominek.disenchantment.events.api.PostShatterEvent";
    static final BlockPos ANVIL = new BlockPos(0, 64, 2);
    /** The workstation block of a grindstone test is placed at the same spot as the anvil. */
    static final BlockPos STATION = ANVIL;
    static final String POST_ERASE = "com.jankominek.disenchantment.events.api.PostEraseEvent";
    static final String POST_GRIND_EXTRACT = "com.jankominek.disenchantment.events.api.PostGrindstoneExtractEvent";
    static final String POST_GRIND_TRANSFER = "com.jankominek.disenchantment.events.api.PostGrindstoneTransferEvent";
    static final String PRE_DISENCHANT = "com.jankominek.disenchantment.events.api.PreDisenchantEvent";

    private Fixture() {
    }

    /** Joins the world at a spot with ground, an anvil in reach and an empty inventory. */
    static PlayerHandle standNextToAnAnvil(Agent agent, Client client) {
        return standNextTo(agent, client, "minecraft:anvil");
    }

    /** Same as {@link #standNextToAnAnvil}, with any block (a block state string) at {@link #STATION}. */
    static PlayerHandle standNextTo(Agent agent, Client client, String blockState) {
        // Reset first: it also teleports the player to the fixture spot, which would undo the teleport below.
        PlayerHandle player = agent.player(client).reset();
        player.awaitState("empty inventory", state -> state.inventory().isEmpty());
        // Chunks load around players only, and flat worlds spawn underground (Proofstone docs/platforms/paper.md).
        agent.command("setblock 0 63 0 minecraft:stone");
        agent.command("setblock 0 63 2 minecraft:stone");
        agent.command("setblock 0 64 2 " + blockState);
        agent.command("tp " + client.name() + " 0.5 64 0.5");
        Position at = client.awaitPosition("teleport to the anvil", p -> p.y() == 64.0);
        assertThat(at.x()).isEqualTo(0.5);
        return player;
    }

    /** The first empty slot after the anvil's own three, that is the player's inventory in server order. */
    static int firstFreePlayerSlot(Window window) {
        List<ItemView> slots = window.slots();
        for (int slot = 3; slot < slots.size(); slot++) {
            if (slots.get(slot).isEmpty()) {
                return slot;
            }
        }
        throw new IllegalStateException("no free player slot in the anvil window: " + slots);
    }

    /**
     * Sends a click that the plugin must refuse. A refused click changes nothing and the driver sends no predicted
     * changes, so the server has no mismatch to resync and {@code Window.click} reports that the server did not answer.
     * That one message is accepted here. Every other failure is rethrown, and the effects are asserted on the server
     * oracle afterwards (an unrefused click would deliver, move or drop a book and show there).
     */
    static void refusedClick(Window anvil, int slot, Click click) {
        try {
            anvil.click(slot, click);
        } catch (RuntimeException e) {
            if (e.getMessage() == null || !e.getMessage().startsWith("server did not answer " + click + " on slot " + slot)) {
                throw e;
            }
        }
    }

    /** The window slot of the first player-inventory item of the given type. */
    static int firstSlotOf(Window window, String type) {
        List<ItemView> slots = window.slots();
        for (int slot = 3; slot < slots.size(); slot++) {
            if (slots.get(slot).is(type)) {
                return slot;
            }
        }
        throw new IllegalStateException("no " + type + " in the window: " + slots);
    }
}
