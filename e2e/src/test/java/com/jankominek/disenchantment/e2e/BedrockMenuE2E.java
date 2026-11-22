package com.jankominek.disenchantment.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.h7kz.proofstone.api.Agent;
import io.github.h7kz.proofstone.api.CellTest;
import io.github.h7kz.proofstone.api.Client;
import io.github.h7kz.proofstone.api.Matrix;
import io.github.h7kz.proofstone.api.Platform;
import io.github.h7kz.proofstone.api.PlayerHandle;
import io.github.h7kz.proofstone.api.Proofstone;
import io.github.h7kz.proofstone.api.ServerWindow;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The Bedrock fallback's chest menu, asserted from the server side with {@code PlayerState.openWindow} (Proofstone T-M11-02).
 * Detection is the PREFIX method only: the player's own name is the configured prefix, so the test needs no Floodgate or Geyser.
 * Layout constants are those of {@code BedrockAnvilGUI}. Cells: paper@26.3 and folia@26.3 (the cells this suite runs on 26.x).
 */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class BedrockMenuE2E {

    private static final int SLOT_ITEM = 10;
    private static final int SLOT_SECOND = 12;
    private static final int SLOT_MODE = 13;
    private static final int SLOT_CONFIRM = 16;
    private static final int SLOT_CANCEL = 25;

    @CellTest
    void aPrefixDetectedPlayerGetsTheChestMenuAndOthersDoNot(Agent agent, Client client) {
        PlayerHandle player = agent.player(client).reset();
        player.awaitState("empty inventory", state -> state.inventory().isEmpty());

        // Control 1: the fallback is off by default, the command refuses and no container is open on the server.
        client.command("disenchantment anvil");
        client.awaitMessage(Pattern.compile("not available on this server"));
        assertThat(player.state().openWindow()).as("fallback off").isEmpty();

        // Control 2: the fallback is on, but the player's name does not start with a configured prefix.
        try (Cfg ignored = Cfg.with(agent, Map.of("bedrock.fallback-enabled", true, "bedrock.detect", List.of("PREFIX"),
                "bedrock.prefixes", List.of("no-such-prefix-")))) {
            client.command("disenchantment anvil");
            client.awaitMessage(Pattern.compile("only available to Bedrock players"));
            assertThat(player.state().openWindow()).as("name without a Bedrock prefix").isEmpty();
        }

        // Positive: the same player, the same command, the player's name is now a configured prefix.
        try (Cfg ignored = Cfg.with(agent, Map.of("bedrock.fallback-enabled", true, "bedrock.detect", List.of("PREFIX"),
                "bedrock.prefixes", List.of(client.name())))) {
            client.command("disenchantment anvil");
            ServerWindow menu = player.awaitState("the Bedrock menu is open", state -> state.openWindow().isPresent())
                    .openWindow().orElseThrow();

            assertThat(menu.type()).isEqualTo("chest");
            assertThat(menu.title()).contains("Disenchantment anvil");
            assertThat(menu.slots()).hasSize(27);
            assertThat(menu.slots().get(SLOT_ITEM).isEmpty()).as("item input").isTrue();
            assertThat(menu.slots().get(SLOT_SECOND).isEmpty()).as("book input").isTrue();
            assertThat(menu.slots().get(SLOT_MODE).is("anvil")).as("mode label").isTrue();
            assertThat(menu.slots().get(SLOT_CONFIRM).is("gray_dye")).as("confirm is greyed out with nothing inside").isTrue();
            assertThat(menu.slots().get(SLOT_CANCEL).is("barrier")).as("cancel button").isTrue();
        }
    }
}
