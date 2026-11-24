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
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Per-material, per-enchantment and per-feature switches. Every "nothing happens" assertion has its paired control in the
 * same test: the same layout is first loaded with the default config and offers the enchanted book, so the empty result
 * under the patch is about the patch and not about the layout (a control run without the plugin fails the control step).
 * Per-world disabling is in {@link DisabledWorldE2E} (own server, same reason: the patch must not leak).
 */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.4", "1.21.7", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class RestrictionsE2E {

    private static final Map<String, Integer> SWORD = Map.of("minecraft:sharpness", 5, "minecraft:unbreaking", 3);
    private static final int START_LEVEL = 30;

    private static PlayerHandle sword(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL).give(Item.of("diamond_sword").enchant("sharpness", 5).enchant("unbreaking", 3))
                .give(Item.of("book"));
        return player;
    }

    /** Loads sword and book into a fresh anvil window and returns the result slot after the layout settled (or empty). */
    private static ItemView load(Client client, String source, String second, boolean expectResult, Predicate<ItemView> shape) {
        try (Window anvil = client.openBlock(ANVIL)) {
            anvil.moveFromInventory(item -> item.is(source), 0);
            anvil.moveFromInventory(item -> item.is(second), 1);
            if (expectResult) {
                return anvil.awaitSlot(2, shape);
            }
            // Window actions are server-confirmed in order, so the result of this layout has been decided here.
            assertThat(anvil.slot(2).isEmpty()).as("no result for this layout").isTrue();
            return ItemView.EMPTY;
        }
    }

    @CellTest
    void aDeniedMaterialCannotBeDisenchanted(Agent agent, Client client) {
        PlayerHandle player = sword(agent, client);
        try (EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            load(client, "diamond_sword", "book", true, item -> item.is("enchanted_book"));
            try (Cfg ignored = Cfg.with(agent, Map.of("disenchantment.materials.mode", "DENYLIST",
                    "disenchantment.materials.list", List.of("DIAMOND_SWORD")))) {
                load(client, "diamond_sword", "book", false, null);
            }
            DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).enchants(SWORD);
            assertThat(post.events()).isEmpty();
            DualAssert.level(player, client).isEqualTo(START_LEVEL);
        }
    }

    @CellTest
    void anAllowlistWithoutTheMaterialBlocksItAndWithItAllowsIt(Agent agent, Client client) {
        PlayerHandle player = sword(agent, client);
        try (EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            try (Cfg ignored = Cfg.with(agent, Map.of("disenchantment.materials.mode", "ALLOWLIST",
                    "disenchantment.materials.list", List.of("DIAMOND_SWORD")))) {
                load(client, "diamond_sword", "book", true, item -> item.is("enchanted_book"));
            }
            try (Cfg ignored = Cfg.with(agent, Map.of("disenchantment.materials.mode", "ALLOWLIST",
                    "disenchantment.materials.list", List.of("IRON_SWORD")))) {
                load(client, "diamond_sword", "book", false, null);
            }
            assertThat(post.events()).as("only layouts were offered, nothing was taken").isEmpty();
            DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).enchants(SWORD);
        }
    }

    @CellTest
    void aDisabledEnchantmentBlocksTheWholeOperation(Agent agent, Client client) {
        PlayerHandle player = sword(agent, client);
        load(client, "diamond_sword", "book", true, item -> item.is("enchanted_book"));
        try (Cfg ignored = Cfg.with(agent, Map.of("disenchantment.enchantments-states", List.of("unbreaking:disable")))) {
            load(client, "diamond_sword", "book", false, null);
        }
        DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).enchants(SWORD);
        DualAssert.level(player, client).isEqualTo(START_LEVEL);
    }

    /** KEEP: the enchantment stays on the item and is not on the book. The default (control) moves both. */
    @CellTest
    void aKeptEnchantmentStaysOnTheItem(Agent agent, Client client) {
        PlayerHandle player = sword(agent, client);
        ItemView control = load(client, "diamond_sword", "book", true, item -> item.is("enchanted_book"));
        assertThat(control.storedEnchants()).as("control: the default offers both").isEqualTo(SWORD);

        try (Cfg ignored = Cfg.with(agent, Map.of("disenchantment.enchantments-states", List.of("sharpness:keep")));
             EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                ItemView offered = anvil.awaitSlot(2, item -> item.is("enchanted_book"));
                assertThat(offered.storedEnchants()).isEqualTo(Map.of("minecraft:unbreaking", 3));
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().size() == 1);
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }
            post.awaitOne();
            DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword"))
                    .enchants(Map.of("minecraft:sharpness", 5));
            DualAssert.item(player, client).inInventory(item -> item.is("enchanted_book"))
                    .storedEnchants(Map.of("minecraft:unbreaking", 3));
        }
    }

    /** DELETE: the enchantment is stripped from the item but never reaches the book. */
    @CellTest
    void aDeletedEnchantmentIsDestroyed(Agent agent, Client client) {
        PlayerHandle player = sword(agent, client);
        try (Cfg ignored = Cfg.with(agent, Map.of("disenchantment.enchantments-states", List.of("sharpness:delete")));
             EventProbe post = agent.events().subscribe(POST_DISENCHANT)) {
            try (Window anvil = client.openBlock(ANVIL)) {
                anvil.moveFromInventory(item -> item.is("diamond_sword"), 0);
                anvil.moveFromInventory(item -> item.is("book"), 1);
                ItemView offered = anvil.awaitSlot(2, item -> item.is("enchanted_book"));
                assertThat(offered.storedEnchants()).isEqualTo(Map.of("minecraft:unbreaking", 3));
                anvil.click(2, Click.LEFT);
                anvil.awaitSlot(0, item -> item.is("diamond_sword") && item.enchants().isEmpty());
                anvil.click(Fixture.firstFreePlayerSlot(anvil), Click.LEFT);
            }
            post.awaitOne();
            DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).enchants(Map.of());
            DualAssert.item(player, client).inInventory(item -> item.is("enchanted_book"))
                    .storedEnchants(Map.of("minecraft:unbreaking", 3));
        }
    }

    @CellTest
    void disenchantmentSwitchedOffGivesTheVanillaResult(Agent agent, Client client) {
        PlayerHandle player = sword(agent, client);
        load(client, "diamond_sword", "book", true, item -> item.is("enchanted_book"));
        try (Cfg ignored = Cfg.with(agent, Map.of("disenchantment.enabled", false))) {
            load(client, "diamond_sword", "book", false, null);
        }
        DualAssert.item(player, client).inInventory(item -> item.is("diamond_sword")).enchants(SWORD);
    }

    @CellTest
    void shatterSwitchedOffGivesTheVanillaResult(Agent agent, Client client) {
        PlayerHandle player = Fixture.standNextToAnAnvil(agent, client);
        player.level(START_LEVEL)
                .give(Item.of("enchanted_book").storedEnchant("sharpness", 5).storedEnchant("unbreaking", 3))
                .give(Item.of("book"));
        try (EventProbe post = agent.events().subscribe(POST_SHATTER)) {
            load(client, "enchanted_book", "book", true, item -> item.is("enchanted_book"));
            try (Cfg ignored = Cfg.with(agent, Map.of("shatterment.enabled", false))) {
                load(client, "enchanted_book", "book", false, null);
            }
            assertThat(post.events()).isEmpty();
            DualAssert.item(player, client).inInventory(item -> item.is("enchanted_book") && item.amount() == 1)
                    .storedEnchants(SWORD);
        }
    }
}
