package com.jankominek.disenchantment.e2e;

import static com.jankominek.disenchantment.e2e.Fixture.ANVIL;
import static com.jankominek.disenchantment.e2e.Fixture.POST_ERASE;
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
import io.github.h7kz.proofstone.api.Proofstone;
import io.github.h7kz.proofstone.api.Window;
import java.util.List;
import java.util.Map;

/**
 * Erase mode: an enchanted item in the first anvil slot and a catalyst in the second give the same item without its
 * enchantments, destroyed rather than moved to a book. The catalyst is a nether star because it exists on every cell
 * (the default echo shard is not in 1.18) and is no repair material. The item carries a curse that erase keeps by default,
 * so the curse is the paired control for "erase-curses" and the first test is the paired control for the others.
 */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class EraseModeE2E {

    private static final int START_LEVEL = 30;
    private static final Map<String, Object> ERASE_ON = Map.of("erase.enabled", true, "erase.catalysts", List.of("NETHER_STAR"));

    private static PlayerHandle stand(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL)
                .give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3).enchant("vanishing_curse", 1))
                .give(Item.of("nether_star").amount(2));
        return player;
    }

    /** Opens the anvil, loads sword and catalyst, takes the result and parks it. Returns the catalyst slot after the take. */
    private static ItemView erase(Window anvil, java.util.function.Predicate<ItemView> resultShape) {
        anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
        anvil.moveFromInventory(item -> item.is("nether_star"), 1);
        anvil.awaitSlot(2, resultShape);
        anvil.click(2, Click.LEFT);
        ItemView catalyst = anvil.awaitSlot(0, ItemView::isEmpty);
        assertThat(catalyst.isEmpty()).as("the source slot is emptied: the result goes to the cursor").isTrue();
        ItemView left = anvil.slot(1);
        assertThat(anvil.cursor().is("diamond_sword")).isTrue();
        anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
        return left;
    }

    @CellTest
    void eraseDestroysTheEnchantmentsAndSpendsOneCatalyst(Agent agent, Client client) {
        PlayerHandle player = stand(agent, client);
        try (Cfg ignored = Cfg.with(agent, ERASE_ON); EventProbe post = agent.events().subscribe(POST_ERASE)) {
            ItemView catalystLeft;
            try (Window anvil = client.openBlock(ANVIL)) {
                catalystLeft = erase(anvil, item -> item.is("diamond_sword")
                        && item.enchants().equals(Map.of("minecraft:vanishing_curse", 1)));
            }
            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(xpCost).as("PostEraseEvent xpCost").isPositive().isLessThan(START_LEVEL);
            assertThat(catalystLeft.is("nether_star")).isTrue();
            assertThat(catalystLeft.amount()).as("one of two catalysts consumed").isEqualTo(1);
            DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).amount(1)
                    .enchants(Map.of("minecraft:vanishing_curse", 1));
            assertThat(player.state().inventory().values()).as("erase makes no book").noneMatch(item -> item.is("enchanted_book"));
            DualAssert.item(player, client).inInventory(item -> item.is("nether_star")).amount(1);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
            assertThat(post.events()).hasSize(1);
        }
    }

    /** Paired control: with erase off (the default) the same layout offers nothing and nothing is spent. */
    @CellTest
    void eraseOffersNothingWhileItIsOff(Agent agent, Client client) {
        PlayerHandle player = stand(agent, client);
        try (EventProbe post = agent.events().subscribe(POST_ERASE)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("nether_star"), 1);
                assertThat(anvil.slot(2).isEmpty()).as("vanilla offers nothing for a sword and a nether star").isTrue();
            }
            assertThat(post.events()).isEmpty();
            DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword"))
                    .enchants(Map.of("minecraft:sharpness", 5, "minecraft:unbreaking", 3, "minecraft:vanishing_curse", 1));
            DualAssert.item(player, client).inInventory(item -> item.is("nether_star")).amount(2);
            DualAssert.level(player, client).isEqualTo(START_LEVEL);
        }
    }

    @CellTest
    void consumeCatalystOffKeepsTheCatalyst(Agent agent, Client client) {
        PlayerHandle player = stand(agent, client);
        try (Cfg ignored = Cfg.with(agent, Map.of("erase.enabled", true, "erase.catalysts", List.of("NETHER_STAR"),
                "erase.consume-catalyst", false)); EventProbe post = agent.events().subscribe(POST_ERASE)) {
            ItemView catalystLeft;
            try (Window anvil = client.openBlock(ANVIL)) {
                catalystLeft = erase(anvil, item -> item.is("diamond_sword"));
            }
            post.awaitOne();
            assertThat(catalystLeft.amount()).as("no catalyst consumed (first test consumes one)").isEqualTo(2);
            DualAssert.item(player, client).inInventory(item -> item.is("nether_star")).amount(2);
        }
    }

    @CellTest
    void eraseCursesOnRemovesTheCurseToo(Agent agent, Client client) {
        PlayerHandle player = stand(agent, client);
        try (Cfg ignored = Cfg.with(agent, Map.of("erase.enabled", true, "erase.catalysts", List.of("NETHER_STAR"),
                "erase.erase-curses", true)); EventProbe post = agent.events().subscribe(POST_ERASE)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                erase(anvil, item -> item.is("diamond_sword") && item.enchants().isEmpty());
            }
            post.awaitOne();
            DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).amount(1).enchants(Map.of());
        }
    }
}
