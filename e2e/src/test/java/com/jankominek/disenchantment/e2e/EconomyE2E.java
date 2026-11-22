package com.jankominek.disenchantment.e2e;

import static com.jankominek.disenchantment.e2e.Fixture.ANVIL;
import static com.jankominek.disenchantment.e2e.Fixture.POST_DISENCHANT;
import static com.jankominek.disenchantment.e2e.Fixture.POST_GRIND_EXTRACT;
import static com.jankominek.disenchantment.e2e.Fixture.POST_SHATTER;
import static com.jankominek.disenchantment.e2e.Fixture.STATION;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.h7kz.proofstone.api.Agent;
import io.github.h7kz.proofstone.api.CapturedEvent;
import io.github.h7kz.proofstone.api.CellTest;
import io.github.h7kz.proofstone.api.Click;
import io.github.h7kz.proofstone.api.Client;
import io.github.h7kz.proofstone.api.DualAssert;
import io.github.h7kz.proofstone.api.EventProbe;
import io.github.h7kz.proofstone.api.Item;
import io.github.h7kz.proofstone.api.Matrix;
import io.github.h7kz.proofstone.api.Platform;
import io.github.h7kz.proofstone.api.PlayerHandle;
import io.github.h7kz.proofstone.api.Proofstone;
import io.github.h7kz.proofstone.api.Window;
import java.util.Map;

/**
 * Economy charging through a Vault economy. The economy is a test double (src/main/.../support/vault): a plugin named
 * Vault that carries the VaultAPI classes and keeps balances in its own config at {@code balances.<lowercase name>}, so a
 * test seeds a balance with patchConfig and reads the server's balance back with config(). The money is therefore a
 * server oracle, not something the client can see. Each test seeds its own player's balance (names are unique per test);
 * the double's config is restored by Proofstone after every test (it re-reads its live config on each call).
 *
 * <p>The refund when writing the result fails (commit 4352679) is in {@link RefundOnWriteFailureE2E}.
 */
@Proofstone(plugins = {"target/support-jars/*-vault.jar", "target/plugin-under-test/Disenchantment-*.jar"})
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class EconomyE2E {

    private static final int START_LEVEL = 30;

    private static double balance(Agent agent, Client client) {
        return ((Number) agent.plugin("Vault").config("balances." + client.name().toLowerCase())).doubleValue();
    }

    private static void seed(Agent agent, Client client, double amount) {
        agent.plugin("Vault").patchConfig("balances." + client.name().toLowerCase(), amount);
    }

    private static PlayerHandle swordPlayer(Agent agent, Client client, double balance) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL).give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        seed(agent, client, balance);
        return player;
    }

    @CellTest
    void disenchantChargesTheEconomyAndTheEventCarriesTheCost(Agent agent, Client client) {
        PlayerHandle player = swordPlayer(agent, client, 100.0);
        try (Cfg ignored = Cfg.with(agent, Map.of("disenchantment.economy.enabled", true, "disenchantment.economy.cost", 25.0));
             EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            assertThat(balance(agent, client)).as("seeded balance").isEqualTo(100.0);
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                anvil.awaitSlot(2, item -> item.is("enchanted_book"));
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }
            CapturedEvent event = post.awaitOne();
            assertThat(((Number) event.field("economyCost")).doubleValue()).as("Post event economyCost").isEqualTo(25.0);
            assertThat(balance(agent, client)).as("money charged once").isEqualTo(75.0);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - event.getInt("xpCost"));
            DualAssert.item(player, client).inInventory(item -> item.is("enchanted_book")).amount(1);
            assertThat(post.events()).hasSize(1);
        }
    }

    /**
     * Too little money: nothing is delivered, no money or XP moves, no Post event. After the balance is raised the same
     * window completes the operation, which bounds the absence checks.
     */
    @CellTest
    void insufficientFundsDeliverNothingUntilThePlayerCanPay(Agent agent, Client client) {
        PlayerHandle player = swordPlayer(agent, client, 10.0);
        try (Cfg ignored = Cfg.with(agent, Map.of("disenchantment.economy.enabled", true, "disenchantment.economy.cost", 25.0));
             EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                anvil.awaitSlot(2, item -> item.is("enchanted_book"));

                Fixture.refusedClick(anvil, 2, Click.LEFT);
                assertThat(balance(agent, client)).as("balance after the refused click").isEqualTo(10.0);
                assertThat(player.state().level()).as("level after the refused click").isEqualTo(START_LEVEL);
                assertThat(player.state().cursor().isEmpty()).isTrue();
                assertThat(anvil.slot(0).enchants()).as("source keeps its enchantments").hasSize(2);
                assertThat(post.events()).isEmpty();

                seed(agent, client, 100.0);
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }
            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(balance(agent, client)).isEqualTo(75.0);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
            assertThat(post.events()).hasSize(1);
        }
    }

    @CellTest
    void shatterChargesItsOwnPrice(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL)
                .give(Item.of("enchanted_book").storedEnchant("sharpness", 5).storedEnchant("unbreaking", 3))
                .give(Item.of("book"));
        seed(agent, client, 100.0);
        try (Cfg ignored = Cfg.with(agent, Map.of("shatterment.economy.enabled", true, "shatterment.economy.cost", 15.0));
             EventProbe post = agent.events().subscribe(POST_SHATTER)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("enchanted_book"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                anvil.awaitSlot(2, item -> item.is("enchanted_book"));
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, item -> item.is("enchanted_book") && item.storedEnchants().size() == 1);
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }
            CapturedEvent event = post.awaitOne();
            assertThat(((Number) event.field("economyCost")).doubleValue()).isEqualTo(15.0);
            assertThat(balance(agent, client)).isEqualTo(85.0);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - event.getInt("xpCost"));
        }
    }

    @CellTest
    void grindstoneExtractChargesItsOwnPrice(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextTo(agent, client, "minecraft:grindstone[face=floor]");
        player.level(START_LEVEL).give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        seed(agent, client, 100.0);
        try (Cfg ignored = Cfg.with(agent, Map.of("grindstone.enabled", true, "grindstone.economy.enabled", true,
                "grindstone.economy.cost", 40.0)); EventProbe post = agent.events().subscribe(POST_GRIND_EXTRACT)) {
            try (Window grind = client.openBlock(STATION)) {
                grind.moveFromInventory(item -> item.is("diamond_sword"), 0);
                grind.moveFromInventory(item -> item.is("book"), 1);
                grind.awaitSlot(2, item -> item.is("enchanted_book"));
                grind.click(2, Click.LEFT);
                grind.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                grind.click(Fixture.firstFreePlayerSlot(grind), Click.LEFT);
            }
            CapturedEvent event = post.awaitOne();
            assertThat(((Number) event.field("economyCost")).doubleValue()).isEqualTo(40.0);
            assertThat(balance(agent, client)).isEqualTo(60.0);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - event.getInt("xpCost"));
        }
    }

    /** Paired control for the charging tests: with the economy off (the default) no money moves and the event says 0. */
    @CellTest
    void withTheEconomyOffNoMoneyMoves(Agent agent, Client client) {
        PlayerHandle player = swordPlayer(agent, client, 100.0);
        try (EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                anvil.awaitSlot(2, item -> item.is("enchanted_book"));
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }
            CapturedEvent event = post.awaitOne();
            assertThat(((Number) event.field("economyCost")).doubleValue()).isZero();
            assertThat(balance(agent, client)).isEqualTo(100.0);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - event.getInt("xpCost"));
        }
    }
}
