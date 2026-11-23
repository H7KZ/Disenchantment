package com.jankominek.disenchantment.e2e;

import static com.jankominek.disenchantment.e2e.Fixture.ANVIL;
import static com.jankominek.disenchantment.e2e.Fixture.POST_DISENCHANT;
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
import java.util.Map;

/** Disenchanting: a sword and a blank book in the anvil give an enchanted book and strip the sword. */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.4", "1.21.7", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class DisenchantE2E {

    private static final Map<String, Integer> ENCHANTS = Map.of("minecraft:sharpness", 5, "minecraft:unbreaking", 3);
    private static final int START_LEVEL = 30;

    @CellTest
    void disenchantMovesEnchantsToTheBookAndChargesTheEventCost(Agent agent, Client client) {
        // Chances default to 1.0, so every enchantment transfers.
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL)
                .give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).enchants(ENCHANTS);
        DualAssert.level(player, client).isEqualTo(START_LEVEL);

        try (EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                assertThat(anvil.type()).isEqualTo("minecraft:anvil");
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);

                ItemView offered = anvil.awaitSlot(2, item -> item.is("enchanted_book"));
                assertThat(offered.storedEnchants()).isEqualTo(ENCHANTS);

                anvil.click(2, Click.LEFT);
                ItemView stripped = anvil.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                assertThat(stripped.amount()).isEqualTo(1);
                anvil.awaitCursor(item -> item.is("enchanted_book"));
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }

            // The plugin's own event gives the cost, so the level check is never a number copied from a run.
            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(xpCost).as("PostDisenchantEvent xpCost").isPositive().isLessThan(START_LEVEL);

            DualAssert.item(player, client).inInventory(item -> item.is("enchanted_book")).amount(1)
                    .storedEnchants(ENCHANTS).enchants(Map.of());
            DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).amount(1)
                    .enchants(Map.of()).storedEnchants(Map.of());
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
            assertThat(post.events()).as("one operation fires one PostDisenchantEvent").hasSize(1);
        }
    }

    /**
     * One level is below any disenchant cost. Taking the result must deliver nothing and charge nothing. Raising the
     * level and clicking again must then work and fire exactly one event: the bound that proves the first click was
     * handled without a delivery (it would otherwise show up as a second book or event).
     */
    @CellTest
    void tooLittleExperienceDeliversNothingUntilThePlayerCanPay(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(1)
                .give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        DualAssert.level(player, client).isEqualTo(1);

        try (EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                anvil.awaitSlot(2, item -> item.is("enchanted_book"));

                Fixture.refusedClick(anvil, 2, Click.LEFT);
                assertThat(anvil.slot(2).is("enchanted_book")).as("the result is still offered").isTrue();
                assertThat(anvil.slot(0).enchants()).as("source keeps its enchantments").isEqualTo(Map.of(
                        "minecraft:sharpness", 5, "minecraft:unbreaking", 3));
                assertThat(player.state().cursor().isEmpty()).as("server cursor after the refused click").isTrue();
                anvil.awaitCursor(ItemView::isEmpty);
                assertThat(player.state().level()).as("level after the refused click").isEqualTo(1);
                assertThat(post.events()).as("no Post event for a refused click").isEmpty();

                player.level(START_LEVEL);
                DualAssert.level(player, client).isEqualTo(START_LEVEL);
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                anvil.awaitCursor(item -> item.is("enchanted_book"));
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }

            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(post.events()).as("only the paid click fires an event").hasSize(1);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
            DualAssert.item(player, client).inInventory(item -> item.is("enchanted_book")).amount(1)
                    .storedEnchants(ENCHANTS);
        }
    }
}
