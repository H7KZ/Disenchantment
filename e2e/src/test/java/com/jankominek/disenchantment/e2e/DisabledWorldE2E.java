package com.jankominek.disenchantment.e2e;

import static com.jankominek.disenchantment.e2e.Fixture.ANVIL;
import static com.jankominek.disenchantment.e2e.Fixture.POST_DISENCHANT;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.h7kz.proofstone.api.Agent;
import io.github.h7kz.proofstone.api.CellTest;
import io.github.h7kz.proofstone.api.Client;
import io.github.h7kz.proofstone.api.DualAssert;
import io.github.h7kz.proofstone.api.EventProbe;
import io.github.h7kz.proofstone.api.Item;
import io.github.h7kz.proofstone.api.Matrix;
import io.github.h7kz.proofstone.api.Platform;
import io.github.h7kz.proofstone.api.PlayerHandle;
import io.github.h7kz.proofstone.api.PluginHandle;
import io.github.h7kz.proofstone.api.Proofstone;
import io.github.h7kz.proofstone.api.Window;
import java.util.List;
import java.util.Map;

/**
 * A disabled world gives the vanilla result. This is the only class that patches the plugin's config, and it has its own
 * server (one class, one server) for a reason: Disenchantment keeps the object {@code getConfig()} returned at enable,
 * Proofstone's patch changes that live object, but Proofstone's restore writes the file back and calls
 * {@code reloadConfig()}, which builds a new object the plugin never sees. A patch would therefore leak into every later
 * test on the same server. The test reverts the live values itself in a {@code finally}.
 */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.4", "1.21.7", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class DisabledWorldE2E {

    private static final Map<String, Integer> ENCHANTS = Map.of("minecraft:sharpness", 5, "minecraft:unbreaking", 3);
    private static final int START_LEVEL = 30;

    /**
     * In a world where the feature is off the plugin steps aside, so the vanilla result stands: a sword and a blank
     * book give no result. The same layout gives the enchanted book while the world is enabled (first window), which
     * makes the empty result in the second window a finding about the config and not about the layout.
     */
    @CellTest
    void aDisabledWorldGivesTheVanillaResult(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL)
                .give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        PluginHandle plugin = agent.plugin("Disenchantment");

        try (EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                anvil.awaitSlot(2, item -> item.is("enchanted_book"));
            }

            // An allowlist with no world turns the feature off everywhere.
            plugin.patchConfig("disenchantment.worlds.mode", "ALLOWLIST");
            plugin.patchConfig("disenchantment.worlds.list", List.of());
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                // Window actions are server-confirmed in order, so the result for this layout has been decided when
                // the second move returns.
                anvil.moveFromInventory(item -> item.is("book"), 1);
                assertThat(anvil.slot(2).isEmpty()).as("vanilla offers nothing for a sword and a blank book").isTrue();
            } finally {
                plugin.patchConfig("disenchantment.worlds.mode", "DENYLIST");
                plugin.patchConfig("disenchantment.worlds.list", List.of());
            }

            DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).amount(1)
                    .enchants(ENCHANTS);
            assertThat(player.state().inventory().values()).noneMatch(item -> item.is("enchanted_book"));
            assertThat(client.inventory()).noneMatch(item -> item.is("enchanted_book"));
            DualAssert.level(player, client).isEqualTo(START_LEVEL);
            assertThat(post.events()).as("a disabled world fires no Post event").isEmpty();
        }
    }
}
