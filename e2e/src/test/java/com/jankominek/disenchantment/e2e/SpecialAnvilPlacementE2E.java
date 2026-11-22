package com.jankominek.disenchantment.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.h7kz.proofstone.api.Agent;
import io.github.h7kz.proofstone.api.BlockPos;
import io.github.h7kz.proofstone.api.CellTest;
import io.github.h7kz.proofstone.api.Cell;
import io.github.h7kz.proofstone.api.Client;
import io.github.h7kz.proofstone.api.Face;
import io.github.h7kz.proofstone.api.Item;
import io.github.h7kz.proofstone.api.ItemView;
import io.github.h7kz.proofstone.api.Matrix;
import io.github.h7kz.proofstone.api.Platform;
import io.github.h7kz.proofstone.api.PlayerHandle;
import io.github.h7kz.proofstone.api.Proofstone;
import io.github.h7kz.proofstone.api.Window;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;

/**
 * Special anvils that a player really places and breaks ({@code Client.placeBlock} / {@code breakBlock}, Proofstone T-M11-04): the
 * chunk-PDC mark written by {@code SpecialAnvilStore} on a {@code BlockPlaceEvent}, {@code MARKED} mode on that block, and the drop swap
 * on breaking. Every block is placed against plain stone (placing against an anvil would open it). The mark itself is not readable from
 * outside, so its oracle is behaviour: in {@code MARKED} mode a placed special anvil offers a result and a placed plain anvil at the same
 * spot does not. Blocks are checked on the server with {@code Agent.blockType}.
 *
 * <p>Cells: paper@26.3, paper@1.21.11, folia@26.3 (the cells this was verified on).
 */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class SpecialAnvilPlacementE2E {

    private static final String SPECIAL_KEY = "disenchantment:special_anvil";
    /** Plain stone at (0,64,1); the anvil lands on it at (0,65,1), next to the player. */
    private static final BlockPos BASE = new BlockPos(0, 64, 1);
    private static final BlockPos PLACED = new BlockPos(0, 65, 1);

    /**
     * The block-break-speed attribute id of the cell's Minecraft version, or {@code null} when it has none. Added in 1.20.5 as
     * {@code minecraft:player.block_break_speed}, renamed to {@code minecraft:block_break_speed} in 1.21.2 (26.x keeps that id).
     * 1.18.2 and 1.20.4 have no such attribute, so a survival break there never completes in test time. The cell version is the
     * only handle: {@code Agent.command}'s reply is best effort and must not be asserted on.
     */
    static String breakSpeedAttribute(Cell cell) {
        String[] parts = cell.version().split("\\.");
        int major = Integer.parseInt(parts[0]);
        if (major != 1) {
            return "minecraft:block_break_speed"; // 26.x and later
        }
        int minor = Integer.parseInt(parts[1]);
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        if (minor > 21 || (minor == 21 && patch >= 2)) {
            return "minecraft:block_break_speed";
        }
        return minor > 20 || (minor == 20 && patch >= 5) ? "minecraft:player.block_break_speed" : null;
    }

    private static PlayerHandle standByTheBase(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextTo(agent, client, "minecraft:stone");
        agent.command("setblock 0 64 1 minecraft:stone");
        return player;
    }

    private static boolean offersABook(Client client) {
        try (Window anvil = client.openBlock(PLACED)) {
            anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
            anvil.moveFromInventory(item -> item.is("book"), 1);
            return !anvil.slot(2).isEmpty() && anvil.slot(2).is("enchanted_book");
        }
    }

    private static void placeHeldAnvil(Agent agent, Client client) {
        client.placeBlock(BASE, Face.UP);
        assertThat(agent.blockType("world", PLACED)).as("the anvil was placed").isEqualTo("minecraft:anvil");
    }

    private static void breakPlaced(Agent agent, Client client) {
        client.breakBlock(PLACED);
        assertThat(agent.blockType("world", PLACED)).as("the anvil was broken").isEqualTo("minecraft:air");
    }

    /** Moves the player onto the spot of the broken block, where the drop lies, so the pickup does not depend on the drop's random drift. */
    private static void standWhereTheDropIs(Agent agent, Client client) {
        agent.command("tp " + client.name() + " 0.5 65 1.5");
        client.awaitPosition("on the drop's spot", p -> p.y() >= 65.0 && p.y() < 65.5);
    }

    /** Empties the inventory, then the anvil is in hotbar slot 0 (the held item), followed by a sword and a book for the anvil window. */
    private static void loadPlainAnvil(Agent agent, Client client, PlayerHandle player) {
        agent.command("clear " + client.name());
        player.awaitState("empty inventory", state -> state.inventory().isEmpty());
        player.give(Item.of("anvil"));
        giveWindowItems(player);
    }

    private static void loadSpecialAnvil(Agent agent, Client client, PlayerHandle player) {
        agent.command("clear " + client.name());
        player.awaitState("empty inventory", state -> state.inventory().isEmpty());
        agent.command("disenchantment give anvil " + client.name() + " 1");
        player.awaitState("a special anvil in the inventory", SpecialAnvilPlacementE2E::holdsSpecialAnvil);
        giveWindowItems(player);
    }

    private static void giveWindowItems(PlayerHandle player) {
        player.give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3)).give(Item.of("book"));
    }

    private static boolean holdsSpecialAnvil(io.github.h7kz.proofstone.api.PlayerState state) {
        return state.inventory().values().stream()
                .anyMatch(item -> item.is("anvil") && item.pdc() != null && item.pdc().containsKey(SPECIAL_KEY));
    }

    private static ItemView firstAnvil(io.github.h7kz.proofstone.api.PlayerState state) {
        return state.inventory().values().stream().filter(item -> item.is("anvil")).findFirst().orElseThrow();
    }

    /**
     * MARKED mode follows the mark a placement writes. A plain anvil placed from a plain item is not marked (nothing offered); after
     * breaking it, a special anvil placed from the special item at the same spot is (the book is offered); after breaking that, a plain
     * anvil is refused again. Creative mode: the break is instant and no drop is involved.
     */
    @CellTest
    void markedModeWorksOnlyOnAnAnvilPlacedFromTheSpecialItem(Agent agent, Client client) {
        PlayerHandle player = standByTheBase(agent, client);
        agent.command("gamemode creative " + client.name());
        player.level(30);
        try (Cfg ignored = Cfg.with(agent, Map.of("special-anvils.mode", "MARKED"))) {
            loadPlainAnvil(agent, client, player);
            placeHeldAnvil(agent, client);
            assertThat(offersABook(client)).as("plain anvil placed in MARKED mode").isFalse();
            breakPlaced(agent, client);

            loadSpecialAnvil(agent, client, player);
            placeHeldAnvil(agent, client);
            assertThat(offersABook(client)).as("special anvil placed in MARKED mode").isTrue();
            breakPlaced(agent, client);

            loadPlainAnvil(agent, client, player);
            placeHeldAnvil(agent, client);
            assertThat(offersABook(client)).as("plain anvil placed after the special one was broken").isFalse();
            breakPlaced(agent, client);
        }
    }

    /**
     * Breaking a marked anvil in survival drops the special item, not a plain anvil (the drop swap); the control is a plain anvil
     * placed and broken the same way. Survival with the break-speed attribute raised and a pickaxe given into the freed main hand (an anvil drops
     * only for a pickaxe), so the break completes at once; the player stands next to the block and picks the drop up.
     */
    @CellTest
    void breakingAMarkedAnvilDropsTheSpecialItem(Cell cell, Agent agent, Client client) {
        String breakSpeed = breakSpeedAttribute(cell);
        Assumptions.assumeTrue(breakSpeed != null, "Minecraft " + cell.version() + " has no block-break-speed attribute (added in 1.20.5), "
                + "so a survival break cannot be made instant; the drop swap is covered on the cells that have it");
        PlayerHandle player = standByTheBase(agent, client);
        agent.command("gamemode survival " + client.name());
        agent.command("attribute " + client.name() + " " + breakSpeed + " base set 1000000");

        player.give(Item.of("anvil"));
        placeHeldAnvil(agent, client);
        player.give(Item.of("iron_pickaxe")); // the placed anvil freed hotbar slot 0, so the pickaxe is the held item
        breakPlaced(agent, client);
        standWhereTheDropIs(agent, client);
        ItemView plain = firstAnvil(player.awaitState("the dropped plain anvil is picked up",
                state -> state.inventory().values().stream().anyMatch(item -> item.is("anvil"))));
        assertThat(plain.name()).as("a plain anvil drops as a plain anvil").isNull();
        assertThat(plain.pdc() == null || !plain.pdc().containsKey(SPECIAL_KEY)).isTrue();

        // Back beside the base: the player now stands where the next anvil goes.
        agent.command("tp " + client.name() + " 0.5 64 0.5");
        client.awaitPosition("back beside the base", p -> p.y() == 64.0 && p.z() < 1.0);
        agent.command("clear " + client.name());
        agent.command("disenchantment give anvil " + client.name() + " 1");
        player.awaitState("a special anvil in the inventory", SpecialAnvilPlacementE2E::holdsSpecialAnvil);
        placeHeldAnvil(agent, client);
        player.give(Item.of("iron_pickaxe")); // the placed anvil freed hotbar slot 0, so the pickaxe is the held item
        breakPlaced(agent, client);
        standWhereTheDropIs(agent, client);
        ItemView dropped = firstAnvil(player.awaitState("the dropped special anvil is picked up", state ->
                state.inventory().values().stream().anyMatch(item -> item.is("anvil"))));
        assertThat(dropped.name()).contains("Enchanter's Anvil");
        assertThat(dropped.pdc()).containsEntry(SPECIAL_KEY, (byte) 1);
    }
}
