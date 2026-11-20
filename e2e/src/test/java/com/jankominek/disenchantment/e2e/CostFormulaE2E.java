package com.jankominek.disenchantment.e2e;

import static com.jankominek.disenchantment.e2e.Fixture.ANVIL;
import static com.jankominek.disenchantment.e2e.Fixture.POST_DISENCHANT;
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
import java.util.Map;

/**
 * The XP cost formula, {@code base + sum of round(level * multiplier)} with the multiplier growing by {@code multiply} for
 * each enchantment (highest level first), checked against hand-computed literals for a sword with sharpness 5 and
 * unbreaking 3. Two different (base, multiply) pairs pin both terms, the default pair pins the shipped defaults, and the
 * switches around it (cost off, max-cost, per-enchantment override) are each compared with that default. The charged
 * levels are read from the server's XP, the cost from the plugin's own Post event.
 */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class CostFormulaE2E {

    private static final int START_LEVEL = 60;
    private static final String REPAIR = "disenchantment.anvil.repair.";

    /** Disenchants a sharpness 5 / unbreaking 3 sword and returns the cost the Post event reports (the level is asserted). */
    private static int disenchantCost(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL).give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        try (EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                anvil.awaitSlot(2, item -> item.is("enchanted_book"));
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }
            int xpCost = post.awaitOne().getInt("xpCost");
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
            return xpCost;
        }
    }

    /** 10 + round(5 * 0.25) + round(3 * 0.5) = 10 + 1 + 2. */
    @CellTest
    void theDefaultFormulaCharges13(Agent agent, Client client) {
        assertThat(disenchantCost(agent, client)).isEqualTo(13);
    }

    /** 7 + round(5 * 1.0) + round(3 * 2.0) = 18. */
    @CellTest
    void baseSevenMultiplyOneCharges18(Agent agent, Client client) {
        try (Cfg ignored = Cfg.with(agent, Map.of(REPAIR + "base", 7, REPAIR + "multiply", 1.0))) {
            assertThat(disenchantCost(agent, client)).isEqualTo(18);
        }
    }

    /** 4 + round(5 * 0.5) + round(3 * 1.0) = 4 + 3 + 3 = 10 (a different pair, so base and multiply cannot be swapped). */
    @CellTest
    void baseFourMultiplyHalfCharges10(Agent agent, Client client) {
        try (Cfg ignored = Cfg.with(agent, Map.of(REPAIR + "base", 4, REPAIR + "multiply", 0.5))) {
            assertThat(disenchantCost(agent, client)).isEqualTo(10);
        }
    }

    /** With the cost switched off the operation is free (compare the default test: 13). */
    @CellTest
    void costOffChargesNothing(Agent agent, Client client) {
        try (Cfg ignored = Cfg.with(agent, Map.of(REPAIR + "cost", false))) {
            assertThat(disenchantCost(agent, client)).isZero();
        }
    }

    /** max-cost caps the formula result (18 uncapped with base 7 and multiply 1). */
    @CellTest
    void maxCostCapsTheCharge(Agent agent, Client client) {
        try (Cfg ignored = Cfg.with(agent, Map.of(REPAIR + "base", 7, REPAIR + "multiply", 1.0, REPAIR + "max-cost", 8))) {
            assertThat(disenchantCost(agent, client)).isEqualTo(8);
        }
    }

    /** A fixed per-enchantment cost replaces that enchantment's term and does not advance the multiplier: 7 + 3 + round(3 * 1.0). */
    @CellTest
    void perEnchantmentOverrideReplacesItsTerm(Agent agent, Client client) {
        try (Cfg ignored = Cfg.with(agent, Map.of(REPAIR + "base", 7, REPAIR + "multiply", 1.0,
                REPAIR + "enchantment-costs", Map.of("sharpness", 3)))) {
            assertThat(disenchantCost(agent, client)).isEqualTo(13);
        }
    }

    /** Shatter prices the one split-off enchantment: 7 + round(level * 1.0). The level is read off the offered book. */
    @CellTest
    void shatterCostFollowsTheSameFormula(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL)
                .give(Item.of("enchanted_book").storedEnchant("sharpness", 5).storedEnchant("unbreaking", 3)
                        .storedEnchant("looting", 3))
                .give(Item.of("book"));
        try (Cfg ignored = Cfg.with(agent, Map.of("shatterment.anvil.repair.base", 7, "shatterment.anvil.repair.multiply", 1.0));
             EventProbe post = agent.events().subscribe(POST_SHATTER)) {
            int level;
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("enchanted_book"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                ItemView offered = anvil.awaitSlot(2, item -> item.is("enchanted_book"));
                assertThat(offered.storedEnchants()).hasSize(1);
                level = offered.storedEnchants().values().iterator().next();
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, item -> item.is("enchanted_book") && item.storedEnchants().size() == 2);
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }
            int xpCost = post.awaitOne().getInt("xpCost");
            assertThat(xpCost).isEqualTo(7 + level);
            DualAssert.level(player, client).isEqualTo(START_LEVEL - xpCost);
        }
    }
}
