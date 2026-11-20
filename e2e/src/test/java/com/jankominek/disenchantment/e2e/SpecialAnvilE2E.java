package com.jankominek.disenchantment.e2e;

import static com.jankominek.disenchantment.e2e.Fixture.ANVIL;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.h7kz.proofstone.api.Agent;
import io.github.h7kz.proofstone.api.CellTest;
import io.github.h7kz.proofstone.api.Click;
import io.github.h7kz.proofstone.api.Client;
import io.github.h7kz.proofstone.api.Item;
import io.github.h7kz.proofstone.api.ItemView;
import io.github.h7kz.proofstone.api.Matrix;
import io.github.h7kz.proofstone.api.Platform;
import io.github.h7kz.proofstone.api.PlayerHandle;
import io.github.h7kz.proofstone.api.PluginHandle;
import io.github.h7kz.proofstone.api.Proofstone;
import io.github.h7kz.proofstone.api.Window;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Special anvils. The gating by {@code special-anvils.mode} is tested with the two modes that need no block placement:
 * BLOCK_BELOW (setblock a lapis block under the anvil) and MARKED with an anvil that was never placed from the special item.
 * The item itself comes from {@code /disenchantment give anvil} and from the crafting recipe, and is identified by its
 * persistent-data marker on the server. The recipe test reloads through a player command: on Folia that runs on the
 * player's region thread, which is the case commit e2970b6 fixed (the recipe manager belongs to the global region, so the
 * reload has to hand the registration to the global scheduler).
 *
 * <p>Not covered: marking by placing the special item (the chunk-PDC mark of {@code SpecialAnvilStore}) and the drop swap
 * on breaking. Proofstone 0.1.0 has no client block placement or breaking. See {@link SkippedScenariosE2E}.
 */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class SpecialAnvilE2E {

    private static final String MARKER = "disenchantment:special_anvil";
    private static final int START_LEVEL = 30;

    private static PlayerHandle swordPlayer(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL).give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        return player;
    }

    private static boolean offersABook(Client client) {
        try (Window anvil = client.openBlock(ANVIL)) {
            anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
            anvil.moveFromInventory(item -> item.is("book"), 1);
            ItemView result = anvil.slot(2);
            if (result.isEmpty()) {
                return false;
            }
            return result.is("enchanted_book");
        }
    }

    /**
     * BLOCK_BELOW: the anvil works only on the configured block. The same anvil is first checked on stone (nothing), then
     * with lapis under it (the book), then with stone again (nothing), so a result is tied to the block and not to timing.
     * The positive step awaits the result (it arrives after the prepare event) and the negative steps read the slot after
     * the server-confirmed moves.
     */
    @CellTest
    void blockBelowModeGatesTheAnvilByTheBlockUnderIt(Agent agent, Client client) {
        PlayerHandle player = swordPlayer(agent, client);
        try (Cfg ignored = Cfg.with(agent, Map.of("special-anvils.mode", "BLOCK_BELOW", "special-anvils.block-below", "LAPIS_BLOCK"))) {
            assertThat(offersABook(client)).as("stone under the anvil").isFalse();

            agent.command("setblock 0 63 2 minecraft:lapis_block");
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                ItemView offered = anvil.awaitSlot(2, item -> item.is("enchanted_book"));
                assertThat(offered.storedEnchants()).isEqualTo(Map.of("minecraft:sharpness", 5, "minecraft:unbreaking", 3));
            }

            agent.command("setblock 0 63 2 minecraft:stone");
            assertThat(offersABook(client)).as("stone under the anvil again").isFalse();
        } finally {
            agent.command("setblock 0 63 2 minecraft:stone");
        }
        assertThat(player.state().inventory().values()).noneMatch(item -> item.is("enchanted_book"));
        assertThat(player.state().level()).isEqualTo(START_LEVEL);
    }

    /**
     * MARKED: only anvils placed from the special item work, and nothing marked this anvil. With the hint message on, the
     * player is told why (the hint is the positive proof that the gate, and not the layout, refused the operation).
     */
    @CellTest
    void markedModeRefusesAnUnmarkedAnvilAndSaysWhy(Agent agent, Client client) {
        PlayerHandle player = swordPlayer(agent, client);
        try (Cfg ignored = Cfg.with(agent, Map.of("special-anvils.mode", "MARKED", "special-anvils.hint-message", true))) {
            assertThat(offersABook(client)).isFalse();
            String hint = client.awaitMessage(Pattern.compile("only works at a special anvil"));
            assertThat(hint).contains("special anvil");
        }
        // Paired control: the default mode (ALL) offers the book at the very same anvil.
        assertThat(awaitsBook(client)).as("mode ALL again").isTrue();
        assertThat(player.state().level()).isEqualTo(START_LEVEL);
    }

    private static boolean awaitsBook(Client client) {
        try (Window anvil = client.openBlock(ANVIL)) {
            anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
            anvil.moveFromInventory(item -> item.is("book"), 1);
            anvil.awaitSlot(2, item -> item.is("enchanted_book"));
            return true;
        }
    }

    /** The give command hands out anvils carrying the marker; a plain anvil from the server's own give does not. */
    @CellTest
    void theGiveCommandHandsOutMarkedAnvils(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.give(Item.of("anvil"));
        assertThat(player.state().inventory().values()).as("control: a plain anvil has no marker")
                .allSatisfy(item -> assertThat(item.pdc() == null || !item.pdc().containsKey(MARKER)).isTrue());

        agent.command("disenchantment give anvil " + client.name() + " 2");
        var special = player.awaitState("two special anvils", state -> state.inventory().values().stream()
                .anyMatch(item -> item.is("anvil") && item.amount() == 2)).inventory().values().stream()
                .filter(item -> item.is("anvil") && item.amount() == 2).findFirst().orElseThrow();
        assertThat(special.pdc()).as("server-side persistent data of the special anvil").containsKey(MARKER);
        assertThat(special.name()).contains("Enchanter's Anvil");
        assertThat(player.state().inventory().values().stream().filter(item -> item.is("anvil")).count())
                .as("the plain anvil and the special stack are separate stacks").isEqualTo(2);
    }

    /**
     * The crafting recipe (disabled by default, so the first grid gives nothing) appears after a reload issued by a player:
     * a one-emerald-over-one-anvil recipe crafts the marked item. On Folia the reload runs on the player's region thread and
     * the recipe is registered on the global scheduler.
     */
    @CellTest
    void theRecipeIsRegisteredByAPlayerReloadAndCraftsTheSpecialAnvil(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextTo(agent, client, "minecraft:crafting_table");
        player.give(Item.of("emerald")).give(Item.of("anvil"));
        PluginHandle plugin = agent.plugin("Disenchantment");
        Object shape = plugin.config("special-anvils.recipe.shape");
        Object enabled = plugin.config("special-anvils.recipe.enabled");
        agent.command("op " + client.name());
        try {
            try (Window table = client.openBlock(ANVIL)) {
                assertThat(table.type()).isEqualTo("minecraft:crafting");
                table.moveFromInventory(item -> item.is("emerald"), 1);
                table.moveFromInventory(item -> item.is("anvil"), 4);
                assertThat(table.slot(0).isEmpty()).as("no recipe while it is disabled").isTrue();
            }

            plugin.patchConfig("special-anvils.recipe.shape", List.of("E", "A"));
            plugin.patchConfig("special-anvils.recipe.enabled", true);
            client.command("disenchantment reload");
            client.awaitMessage(Pattern.compile("(?i)reload"));

            try (Window table = client.openBlock(ANVIL)) {
                // The first window returned the items to the inventory when it closed.
                table.moveFromInventory(item -> item.is("emerald"), 1);
                table.moveFromInventory(item -> item.is("anvil"), 4);
                ItemView crafted = table.awaitSlot(0, item -> item.is("anvil"));
                assertThat(crafted.name()).contains("Enchanter's Anvil");
                table.click(0, Click.LEFT);
                assertThat(table.cursor().is("anvil")).isTrue();
                table.click(Fixture.firstFreePlayerSlot(table), Click.LEFT);
            }
            var special = player.awaitState("the crafted special anvil", state -> state.inventory().values().stream()
                    .anyMatch(item -> item.is("anvil") && item.pdc() != null && item.pdc().containsKey(MARKER)));
            assertThat(special.inventory().values()).noneMatch(item -> item.is("emerald"));
        } finally {
            plugin.patchConfig("special-anvils.recipe.shape", shape);
            plugin.patchConfig("special-anvils.recipe.enabled", enabled);
            agent.command("disenchantment reload");
            agent.command("deop " + client.name());
        }
    }
}
