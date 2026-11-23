package com.jankominek.disenchantment.e2e;

import static com.jankominek.disenchantment.e2e.Fixture.ANVIL;
import static com.jankominek.disenchantment.e2e.Fixture.POST_SHATTER;
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
import java.util.HashMap;
import java.util.Map;

/** Shattering (book splitting): a multi-enchantment book and a blank book give one enchantment on a new book. */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.4", "1.21.7", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class ShatterE2E {

    private static final Map<String, Integer> ENCHANTS =
            Map.of("minecraft:sharpness", 5, "minecraft:unbreaking", 3, "minecraft:looting", 3);
    private static final int START_LEVEL = 30;

    @CellTest
    void shatterSplitsOneEnchantmentOffTheBook(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL)
                .give(Item.of("enchanted_book").storedEnchant("sharpness", 5).storedEnchant("unbreaking", 3)
                        .storedEnchant("looting", 3))
                .give(Item.of("book"));
        DualAssert.item(player, client).inInventory(item -> item.is("enchanted_book")).storedEnchants(ENCHANTS);
        DualAssert.level(player, client).isEqualTo(START_LEVEL);

        Map<String, Integer> split;
        try (EventProbe post = agent.events().subscribe(POST_SHATTER)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                assertThat(anvil.type()).isEqualTo("minecraft:anvil");
                anvil.moveFromInventory(item -> item.is("enchanted_book"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);

                ItemView offered = anvil.awaitSlot(2, item -> item.is("enchanted_book"));
                split = offered.storedEnchants();
                assertThat(split).as("shatter offers exactly one enchantment (split-count 1)").hasSize(1);
                assertThat(ENCHANTS).containsAllEntriesOf(split);

                anvil.click(2, Click.LEFT);
                Map<String, Integer> expectedRest = new HashMap<>(ENCHANTS);
                split.keySet().forEach(expectedRest::remove);
                ItemView rest = anvil.awaitSlot(0, item -> item.is("enchanted_book") && item.storedEnchants().size() == 2);
                assertThat(rest.storedEnchants()).isEqualTo(expectedRest);
                ItemView delivered = anvil.awaitCursor(item -> item.is("enchanted_book") && item.storedEnchants().equals(split));
                assertThat(delivered.storedEnchants()).isEqualTo(split);
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }

            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(xpCost).as("PostShatterEvent xpCost").isPositive().isLessThan(START_LEVEL);

            Map<String, Integer> expectedRest = new HashMap<>(ENCHANTS);
            split.keySet().forEach(expectedRest::remove);
            DualAssert.item(player, client).inInventory(item -> item.is("enchanted_book") && item.storedEnchants().size() == 1)
                    .amount(1).storedEnchants(split);
            DualAssert.item(player, client).inInventory(item -> item.is("enchanted_book") && item.storedEnchants().size() == 2)
                    .amount(1).storedEnchants(expectedRest);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
            assertThat(post.events()).as("one operation fires one PostShatterEvent").hasSize(1);
        }
    }
}
