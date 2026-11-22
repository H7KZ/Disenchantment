package com.jankominek.disenchantment.e2e;

import static com.jankominek.disenchantment.e2e.Fixture.ANVIL;
import static com.jankominek.disenchantment.e2e.Fixture.POST_DISENCHANT;
import static com.jankominek.disenchantment.e2e.Fixture.POST_SHATTER;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.h7kz.proofstone.api.Agent;
import io.github.h7kz.proofstone.api.CellTest;
import io.github.h7kz.proofstone.api.Click;
import io.github.h7kz.proofstone.api.Client;
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
import java.util.function.Predicate;

/**
 * Unsafe result clicks are cancelled: a shift, number-key, off-hand, drop or double click on the result slot must not
 * deliver a book (vanilla would put a second copy in the inventory next to the one the plugin hands out). After every
 * unsafe click the server oracle sees no book anywhere, the XP untouched and no Post event. A plain left click then
 * delivers exactly one book and fires exactly one Post event: the bound that proves each cancelled click was really
 * handled, and that the total is one copy.
 */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class UnsafeResultClickE2E {

    private static final int START_LEVEL = 30;
    private static final List<Click> UNSAFE = List.of(Click.SHIFT_LEFT, Click.NUMBER_1, Click.SWAP_OFFHAND, Click.DROP,
            Click.DOUBLE);

    @CellTest
    void unsafeDisenchantResultClicksDeliverNothing(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL)
                .give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        run(agent, client, player, POST_DISENCHANT, "diamond_sword", 1L,
                item -> item.is("diamond_sword") && item.enchants().isEmpty());
    }

    @CellTest
    void unsafeShatterResultClicksDeliverNothing(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL)
                .give(Item.of("enchanted_book").storedEnchant("sharpness", 5).storedEnchant("unbreaking", 3)
                        .storedEnchant("looting", 3))
                .give(Item.of("book"));
        // Shatter leaves two books: the split-off one and the source book with the rest.
        run(agent, client, player, POST_SHATTER, "enchanted_book", 2L,
                item -> item.is("enchanted_book") && item.storedEnchants().size() == 2);
    }

    private static void run(Agent agent, Client client, PlayerHandle player, String postEvent, String source,
            long booksAtTheEnd, Predicate<ItemView> sourceAfterTheOperation) {
        try (EventProbe post = agent.events().subscribe(postEvent)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is(source), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                anvil.awaitSlot(2, item -> item.is("enchanted_book"));

                for (Click unsafe : UNSAFE) {
                    Fixture.refusedClick(anvil, 2, unsafe);
                    // The server resyncs the window after a cancelled click, and the result is still offered.
                    anvil.awaitSlot(2, item -> item.is("enchanted_book"));
                    PlayerState state = player.state();
                    assertThat(state.level()).as("level after %s", unsafe).isEqualTo(START_LEVEL);
                    assertThat(state.cursor().isEmpty()).as("server cursor after %s", unsafe).isTrue();
                    assertThat(state.inventory().values()).as("server inventory after %s", unsafe)
                            .noneMatch(item -> item.is("enchanted_book"));
                    // Window slots 0 to 2 are the anvil's own (the source may itself be a book); the rest is the player's.
                    assertThat(anvil.slots().subList(3, anvil.slots().size())).as("client inventory after %s", unsafe)
                            .noneMatch(item -> item.is("enchanted_book"));
                    anvil.awaitCursor(ItemView::isEmpty);
                    assertThat(post.events()).as("Post events after %s", unsafe).isEmpty();
                }

                // The safe click delivers exactly one book.
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, sourceAfterTheOperation);
                ItemView cursor = anvil.awaitCursor(item -> item.is("enchanted_book"));
                assertThat(cursor.amount()).isEqualTo(1);
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }

            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(post.events()).as("exactly one Post event for the one safe click").hasSize(1);
            PlayerState end = player.awaitState("level charged once", state -> state.level() == START_LEVEL - xpCost);
            assertThat(end.inventory().values().stream().filter(item -> item.is("enchanted_book")).count())
                    .as("enchanted books in the server inventory after one operation").isEqualTo(booksAtTheEnd);
            assertThat(client.inventory().stream().filter(item -> item.is("enchanted_book")).count())
                    .as("enchanted books in the client inventory after one operation").isEqualTo(booksAtTheEnd);
        }
    }
}
