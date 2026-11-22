package com.jankominek.disenchantment.e2e;

import static com.jankominek.disenchantment.e2e.Fixture.ANVIL;
import static com.jankominek.disenchantment.e2e.Fixture.POST_DISENCHANT;
import static com.jankominek.disenchantment.e2e.Fixture.POST_GRIND_EXTRACT;
import static com.jankominek.disenchantment.e2e.Fixture.POST_GRIND_TRANSFER;
import static com.jankominek.disenchantment.e2e.Fixture.STATION;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.h7kz.proofstone.api.Agent;
import io.github.h7kz.proofstone.api.CellTest;
import io.github.h7kz.proofstone.api.Click;
import io.github.h7kz.proofstone.api.Client;
import io.github.h7kz.proofstone.api.EventProbe;
import io.github.h7kz.proofstone.api.Item;
import io.github.h7kz.proofstone.api.Matrix;
import io.github.h7kz.proofstone.api.Platform;
import io.github.h7kz.proofstone.api.PlayerHandle;
import io.github.h7kz.proofstone.api.Proofstone;
import io.github.h7kz.proofstone.api.Window;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Operation counters (commit fa978b1: grindstone operations have their own counters next to the disenchant and shatter
 * ones). The counters are read the way an admin does, through {@code /disenchantment stats} as an operator, and every
 * number is compared before and after an operation: a grindstone extract moves only "Grind extract", a transfer only
 * "Grind transfer", an anvil disenchant only "Disenchants". Counters are server-wide and persist in the stats database across the class's tests, so every check is a delta from a read taken just before.
 * {@code logging.operations} is read at boot and on reload, so the test turns it on through {@link Cfg}.
 *
 * <p>The player is made an operator for the command and de-opped in the finally (names are unique per test, but the ops file is
 * shared by the class).
 */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class StatsE2E {

    private static final int START_LEVEL = 40;
    private static final Map<String, Object> STATS_AND_GRIND = Map.of("logging.operations", true, "grindstone.enabled", true,
            "grindstone.transfer.enabled", true);

    private static int read(Client client, String label) {
        String line = client.awaitMessage(Pattern.compile(Pattern.quote(label) + ":\\s+(\\d+)"));
        Matcher m = Pattern.compile(":\\s+(\\d+)").matcher(line);
        assertThat(m.find()).as(line).isTrue();
        return Integer.parseInt(m.group(1));
    }

    /** The three counters of this test, from one {@code /disenchantment stats}. */
    private record Counters(int disenchants, int extracts, int transfers) {
        Counters plus(int d, int e, int t) {
            return new Counters(disenchants + d, extracts + e, transfers + t);
        }
    }

    private static Counters counters(Client client) {
        client.command("disenchantment stats");
        return new Counters(read(client, "Disenchants"), read(client, "Grind extract"), read(client, "Grind transfer"));
    }

    private static void extractOnce(Client client, PlayerHandle player, EventProbe post) {
        try (Window grind = client.openBlock(STATION)) {
            grind.moveFromInventory(item -> item.is("diamond_sword"), 0);
            grind.moveFromInventory(item -> item.is("book"), 1);
            grind.awaitSlot(2, item -> item.is("enchanted_book"));
            grind.click(2, Click.LEFT);
            grind.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
            grind.click(Fixture.firstFreePlayerSlot(grind), Click.LEFT);
        }
        post.awaitOne();
    }

    @CellTest
    void grindstoneOperationsHaveTheirOwnCounters(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextTo(agent, client, "minecraft:grindstone[face=floor]");
        player.level(START_LEVEL).give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book")).give(Item.of("iron_sword"));
        agent.command("op " + client.name());
        try (Cfg ignored = Cfg.with(agent, STATS_AND_GRIND);
             EventProbe extract = agent.events().subscribe(POST_GRIND_EXTRACT);
             EventProbe transfer = agent.events().subscribe(POST_GRIND_TRANSFER)) {
            Counters before = counters(client);

            extractOnce(client, player, extract);
            Counters afterExtract = counters(client);
            assertThat(afterExtract).as("one extract moves only the extract counter").isEqualTo(before.plus(0, 1, 0));

            // The extract left a plain stripped sword; give it a new enchantment set to transfer onto the iron sword.
            player.give(Item.of("diamond_sword").enchant("sharpness", 4));
            try (Window grind = client.openBlock(STATION)) {
                grind.moveFromInventory(item -> item.is("diamond_sword") && !item.enchants().isEmpty(), 0);
                grind.moveFromInventory(item -> item.is("iron_sword"), 1);
                grind.awaitSlot(2, item -> item.is("iron_sword") && !item.enchants().isEmpty());
                grind.click(2, Click.LEFT);
                grind.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                grind.click(Fixture.firstFreePlayerSlot(grind), Click.LEFT);
            }
            transfer.awaitOne();
            assertThat(counters(client)).as("one transfer moves only the transfer counter").isEqualTo(before.plus(0, 1, 1));

            client.command("disenchantment stats " + client.name());
            assertThat(read(client, "Grindstone")).as("per-player grindstone counter (extract + transfer)").isEqualTo(2);
        } finally {
            agent.command("deop " + client.name());
        }
    }

    /** The other direction: an anvil disenchant moves "Disenchants" and leaves both grindstone counters at zero. */
    @CellTest
    void anAnvilDisenchantDoesNotTouchTheGrindstoneCounters(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL).give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        agent.command("op " + client.name());
        try (Cfg ignored = Cfg.with(agent, STATS_AND_GRIND); EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            Counters before = counters(client);
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                anvil.awaitSlot(2, item -> item.is("enchanted_book"));
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }
            post.awaitOne();
            assertThat(counters(client)).isEqualTo(before.plus(1, 0, 0));
        } finally {
            agent.command("deop " + client.name());
        }
    }
}
