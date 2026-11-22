package com.jankominek.disenchantment.e2e;

import static com.jankominek.disenchantment.e2e.Fixture.POST_GRIND_EXTRACT;
import static com.jankominek.disenchantment.e2e.Fixture.POST_GRIND_TRANSFER;
import static com.jankominek.disenchantment.e2e.Fixture.STATION;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.h7kz.proofstone.api.Agent;
import io.github.h7kz.proofstone.api.CellTest;
import io.github.h7kz.proofstone.api.Click;
import io.github.h7kz.proofstone.api.Client;
import io.github.h7kz.proofstone.api.DualAssert;
import io.github.h7kz.proofstone.api.EventProbe;
import io.github.h7kz.proofstone.api.Item;
import io.github.h7kz.proofstone.api.ItemView;
import io.github.h7kz.proofstone.api.Matrix;
import io.github.h7kz.proofstone.api.Platform;
import io.github.h7kz.proofstone.api.PlayerHandle;
import io.github.h7kz.proofstone.api.PlayerState;
import io.github.h7kz.proofstone.api.Proofstone;
import io.github.h7kz.proofstone.api.Window;
import java.util.List;
import java.util.Map;

/**
 * Grindstone operations. Extract: an enchanted item and a plain book give an enchanted book and strip the item.
 * Transfer: an enchanted item and a different kind of item move the enchantments (moved, never copied). Every test turns
 * its feature on through {@link Cfg} (both are off by default) and reverts it when the scope closes. The result of both
 * operations is read from the client window, from the server's inventory, cursor and XP, and from the Post event.
 */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class GrindstoneE2E {

    private static final Map<String, Integer> ENCHANTS = Map.of("minecraft:sharpness", 5, "minecraft:unbreaking", 3);
    private static final int START_LEVEL = 30;
    private static final String GRINDSTONE = "minecraft:grindstone[face=floor]";
    private static final Map<String, Object> EXTRACT_ON = Map.of("grindstone.enabled", true);
    private static final Map<String, Object> BOTH_ON = Map.of("grindstone.enabled", true, "grindstone.transfer.enabled", true);

    private static PlayerHandle stand(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextTo(agent, client, GRINDSTONE);
        player.level(START_LEVEL).give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        return player;
    }

    /** Sword into slot 0, then the book into slot 1 (the plugin places it: vanilla refuses a plain book). */
    private static void loadExtract(Window grind) {
        grind.moveFromInventory(item -> item.is("diamond_sword"), 0);
        grind.moveFromInventory(item -> item.is("book"), 1);
        grind.awaitSlot(1, item -> item.is("book"));
    }

    @CellTest
    void extractMovesEnchantsToTheBookAndStripsTheItem(Agent agent, Client client) {
        PlayerHandle player = stand(agent, client);
        try (Cfg ignored = Cfg.with(agent, EXTRACT_ON); EventProbe post = agent.events().subscribe(POST_GRIND_EXTRACT)) {
            try (Window grind = client.openBlock(STATION)) {
                assertThat(grind.type()).isEqualTo("minecraft:grindstone");
                loadExtract(grind);
                ItemView offered = grind.awaitSlot(2, item -> item.is("enchanted_book"));
                assertThat(offered.storedEnchants()).isEqualTo(ENCHANTS);

                grind.click(2, Click.LEFT);
                ItemView stripped = grind.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                assertThat(stripped.amount()).isEqualTo(1);
                assertThat(grind.slot(1).isEmpty()).as("the consumed book left slot 1").isTrue();
                grind.awaitCursor(item -> item.is("enchanted_book"));
                grind.click(Fixture.firstFreePlayerSlot(grind), Click.LEFT);
            }

            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(xpCost).as("PostGrindstoneExtractEvent xpCost").isPositive().isLessThan(START_LEVEL);
            DualAssert.item(player, client).inInventory(item -> item.is("enchanted_book")).amount(1)
                    .storedEnchants(ENCHANTS);
            DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).amount(1).enchants(Map.of());
            assertThat(player.state().inventory().values()).as("the plain book is gone").noneMatch(item -> item.is("book"));
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
            assertThat(post.events()).as("one operation, one Post event").hasSize(1);
        }
    }

    /** Paired control for the test above: the same layout with the feature off offers no enchanted book. */
    @CellTest
    void extractOffersNothingWhileTheFeatureIsOff(Agent agent, Client client) {
        PlayerHandle player = stand(agent, client);
        try (EventProbe post = agent.events().subscribe(POST_GRIND_EXTRACT)) {
            try (Window grind = client.openBlock(STATION)) {
                grind.moveFromInventory(item -> item.is("diamond_sword"), 0);
                assertThat(grind.slot(0).is("diamond_sword")).isTrue();
                // The vanilla grindstone refuses a plain book in slot 1.
                Fixture.refusedClick(grind, Fixture.firstSlotOf(grind, "book"), Click.LEFT);
                Fixture.refusedClick(grind, 1, Click.LEFT);
                assertThat(grind.slot(1).isEmpty()).as("vanilla refuses a plain book in slot 1").isTrue();
                // Vanilla still grinds the lone sword to a plain one; what must be absent is the plugin's enchanted book.
                ItemView vanilla = grind.awaitSlot(2, item -> item.is("diamond_sword"));
                assertThat(vanilla.enchants()).as("vanilla result is the stripped sword").isEmpty();
                assertThat(grind.slots()).as("no enchanted book anywhere in the window").noneMatch(item -> item.is("enchanted_book"));
            }
            assertThat(post.events()).isEmpty();
            DualAssert.level(player, client).isEqualTo(START_LEVEL);
        }
    }

    @CellTest
    void transferMovesTheEnchantmentsToTheOtherItem(Agent agent, Client client) {
        PlayerHandle player = stand(agent, client);
        player.give(Item.of("iron_sword"));
        try (Cfg ignored = Cfg.with(agent, BOTH_ON); EventProbe post = agent.events().subscribe(POST_GRIND_TRANSFER)) {
            try (Window grind = client.openBlock(STATION)) {
                grind.moveFromInventory(item -> item.is("diamond_sword"), 0);
                grind.moveFromInventory(item -> item.is("iron_sword"), 1);
                ItemView offered = grind.awaitSlot(2, item -> item.is("iron_sword") && !item.enchants().isEmpty());
                assertThat(offered.enchants()).isEqualTo(ENCHANTS);

                grind.click(2, Click.LEFT);
                grind.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                assertThat(grind.slot(1).isEmpty()).as("the target item left slot 1").isTrue();
                grind.awaitCursor(item -> item.is("iron_sword"));
                grind.click(Fixture.firstFreePlayerSlot(grind), Click.LEFT);
            }

            int xpCost = post.awaitOne().getInt("xpCost");
            DualAssert.item(player, client).inInventory(item -> item.is("iron_sword")).amount(1).enchants(ENCHANTS);
            DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).amount(1).enchants(Map.of());
            assertThat(player.state().inventory().values().stream().filter(item -> item.is("iron_sword")).count())
                    .as("the enchantments moved, the iron sword is not duplicated").isEqualTo(1);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
            assertThat(post.events()).hasSize(1);
        }
    }

    /** Paired control: transfer is off by default, so the same two swords offer nothing. */
    @CellTest
    void transferOffersNothingWhileItIsOff(Agent agent, Client client) {
        PlayerHandle player = stand(agent, client);
        player.give(Item.of("iron_sword"));
        try (Cfg ignored = Cfg.with(agent, EXTRACT_ON); EventProbe post = agent.events().subscribe(POST_GRIND_TRANSFER)) {
            try (Window grind = client.openBlock(STATION)) {
                grind.moveFromInventory(item -> item.is("diamond_sword"), 0);
                grind.moveFromInventory(item -> item.is("iron_sword"), 1);
                // A vanilla grindstone gives no result for two different swords.
                assertThat(grind.slot(2).isEmpty()).isTrue();
            }
            assertThat(post.events()).isEmpty();
            DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).enchants(ENCHANTS);
            DualAssert.item(player, client).inInventory(item -> item.is("iron_sword")).enchants(Map.of());
        }
    }

    /**
     * Unsafe result clicks (shift, number key, off hand, drop, double) are cancelled without delivering or charging, and
     * the one safe click then delivers exactly one book. The safe click bounds the absence checks: the same window
     * produces a book when the click is allowed.
     */
    @CellTest
    void unsafeResultClicksDeliverNothingAndDuplicateNothing(Agent agent, Client client) {
        PlayerHandle player = stand(agent, client);
        try (Cfg ignored = Cfg.with(agent, EXTRACT_ON); EventProbe post = agent.events().subscribe(POST_GRIND_EXTRACT)) {
            try (Window grind = client.openBlock(STATION)) {
                loadExtract(grind);
                grind.awaitSlot(2, item -> item.is("enchanted_book"));

                for (Click unsafe : List.of(Click.SHIFT_LEFT, Click.NUMBER_1, Click.SWAP_OFFHAND, Click.DROP, Click.DOUBLE)) {
                    Fixture.refusedClick(grind, 2, unsafe);
                    grind.awaitSlot(2, item -> item.is("enchanted_book"));
                    PlayerState state = player.state();
                    assertThat(state.level()).as("level after %s", unsafe).isEqualTo(START_LEVEL);
                    assertThat(state.cursor().isEmpty()).as("server cursor after %s", unsafe).isTrue();
                    assertThat(state.inventory().values()).as("server inventory after %s", unsafe)
                            .noneMatch(item -> item.is("enchanted_book"));
                    grind.awaitCursor(ItemView::isEmpty);
                    assertThat(grind.slots().subList(3, grind.slots().size())).as("client inventory after %s", unsafe)
                            .noneMatch(item -> item.is("enchanted_book"));
                    assertThat(grind.slot(0).enchants()).as("source keeps its enchantments after %s", unsafe).isEqualTo(ENCHANTS);
                    assertThat(post.events()).as("Post events after %s", unsafe).isEmpty();
                }

                grind.click(2, Click.LEFT);
                grind.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                ItemView delivered = grind.awaitCursor(item -> item.is("enchanted_book"));
                assertThat(delivered.amount()).isEqualTo(1);
                grind.click(Fixture.firstFreePlayerSlot(grind), Click.LEFT);
            }

            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(post.events()).as("exactly one Post event for the one safe click").hasSize(1);
            PlayerState end = player.awaitState("level charged once", state -> state.level() == START_LEVEL - xpCost);
            assertThat(end.inventory().values().stream().filter(item -> item.is("enchanted_book")).count())
                    .as("enchanted books on the server after one operation").isEqualTo(1);
            assertThat(client.inventory().stream().filter(item -> item.is("enchanted_book")).count())
                    .as("enchanted books on the client after one operation").isEqualTo(1);
        }
    }

    /** Too little XP: the click is refused, nothing is delivered or charged; once the player can pay it works once. */
    @CellTest
    void extractWithTooLittleExperienceDeliversNothing(Agent agent, Client client) {
        PlayerHandle player = stand(agent, client);
        player.level(1);
        try (Cfg ignored = Cfg.with(agent, EXTRACT_ON); EventProbe post = agent.events().subscribe(POST_GRIND_EXTRACT)) {
            try (Window grind = client.openBlock(STATION)) {
                loadExtract(grind);
                grind.awaitSlot(2, item -> item.is("enchanted_book"));
                Fixture.refusedClick(grind, 2, Click.LEFT);
                assertThat(player.state().level()).isEqualTo(1);
                assertThat(player.state().cursor().isEmpty()).isTrue();
                assertThat(grind.slot(0).enchants()).isEqualTo(ENCHANTS);
                assertThat(post.events()).isEmpty();

                player.level(START_LEVEL);
                DualAssert.level(player, client).isEqualTo(START_LEVEL);
                grind.click(2, Click.LEFT);
                grind.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                grind.click(Fixture.firstFreePlayerSlot(grind), Click.LEFT);
            }
            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(post.events()).hasSize(1);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
        }
    }
}
