package com.jankominek.disenchantment.e2e;

import static com.jankominek.disenchantment.e2e.Fixture.ANVIL;
import static com.jankominek.disenchantment.e2e.Fixture.POST_DISENCHANT;
import static com.jankominek.disenchantment.e2e.Fixture.POST_GRIND_EXTRACT;
import static com.jankominek.disenchantment.e2e.Fixture.POST_SHATTER;
import static com.jankominek.disenchantment.e2e.Fixture.PRE_DISENCHANT;
import static com.jankominek.disenchantment.e2e.Fixture.STATION;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.h7kz.proofstone.api.Agent;
import io.github.h7kz.proofstone.api.CellTest;
import io.github.h7kz.proofstone.api.Click;
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
import java.util.Map;

/**
 * The cancellable Pre events. A third-party listener (the e2e-hooks test plugin, written like an API consumer) cancels the
 * Pre event while its config flag is true. A cancelled Pre event must leave the world as it was: no result delivered, no
 * XP or item change, no Post event. Each test then clears the flag in the same window and takes the result once, which
 * bounds the absence checks (the same window delivers when nothing vetoes) and shows the Pre event itself fired.
 */
@Proofstone(plugins = {"target/support-jars/*-hooks.jar", "target/plugin-under-test/Disenchantment-*.jar"})
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.4", "1.21.7", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class PreEventCancelE2E {

    private static final String PRE_SHATTER = "com.jankominek.disenchantment.events.api.PreShatterEvent";
    private static final String PRE_GRIND_EXTRACT = "com.jankominek.disenchantment.events.api.PreGrindstoneExtractEvent";
    private static final Map<String, Integer> SWORD = Map.of("minecraft:sharpness", 5, "minecraft:unbreaking", 3);
    private static final int START_LEVEL = 30;

    @CellTest
    void aCancelledPreDisenchantEventChangesNothing(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL).give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        PluginHandle hooks = agent.plugin("DisenchantmentE2eHooks");
        hooks.patchConfig("cancel.PreDisenchantEvent", true);
        try (EventProbe pre = agent.events().subscribe(PRE_DISENCHANT);
             EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                anvil.awaitSlot(2, item -> item.is("enchanted_book"));

                Fixture.refusedClick(anvil, 2, Click.LEFT);
                pre.awaitOne();
                assertThat(pre.events()).as("the Pre event fired once for the vetoed click").hasSize(1);
                assertThat(post.events()).as("no Post event after a veto").isEmpty();
                assertThat(player.state().level()).as("level after the veto").isEqualTo(START_LEVEL);
                assertThat(player.state().cursor().isEmpty()).isTrue();
                assertThat(player.state().inventory().values()).noneMatch(item -> item.is("enchanted_book"));
                assertThat(anvil.slot(0).enchants()).as("source keeps its enchantments").isEqualTo(SWORD);

                hooks.patchConfig("cancel.PreDisenchantEvent", false);
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }
            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(post.events()).hasSize(1);
            assertThat(pre.events()).as("the second click fired the Pre event again").hasSize(2);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
            DualAssert.item(player, client).inInventory(item -> item.is("enchanted_book")).amount(1).storedEnchants(SWORD);
        } finally {
            hooks.patchConfig("cancel.PreDisenchantEvent", false);
        }
    }

    @CellTest
    void aCancelledPreShatterEventChangesNothing(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL)
                .give(Item.of("enchanted_book").storedEnchant("sharpness", 5).storedEnchant("unbreaking", 3))
                .give(Item.of("book"));
        PluginHandle hooks = agent.plugin("DisenchantmentE2eHooks");
        hooks.patchConfig("cancel.PreShatterEvent", true);
        try (EventProbe pre = agent.events().subscribe(PRE_SHATTER);
             EventProbe post = agent.events().subscribe(POST_SHATTER)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("enchanted_book"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                anvil.awaitSlot(2, item -> item.is("enchanted_book"));

                Fixture.refusedClick(anvil, 2, Click.LEFT);
                pre.awaitOne();
                assertThat(post.events()).isEmpty();
                assertThat(player.state().level()).isEqualTo(START_LEVEL);
                assertThat(player.state().cursor().isEmpty()).isTrue();
                assertThat(anvil.slot(0).storedEnchants()).as("the source book keeps both enchantments").isEqualTo(SWORD);

                hooks.patchConfig("cancel.PreShatterEvent", false);
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, item -> item.is("enchanted_book") && item.storedEnchants().size() == 1);
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }
            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(post.events()).hasSize(1);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
        } finally {
            hooks.patchConfig("cancel.PreShatterEvent", false);
        }
    }

    @CellTest
    void aCancelledPreGrindstoneExtractEventChangesNothing(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextTo(agent, client, "minecraft:grindstone[face=floor]");
        player.level(START_LEVEL).give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        PluginHandle hooks = agent.plugin("DisenchantmentE2eHooks");
        hooks.patchConfig("cancel.PreGrindstoneExtractEvent", true);
        try (Cfg ignored = Cfg.with(agent, Map.of("grindstone.enabled", true));
             EventProbe pre = agent.events().subscribe(PRE_GRIND_EXTRACT);
             EventProbe post = agent.events().subscribe(POST_GRIND_EXTRACT)) {
            try (Window grind = client.openBlock(STATION)) {
                grind.moveFromInventory(item -> item.is("diamond_sword"), 0);
                grind.moveFromInventory(item -> item.is("book"), 1);
                grind.awaitSlot(2, item -> item.is("enchanted_book"));

                Fixture.refusedClick(grind, 2, Click.LEFT);
                pre.awaitOne();
                assertThat(post.events()).isEmpty();
                assertThat(player.state().level()).isEqualTo(START_LEVEL);
                assertThat(player.state().cursor().isEmpty()).isTrue();
                assertThat(grind.slot(0).enchants()).isEqualTo(SWORD);
                assertThat(grind.slot(1).is("book")).as("the book stays in slot 1").isTrue();

                hooks.patchConfig("cancel.PreGrindstoneExtractEvent", false);
                grind.click(2, Click.LEFT);
                grind.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                grind.click(Fixture.firstFreePlayerSlot(grind), Click.LEFT);
            }
            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(post.events()).hasSize(1);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
        } finally {
            hooks.patchConfig("cancel.PreGrindstoneExtractEvent", false);
        }
    }
}
